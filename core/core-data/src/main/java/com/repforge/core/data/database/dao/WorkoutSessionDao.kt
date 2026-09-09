package com.repforge.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.repforge.core.data.database.entity.PersonalRecordEntity
import com.repforge.core.data.database.entity.WorkoutSessionEntity
import com.repforge.core.data.database.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Query("SELECT * FROM workout_sessions WHERE state != 'COMPLETED' AND state != 'IDLE' LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE state = 'COMPLETED' ORDER BY completed_at DESC")
    fun getAllCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun getSessionById(id: String): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sets WHERE session_id = :sessionId ORDER BY set_number ASC")
    fun getSetsForSession(sessionId: String): Flow<List<WorkoutSetEntity>>

    @Query("""
        SELECT * FROM workout_sessions 
        WHERE routine_id = :routineId AND state = 'COMPLETED'
        ORDER BY completed_at DESC 
        LIMIT :limit
    """)
    fun getSessionsForRoutine(routineId: String, limit: Int): Flow<List<WorkoutSessionEntity>>

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_sessions s ON ws.session_id = s.id
        WHERE ws.exercise_id = :exerciseId 
        AND s.state = 'COMPLETED'
        AND ws.set_type != 'WARMUP'
        ORDER BY s.completed_at DESC
        LIMIT :limit
    """)
    fun getLastSetsForExercise(exerciseId: String, limit: Int = 10): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity)

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSet(id: String)

    // Personal Records
    @Query("SELECT * FROM personal_records ORDER BY achieved_at DESC")
    fun getAllPersonalRecords(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exercise_id = :exerciseId ORDER BY achieved_at DESC")
    fun getPersonalRecordsForExercise(exerciseId: String): Flow<List<PersonalRecordEntity>>

    @Query("""
        SELECT * FROM personal_records 
        WHERE exercise_id = :exerciseId AND type = :type 
        ORDER BY value DESC LIMIT 1
    """)
    suspend fun getBestPR(exerciseId: String, type: String): PersonalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalRecord(record: PersonalRecordEntity)
}
