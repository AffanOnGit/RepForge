package com.repforge.core.data.repository

import com.repforge.core.data.database.dao.RoutineDao
import com.repforge.core.data.mapper.toDomain
import com.repforge.core.data.mapper.toEntity
import com.repforge.core.domain.model.ExerciseGroup
import com.repforge.core.domain.model.Routine
import com.repforge.core.domain.model.RoutineExercise
import com.repforge.core.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun getAllRoutines(): Flow<List<Routine>> {
        return routineDao.getAllRoutines().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getRoutineById(id: String): Flow<Routine?> {
        return routineDao.getRoutineById(id).map { entity ->
            entity?.toDomain()
        }
    }

    override fun searchRoutines(query: String): Flow<List<Routine>> {
        return routineDao.searchRoutines(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun createRoutine(
        routine: Routine,
        groups: List<ExerciseGroup>,
        exercises: List<RoutineExercise>
    ) {
        val routineEntity = routine.toEntity()
        val groupEntities = groups.map { it.toEntity(routine.id) }
        val exerciseEntities = exercises.map { it.toEntity() }
        routineDao.createRoutineWithExercises(routineEntity, groupEntities, exerciseEntities)
    }

    override suspend fun updateRoutine(
        routine: Routine,
        groups: List<ExerciseGroup>,
        exercises: List<RoutineExercise>
    ) {
        val routineEntity = routine.copy(updatedAtMillis = System.currentTimeMillis()).toEntity()
        val groupEntities = groups.map { it.toEntity(routine.id) }
        val exerciseEntities = exercises.map { it.toEntity() }
        routineDao.updateRoutineWithExercises(routineEntity, groupEntities, exerciseEntities)
    }

    override suspend fun deleteRoutine(id: String) {
        routineDao.deleteRoutine(id)
    }

    override suspend fun duplicateRoutine(sourceId: String, newName: String): String {
        val existingRoutine = routineDao.getRoutineById(sourceId).firstOrNull() ?: return ""
        val existingGroups = routineDao.getGroupsForRoutine(sourceId).firstOrNull().orEmpty()
        val existingExercises = routineDao.getAllExercisesForRoutine(sourceId).firstOrNull().orEmpty()

        val newRoutineId = UUID.randomUUID().toString()
        val groupIdMap = mutableMapOf<String, String>()

        val newGroups = existingGroups.map { oldGroup ->
            val newGroupId = UUID.randomUUID().toString()
            groupIdMap[oldGroup.id] = newGroupId
            oldGroup.copy(
                id = newGroupId,
                routineId = newRoutineId
            )
        }

        val newExercises = existingExercises.map { oldExercise ->
            oldExercise.copy(
                id = UUID.randomUUID().toString(),
                exerciseGroupId = groupIdMap[oldExercise.exerciseGroupId] ?: oldExercise.exerciseGroupId
            )
        }

        val newRoutine = existingRoutine.copy(
            id = newRoutineId,
            name = newName,
            createdAtMillis = System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis()
        )

        routineDao.createRoutineWithExercises(newRoutine, newGroups, newExercises)
        return newRoutineId
    }
}
