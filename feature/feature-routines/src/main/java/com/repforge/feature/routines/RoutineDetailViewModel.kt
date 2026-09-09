package com.repforge.feature.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.data.database.dao.ExerciseDao
import com.repforge.core.data.database.dao.RoutineDao
import com.repforge.core.data.mapper.toDomain
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.ExerciseGroup
import com.repforge.core.domain.model.Routine
import com.repforge.core.domain.model.RoutineExercise
import com.repforge.core.domain.repository.ExerciseRepository
import com.repforge.core.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupedExercisesUi(
    val group: ExerciseGroup,
    val exercises: List<RoutineExerciseDetail>
)

data class RoutineExerciseDetail(
    val routineExercise: RoutineExercise,
    val exercise: Exercise?
)

data class RoutineDetailUiState(
    val routine: Routine? = null,
    val groupsWithExercises: List<GroupedExercisesUi> = emptyList(),
    val isSwapSheetOpen: Boolean = false,
    val exerciseToSwap: RoutineExerciseDetail? = null,
    val swapSuggestions: List<Exercise> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle["routineId"])

    private val _uiState = MutableStateFlow(RoutineDetailUiState())
    val uiState: StateFlow<RoutineDetailUiState> = _uiState.asStateFlow()

    init {
        loadRoutineDetails()
    }

    fun loadRoutineDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val routine = routineRepository.getRoutineById(routineId).firstOrNull()
            val groupEntities = routineDao.getGroupsForRoutine(routineId).firstOrNull().orEmpty()
            val allRoutineExercises = routineDao.getAllExercisesForRoutine(routineId).firstOrNull().orEmpty()

            val groupsWithEx = groupEntities.map { groupEntity ->
                val group = groupEntity.toDomain()
                val exercisesInGroup = allRoutineExercises
                    .filter { it.exerciseGroupId == group.id }
                    .map { reEntity ->
                        val domainRE = reEntity.toDomain()
                        val exercise = exerciseRepository.getExerciseById(domainRE.exerciseId)
                        RoutineExerciseDetail(
                            routineExercise = domainRE,
                            exercise = exercise
                        )
                    }
                GroupedExercisesUi(
                    group = group,
                    exercises = exercisesInGroup
                )
            }

            _uiState.update {
                it.copy(
                    routine = routine,
                    groupsWithExercises = groupsWithEx,
                    isLoading = false
                )
            }
        }
    }

    fun openSwapSheet(exerciseDetail: RoutineExerciseDetail) {
        viewModelScope.launch {
            val suggestions = exerciseRepository.getSwapSuggestions(exerciseDetail.routineExercise.exerciseId)
                .firstOrNull().orEmpty()
            _uiState.update {
                it.copy(
                    isSwapSheetOpen = true,
                    exerciseToSwap = exerciseDetail,
                    swapSuggestions = suggestions
                )
            }
        }
    }

    fun closeSwapSheet() {
        _uiState.update { it.copy(isSwapSheetOpen = false, exerciseToSwap = null) }
    }

    fun swapExercise(newExercise: Exercise) {
        val target = _uiState.value.exerciseToSwap ?: return
        viewModelScope.launch {
            val updatedRE = target.routineExercise.copy(exerciseId = newExercise.id)
            val updatedGroups = _uiState.value.groupsWithExercises.map { it.group }
            val updatedExercises = _uiState.value.groupsWithExercises.flatMap { groupUi ->
                groupUi.exercises.map {
                    if (it.routineExercise.id == updatedRE.id) updatedRE else it.routineExercise
                }
            }
            val currentRoutine = _uiState.value.routine ?: return@launch
            routineRepository.updateRoutine(currentRoutine, updatedGroups, updatedExercises)
            closeSwapSheet()
            loadRoutineDetails()
        }
    }

    fun deleteRoutine(onDeleted: () -> Unit) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
            onDeleted()
        }
    }
}
