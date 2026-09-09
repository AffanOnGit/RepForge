package com.repforge.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.domain.model.Equipment
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.ExerciseGroup
import com.repforge.core.domain.model.ExerciseGroupType
import com.repforge.core.domain.model.MuscleGroup
import com.repforge.core.domain.model.Routine
import com.repforge.core.domain.model.RoutineExercise
import com.repforge.core.domain.model.SubMuscle
import com.repforge.core.domain.repository.ExerciseRepository
import com.repforge.core.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class WizardExerciseItem(
    val exercise: Exercise,
    val isSelected: Boolean = true,
    val prescribedSets: Int = 3,
    val prescribedRepsMin: Int = 8,
    val prescribedRepsMax: Int = 12,
    val restSeconds: Int = 90,
    val isSupersetWithNext: Boolean = false
)

data class RapidTemplateWizardUiState(
    val currentStep: Int = 0,
    val routineName: String = "Rapid Workout",
    val routineDescription: String = "",
    val selectedSubMuscles: Set<SubMuscle> = setOf(SubMuscle.UPPER_CHEST, SubMuscle.SIDE_DELTS, SubMuscle.TRICEPS),
    val candidateExercises: List<WizardExerciseItem> = emptyList(),
    val isSaving: Boolean = false
) {
    val totalSets: Int
        get() = candidateExercises.filter { it.isSelected }.sumOf { it.prescribedSets }

    val estimatedDurationMinutes: Int
        get() {
            val selected = candidateExercises.filter { it.isSelected }
            if (selected.isEmpty()) return 0
            // ~45s per set active time + rest time
            val totalRestSecs = selected.sumOf { (it.prescribedSets - 1) * it.restSeconds } + (selected.size * 60)
            val totalActiveSecs = selected.sumOf { it.prescribedSets * 45 }
            return ((totalRestSecs + totalActiveSecs) / 60).coerceAtLeast(15)
        }

    val estimatedCaloriesRange: Pair<Int, Int>
        get() {
            val duration = estimatedDurationMinutes
            val base = duration * 6.5
            return (base * 0.85).toInt() to (base * 1.15).toInt()
        }
}

@HiltViewModel
class RapidTemplateWizardViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RapidTemplateWizardUiState())
    val uiState: StateFlow<RapidTemplateWizardUiState> = _uiState.asStateFlow()

    init {
        loadExercisesForSelectedSubMuscles()
    }

    fun updateRoutineName(name: String) {
        _uiState.update { it.copy(routineName = name) }
    }

    fun toggleSubMuscle(subMuscle: SubMuscle) {
        _uiState.update { current ->
            val updated = current.selectedSubMuscles.toMutableSet()
            if (updated.contains(subMuscle)) {
                if (updated.size > 1) updated.remove(subMuscle)
            } else {
                updated.add(subMuscle)
            }
            current.copy(selectedSubMuscles = updated)
        }
        loadExercisesForSelectedSubMuscles()
    }

    private fun loadExercisesForSelectedSubMuscles() {
        viewModelScope.launch {
            val selected = _uiState.value.selectedSubMuscles
            val all = exerciseRepository.getAllExercises().firstOrNull().orEmpty()
            val filtered = all.filter { it.primarySubMuscle in selected }
                .distinctBy { it.id }
                .take(8)
                .map { WizardExerciseItem(exercise = it) }

            _uiState.update { it.copy(candidateExercises = filtered) }
        }
    }

    fun toggleExerciseSelection(exerciseId: String) {
        _uiState.update { current ->
            val updated = current.candidateExercises.map { item ->
                if (item.exercise.id == exerciseId) item.copy(isSelected = !item.isSelected) else item
            }
            current.copy(candidateExercises = updated)
        }
    }

    fun toggleSuperset(index: Int) {
        _uiState.update { current ->
            val updated = current.candidateExercises.toMutableList()
            if (index in 0 until updated.size - 1) {
                val item = updated[index]
                updated[index] = item.copy(isSupersetWithNext = !item.isSupersetWithNext)
            }
            current.copy(candidateExercises = updated)
        }
    }

    fun updateSets(exerciseId: String, sets: Int) {
        _uiState.update { current ->
            val updated = current.candidateExercises.map { item ->
                if (item.exercise.id == exerciseId) item.copy(prescribedSets = sets.coerceIn(1, 10)) else item
            }
            current.copy(candidateExercises = updated)
        }
    }

    fun updateRepsMin(exerciseId: String, reps: Int) {
        _uiState.update { current ->
            val updated = current.candidateExercises.map { item ->
                if (item.exercise.id == exerciseId) item.copy(prescribedRepsMin = reps.coerceIn(1, 50)) else item
            }
            current.copy(candidateExercises = updated)
        }
    }

    fun updateRepsMax(exerciseId: String, reps: Int) {
        _uiState.update { current ->
            val updated = current.candidateExercises.map { item ->
                if (item.exercise.id == exerciseId) item.copy(prescribedRepsMax = reps.coerceIn(1, 50)) else item
            }
            current.copy(candidateExercises = updated)
        }
    }

    fun updateRest(exerciseId: String, rest: Int) {
        _uiState.update { current ->
            val updated = current.candidateExercises.map { item ->
                if (item.exercise.id == exerciseId) item.copy(restSeconds = rest.coerceIn(15, 300)) else item
            }
            current.copy(candidateExercises = updated)
        }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }
    }

    fun prevStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun saveRoutine(onSaved: (String) -> Unit) {
        val state = _uiState.value
        val selectedItems = state.candidateExercises.filter { it.isSelected }
        if (selectedItems.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val routineId = UUID.randomUUID().toString()
            val groups = mutableListOf<ExerciseGroup>()
            val routineExercises = mutableListOf<RoutineExercise>()

            var i = 0
            var groupOrder = 0
            while (i < selectedItems.size) {
                val item = selectedItems[i]
                val groupId = UUID.randomUUID().toString()

                if (item.isSupersetWithNext && i + 1 < selectedItems.size) {
                    // Superset group
                    val nextItem = selectedItems[i + 1]
                    val group = ExerciseGroup(
                        id = groupId,
                        groupType = ExerciseGroupType.SUPERSET,
                        orderInRoutine = groupOrder++,
                        restAfterGroupSeconds = item.restSeconds
                    )
                    groups.add(group)

                    routineExercises.add(
                        RoutineExercise(
                            id = UUID.randomUUID().toString(),
                            exerciseGroupId = groupId,
                            exerciseId = item.exercise.id,
                            orderInGroup = 0,
                            prescribedSets = item.prescribedSets,
                            prescribedRepsMin = item.prescribedRepsMin,
                            prescribedRepsMax = item.prescribedRepsMax,
                            restSeconds = 0
                        )
                    )
                    routineExercises.add(
                        RoutineExercise(
                            id = UUID.randomUUID().toString(),
                            exerciseGroupId = groupId,
                            exerciseId = nextItem.exercise.id,
                            orderInGroup = 1,
                            prescribedSets = nextItem.prescribedSets,
                            prescribedRepsMin = nextItem.prescribedRepsMin,
                            prescribedRepsMax = nextItem.prescribedRepsMax,
                            restSeconds = nextItem.restSeconds
                        )
                    )
                    i += 2
                } else {
                    // Single exercise group
                    val group = ExerciseGroup(
                        id = groupId,
                        groupType = ExerciseGroupType.SINGLE,
                        orderInRoutine = groupOrder++,
                        restAfterGroupSeconds = item.restSeconds
                    )
                    groups.add(group)

                    routineExercises.add(
                        RoutineExercise(
                            id = UUID.randomUUID().toString(),
                            exerciseGroupId = groupId,
                            exerciseId = item.exercise.id,
                            orderInGroup = 0,
                            prescribedSets = item.prescribedSets,
                            prescribedRepsMin = item.prescribedRepsMin,
                            prescribedRepsMax = item.prescribedRepsMax,
                            restSeconds = item.restSeconds
                        )
                    )
                    i += 1
                }
            }

            val routine = Routine(
                id = routineId,
                name = state.routineName.ifBlank { "Rapid Workout" },
                description = "Targets: " + state.selectedSubMuscles.joinToString(", ") { it.displayName },
                estimatedDurationMinutes = state.estimatedDurationMinutes,
                exerciseGroups = groups
            )

            routineRepository.createRoutine(routine, groups, routineExercises)
            _uiState.update { it.copy(isSaving = false) }
            onSaved(routineId)
        }
    }
}
