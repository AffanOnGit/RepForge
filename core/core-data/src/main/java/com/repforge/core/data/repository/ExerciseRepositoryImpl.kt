package com.repforge.core.data.repository

import com.repforge.core.data.database.dao.ExerciseDao
import com.repforge.core.data.database.seed.ExerciseSeedData
import com.repforge.core.data.mapper.toDomain
import com.repforge.core.data.mapper.toEntity
import com.repforge.core.domain.model.Equipment
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.SubMuscle
import com.repforge.core.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    private suspend fun ensureSeeded() {
        if (exerciseDao.getExerciseCount() == 0) {
            exerciseDao.upsertExercises(ExerciseSeedData.exercises)
        }
    }

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises()
            .onStart { ensureSeeded() }
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getExerciseById(id: String): Exercise? {
        ensureSeeded()
        return exerciseDao.getExerciseById(id)?.toDomain()
    }

    override fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.searchExercises(query)
            .onStart { ensureSeeded() }
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getExercisesBySubMuscle(subMuscle: SubMuscle): Flow<List<Exercise>> {
        return exerciseDao.getExercisesBySubMuscle(subMuscle.name)
            .onStart { ensureSeeded() }
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getExercisesByEquipment(equipment: Equipment): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByEquipment(equipment.name)
            .onStart { ensureSeeded() }
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getSwapSuggestions(exerciseId: String): Flow<List<Exercise>> {
        return exerciseDao.getSwapSuggestions(exerciseId)
            .onStart { ensureSeeded() }
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun upsertExercise(exercise: Exercise) {
        exerciseDao.upsertExercise(exercise.toEntity())
    }

    override suspend fun deleteExercise(id: String) {
        exerciseDao.deleteCustomExercise(id)
    }
}
