package com.repforge.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.domain.model.BiologicalSex
import com.repforge.core.domain.model.TrainingExperience
import com.repforge.core.domain.model.UnitConverter
import com.repforge.core.domain.model.UnitSystem
import com.repforge.core.domain.model.UserProfile
import com.repforge.core.domain.repository.AuthRepository
import com.repforge.core.domain.repository.UserProfileRepository
import com.repforge.core.health.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProfileUiState(
    val userEmail: String = "Guest Athlete",
    val isGuest: Boolean = true,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val weightInput: String = "",
    val heightInput: String = "",
    val ageInput: String = "",
    val bodyFatInput: String = "",
    val biologicalSex: BiologicalSex = BiologicalSex.MALE,
    val trainingExperience: TrainingExperience = TrainingExperience.INTERMEDIATE,
    val defaultBarWeightKg: Double = 20.0,
    val isHealthConnectAvailable: Boolean = false,
    val isHealthConnectConnected: Boolean = false,
    val isSyncingHealthConnect: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccessMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository,
    private val healthConnectManager: HealthConnectManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("repforge_user_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentProfileId: String = UUID.randomUUID().toString()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Check Health Connect availability & permissions
            val hcAvailable = healthConnectManager.isHealthConnectAvailable()
            val hcConnected = if (hcAvailable) healthConnectManager.hasAllPermissions() else false

            // Stored barbell preference
            val barWeight = prefs.getFloat("default_bar_weight_kg", 20.0f).toDouble()

            // Observe Auth & UserProfile
            val profile = userProfileRepository.getUserProfile().firstOrNull()
            val user = authRepository.currentUser.firstOrNull()
            val isGuest = authRepository.isGuestMode.firstOrNull() ?: true

            if (profile != null) {
                currentProfileId = profile.id
                val unitSystem = profile.unitSystem

                val displayWeight = profile.weightKg?.let { kg ->
                    when (unitSystem) {
                        is UnitSystem.Metric -> "%.1f".format(kg)
                        is UnitSystem.Imperial -> "%.1f".format(UnitConverter.kgToLb(kg))
                    }
                }.orEmpty()

                val displayHeight = profile.heightCm?.let { cm ->
                    when (unitSystem) {
                        is UnitSystem.Metric -> "%.0f".format(cm)
                        is UnitSystem.Imperial -> {
                            val (feet, inches) = UnitConverter.cmToFeetInches(cm)
                            "$feet'$inches\""
                        }
                    }
                }.orEmpty()

                _uiState.update { current ->
                    current.copy(
                        userEmail = user?.email?.ifBlank { "Guest Athlete" } ?: profile.email.ifBlank { "Guest Athlete" },
                        isGuest = isGuest,
                        unitSystem = unitSystem,
                        weightInput = displayWeight,
                        heightInput = displayHeight,
                        ageInput = profile.age?.toString().orEmpty(),
                        bodyFatInput = profile.bodyFatPercentage?.let { "%.1f".format(it) }.orEmpty(),
                        biologicalSex = profile.biologicalSex ?: BiologicalSex.MALE,
                        trainingExperience = profile.trainingExperience,
                        defaultBarWeightKg = barWeight,
                        isHealthConnectAvailable = hcAvailable,
                        isHealthConnectConnected = hcConnected
                    )
                }
            } else {
                _uiState.update { current ->
                    current.copy(
                        userEmail = user?.email?.ifBlank { "Guest Athlete" } ?: "Guest Athlete",
                        isGuest = isGuest,
                        defaultBarWeightKg = barWeight,
                        isHealthConnectAvailable = hcAvailable,
                        isHealthConnectConnected = hcConnected
                    )
                }
            }
        }
    }

    fun onWeightChanged(input: String) {
        _uiState.update { it.copy(weightInput = input, saveSuccessMessage = null) }
    }

    fun onHeightChanged(input: String) {
        _uiState.update { it.copy(heightInput = input, saveSuccessMessage = null) }
    }

    fun onAgeChanged(input: String) {
        _uiState.update { it.copy(ageInput = input.filter { char -> char.isDigit() }, saveSuccessMessage = null) }
    }

    fun onBodyFatChanged(input: String) {
        _uiState.update { it.copy(bodyFatInput = input, saveSuccessMessage = null) }
    }

    fun onSexSelected(sex: BiologicalSex) {
        _uiState.update { it.copy(biologicalSex = sex, saveSuccessMessage = null) }
    }

    fun onExperienceSelected(exp: TrainingExperience) {
        _uiState.update { it.copy(trainingExperience = exp, saveSuccessMessage = null) }
    }

    fun onDefaultBarWeightSelected(weightKg: Double) {
        prefs.edit().putFloat("default_bar_weight_kg", weightKg.toFloat()).apply()
        _uiState.update { it.copy(defaultBarWeightKg = weightKg, saveSuccessMessage = null) }
    }

    fun toggleUnitSystem(newSystem: UnitSystem) {
        val current = _uiState.value
        if (current.unitSystem == newSystem) return

        var newWeight = current.weightInput
        var newHeight = current.heightInput

        // Perform seamless conversion of existing field inputs
        if (newSystem is UnitSystem.Imperial) {
            // From Metric (kg, cm) to Imperial (lb, ft'in")
            val kgVal = current.weightInput.toDoubleOrNull()
            if (kgVal != null) {
                newWeight = "%.1f".format(UnitConverter.kgToLb(kgVal))
            }
            val cmVal = current.heightInput.toDoubleOrNull()
            if (cmVal != null) {
                val (f, i) = UnitConverter.cmToFeetInches(cmVal)
                newHeight = "$f'$i\""
            }
        } else {
            // From Imperial (lb, ft'in") to Metric (kg, cm)
            val lbVal = current.weightInput.toDoubleOrNull()
            if (lbVal != null) {
                newWeight = "%.1f".format(UnitConverter.lbToKg(lbVal))
            }
            if (current.heightInput.contains("'")) {
                val parts = current.heightInput.replace("\"", "").split("'")
                val feet = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val inches = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val cm = UnitConverter.feetInchesToCm(feet, inches)
                newHeight = "%.0f".format(cm)
            }
        }

        _uiState.update {
            it.copy(
                unitSystem = newSystem,
                weightInput = newWeight,
                heightInput = newHeight,
                saveSuccessMessage = null
            )
        }
    }

    fun syncFromHealthConnect() {
        if (!_uiState.value.isHealthConnectAvailable) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingHealthConnect = true) }
            val latestWeight = healthConnectManager.readLatestWeightKg()
            val hasPerms = healthConnectManager.hasAllPermissions()

            if (latestWeight != null) {
                val display = when (_uiState.value.unitSystem) {
                    is UnitSystem.Metric -> "%.1f".format(latestWeight)
                    is UnitSystem.Imperial -> "%.1f".format(UnitConverter.kgToLb(latestWeight))
                }
                _uiState.update {
                    it.copy(
                        weightInput = display,
                        isHealthConnectConnected = hasPerms,
                        isSyncingHealthConnect = false,
                        saveSuccessMessage = "Synced latest weight from Health Connect!"
                    )
                }
                userProfileRepository.updateWeight(latestWeight)
            } else {
                _uiState.update {
                    it.copy(
                        isHealthConnectConnected = hasPerms,
                        isSyncingHealthConnect = false,
                        saveSuccessMessage = if (hasPerms) "Health Connect up to date." else "Health Connect permissions required."
                    )
                }
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            val weightKg: Double? = state.weightInput.toDoubleOrNull()?.let {
                when (state.unitSystem) {
                    is UnitSystem.Metric -> it
                    is UnitSystem.Imperial -> UnitConverter.lbToKg(it)
                }
            }

            val heightCm: Double? = if (state.heightInput.contains("'")) {
                val parts = state.heightInput.replace("\"", "").split("'")
                val f = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val i = parts.getOrNull(1)?.toIntOrNull() ?: 0
                UnitConverter.feetInchesToCm(f, i)
            } else {
                state.heightInput.toDoubleOrNull()
            }

            val age: Int? = state.ageInput.toIntOrNull()
            val bodyFat: Double? = state.bodyFatInput.toDoubleOrNull()

            val updatedProfile = UserProfile(
                id = currentProfileId,
                email = state.userEmail,
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                biologicalSex = state.biologicalSex,
                bodyFatPercentage = bodyFat,
                trainingExperience = state.trainingExperience,
                unitSystem = state.unitSystem
            )

            userProfileRepository.upsertProfile(updatedProfile)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    saveSuccessMessage = "Profile & Biometrics saved successfully!"
                )
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            authRepository.setGuestMode(true)
            onSignedOut()
        }
    }
}
