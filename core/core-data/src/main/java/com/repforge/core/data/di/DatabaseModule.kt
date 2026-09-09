package com.repforge.core.data.di

import android.content.Context
import androidx.room.Room
import com.repforge.core.data.database.RepForgeDatabase
import com.repforge.core.data.database.dao.ExerciseDao
import com.repforge.core.data.database.dao.RoutineDao
import com.repforge.core.data.database.dao.UserProfileDao
import com.repforge.core.data.database.dao.WorkoutSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RepForgeDatabase {
        return Room.databaseBuilder(
            context,
            RepForgeDatabase::class.java,
            RepForgeDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideExerciseDao(database: RepForgeDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideRoutineDao(database: RepForgeDatabase): RoutineDao = database.routineDao()

    @Provides
    fun provideWorkoutSessionDao(database: RepForgeDatabase): WorkoutSessionDao = database.workoutSessionDao()

    @Provides
    fun provideUserProfileDao(database: RepForgeDatabase): UserProfileDao = database.userProfileDao()
}
