package com.repforge.core.domain.repository

import com.repforge.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user profile and biometrics.
 */
interface UserProfileRepository {

    /** Get the current user's profile as a reactive Flow. */
    fun getUserProfile(): Flow<UserProfile?>

    /** Create or update the user profile. */
    suspend fun upsertProfile(profile: UserProfile)

    /** Update just the weight (e.g., from Health Connect auto-sync). */
    suspend fun updateWeight(weightKg: Double)

    /** Check if a profile has been set up (onboarding completed). */
    suspend fun isOnboardingComplete(): Boolean
}
