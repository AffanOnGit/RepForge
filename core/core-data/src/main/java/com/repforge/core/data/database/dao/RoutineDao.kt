package com.repforge.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.repforge.core.data.database.entity.ExerciseGroupEntity
import com.repforge.core.data.database.entity.RoutineEntity
import com.repforge.core.data.database.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query("SELECT * FROM routines ORDER BY updated_at DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    fun getRoutineById(id: String): Flow<RoutineEntity?>

    @Query("SELECT * FROM routines WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchRoutines(query: String): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM exercise_groups WHERE routine_id = :routineId ORDER BY order_in_routine ASC")
    fun getGroupsForRoutine(routineId: String): Flow<List<ExerciseGroupEntity>>

    @Query("SELECT * FROM routine_exercises WHERE exercise_group_id = :groupId ORDER BY order_in_group ASC")
    fun getExercisesForGroup(groupId: String): Flow<List<RoutineExerciseEntity>>

    @Query("""
        SELECT re.* FROM routine_exercises re
        INNER JOIN exercise_groups eg ON re.exercise_group_id = eg.id
        WHERE eg.routine_id = :routineId
        ORDER BY eg.order_in_routine ASC, re.order_in_group ASC
    """)
    fun getAllExercisesForRoutine(routineId: String): Flow<List<RoutineExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<ExerciseGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(exercises: List<RoutineExerciseEntity>)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: String)

    @Query("DELETE FROM exercise_groups WHERE routine_id = :routineId")
    suspend fun deleteGroupsForRoutine(routineId: String)

    @Query("DELETE FROM routine_exercises WHERE exercise_group_id IN (SELECT id FROM exercise_groups WHERE routine_id = :routineId)")
    suspend fun deleteExercisesForRoutine(routineId: String)

    /**
     * Create a full routine with groups and exercises in a single transaction.
     */
    @Transaction
    suspend fun createRoutineWithExercises(
        routine: RoutineEntity,
        groups: List<ExerciseGroupEntity>,
        exercises: List<RoutineExerciseEntity>
    ) {
        insertRoutine(routine)
        insertGroups(groups)
        insertRoutineExercises(exercises)
    }

    /**
     * Replace a routine's exercises (for updates).
     */
    @Transaction
    suspend fun updateRoutineWithExercises(
        routine: RoutineEntity,
        groups: List<ExerciseGroupEntity>,
        exercises: List<RoutineExerciseEntity>
    ) {
        deleteExercisesForRoutine(routine.id)
        deleteGroupsForRoutine(routine.id)
        insertRoutine(routine)
        insertGroups(groups)
        insertRoutineExercises(exercises)
    }
}
