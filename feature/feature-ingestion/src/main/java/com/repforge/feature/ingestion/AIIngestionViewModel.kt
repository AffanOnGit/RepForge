package com.repforge.feature.ingestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.domain.model.ExerciseGroup
import com.repforge.core.domain.model.ExerciseGroupType
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
import java.util.UUID
import javax.inject.Inject

data class AIIngestionUiState(
    val urlInput: String = "",
    val stage: IngestionStage = IngestionStage.IDLE,
    val parsedData: ParsedWorkoutData? = null,
    val editedExercises: List<ParsedExerciseItem> = emptyList(),
    val error: IngestionError? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class AIIngestionViewModel @Inject constructor(
    private val ingestionService: AIIngestionService,
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIIngestionUiState())
    val uiState: StateFlow<AIIngestionUiState> = _uiState.asStateFlow()

    fun updateUrl(url: String) {
        _uiState.update { it.copy(urlInput = url, error = null) }
    }

    fun startIngestion() {
        val url = _uiState.value.urlInput.trim()
        if (url.isBlank()) {
            _uiState.update { it.copy(error = IngestionError.InvalidYouTubeUrl) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(stage = IngestionStage.CHECKING_GLOBAL_CACHE, error = null) }

            val result = ingestionService.parseWorkoutFromUrl(url) { newStage ->
                _uiState.update { it.copy(stage = newStage) }
            }

            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            parsedData = data,
                            editedExercises = data.exercises,
                            stage = IngestionStage.READY_FOR_REVIEW
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            stage = IngestionStage.IDLE,
                            error = IngestionError.ParsingFailed(err.localizedMessage ?: "Unknown error")
                        )
                    }
                }
            )
        }
    }

    fun removeExercise(exerciseId: String) {
        _uiState.update { current ->
            val updated = current.editedExercises.filterNot { it.id == exerciseId }
            current.copy(editedExercises = updated)
        }
    }

    fun updateExerciseSets(exerciseId: String, sets: Int) {
        _uiState.update { current ->
            val updated = current.editedExercises.map {
                if (it.id == exerciseId) it.copy(sets = sets.coerceIn(1, 10)) else it
            }
            current.copy(editedExercises = updated)
        }
    }

    fun updateExerciseReps(exerciseId: String, min: Int, max: Int) {
        _uiState.update { current ->
            val updated = current.editedExercises.map {
                if (it.id == exerciseId) it.copy(repsMin = min, repsMax = max) else it
            }
            current.copy(editedExercises = updated)
        }
    }

    fun confirmAndSaveRoutine(onSaved: (String) -> Unit) {
        val state = _uiState.value
        val parsed = state.parsedData ?: return
        val exercises = state.editedExercises
        if (exercises.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val routineId = UUID.randomUUID().toString()
            val groups = mutableListOf<ExerciseGroup>()
            val routineExercises = mutableListOf<RoutineExercise>()

            // Ensure dictionary has exercises or resolve them
            val allDictionary = exerciseRepository.getAllExercises().firstOrNull().orEmpty()

            exercises.forEachIndexed { index, parsedEx ->
                val matchedDictionaryEx = allDictionary.find { it.name.equals(parsedEx.name, ignoreCase = true) }
                val exId = matchedDictionaryEx?.id ?: "ex_flat_bb_bench"

                val groupId = UUID.randomUUID().toString()
                groups.add(
                    ExerciseGroup(
                        id = groupId,
                        groupType = if (parsedEx.isSuperset) ExerciseGroupType.SUPERSET else ExerciseGroupType.SINGLE,
                        orderInRoutine = index,
                        restAfterGroupSeconds = parsedEx.restSeconds
                    )
                )

                routineExercises.add(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        exerciseGroupId = groupId,
                        exerciseId = exId,
                        orderInGroup = 0,
                        prescribedSets = parsedEx.sets,
                        prescribedRepsMin = parsedEx.repsMin,
                        prescribedRepsMax = parsedEx.repsMax,
                        restSeconds = parsedEx.restSeconds,
                        executionNotes = parsedEx.notes
                    )
                )
            }

            val routine = Routine(
                id = routineId,
                name = parsed.videoTitle,
                description = "Imported from ${parsed.creatorName}'s YouTube workout.",
                creatorName = parsed.creatorName,
                sourceVideoId = parsed.videoId,
                exerciseGroups = groups,
                estimatedDurationMinutes = (exercises.sumOf { it.sets * 2 } + 15).coerceIn(20, 90)
            )

            routineRepository.createRoutine(routine, groups, routineExercises)
            _uiState.update { it.copy(isSaving = false) }
            onSaved(routineId)
        }
    }

    fun reset() {
        _uiState.value = AIIngestionUiState()
    }
}
