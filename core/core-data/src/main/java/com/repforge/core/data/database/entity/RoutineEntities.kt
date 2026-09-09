package com.repforge.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for workout routines (templates).
 */
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val description: String = "",

    @ColumnInfo(name = "estimated_duration_minutes")
    val estimatedDurationMinutes: Int? = null,

    @ColumnInfo(name = "creator_name")
    val creatorName: String? = null,

    @ColumnInfo(name = "source_video_id")
    val sourceVideoId: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAtMillis: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAtMillis: Long = System.currentTimeMillis()
)

/**
 * Room entity for exercise groups within a routine.
 * Supports supersets, circuits, AMRAP, EMOM, etc.
 */
@Entity(
    tableName = "exercise_groups",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routine_id")]
)
data class ExerciseGroupEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "routine_id")
    val routineId: String,

    /** SINGLE, SUPERSET, TRISET, CIRCUIT, AMRAP, EMOM */
    @ColumnInfo(name = "group_type")
    val groupType: String,

    @ColumnInfo(name = "order_in_routine")
    val orderInRoutine: Int,

    @ColumnInfo(name = "rest_after_group_seconds")
    val restAfterGroupSeconds: Int,

    @ColumnInfo(name = "time_cap_seconds")
    val timeCapSeconds: Int? = null
)

/**
 * Room entity for exercises within a routine, belonging to an exercise group.
 * Contains prescribed volume from the template.
 */
@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_group_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exercise_group_id"), Index("exercise_id")]
)
data class RoutineExerciseEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "exercise_group_id")
    val exerciseGroupId: String,

    @ColumnInfo(name = "exercise_id")
    val exerciseId: String,

    @ColumnInfo(name = "order_in_group")
    val orderInGroup: Int,

    @ColumnInfo(name = "prescribed_sets")
    val prescribedSets: Int,

    @ColumnInfo(name = "prescribed_reps_min")
    val prescribedRepsMin: Int,

    @ColumnInfo(name = "prescribed_reps_max")
    val prescribedRepsMax: Int,

    @ColumnInfo(name = "rest_seconds")
    val restSeconds: Int,

    @ColumnInfo(name = "execution_notes")
    val executionNotes: String = ""
)
