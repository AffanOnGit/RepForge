package com.repforge.core.network.auth

import com.google.firebase.auth.FirebaseAuth
import com.repforge.core.domain.model.UserSession
import com.repforge.core.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Timber.w(e, "Firebase not initialized, defaulting to offline guest mode")
            null
        }
    }

    private val _isGuestMode = MutableStateFlow(false)
    override val isGuestMode: Flow<Boolean> = _isGuestMode.asStateFlow()

    override val currentUser: Flow<UserSession?> = callbackFlow {
        val auth = firebaseAuth
        if (auth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            val user = fbAuth.currentUser
            val session = user?.let {
                UserSession(
                    uid = it.uid,
                    email = it.email.orEmpty(),
                    isAnonymous = it.isAnonymous
                )
            }
            trySend(session)
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth unavailable"))
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            _isGuestMode.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sign in failed")
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> {
        return try {
            val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth unavailable"))
            auth.createUserWithEmailAndPassword(email.trim(), password).await()
            _isGuestMode.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sign up failed")
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth unavailable"))
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Password reset failed")
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Timber.e(e, "Sign out error")
        }
        _isGuestMode.value = false
    }

    override fun setGuestMode(enabled: Boolean) {
        _isGuestMode.value = enabled
    }
}
