package com.repforge.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.repforge.core.data.database.dao.ExerciseDao
import com.repforge.core.data.database.dao.RoutineDao
import com.repforge.core.data.database.dao.UserProfileDao
import com.repforge.core.data.database.dao.WorkoutSessionDao
import com.repforge.core.data.database.entity.ExerciseEntity
import com.repforge.core.data.database.entity.ExerciseGroupEntity
import com.repforge.core.data.database.entity.PersonalRecordEntity
import com.repforge.core.data.database.entity.RoutineEntity
import com.repforge.core.data.database.entity.RoutineExerciseEntity
import com.repforge.core.data.database.entity.UserProfileEntity
import com.repforge.core.data.database.entity.WorkoutSessionEntity
import com.repforge.core.data.database.entity.WorkoutSetEntity

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        ExerciseGroupEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class,
        PersonalRecordEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class RepForgeDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        const val DATABASE_NAME = "repforge_database"
    }
}
