package com.repforge.feature.history

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.domain.model.PersonalRecord
import com.repforge.core.domain.model.SetType
import com.repforge.core.domain.model.UnitConverter
import com.repforge.core.domain.model.UnitSystem
import com.repforge.core.domain.model.WorkoutSession
import com.repforge.core.domain.model.WorkoutSet
import com.repforge.core.domain.repository.ExerciseRepository
import com.repforge.core.domain.repository.UserProfileRepository
import com.repforge.core.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class DayContribution(
    val date: LocalDate,
    val workoutCount: Int,
    val intensityLevel: Int // 0: none, 1: 1 workout, 2: 2 workouts, 3: 3+ workouts
)

data class WorkoutSetDetail(
    val id: String,
    val exerciseName: String,
    val setNumber: Int,
    val setType: SetType,
    val weightKg: Double,
    val repsCompleted: Int,
    val rpe: Double?,
    val isCompleted: Boolean,
    val isPR: Boolean = false
)

data class SessionCardItem(
    val session: WorkoutSession,
    val formattedDate: String,
    val formattedDuration: String,
    val formattedTonnage: String,
    val formattedCalories: String,
    val monthYearKey: String
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val totalWorkouts: Int = 0,
    val streakWeeks: Int = 0,
    val totalTonnageDisplay: String = "0 kg",
    val personalRecords: List<PersonalRecord> = emptyList(),
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val monthlySessions: Map<String, List<SessionCardItem>> = emptyMap(),
    val frequencyGrid: List<DayContribution> = emptyList(),
    val expandedSessionId: String? = null,
    val expandedSets: List<WorkoutSetDetail> = emptyList(),
    val isLoadingSets: Boolean = false,
    val showTrophyRoom: Boolean = false,
    val showExportSheet: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a", Locale.getDefault())

    init {
        loadHistoryData()
    }

    private fun loadHistoryData() {
        viewModelScope.launch {
            combine(
                sessionRepository.getAllSessions(),
                sessionRepository.getAllPersonalRecords(),
                userProfileRepository.getUserProfile()
            ) { sessions, prs, profile ->
                val unitSystem = profile?.unitSystem ?: UnitSystem.Metric

                // Calculate total tonnage
                val totalTonnageRaw = sessions.sumOf { it.totalTonnageKg }
                val totalTonnageFormatted = when (unitSystem) {
                    is UnitSystem.Metric -> "%,d kg".format(totalTonnageRaw.toLong())
                    is UnitSystem.Imperial -> "%,d lb".format(UnitConverter.kgToLb(totalTonnageRaw).toLong())
                }

                // Process session items
                val sessionItems = sessions.map { session ->
                    val startedMillis = session.startedAtMillis ?: session.completedAtMillis ?: System.currentTimeMillis()
                    val sessionDate = Instant.ofEpochMilli(startedMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()

                    val startedAt = session.startedAtMillis
                    val completedAt = session.completedAtMillis
                    val durationMinutes = if (startedAt != null && completedAt != null) {
                        ((completedAt - startedAt) / 60000).coerceAtLeast(1)
                    } else 45

                    val hours = durationMinutes / 60
                    val mins = durationMinutes % 60
                    val durationStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

                    val tonnageStr = when (unitSystem) {
                        is UnitSystem.Metric -> "%,d kg".format(session.totalTonnageKg.toLong())
                        is UnitSystem.Imperial -> "%,d lb".format(UnitConverter.kgToLb(session.totalTonnageKg).toLong())
                    }

                    val caloriesStr = if (session.estimatedCaloriesLow > 0 && session.estimatedCaloriesHigh > 0) {
                        "%.0f-%.0f kcal".format(session.estimatedCaloriesLow, session.estimatedCaloriesHigh)
                    } else "Est. N/A"

                    SessionCardItem(
                        session = session,
                        formattedDate = sessionDate.format(dateFormatter),
                        formattedDuration = durationStr,
                        formattedTonnage = tonnageStr,
                        formattedCalories = caloriesStr,
                        monthYearKey = sessionDate.format(monthFormatter)
                    )
                }

                // Group by month
                val monthlyGrouped = sessionItems.groupBy { it.monthYearKey }

                // Calculate frequency grid (last 70 days = 10 weeks)
                val today = LocalDate.now()
                val startDate = today.minusDays(69) // 70 days total
                val sessionDateCounts = sessions.mapNotNull { it.startedAtMillis ?: it.completedAtMillis }
                    .map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                    .groupingBy { it }
                    .eachCount()

                val grid = (0 until 70).map { dayOffset ->
                    val day = startDate.plusDays(dayOffset.toLong())
                    val count = sessionDateCounts[day] ?: 0
                    val intensity = when {
                        count == 0 -> 0
                        count == 1 -> 1
                        count == 2 -> 2
                        else -> 3
                    }
                    DayContribution(date = day, workoutCount = count, intensityLevel = intensity)
                }

                // Calculate active streak weeks
                val streakWeeks = calculateStreakWeeks(sessions)

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        totalWorkouts = sessions.size,
                        streakWeeks = streakWeeks,
                        totalTonnageDisplay = totalTonnageFormatted,
                        personalRecords = prs.sortedByDescending { it.achievedAtMillis },
                        unitSystem = unitSystem,
                        monthlySessions = monthlyGrouped,
                        frequencyGrid = grid
                    )
                }
            }.collect {}
        }
    }

    private fun calculateStreakWeeks(sessions: List<WorkoutSession>): Int {
        if (sessions.isEmpty()) return 0
        val weeksWithWorkouts = sessions.mapNotNull { it.startedAtMillis ?: it.completedAtMillis }
            .map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { it.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)) }
            .toSet()

        val currentWeekMonday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        var streak = 0
        var checkWeek = currentWeekMonday

        // If no workout this week yet, allow checking from previous week
        if (!weeksWithWorkouts.contains(checkWeek)) {
            checkWeek = checkWeek.minusWeeks(1)
        }

        while (weeksWithWorkouts.contains(checkWeek)) {
            streak++
            checkWeek = checkWeek.minusWeeks(1)
        }
        return streak
    }

    fun toggleSessionExpand(sessionId: String) {
        if (_uiState.value.expandedSessionId == sessionId) {
            _uiState.update { it.copy(expandedSessionId = null, expandedSets = emptyList()) }
            return
        }

        _uiState.update { it.copy(expandedSessionId = sessionId, isLoadingSets = true) }
        viewModelScope.launch {
            val sets = sessionRepository.getSetsForSession(sessionId).firstOrNull().orEmpty()
            val prSetIds = _uiState.value.personalRecords.filter { it.sessionId == sessionId }.map { it.id }.toSet()

            val details = sets.map { set ->
                val exercise = exerciseRepository.getExerciseById(set.exerciseId)
                WorkoutSetDetail(
                    id = set.id,
                    exerciseName = exercise?.name ?: "Exercise",
                    setNumber = set.setNumber,
                    setType = set.setType,
                    weightKg = set.weightKg,
                    repsCompleted = set.repsCompleted,
                    rpe = set.rpe,
                    isCompleted = set.isCompleted,
                    isPR = prSetIds.contains(set.id)
                )
            }
            _uiState.update { it.copy(expandedSets = details, isLoadingSets = false) }
        }
    }

    fun toggleTrophyRoom(show: Boolean) {
        _uiState.update { it.copy(showTrophyRoom = show) }
    }

    fun toggleExportSheet(show: Boolean) {
        _uiState.update { it.copy(showExportSheet = show) }
    }

    fun exportWorkoutsAsCsv(context: Context) {
        viewModelScope.launch {
            val sessions = sessionRepository.getAllSessions().firstOrNull().orEmpty()
            val csvBuilder = StringBuilder()
            csvBuilder.appendLine("SessionId,Date,RoutineName,DurationMinutes,TotalTonnageKg,CaloriesLow,CaloriesHigh,Notes")

            sessions.forEach { s ->
                val started = s.startedAtMillis ?: s.completedAtMillis ?: 0L
                val dateStr = if (started > 0) Instant.ofEpochMilli(started).atZone(ZoneId.systemDefault()).toLocalDate().toString() else ""
                val startedAt = s.startedAtMillis
                val completedAt = s.completedAtMillis
                val duration = if (startedAt != null && completedAt != null) {
                    (completedAt - startedAt) / 60000
                } else 0
                val safeRoutine = s.routineName.replace(",", ";")
                val safeNotes = s.notes.replace(",", ";").replace("\n", " ")
                csvBuilder.appendLine("${s.id},$dateStr,$safeRoutine,$duration,${s.totalTonnageKg},${s.estimatedCaloriesLow},${s.estimatedCaloriesHigh},$safeNotes")
            }

            shareText(context, csvBuilder.toString(), "repforge_workouts_export.csv", "text/csv")
        }
    }

    fun exportWorkoutsAsJson(context: Context) {
        viewModelScope.launch {
            val sessions = sessionRepository.getAllSessions().firstOrNull().orEmpty()
            val jsonBuilder = StringBuilder()
            jsonBuilder.append("[\n")
            sessions.forEachIndexed { index, s ->
                val started = s.startedAtMillis ?: s.completedAtMillis ?: 0L
                val dateStr = if (started > 0) Instant.ofEpochMilli(started).atZone(ZoneId.systemDefault()).toLocalDate().toString() else ""
                jsonBuilder.append("  {\n")
                jsonBuilder.append("    \"id\": \"${s.id}\",\n")
                jsonBuilder.append("    \"routineName\": \"${s.routineName.replace("\"", "\\\"")}\",\n")
                jsonBuilder.append("    \"date\": \"$dateStr\",\n")
                jsonBuilder.append("    \"totalTonnageKg\": ${s.totalTonnageKg},\n")
                jsonBuilder.append("    \"estimatedCaloriesLow\": ${s.estimatedCaloriesLow},\n")
                jsonBuilder.append("    \"estimatedCaloriesHigh\": ${s.estimatedCaloriesHigh},\n")
                jsonBuilder.append("    \"notes\": \"${s.notes.replace("\"", "\\\"")}\"\n")
                jsonBuilder.append("  }${if (index < sessions.size - 1) "," else ""}\n")
            }
            jsonBuilder.append("]")

            shareText(context, jsonBuilder.toString(), "repforge_workouts_export.json", "application/json")
        }
    }

    private fun shareText(context: Context, content: String, title: String, mimeType: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_TITLE, title)
            type = mimeType
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export RepForge Data")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
