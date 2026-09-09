package com.repforge.core.domain.repository

import com.repforge.core.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** Current user session stream. Emits null if unauthenticated. */
    val currentUser: Flow<UserSession?>

    /** Stream of offline guest mode status. */
    val isGuestMode: Flow<Boolean>

    /** Sign in with email and password. */
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    /** Create a new account with email and password. */
    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    /** Send password reset email. */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    /** Sign out the current user. */
    suspend fun signOut()

    /** Continue as guest (full offline local functionality). */
    fun setGuestMode(enabled: Boolean)
}
