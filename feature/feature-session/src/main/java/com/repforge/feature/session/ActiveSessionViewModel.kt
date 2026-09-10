package com.repforge.feature.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.data.database.dao.ExerciseDao
import com.repforge.core.data.database.dao.RoutineDao
import com.repforge.core.data.mapper.toDomain
import com.repforge.core.domain.model.Equipment
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.PersonalRecord
import com.repforge.core.domain.model.SessionState
import com.repforge.core.domain.model.SetType
import com.repforge.core.domain.model.SubMuscle
import com.repforge.core.domain.model.UnitSystem
import com.repforge.core.domain.model.WorkoutSession
import com.repforge.core.domain.model.WorkoutSet
import com.repforge.core.domain.repository.ExerciseRepository
import com.repforge.core.domain.repository.RoutineRepository
import com.repforge.core.domain.repository.UserProfileRepository
import com.repforge.core.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ActiveExerciseSetUi(
    val set: WorkoutSet,
    val ghostText: String? = null
)

data class ActiveExerciseUi(
    val exercise: Exercise,
    val sets: List<ActiveExerciseSetUi>,
    val defaultRestSeconds: Int = 90,
    val bestPRDisplay: String? = null
)

data class ActiveSessionUiState(
    val sessionId: String = "",
    val routineName: String = "Freestyle Workout",
    val routineId: String? = null,
    val exercises: List<ActiveExerciseUi> = emptyList(),
    val startedAtMillis: Long = System.currentTimeMillis(),
    val elapsedSeconds: Long = 0,
    val totalTonnageKg: Double = 0.0,
    val estimatedCalories: Int = 0,
    val unitSystem: UnitSystem = UnitSystem.Metric,

    // Rest Timer
    val isRestTimerVisible: Boolean = false,
    val restTimerRemainingSeconds: Int = 90,
    val restTimerTotalSeconds: Int = 90,

    // Plate Math Helper
    val isPlateMathOpen: Boolean = false,
    val plateMathWeightKg: Double = 60.0,
    val plateMathBarKg: Double = 20.0,

    // Exercise Swap
    val isSwapSheetOpen: Boolean = false,
    val exerciseToSwapIndex: Int = -1,
    val swapSuggestions: List<Exercise> = emptyList(),

    // PR Celebration
    val latestPR: PersonalRecord? = null,

    val isFinished: Boolean = false,
    // true when no user profile exists; calorie figures use population defaults
    val isProfileMissing: Boolean = false
)

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val userProfileRepository: UserProfileRepository,
    private val routineDao: RoutineDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routineIdParam: String? = savedStateHandle["routineId"]

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        initializeSession()
        startElapsedTimer()
    }

    private fun initializeSession() {
        viewModelScope.launch {
            val userProfile = userProfileRepository.getUserProfile().firstOrNull()
            val unitSystem = userProfile?.unitSystem ?: UnitSystem.Metric
            val profileMissing = userProfile == null
            val sessionId = UUID.randomUUID().toString()

            val activeRoutineId = routineIdParam
            if (activeRoutineId != null && activeRoutineId.isNotBlank()) {
                val routine = routineRepository.getRoutineById(activeRoutineId).firstOrNull()
                val exercisesInRoutine = routineDao.getAllExercisesForRoutine(activeRoutineId).firstOrNull().orEmpty()

                val exerciseUiList = mutableListOf<ActiveExerciseUi>()

                for (reEntity in exercisesInRoutine) {
                    val exercise = exerciseRepository.getExerciseById(reEntity.exerciseId) ?: continue
                    val lastSets = sessionRepository.getLastSetsForExercise(exercise.id, 1).firstOrNull().orEmpty()

                    val sets = (1..reEntity.prescribedSets).map { setIndex ->
                        val ghostSet = lastSets.getOrNull(setIndex - 1)
                        val ghostText = ghostSet?.let {
                            "Prev: ${it.weightKg.toInt()} kg × ${it.repsCompleted}"
                        }

                        val workoutSet = WorkoutSet(
                            id = UUID.randomUUID().toString(),
                            sessionId = sessionId,
                            exerciseId = exercise.id,
                            exerciseGroupId = reEntity.exerciseGroupId,
                            setNumber = setIndex,
                            setType = SetType.WORKING,
                            weightKg = ghostSet?.weightKg ?: 40.0,
                            repsCompleted = ghostSet?.repsCompleted ?: reEntity.prescribedRepsMax,
                            targetReps = reEntity.prescribedRepsMax
                        )
                        ActiveExerciseSetUi(set = workoutSet, ghostText = ghostText)
                    }

                    exerciseUiList.add(
                        ActiveExerciseUi(
                            exercise = exercise,
                            sets = sets,
                            defaultRestSeconds = reEntity.restSeconds
                        )
                    )
                }

                val workoutSession = WorkoutSession(
                    id = sessionId,
                    routineId = activeRoutineId,
                    routineName = routine?.name ?: "Workout",
                    state = SessionState.ACTIVE,
                    startedAtMillis = System.currentTimeMillis()
                )
                sessionRepository.startSession(workoutSession)

                _uiState.update {
                    it.copy(
                        sessionId = sessionId,
                        routineId = activeRoutineId,
                        routineName = routine?.name ?: "Workout",
                        exercises = exerciseUiList,
                        unitSystem = unitSystem,
                        isProfileMissing = profileMissing
                    )
                }
            } else {
                // Freestyle workout — start with 1 default exercise (e.g. Barbell Bench Press)
                val defaultEx = exerciseRepository.getExerciseById("ex_flat_bb_bench")
                    ?: exerciseRepository.getAllExercises().firstOrNull()?.firstOrNull()

                val initialExercises = if (defaultEx != null) {
                    listOf(
                        ActiveExerciseUi(
                            exercise = defaultEx,
                            sets = (1..3).map { setNum ->
                                ActiveExerciseSetUi(
                                    set = WorkoutSet(
                                        id = UUID.randomUUID().toString(),
                                        sessionId = sessionId,
                                        exerciseId = defaultEx.id,
                                        setNumber = setNum,
                                        setType = SetType.WORKING,
                                        weightKg = 60.0,
                                        repsCompleted = 10
                                    )
                                )
                            }
                        )
                    )
                } else emptyList()

                val workoutSession = WorkoutSession(
                    id = sessionId,
                    routineName = "Freestyle Workout",
                    state = SessionState.ACTIVE,
                    startedAtMillis = System.currentTimeMillis()
                )
                sessionRepository.startSession(workoutSession)

                _uiState.update {
                    it.copy(
                        sessionId = sessionId,
                        routineName = "Freestyle Workout",
                        exercises = initialExercises,
                        unitSystem = unitSystem,
                        isProfileMissing = profileMissing
                    )
                }
            }
        }
    }

    private fun startElapsedTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { current ->
                    val newElapsed = current.elapsedSeconds + 1
                    val minutes = newElapsed / 60.0
                    val burn = (minutes * 5.5 + current.totalTonnageKg * 0.015).toInt()
                    current.copy(
                        elapsedSeconds = newElapsed,
                        estimatedCalories = burn
                    )
                }
            }
        }
    }

    fun toggleSetComplete(exerciseIndex: Int, setIndex: Int) {
        val currentEx = _uiState.value.exercises.getOrNull(exerciseIndex) ?: return
        val currentSetUi = currentEx.sets.getOrNull(setIndex) ?: return
        val currentSet = currentSetUi.set

        val toggledSet = currentSet.copy(
            isCompleted = !currentSet.isCompleted,
            completedAtMillis = if (!currentSet.isCompleted) System.currentTimeMillis() else null
        )

        viewModelScope.launch {
            sessionRepository.logSet(toggledSet)

            // Update UI state
            _uiState.update { state ->
                val updatedExercises = state.exercises.toMutableList()
                val updatedSets = currentEx.sets.toMutableList()
                updatedSets[setIndex] = currentSetUi.copy(set = toggledSet)
                updatedExercises[exerciseIndex] = currentEx.copy(sets = updatedSets)

                // Recalculate total tonnage
                val tonnage = updatedExercises.flatMap { it.sets }
                    .filter { it.set.isCompleted && it.set.setType != SetType.WARMUP }
                    .sumOf { it.set.weightKg * it.set.repsCompleted }

                state.copy(
                    exercises = updatedExercises,
                    totalTonnageKg = tonnage
                )
            }

            // If checked as completed, trigger Auto-Rest Timer!
            if (toggledSet.isCompleted) {
                startRestTimer(currentEx.defaultRestSeconds)
            }
        }
    }

    fun cycleSetType(exerciseIndex: Int, setIndex: Int) {
        val currentEx = _uiState.value.exercises.getOrNull(exerciseIndex) ?: return
        val currentSetUi = currentEx.sets.getOrNull(setIndex) ?: return
        val currentSet = currentSetUi.set

        val nextType = when (currentSet.setType) {
            SetType.WORKING -> SetType.WARMUP
            SetType.WARMUP -> SetType.DROP_SET
            SetType.DROP_SET -> SetType.FAILURE
            SetType.FAILURE -> SetType.WORKING
        }

        val updatedSet = currentSet.copy(setType = nextType)
        viewModelScope.launch {
            sessionRepository.updateSet(updatedSet)
            _uiState.update { state ->
                val updatedExercises = state.exercises.toMutableList()
                val updatedSets = currentEx.sets.toMutableList()
                updatedSets[setIndex] = currentSetUi.copy(set = updatedSet)
                updatedExercises[exerciseIndex] = currentEx.copy(sets = updatedSets)
                state.copy(exercises = updatedExercises)
            }
        }
    }

    fun updateWeight(exerciseIndex: Int, setIndex: Int, weightText: String) {
        val weight = weightText.toDoubleOrNull() ?: return
        updateSetValues(exerciseIndex, setIndex, weight = weight)
    }

    fun updateReps(exerciseIndex: Int, setIndex: Int, repsText: String) {
        val reps = repsText.toIntOrNull() ?: return
        updateSetValues(exerciseIndex, setIndex, reps = reps)
    }

    private fun updateSetValues(exerciseIndex: Int, setIndex: Int, weight: Double? = null, reps: Int? = null) {
        val currentEx = _uiState.value.exercises.getOrNull(exerciseIndex) ?: return
        val currentSetUi = currentEx.sets.getOrNull(setIndex) ?: return
        val updatedSet = currentSetUi.set.copy(
            weightKg = weight ?: currentSetUi.set.weightKg,
            repsCompleted = reps ?: currentSetUi.set.repsCompleted
        )

        viewModelScope.launch {
            sessionRepository.updateSet(updatedSet)
            _uiState.update { state ->
                val updatedExercises = state.exercises.toMutableList()
                val updatedSets = currentEx.sets.toMutableList()
                updatedSets[setIndex] = currentSetUi.copy(set = updatedSet)
                updatedExercises[exerciseIndex] = currentEx.copy(sets = updatedSets)
                state.copy(exercises = updatedExercises)
            }
        }
    }

    fun addSet(exerciseIndex: Int) {
        val currentEx = _uiState.value.exercises.getOrNull(exerciseIndex) ?: return
        val lastSet = currentEx.sets.lastOrNull()?.set

        val newSet = WorkoutSet(
            id = UUID.randomUUID().toString(),
            sessionId = _uiState.value.sessionId,
            exerciseId = currentEx.exercise.id,
            setNumber = currentEx.sets.size + 1,
            setType = SetType.WORKING,
            weightKg = lastSet?.weightKg ?: 40.0,
            repsCompleted = lastSet?.repsCompleted ?: 10
        )

        viewModelScope.launch {
            sessionRepository.logSet(newSet)
            _uiState.update { state ->
                val updatedExercises = state.exercises.toMutableList()
                val updatedSets = currentEx.sets + ActiveExerciseSetUi(set = newSet)
                updatedExercises[exerciseIndex] = currentEx.copy(sets = updatedSets)
                state.copy(exercises = updatedExercises)
            }
        }
    }

    // Auto-Rest Timer
    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _uiState.update {
            it.copy(
                isRestTimerVisible = true,
                restTimerTotalSeconds = seconds,
                restTimerRemainingSeconds = seconds
            )
        }

        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restTimerRemainingSeconds > 0) {
                delay(1000)
                _uiState.update {
                    val remaining = it.restTimerRemainingSeconds - 1
                    it.copy(
                        restTimerRemainingSeconds = remaining,
                        isRestTimerVisible = remaining > 0
                    )
                }
            }
        }
    }

    fun add30sRest() {
        _uiState.update {
            val newTotal = it.restTimerTotalSeconds + 30
            val newRemaining = it.restTimerRemainingSeconds + 30
            it.copy(restTimerTotalSeconds = newTotal, restTimerRemainingSeconds = newRemaining)
        }
    }

    fun subtract15sRest() {
        _uiState.update {
            val newRemaining = (it.restTimerRemainingSeconds - 15).coerceAtLeast(0)
            it.copy(
                restTimerRemainingSeconds = newRemaining,
                isRestTimerVisible = newRemaining > 0
            )
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isRestTimerVisible = false, restTimerRemainingSeconds = 0) }
    }

    // Plate Math Helper
    fun openPlateMath(weightKg: Double) {
        _uiState.update {
            it.copy(
                isPlateMathOpen = true,
                plateMathWeightKg = weightKg
            )
        }
    }

    fun closePlateMath() {
        _uiState.update { it.copy(isPlateMathOpen = false) }
    }

    // Exercise Swap
    fun openExerciseSwap(exerciseIndex: Int) {
        val exercise = _uiState.value.exercises.getOrNull(exerciseIndex)?.exercise ?: return
        viewModelScope.launch {
            val suggestions = exerciseRepository.getSwapSuggestions(exercise.id).firstOrNull().orEmpty()
            _uiState.update {
                it.copy(
                    isSwapSheetOpen = true,
                    exerciseToSwapIndex = exerciseIndex,
                    swapSuggestions = suggestions
                )
            }
        }
    }

    fun closeExerciseSwap() {
        _uiState.update { it.copy(isSwapSheetOpen = false, exerciseToSwapIndex = -1) }
    }

    fun swapExercise(newExercise: Exercise) {
        val index = _uiState.value.exerciseToSwapIndex
        if (index < 0) return

        val currentExUi = _uiState.value.exercises.getOrNull(index) ?: return
        val updatedSets = currentExUi.sets.map { setUi ->
            setUi.copy(set = setUi.set.copy(exerciseId = newExercise.id))
        }

        viewModelScope.launch {
            updatedSets.forEach { sessionRepository.updateSet(it.set) }

            _uiState.update { state ->
                val updatedExercises = state.exercises.toMutableList()
                updatedExercises[index] = currentExUi.copy(
                    exercise = newExercise,
                    sets = updatedSets
                )
                state.copy(
                    exercises = updatedExercises,
                    isSwapSheetOpen = false,
                    exerciseToSwapIndex = -1
                )
            }
        }
    }

    fun finishWorkout(onFinished: () -> Unit) {
        viewModelScope.launch {
            timerJob?.cancel()
            restTimerJob?.cancel()
            sessionRepository.completeSession(_uiState.value.sessionId)
            _uiState.update { it.copy(isFinished = true) }
            onFinished()
        }
    }
}
