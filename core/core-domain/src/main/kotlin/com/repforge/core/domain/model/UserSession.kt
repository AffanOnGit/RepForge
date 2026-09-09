package com.repforge.core.domain.model

/**
 * Active authenticated user session.
 */
data class UserSession(
    val uid: String,
    val email: String,
    val isAnonymous: Boolean = false
)
