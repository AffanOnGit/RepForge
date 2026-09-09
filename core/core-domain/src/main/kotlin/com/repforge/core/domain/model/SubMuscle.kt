package com.repforge.core.domain.model

/**
 * Mainstream sub-muscle taxonomy using gym-friendly terminology.
 * Rejects obscure Latin medical names in favor of universally recognized gym terms.
 *
 * This enum is the foundation of RepForge's anatomical mapping system, powering
 * the interactive 2D heatmap, exercise filtering, and volume tracking.
 */
enum class SubMuscle(
    val displayName: String,
    val parentGroup: MuscleGroup
) {
    // Chest (Pectorals)
    UPPER_CHEST("Upper Chest", MuscleGroup.CHEST),
    MID_CHEST("Mid Chest", MuscleGroup.CHEST),
    LOWER_CHEST("Lower Chest", MuscleGroup.CHEST),

    // Shoulders (Deltoids)
    FRONT_DELTS("Front Delts", MuscleGroup.SHOULDERS),
    SIDE_DELTS("Side Delts", MuscleGroup.SHOULDERS),
    REAR_DELTS("Rear Delts", MuscleGroup.SHOULDERS),

    // Back
    LATS("Lats (Width)", MuscleGroup.BACK),
    UPPER_BACK_TRAPS("Upper Back / Traps", MuscleGroup.BACK),
    LOWER_BACK("Lower Back", MuscleGroup.BACK),

    // Arms
    BICEPS("Biceps", MuscleGroup.ARMS),
    TRICEPS("Triceps", MuscleGroup.ARMS),
    FOREARMS("Forearms", MuscleGroup.ARMS),

    // Legs
    QUADS("Quads", MuscleGroup.LEGS),
    HAMSTRINGS("Hamstrings", MuscleGroup.LEGS),
    GLUTES("Glutes", MuscleGroup.LEGS),
    CALVES("Calves", MuscleGroup.LEGS),

    // Core
    UPPER_ABS("Upper Abs", MuscleGroup.CORE),
    LOWER_ABS("Lower Abs", MuscleGroup.CORE),
    OBLIQUES("Obliques", MuscleGroup.CORE);
}

/**
 * Top-level muscle group categories that contain sub-muscles.
 */
enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    SHOULDERS("Shoulders"),
    BACK("Back"),
    ARMS("Arms"),
    LEGS("Legs"),
    CORE("Core");

    /** Returns all sub-muscles belonging to this group. */
    val subMuscles: List<SubMuscle>
        get() = SubMuscle.entries.filter { it.parentGroup == this }
}
