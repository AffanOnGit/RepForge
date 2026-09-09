package com.repforge.core.data.di

import com.repforge.core.data.repository.ExerciseRepositoryImpl
import com.repforge.core.data.repository.RoutineRepositoryImpl
import com.repforge.core.data.repository.UserProfileRepositoryImpl
import com.repforge.core.data.repository.WorkoutSessionRepositoryImpl
import com.repforge.core.domain.repository.ExerciseRepository
import com.repforge.core.domain.repository.RoutineRepository
import com.repforge.core.domain.repository.UserProfileRepository
import com.repforge.core.domain.repository.WorkoutSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(impl: RoutineRepositoryImpl): RoutineRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutSessionRepository(impl: WorkoutSessionRepositoryImpl): WorkoutSessionRepository
}
