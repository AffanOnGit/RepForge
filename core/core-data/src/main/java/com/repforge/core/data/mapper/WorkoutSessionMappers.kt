package com.repforge.core.data.mapper

import com.repforge.core.data.database.entity.PersonalRecordEntity
import com.repforge.core.data.database.entity.WorkoutSessionEntity
import com.repforge.core.data.database.entity.WorkoutSetEntity
import com.repforge.core.domain.model.PRType
import com.repforge.core.domain.model.PersonalRecord
import com.repforge.core.domain.model.SessionState
import com.repforge.core.domain.model.SetType
import com.repforge.core.domain.model.WorkoutSession
import com.repforge.core.domain.model.WorkoutSet

fun WorkoutSessionEntity.toDomain(): WorkoutSession {
    return WorkoutSession(
        id = id,
        routineId = routineId,
        routineName = routineName,
        state = try {
            SessionState.valueOf(state)
        } catch (_: Exception) {
            SessionState.IDLE
        },
        startedAtMillis = startedAtMillis,
        completedAtMillis = completedAtMillis,
        totalTonnageKg = totalTonnageKg,
        estimatedCaloriesLow = estimatedCaloriesLow,
        estimatedCaloriesHigh = estimatedCaloriesHigh,
        notes = notes
    )
}

fun WorkoutSession.toEntity(): WorkoutSessionEntity {
    return WorkoutSessionEntity(
        id = id,
        routineId = routineId,
        routineName = routineName,
        state = state.name,
        startedAtMillis = startedAtMillis,
        completedAtMillis = completedAtMillis,
        totalTonnageKg = totalTonnageKg,
        estimatedCaloriesLow = estimatedCaloriesLow,
        estimatedCaloriesHigh = estimatedCaloriesHigh,
        notes = notes
    )
}

fun WorkoutSetEntity.toDomain(): WorkoutSet {
    return WorkoutSet(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        exerciseGroupId = exerciseGroupId,
        setNumber = setNumber,
        setType = try {
            SetType.valueOf(setType)
        } catch (_: Exception) {
            SetType.WORKING
        },
        weightKg = weightKg,
        repsCompleted = repsCompleted,
        targetReps = targetReps,
        isCompleted = isCompleted,
        rpe = rpe,
        completedAtMillis = completedAtMillis
    )
}

fun WorkoutSet.toEntity(): WorkoutSetEntity {
    return WorkoutSetEntity(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        exerciseGroupId = exerciseGroupId,
        setNumber = setNumber,
        setType = setType.name,
        weightKg = weightKg,
        repsCompleted = repsCompleted,
        targetReps = targetReps,
        isCompleted = isCompleted,
        rpe = rpe,
        completedAtMillis = completedAtMillis
    )
}

fun PersonalRecordEntity.toDomain(): PersonalRecord {
    return PersonalRecord(
        id = id,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        type = try {
            PRType.valueOf(type)
        } catch (_: Exception) {
            PRType.WEIGHT
        },
        value = value,
        achievedAtMillis = achievedAtMillis,
        sessionId = sessionId
    )
}

fun PersonalRecord.toEntity(): PersonalRecordEntity {
    return PersonalRecordEntity(
        id = id,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        type = type.name,
        value = value,
        achievedAtMillis = achievedAtMillis,
        sessionId = sessionId
    )
}
