package com.repforge.core.data.repository

import com.repforge.core.data.database.dao.UserProfileDao
import com.repforge.core.data.mapper.toDomain
import com.repforge.core.data.mapper.toEntity
import com.repforge.core.domain.model.UserProfile
import com.repforge.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    override fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun upsertProfile(profile: UserProfile) {
        userProfileDao.upsertProfile(profile.toEntity())
    }

    override suspend fun updateWeight(weightKg: Double) {
        userProfileDao.updateWeight(weightKg)
    }

    override suspend fun isOnboardingComplete(): Boolean {
        return userProfileDao.getProfileCount() > 0
    }
}
