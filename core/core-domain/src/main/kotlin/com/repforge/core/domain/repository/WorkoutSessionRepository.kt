package com.repforge.core.domain.repository

import com.repforge.core.domain.model.WorkoutSession
import com.repforge.core.domain.model.WorkoutSet
import com.repforge.core.domain.model.PersonalRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for workout sessions (logged workouts).
 */
interface WorkoutSessionRepository {

    /** Get the currently active (in-progress) session, if any. */
    fun getActiveSession(): Flow<WorkoutSession?>

    /** Get all completed sessions, ordered by date descending. */
    fun getAllSessions(): Flow<List<WorkoutSession>>

    /** Get a specific session with all its sets. */
    fun getSessionById(id: String): Flow<WorkoutSession?>

    /** Get all sets for a session. */
    fun getSetsForSession(sessionId: String): Flow<List<WorkoutSet>>

    /** Get sessions for a specific routine (for ghost text / history). */
    fun getSessionsForRoutine(routineId: String, limit: Int = 3): Flow<List<WorkoutSession>>

    /** Get the last N sets logged for a specific exercise (for ghost text). */
    fun getLastSetsForExercise(exerciseId: String, sessionLimit: Int = 1): Flow<List<WorkoutSet>>

    /** Start a new workout session. */
    suspend fun startSession(session: WorkoutSession): String

    /** Update session state (pause, resume, finish). */
    suspend fun updateSession(session: WorkoutSession)

    /** Log a completed set. */
    suspend fun logSet(set: WorkoutSet)

    /** Update an existing set. */
    suspend fun updateSet(set: WorkoutSet)

    /** Delete a set. */
    suspend fun deleteSet(id: String)

    /** Complete the session and calculate final stats. */
    suspend fun completeSession(sessionId: String)

    /** Get all personal records. */
    fun getAllPersonalRecords(): Flow<List<PersonalRecord>>

    /** Get PRs for a specific exercise. */
    fun getPersonalRecordsForExercise(exerciseId: String): Flow<List<PersonalRecord>>

    /** Check if a new PR was set and record it. */
    suspend fun checkAndRecordPR(set: WorkoutSet): PersonalRecord?
}
