package com.repforge.core.data.repository

import com.repforge.core.data.database.dao.ExerciseDao
import com.repforge.core.data.database.dao.WorkoutSessionDao
import com.repforge.core.data.database.entity.PersonalRecordEntity
import com.repforge.core.data.mapper.toDomain
import com.repforge.core.data.mapper.toEntity
import com.repforge.core.domain.model.PRType
import com.repforge.core.domain.model.PersonalRecord
import com.repforge.core.domain.model.SessionState
import com.repforge.core.domain.model.SetType
import com.repforge.core.domain.model.WorkoutSession
import com.repforge.core.domain.model.WorkoutSet
import com.repforge.core.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutSessionRepositoryImpl @Inject constructor(
    private val sessionDao: WorkoutSessionDao,
    private val exerciseDao: ExerciseDao
) : WorkoutSessionRepository {

    override fun getActiveSession(): Flow<WorkoutSession?> {
        return sessionDao.getActiveSession().map { it?.toDomain() }
    }

    override fun getAllSessions(): Flow<List<WorkoutSession>> {
        return sessionDao.getAllCompletedSessions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSessionById(id: String): Flow<WorkoutSession?> {
        return sessionDao.getSessionById(id).map { it?.toDomain() }
    }

    override fun getSetsForSession(sessionId: String): Flow<List<WorkoutSet>> {
        return sessionDao.getSetsForSession(sessionId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSessionsForRoutine(routineId: String, limit: Int): Flow<List<WorkoutSession>> {
        return sessionDao.getSessionsForRoutine(routineId, limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getLastSetsForExercise(exerciseId: String, sessionLimit: Int): Flow<List<WorkoutSet>> {
        return sessionDao.getLastSetsForExercise(exerciseId, sessionLimit * 5).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun startSession(session: WorkoutSession): String {
        val startedSession = session.copy(
            state = SessionState.ACTIVE,
            startedAtMillis = session.startedAtMillis ?: System.currentTimeMillis()
        )
        sessionDao.insertSession(startedSession.toEntity())
        return startedSession.id
    }

    override suspend fun updateSession(session: WorkoutSession) {
        sessionDao.updateSession(session.toEntity())
    }

    override suspend fun logSet(set: WorkoutSet) {
        sessionDao.insertSet(set.toEntity())
        if (set.isCompleted) {
            checkAndRecordPR(set)
        }
    }

    override suspend fun updateSet(set: WorkoutSet) {
        sessionDao.updateSet(set.toEntity())
        if (set.isCompleted) {
            checkAndRecordPR(set)
        }
    }

    override suspend fun deleteSet(id: String) {
        sessionDao.deleteSet(id)
    }

    override suspend fun completeSession(sessionId: String) {
        val sessionEntity = sessionDao.getSessionById(sessionId).firstOrNull() ?: return
        val sets = sessionDao.getSetsForSession(sessionId).firstOrNull().orEmpty()

        // Calculate total tonnage for completed working sets
        val totalTonnage = sets
            .filter { it.isCompleted && it.setType != SetType.WARMUP.name }
            .sumOf { it.weightKg * it.repsCompleted }

        // Rough calorie estimate calculation
        val durationMinutes = sessionEntity.startedAtMillis?.let {
            ((System.currentTimeMillis() - it) / 60000.0).coerceAtLeast(10.0)
        } ?: 45.0
        val baseCalories = durationMinutes * 5.5 + (totalTonnage * 0.015)
        val calLow = (baseCalories * 0.85)
        val calHigh = (baseCalories * 1.15)

        val completedSession = sessionEntity.copy(
            state = SessionState.COMPLETED.name,
            completedAtMillis = System.currentTimeMillis(),
            totalTonnageKg = totalTonnage,
            estimatedCaloriesLow = calLow,
            estimatedCaloriesHigh = calHigh
        )

        sessionDao.updateSession(completedSession)
    }

    override fun getAllPersonalRecords(): Flow<List<PersonalRecord>> {
        return sessionDao.getAllPersonalRecords().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getPersonalRecordsForExercise(exerciseId: String): Flow<List<PersonalRecord>> {
        return sessionDao.getPersonalRecordsForExercise(exerciseId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun checkAndRecordPR(set: WorkoutSet): PersonalRecord? {
        if (set.setType == SetType.WARMUP || !set.isCompleted || set.weightKg <= 0) return null

        val currentWeightPR = sessionDao.getBestPR(set.exerciseId, PRType.WEIGHT.name)
        if (currentWeightPR == null || set.weightKg > currentWeightPR.value) {
            val exercise = exerciseDao.getExerciseById(set.exerciseId)
            val exerciseName = exercise?.name ?: "Exercise"
            val newPR = PersonalRecordEntity(
                id = UUID.randomUUID().toString(),
                exerciseId = set.exerciseId,
                exerciseName = exerciseName,
                type = PRType.WEIGHT.name,
                value = set.weightKg,
                achievedAtMillis = System.currentTimeMillis(),
                sessionId = set.sessionId
            )
            sessionDao.insertPersonalRecord(newPR)
            return newPR.toDomain()
        }
        return null
    }
}
