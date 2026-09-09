package com.repforge.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a workout session (a logged workout instance).
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "routine_id")
    val routineId: String? = null,

    @ColumnInfo(name = "routine_name")
    val routineName: String = "",

    /** IDLE, ACTIVE, PAUSED, FINISHING, COMPLETED */
    val state: String = "IDLE",

    @ColumnInfo(name = "started_at")
    val startedAtMillis: Long? = null,

    @ColumnInfo(name = "completed_at")
    val completedAtMillis: Long? = null,

    @ColumnInfo(name = "total_tonnage_kg")
    val totalTonnageKg: Double = 0.0,

    @ColumnInfo(name = "estimated_calories_low")
    val estimatedCaloriesLow: Double = 0.0,

    @ColumnInfo(name = "estimated_calories_high")
    val estimatedCaloriesHigh: Double = 0.0,

    val notes: String = ""
)

/**
 * Room entity for a single set logged during a workout session.
 */
@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("session_id"),
        Index("exercise_id")
    ]
)
data class WorkoutSetEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "exercise_id")
    val exerciseId: String,

    @ColumnInfo(name = "exercise_group_id")
    val exerciseGroupId: String? = null,

    @ColumnInfo(name = "set_number")
    val setNumber: Int,

    /** WARMUP, WORKING, DROP_SET, FAILURE */
    @ColumnInfo(name = "set_type")
    val setType: String,

    @ColumnInfo(name = "weight_kg")
    val weightKg: Double,

    @ColumnInfo(name = "reps_completed")
    val repsCompleted: Int,

    @ColumnInfo(name = "target_reps")
    val targetReps: Int? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    /** Rate of Perceived Exertion 6.0–10.0 */
    val rpe: Double? = null,

    @ColumnInfo(name = "completed_at")
    val completedAtMillis: Long? = null
)

/**
 * Room entity for personal records.
 */
@Entity(
    tableName = "personal_records",
    indices = [Index("exercise_id")]
)
data class PersonalRecordEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "exercise_id")
    val exerciseId: String,

    @ColumnInfo(name = "exercise_name")
    val exerciseName: String,

    /** WEIGHT, REPS, VOLUME */
    val type: String,

    val value: Double,

    @ColumnInfo(name = "achieved_at")
    val achievedAtMillis: Long,

    @ColumnInfo(name = "session_id")
    val sessionId: String
)


