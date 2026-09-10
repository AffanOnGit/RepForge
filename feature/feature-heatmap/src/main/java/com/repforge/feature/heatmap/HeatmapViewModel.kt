package com.repforge.feature.heatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.domain.model.SessionState
import com.repforge.core.domain.model.SetType
import com.repforge.core.domain.model.SubMuscle
import com.repforge.core.domain.repository.ExerciseRepository
import com.repforge.core.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SubMuscleHeatData(
    val subMuscle: SubMuscle,
    val totalSets: Int = 0,
    val totalTonnageKg: Double = 0.0,
    val heatIntensity: Float = 0f, // 0.0 to 1.0
    val daysSinceLastTrained: Int? = null,
    val topExercises: List<String> = emptyList()
)

enum class HeatmapScope {
    WEEKLY,
    SESSION
}

data class SessionHeatmapItem(
    val id: String,
    val routineName: String,
    val formattedDate: String
)

data class HeatmapUiState(
    val scope: HeatmapScope = HeatmapScope.WEEKLY,
    val selectedTimeWindowDays: Int = 7, // 7, 14, 30
    val recentSessions: List<SessionHeatmapItem> = emptyList(),
    val selectedSessionId: String? = null,
    val isFrontView: Boolean = true,
    val heatMapData: Map<SubMuscle, SubMuscleHeatData> = emptyMap(),
    val selectedSubMuscle: SubMuscleHeatData? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HeatmapViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeatmapUiState())
    val uiState: StateFlow<HeatmapUiState> = _uiState.asStateFlow()

    init {
        loadHeatmapData()
    }

    fun setScope(scope: HeatmapScope) {
        _uiState.update { it.copy(scope = scope, selectedSubMuscle = null) }
        loadHeatmapData()
    }

    fun selectSession(sessionId: String) {
        _uiState.update { it.copy(selectedSessionId = sessionId, selectedSubMuscle = null) }
        loadHeatmapData()
    }

    fun setTimeWindow(days: Int) {
        _uiState.update { it.copy(selectedTimeWindowDays = days, selectedSubMuscle = null) }
        loadHeatmapData()
    }

    fun toggleView(isFront: Boolean) {
        _uiState.update { it.copy(isFrontView = isFront) }
    }

    fun selectSubMuscle(subMuscle: SubMuscle?) {
        val detail = subMuscle?.let { _uiState.value.heatMapData[it] }
        _uiState.update { it.copy(selectedSubMuscle = detail) }
    }

    fun loadHeatmapData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val allSessions = sessionRepository.getAllSessions().firstOrNull().orEmpty()
            val completedSessions = allSessions
                .filter { it.state == SessionState.COMPLETED || it.completedAtMillis != null }
                .sortedByDescending { it.completedAtMillis ?: it.startedAtMillis ?: 0L }

            val sessionItems = completedSessions.take(15).map { s ->
                val time = s.completedAtMillis ?: s.startedAtMillis ?: System.currentTimeMillis()
                val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(time))
                val title = if (s.routineName.isNotBlank()) s.routineName else "Freestyle Session"
                SessionHeatmapItem(
                    id = s.id,
                    routineName = title,
                    formattedDate = dateStr
                )
            }

            val currentScope = _uiState.value.scope
            val currentSelectedSessionId = _uiState.value.selectedSessionId
                ?: sessionItems.firstOrNull()?.id

            val targetSessions = if (currentScope == HeatmapScope.SESSION) {
                val found = completedSessions.find { it.id == currentSelectedSessionId }
                    ?: completedSessions.firstOrNull()
                if (found != null) listOf(found) else emptyList()
            } else {
                val cutoffMillis = System.currentTimeMillis() - (_uiState.value.selectedTimeWindowDays * 86400000L)
                allSessions.filter {
                    (it.completedAtMillis ?: it.startedAtMillis ?: 0L) >= cutoffMillis
                }
            }

            val muscleSetsCount = mutableMapOf<SubMuscle, Int>()
            val muscleTonnage = mutableMapOf<SubMuscle, Double>()
            val muscleLastTrainedMillis = mutableMapOf<SubMuscle, Long>()
            val muscleExerciseNames = mutableMapOf<SubMuscle, MutableSet<String>>()

            for (session in targetSessions) {
                val sets = sessionRepository.getSetsForSession(session.id).firstOrNull().orEmpty()
                val sessionTime = session.completedAtMillis ?: session.startedAtMillis ?: System.currentTimeMillis()

                for (set in sets) {
                    if (!set.isCompleted || set.setType == SetType.WARMUP) continue

                    val exercise = exerciseRepository.getExerciseById(set.exerciseId) ?: continue
                    val primary = exercise.primarySubMuscle

                    muscleSetsCount[primary] = muscleSetsCount.getOrDefault(primary, 0) + 1
                    muscleTonnage[primary] = muscleTonnage.getOrDefault(primary, 0.0) + (set.weightKg * set.repsCompleted)

                    val prevTime = muscleLastTrainedMillis.getOrDefault(primary, 0L)
                    if (sessionTime > prevTime) {
                        muscleLastTrainedMillis[primary] = sessionTime
                    }

                    val exSet = muscleExerciseNames.getOrPut(primary) { mutableSetOf() }
                    exSet.add(exercise.name)
                }
            }

            val maxSets = muscleSetsCount.values.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f

            val resultData = SubMuscle.entries.associateWith { muscle ->
                val sets = muscleSetsCount.getOrDefault(muscle, 0)
                val tonnage = muscleTonnage.getOrDefault(muscle, 0.0)
                val intensity = (sets.toFloat() / maxSets).coerceIn(0f, 1f)
                val lastTrained = muscleLastTrainedMillis[muscle]?.let {
                    val diff = System.currentTimeMillis() - it
                    (diff / 86400000L).toInt()
                }

                SubMuscleHeatData(
                    subMuscle = muscle,
                    totalSets = sets,
                    totalTonnageKg = tonnage,
                    heatIntensity = intensity,
                    daysSinceLastTrained = lastTrained,
                    topExercises = muscleExerciseNames[muscle]?.take(3)?.toList().orEmpty()
                )
            }

            _uiState.update {
                it.copy(
                    recentSessions = sessionItems,
                    selectedSessionId = currentSelectedSessionId,
                    heatMapData = resultData,
                    isLoading = false
                )
            }
        }
    }
}
