package com.repforge.core.domain.model

/**
 * Set types for workout logging.
 * Each type has different implications for volume calculations and PR tracking.
 */
enum class SetType(val displayLabel: String) {
    /** Warm-up set. Excluded from working volume and PR calculations. */
    WARMUP("W"),

    /** Standard working set. Counted for progressive overload. */
    WORKING("Working"),

    /** Drop set — immediate lower-weight continuation. Elevated fatigue weighting. */
    DROP_SET("D"),

    /** Set taken to absolute concentric failure (RIR 0). */
    FAILURE("F");
}

/**
 * The state of a workout session, modeled as a finite state machine.
 * This FSM can be shared across phone and WearOS companion.
 */
enum class SessionState {
    IDLE,
    ACTIVE,
    PAUSED,
    FINISHING,
    COMPLETED;
}

/**
 * A single workout session — an instance of performing a routine (or freestyle).
 */
data class WorkoutSession(
    val id: String,
    val routineId: String? = null,
    val routineName: String = "",
    val state: SessionState = SessionState.IDLE,
    val startedAtMillis: Long? = null,
    val completedAtMillis: Long? = null,
    val totalTonnageKg: Double = 0.0,
    val estimatedCaloriesLow: Double = 0.0,
    val estimatedCaloriesHigh: Double = 0.0,
    val notes: String = ""
)

/**
 * A single set logged during a workout session.
 */
data class WorkoutSet(
    val id: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseGroupId: String? = null,
    val setNumber: Int,
    val setType: SetType,
    val weightKg: Double,
    val repsCompleted: Int,
    val targetReps: Int? = null,
    val isCompleted: Boolean = false,
    val rpe: Double? = null, // Rate of Perceived Exertion 6.0–10.0
    val completedAtMillis: Long? = null
)

/**
 * Progressive overload trend indicator for a specific exercise.
 * Based on rolling 3-session performance analysis.
 */
enum class ProgressionTrend {
    /** Increasing weight, reps, or total volume load. */
    PROGRESSING,

    /** Static volume load across 3 consecutive sessions. */
    PLATEAUING,

    /** Reduced volume load or missed rep targets. */
    DECLINING;
}

/**
 * A personal record (PR) for a specific exercise.
 */
data class PersonalRecord(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val type: PRType,
    val value: Double,
    val achievedAtMillis: Long,
    val sessionId: String
)

/**
 * Types of personal records tracked.
 */
enum class PRType {
    /** Heaviest weight lifted for any rep count. */
    WEIGHT,

    /** Most reps completed at a specific weight. */
    REPS,

    /** Highest total volume (weight × reps × sets) in a single session for this exercise. */
    VOLUME;
}
