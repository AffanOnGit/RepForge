package com.repforge.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the canonical exercise dictionary.
 * Maps to the [com.repforge.core.domain.model.Exercise] domain model.
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    /** Equipment type: BARBELL, DUMBBELL, CABLE, MACHINE, BODYWEIGHT, KETTLEBELL, SMITH_MACHINE */
    val equipment: String,

    /** Primary targeted sub-muscle head. Uses SubMuscle enum name. */
    @ColumnInfo(name = "primary_sub_muscle")
    val primarySubMuscle: String,

    /** Comma-separated list of secondary sub-muscle enum names. */
    @ColumnInfo(name = "secondary_sub_muscles")
    val secondarySubMuscles: String = "",

    /** Comma-separated creator tags (e.g., "#JeffNippard,#Hypertrophy"). */
    @ColumnInfo(name = "creator_tags")
    val creatorTags: String = "",

    /** Whether this is a user-created custom exercise. */
    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false
)
