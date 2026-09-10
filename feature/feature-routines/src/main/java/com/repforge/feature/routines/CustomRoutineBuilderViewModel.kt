package com.repforge.feature.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.data.database.dao.RoutineDao
import com.repforge.core.domain.model.Equipment
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.ExerciseGroup
import com.repforge.core.domain.model.ExerciseGroupType
import com.repforge.core.domain.model.Routine
import com.repforge.core.domain.model.RoutineExercise
import com.repforge.core.domain.model.SubMuscle
import com.repforge.core.domain.repository.ExerciseRepository
import com.repforge.core.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** One exercise entry in the builder's list. */
data class BuilderExerciseEntry(
    val id: String = UUID.randomUUID().toString(),
    val exercise: Exercise,
    val sets: Int = 3,
    val repsMin: Int = 8,
    val repsMax: Int = 12,
    val restSeconds: Int = 90
)

/** Filter state shared between builder and exercise picker. */
data class ExercisePickerFilter(
    val searchQuery: String = "",
    val subMuscle: SubMuscle? = null,
    val equipment: Equipment? = null
)

data class CustomRoutineBuilderUiState(
    val routineName: String = "",
    val description: String = "",
    val entries: List<BuilderExerciseEntry> = emptyList(),
    val isPickerOpen: Boolean = false,
    val pickerFilter: ExercisePickerFilter = ExercisePickerFilter(),
    val isSaving: Boolean = false,
    val savedRoutineId: String? = null,     // non-null → trigger navigation
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class CustomRoutineBuilderViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineDao: RoutineDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editRoutineId: String? = savedStateHandle["routineId"]

    private val _uiState = MutableStateFlow(CustomRoutineBuilderUiState())
    val uiState: StateFlow<CustomRoutineBuilderUiState> = _uiState.asStateFlow()

    /** Live-filtered exercise list for the picker bottom sheet. */
    private val _pickerFilter = MutableStateFlow(ExercisePickerFilter())
    val pickerExercises: StateFlow<List<Exercise>> = combine(
        exerciseRepository.getAllExercises(),
        _pickerFilter
    ) { all, filter ->
        all.filter { ex ->
            val q = filter.searchQuery
            val matchesQuery = q.isBlank()
                || ex.name.contains(q, ignoreCase = true)
                || ex.creatorTags.any { it.contains(q, ignoreCase = true) }
            val matchesMuscle = filter.subMuscle == null || ex.primarySubMuscle == filter.subMuscle
            val matchesEquipment = filter.equipment == null || ex.equipment == filter.equipment
            matchesQuery && matchesMuscle && matchesEquipment
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (editRoutineId != null) loadExistingRoutine(editRoutineId)
    }

    private fun loadExistingRoutine(routineId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val routine = routineRepository.getRoutineById(routineId).firstOrNull() ?: return@launch
            val allRoutineExercises = routineDao.getAllExercisesForRoutine(routineId).firstOrNull().orEmpty()
            val builtEntries = allRoutineExercises.mapNotNull { reEntity ->
                val ex = exerciseRepository.getExerciseById(reEntity.exerciseId) ?: return@mapNotNull null
                BuilderExerciseEntry(
                    id = reEntity.exerciseGroupId,
                    exercise = ex,
                    sets = reEntity.prescribedSets,
                    repsMin = reEntity.prescribedRepsMin,
                    repsMax = reEntity.prescribedRepsMax,
                    restSeconds = reEntity.restSeconds
                )
            }
            _uiState.update {
                it.copy(
                    routineName = routine.name,
                    description = routine.description,
                    entries = builtEntries,
                    isEditMode = true,
                    isLoading = false
                )
            }
        }
    }

    // ── Name / description ──────────────────────────────────────────────────
    fun onNameChanged(name: String) = _uiState.update { it.copy(routineName = name) }
    fun onDescriptionChanged(desc: String) = _uiState.update { it.copy(description = desc) }

    // ── Exercise Picker ─────────────────────────────────────────────────────
    fun openPicker() = _uiState.update { it.copy(isPickerOpen = true) }
    fun closePicker() = _uiState.update { it.copy(isPickerOpen = false) }

    fun onPickerSearchChanged(q: String) {
        _pickerFilter.update { it.copy(searchQuery = q) }
        _uiState.update { it.copy(pickerFilter = it.pickerFilter.copy(searchQuery = q)) }
    }

    fun onPickerMuscleChanged(m: SubMuscle?) {
        _pickerFilter.update { it.copy(subMuscle = m) }
        _uiState.update { it.copy(pickerFilter = it.pickerFilter.copy(subMuscle = m)) }
    }

    fun onPickerEquipmentChanged(e: Equipment?) {
        _pickerFilter.update { it.copy(equipment = e) }
        _uiState.update { it.copy(pickerFilter = it.pickerFilter.copy(equipment = e)) }
    }

    fun addExercise(exercise: Exercise) {
        _uiState.update { state ->
            state.copy(
                entries = state.entries + BuilderExerciseEntry(exercise = exercise),
                isPickerOpen = false
            )
        }
        // Reset picker filters so next open is clean
        _pickerFilter.update { ExercisePickerFilter() }
        _uiState.update { it.copy(pickerFilter = ExercisePickerFilter()) }
    }

    // ── Entry mutations ──────────────────────────────────────────────────────
    fun removeExercise(entryId: String) =
        _uiState.update { it.copy(entries = it.entries.filter { e -> e.id != entryId }) }

    fun moveUp(entryId: String) {
        val list = _uiState.value.entries.toMutableList()
        val idx = list.indexOfFirst { it.id == entryId }
        if (idx > 0) {
            val tmp = list[idx - 1]; list[idx - 1] = list[idx]; list[idx] = tmp
        }
        _uiState.update { it.copy(entries = list) }
    }

    fun moveDown(entryId: String) {
        val list = _uiState.value.entries.toMutableList()
        val idx = list.indexOfFirst { it.id == entryId }
        if (idx < list.size - 1) {
            val tmp = list[idx + 1]; list[idx + 1] = list[idx]; list[idx] = tmp
        }
        _uiState.update { it.copy(entries = list) }
    }

    fun updateSets(entryId: String, delta: Int) = updateEntry(entryId) {
        it.copy(sets = (it.sets + delta).coerceIn(1, 20))
    }

    fun updateRepsMin(entryId: String, delta: Int) = updateEntry(entryId) {
        it.copy(repsMin = (it.repsMin + delta).coerceIn(1, 100))
    }

    fun updateRepsMax(entryId: String, delta: Int) = updateEntry(entryId) {
        it.copy(repsMax = (it.repsMax + delta).coerceIn(1, 100))
    }

    fun updateRest(entryId: String, seconds: Int) = updateEntry(entryId) {
        it.copy(restSeconds = seconds.coerceIn(0, 600))
    }

    private fun updateEntry(id: String, transform: (BuilderExerciseEntry) -> BuilderExerciseEntry) {
        _uiState.update { state ->
            state.copy(entries = state.entries.map { if (it.id == id) transform(it) else it })
        }
    }

    // ── Save ─────────────────────────────────────────────────────────────────
    fun saveRoutine() {
        val state = _uiState.value
        if (state.routineName.isBlank() || state.entries.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val routineId = editRoutineId ?: UUID.randomUUID().toString()
            val routine = Routine(
                id = routineId,
                name = state.routineName.trim(),
                description = state.description.trim()
            )

            val groups = state.entries.mapIndexed { idx, entry ->
                ExerciseGroup(
                    id = entry.id,           // reuse entry ID as group ID for simplicity
                    groupType = ExerciseGroupType.SINGLE,
                    orderInRoutine = idx,
                    restAfterGroupSeconds = entry.restSeconds
                )
            }

            val exercises = state.entries.mapIndexed { idx, entry ->
                RoutineExercise(
                    id = UUID.randomUUID().toString(),
                    exerciseGroupId = entry.id,
                    exerciseId = entry.exercise.id,
                    orderInGroup = 0,
                    prescribedSets = entry.sets,
                    prescribedRepsMin = entry.repsMin,
                    prescribedRepsMax = entry.repsMax,
                    restSeconds = entry.restSeconds
                )
            }

            if (state.isEditMode && editRoutineId != null) {
                routineRepository.updateRoutine(routine, groups, exercises)
            } else {
                routineRepository.createRoutine(routine, groups, exercises)
            }

            _uiState.update { it.copy(isSaving = false, savedRoutineId = routineId) }
        }
    }
}
