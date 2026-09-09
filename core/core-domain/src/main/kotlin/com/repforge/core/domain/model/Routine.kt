package com.repforge.core.domain.model

/**
 * Types of exercise groupings within a routine.
 * Supports modern programming patterns from curated creators (RP, Athlean-X, Jeff Nippard).
 */
enum class ExerciseGroupType(val displayName: String) {
    /** Standard single exercise performed independently. */
    SINGLE("Single"),

    /** Two exercises performed back-to-back with no rest between them. */
    SUPERSET("Superset"),

    /** Three exercises performed consecutively. */
    TRISET("Tri-Set"),

    /** 4+ exercises performed in sequence with minimal rest. */
    CIRCUIT("Circuit"),

    /** As Many Reps As Possible within a time cap. */
    AMRAP("AMRAP"),

    /** Every Minute On the Minute — perform a set at the start of each minute. */
    EMOM("EMOM");
}

/**
 * A group of exercises within a routine that are performed together.
 * For SINGLE type, contains exactly one exercise.
 * For SUPERSET/TRISET/CIRCUIT, contains 2+ exercises done back-to-back.
 */
data class ExerciseGroup(
    val id: String,
    val groupType: ExerciseGroupType,
    val orderInRoutine: Int,
    val restAfterGroupSeconds: Int,
    val timeCapSeconds: Int? = null // For AMRAP/EMOM
)

/**
 * A routine is a reusable workout template containing ordered exercise groups.
 */
data class Routine(
    val id: String,
    val name: String,
    val description: String = "",
    val estimatedDurationMinutes: Int? = null,
    val creatorName: String? = null,
    val sourceVideoId: String? = null,
    val exerciseGroups: List<ExerciseGroup> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

/**
 * An exercise within a routine, belonging to an exercise group.
 * Contains prescribed volume (sets, rep range, rest) from the template.
 */
data class RoutineExercise(
    val id: String,
    val exerciseGroupId: String,
    val exerciseId: String,
    val exercise: Exercise? = null, // Populated via join
    val orderInGroup: Int,
    val prescribedSets: Int,
    val prescribedRepsMin: Int,
    val prescribedRepsMax: Int,
    val restSeconds: Int,
    val executionNotes: String = ""
)
