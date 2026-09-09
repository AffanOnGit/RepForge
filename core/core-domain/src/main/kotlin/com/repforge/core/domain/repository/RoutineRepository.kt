package com.repforge.core.domain.repository

import com.repforge.core.domain.model.Routine
import com.repforge.core.domain.model.RoutineExercise
import com.repforge.core.domain.model.ExerciseGroup
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for workout routines (templates).
 */
interface RoutineRepository {

    /** Get all routines as a reactive Flow. */
    fun getAllRoutines(): Flow<List<Routine>>

    /** Get a single routine by ID with all exercise groups and exercises. */
    fun getRoutineById(id: String): Flow<Routine?>

    /** Search routines by name. */
    fun searchRoutines(query: String): Flow<List<Routine>>

    /** Create a new routine with its exercise groups and exercises. */
    suspend fun createRoutine(
        routine: Routine,
        groups: List<ExerciseGroup>,
        exercises: List<RoutineExercise>
    )

    /** Update an existing routine. */
    suspend fun updateRoutine(
        routine: Routine,
        groups: List<ExerciseGroup>,
        exercises: List<RoutineExercise>
    )

    /** Delete a routine and all its associated data. */
    suspend fun deleteRoutine(id: String)

    /** Duplicate a routine with a new name (for "Save as Variation"). */
    suspend fun duplicateRoutine(sourceId: String, newName: String): String
}
