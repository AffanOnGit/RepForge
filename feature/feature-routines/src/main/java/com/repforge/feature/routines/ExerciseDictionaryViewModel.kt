package com.repforge.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.domain.model.Equipment
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.SubMuscle
import com.repforge.core.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ExerciseDictionaryUiState(
    val searchQuery: String = "",
    val selectedSubMuscle: SubMuscle? = null,
    val selectedEquipment: Equipment? = null,
    val isCustomExerciseDialogOpen: Boolean = false,
    val newExerciseName: String = "",
    val newExerciseEquipment: Equipment = Equipment.BARBELL,
    val newExercisePrimaryMuscle: SubMuscle = SubMuscle.MID_CHEST
)

@HiltViewModel
class ExerciseDictionaryViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseDictionaryUiState())
    val uiState: StateFlow<ExerciseDictionaryUiState> = _uiState

    val exercises: StateFlow<List<Exercise>> = combine(
        exerciseRepository.getAllExercises(),
        _uiState
    ) { allExercises, state ->
        allExercises.filter { ex ->
            val matchesQuery = state.searchQuery.isBlank() ||
                    ex.name.contains(state.searchQuery, ignoreCase = true) ||
                    ex.creatorTags.any { it.contains(state.searchQuery, ignoreCase = true) }
            val matchesMuscle = state.selectedSubMuscle == null || ex.primarySubMuscle == state.selectedSubMuscle
            val matchesEquipment = state.selectedEquipment == null || ex.equipment == state.selectedEquipment
            matchesQuery && matchesMuscle && matchesEquipment
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectSubMuscle(subMuscle: SubMuscle?) {
        _uiState.update { it.copy(selectedSubMuscle = subMuscle) }
    }

    fun selectEquipment(equipment: Equipment?) {
        _uiState.update { it.copy(selectedEquipment = equipment) }
    }

    fun openCreateCustomDialog() {
        _uiState.update { it.copy(isCustomExerciseDialogOpen = true) }
    }

    fun closeCreateCustomDialog() {
        _uiState.update { it.copy(isCustomExerciseDialogOpen = false) }
    }

    fun updateNewExerciseName(name: String) {
        _uiState.update { it.copy(newExerciseName = name) }
    }

    fun updateNewExerciseEquipment(eq: Equipment) {
        _uiState.update { it.copy(newExerciseEquipment = eq) }
    }

    fun updateNewExercisePrimaryMuscle(muscle: SubMuscle) {
        _uiState.update { it.copy(newExercisePrimaryMuscle = muscle) }
    }

    fun saveCustomExercise() {
        val state = _uiState.value
        if (state.newExerciseName.isBlank()) return

        val customExercise = Exercise(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            name = state.newExerciseName.trim(),
            equipment = state.newExerciseEquipment,
            primarySubMuscle = state.newExercisePrimaryMuscle,
            isCustom = true
        )

        viewModelScope.launch {
            exerciseRepository.upsertExercise(customExercise)
            _uiState.update {
                it.copy(
                    isCustomExerciseDialogOpen = false,
                    newExerciseName = ""
                )
            }
        }
    }
}
