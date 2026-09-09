package com.repforge.feature.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.data.database.dao.RoutineDao
import com.repforge.core.data.mapper.toDomain
import com.repforge.core.domain.engine.CaloricEngine
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.ExerciseGroup
import com.repforge.core.domain.model.ExerciseGroupType
import com.repforge.core.domain.model.PersonalRecord
import com.repforge.core.domain.model.Routine
import com.repforge.core.domain.model.RoutineExercise
import com.repforge.core.domain.model.SetType
import com.repforge.core.domain.model.SubMuscle
import com.repforge.core.domain.model.WorkoutSession
import com.repforge.core.domain.model.WorkoutSet
import com.repforge.core.domain.repository.ExerciseRepository
import com.repforge.core.domain.repository.RoutineRepository
import com.repforge.core.domain.repository.UserProfileRepository
import com.repforge.core.domain.repository.WorkoutSessionRepository
import com.repforge.core.health.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class SubMuscleVolume(
    val subMuscle: SubMuscle,
    val percentage: Double,
    val totalTonnage: Double
)

data class WorkoutSummaryUiState(
    val session: WorkoutSession? = null,
    val completedSets: List<WorkoutSet> = emptyList(),
    val totalTonnageKg: Double = 0.0,
    val durationMinutes: Int = 0,
    val totalSetsCount: Int = 0,
    val calorieEstimate: CaloricEngine.CalorieEstimate? = null,
    val newPersonalRecords: List<PersonalRecord> = emptyList(),
    val subMuscleDistribution: List<SubMuscleVolume> = emptyList(),

    // Diff Resolver
    val hasDiffs: Boolean = false,
    val diffSummary: String = "",
    val showDiffDialog: Boolean = false,
    val isHealthConnectSynced: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class WorkoutSummaryViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val userProfileRepository: UserProfileRepository,
    private val healthConnectManager: HealthConnectManager,
    private val routineDao: RoutineDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _uiState = MutableStateFlow(WorkoutSummaryUiState())
    val uiState: StateFlow<WorkoutSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val session = sessionRepository.getSessionById(sessionId).firstOrNull() ?: return@launch
            val sets = sessionRepository.getSetsForSession(sessionId).firstOrNull().orEmpty()
            val userProfile = userProfileRepository.getUserProfile().firstOrNull()

            val startedAt = session.startedAtMillis ?: System.currentTimeMillis()
            val completedAt = session.completedAtMillis ?: System.currentTimeMillis()
            val durationMinutes = ((completedAt - startedAt) / 60000.0).toInt().coerceAtLeast(1)

            val workingSets = sets.filter { it.isCompleted && it.setType != SetType.WARMUP }
            val totalTonnage = workingSets.sumOf { it.weightKg * it.repsCompleted }

            // Calorie estimation
            val calories = CaloricEngine.estimateSessionCalories(
                userProfile = userProfile,
                durationMinutes = durationMinutes.toDouble(),
                completedSets = sets
            )

            // PRs in this session
            val prs = sessionRepository.getAllPersonalRecords().firstOrNull().orEmpty()
                .filter { it.sessionId == sessionId }

            // Sub-muscle distribution
            val muscleTonnageMap = mutableMapOf<SubMuscle, Double>()
            for (set in workingSets) {
                val exercise = exerciseRepository.getExerciseById(set.exerciseId)
                if (exercise != null) {
                    val tonnage = set.weightKg * set.repsCompleted
                    val current = muscleTonnageMap.getOrDefault(exercise.primarySubMuscle, 0.0)
                    muscleTonnageMap[exercise.primarySubMuscle] = current + tonnage
                }
            }

            val grandTotalTonnage = muscleTonnageMap.values.sum().coerceAtLeast(1.0)
            val subMuscleList = muscleTonnageMap.map { (muscle, tonnage) ->
                SubMuscleVolume(
                    subMuscle = muscle,
                    percentage = (tonnage / grandTotalTonnage) * 100.0,
                    totalTonnage = tonnage
                )
            }.sortedByDescending { it.percentage }

            // Check Diff Resolver
            var hasDiffs = false
            var diffSummary = ""
            val routineId = session.routineId
            if (routineId != null) {
                val routineExercises = routineDao.getAllExercisesForRoutine(routineId).firstOrNull().orEmpty()
                val originalExerciseIds = routineExercises.map { it.exerciseId }.toSet()
                val loggedExerciseIds = sets.map { it.exerciseId }.toSet()

                val swappedOrAdded = loggedExerciseIds - originalExerciseIds
                if (swappedOrAdded.isNotEmpty() || sets.size != routineExercises.sumOf { it.prescribedSets }) {
                    hasDiffs = true
                    diffSummary = "Changes detected from base routine (${swappedOrAdded.size} swapped/added movements)."
                }
            }

            // Sync to Health Connect
            val synced = healthConnectManager.writeWorkoutSession(
                startTime = Instant.ofEpochMilli(startedAt),
                endTime = Instant.ofEpochMilli(completedAt),
                title = session.routineName.ifBlank { "Strength Training" },
                caloriesKcal = calories.totalKcal
            )

            _uiState.update {
                it.copy(
                    session = session,
                    completedSets = sets,
                    totalTonnageKg = totalTonnage,
                    durationMinutes = durationMinutes,
                    totalSetsCount = sets.count { s -> s.isCompleted },
                    calorieEstimate = calories,
                    newPersonalRecords = prs,
                    subMuscleDistribution = subMuscleList,
                    hasDiffs = hasDiffs,
                    diffSummary = diffSummary,
                    showDiffDialog = hasDiffs,
                    isHealthConnectSynced = synced,
                    isLoading = false
                )
            }
        }
    }

    fun dismissDiffDialog() {
        _uiState.update { it.copy(showDiffDialog = false) }
    }

    fun updateBaseRoutine() {
        val routineId = _uiState.value.session?.routineId ?: return
        viewModelScope.launch {
            val baseRoutine = routineRepository.getRoutineById(routineId).firstOrNull() ?: return@launch
            val completedSets = _uiState.value.completedSets.filter { it.isCompleted }

            val distinctExerciseIds = completedSets.map { it.exerciseId }.distinct()
            val groups = mutableListOf<ExerciseGroup>()
            val exercises = mutableListOf<RoutineExercise>()

            distinctExerciseIds.forEachIndexed { index, exId ->
                val groupId = UUID.randomUUID().toString()
                groups.add(
                    ExerciseGroup(
                        id = groupId,
                        groupType = ExerciseGroupType.SINGLE,
                        orderInRoutine = index,
                        restAfterGroupSeconds = 90
                    )
                )
                val setsForEx = completedSets.filter { it.exerciseId == exId }
                exercises.add(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        exerciseGroupId = groupId,
                        exerciseId = exId,
                        orderInGroup = 0,
                        prescribedSets = setsForEx.size.coerceAtLeast(1),
                        prescribedRepsMin = setsForEx.minOfOrNull { it.repsCompleted } ?: 8,
                        prescribedRepsMax = setsForEx.maxOfOrNull { it.repsCompleted } ?: 12,
                        restSeconds = 90
                    )
                )
            }

            routineRepository.updateRoutine(baseRoutine, groups, exercises)
            dismissDiffDialog()
        }
    }

    fun saveAsNewVariation() {
        val routineId = _uiState.value.session?.routineId ?: return
        viewModelScope.launch {
            val baseRoutine = routineRepository.getRoutineById(routineId).firstOrNull() ?: return@launch
            routineRepository.duplicateRoutine(routineId, "${baseRoutine.name} (Updated)")
            dismissDiffDialog()
        }
    }
}
