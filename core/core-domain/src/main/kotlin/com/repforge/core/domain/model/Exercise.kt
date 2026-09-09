package com.repforge.core.domain.model

/**
 * Equipment types available for exercises.
 */
enum class Equipment(val displayName: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    CABLE("Cable"),
    MACHINE("Machine"),
    BODYWEIGHT("Bodyweight"),
    KETTLEBELL("Kettlebell"),
    SMITH_MACHINE("Smith Machine");
}

/**
 * Canonical exercise from the exercise dictionary.
 * Represents a standardized movement with muscle targeting data.
 *
 * Pure domain model — no Room or Android annotations.
 */
data class Exercise(
    val id: String,
    val name: String,
    val equipment: Equipment,
    val primarySubMuscle: SubMuscle,
    val secondarySubMuscles: List<SubMuscle> = emptyList(),
    val creatorTags: List<String> = emptyList(),
    val isCustom: Boolean = false
)
