package com.repforge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.domain.repository.UserProfileRepository
import com.repforge.navigation.TopLevelDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Lightweight ViewModel that resolves the app's start destination.
 *
 * Emits `null` while the DB check is in progress (splash stays visible),
 * then emits the resolved route string (either "onboarding" or "today").
 */
@HiltViewModel
class AppStartupViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    val startDestination = flow<String?> {
        val isComplete = userProfileRepository.isOnboardingComplete()
        emit(if (isComplete) TopLevelDestination.TODAY.route else "onboarding")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null // null = still loading
    )
}
