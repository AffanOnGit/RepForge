package com.repforge.core.domain.repository

import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.Equipment
import com.repforge.core.domain.model.SubMuscle
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for the canonical exercise dictionary.
 * Implementations live in core-data (Android-specific).
 * WearOS will provide its own thin implementation.
 */
interface ExerciseRepository {

    /** Get all exercises as a reactive Flow. */
    fun getAllExercises(): Flow<List<Exercise>>

    /** Get a single exercise by ID. */
    suspend fun getExerciseById(id: String): Exercise?

    /** Search exercises by name (case-insensitive partial match). */
    fun searchExercises(query: String): Flow<List<Exercise>>

    /** Get exercises filtered by primary sub-muscle target. */
    fun getExercisesBySubMuscle(subMuscle: SubMuscle): Flow<List<Exercise>>

    /** Get exercises filtered by equipment type. */
    fun getExercisesByEquipment(equipment: Equipment): Flow<List<Exercise>>

    /** Get exercises matching the same primary sub-muscle (for exercise swap suggestions). */
    fun getSwapSuggestions(exerciseId: String): Flow<List<Exercise>>

    /** Insert or update a custom exercise. */
    suspend fun upsertExercise(exercise: Exercise)

    /** Delete a custom exercise. Only user-created exercises can be deleted. */
    suspend fun deleteExercise(id: String)
}
