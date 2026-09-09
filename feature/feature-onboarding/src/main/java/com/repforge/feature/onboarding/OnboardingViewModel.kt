package com.repforge.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.domain.model.BiologicalSex
import com.repforge.core.domain.model.TrainingExperience
import com.repforge.core.domain.model.UnitConverter
import com.repforge.core.domain.model.UnitSystem
import com.repforge.core.domain.model.UserProfile
import com.repforge.core.domain.repository.UserProfileRepository
import com.repforge.core.health.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val weightInput: String = "75.0",
    val heightCmInput: String = "175",
    val heightFeetInput: String = "5",
    val heightInchesInput: String = "9",
    val ageInput: String = "25",
    val biologicalSex: BiologicalSex = BiologicalSex.MALE,
    val trainingExperience: TrainingExperience = TrainingExperience.INTERMEDIATE,
    val isHealthConnectAvailable: Boolean = false,
    val isHealthConnectLinked: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            isHealthConnectAvailable = healthConnectManager.isHealthConnectAvailable()
        )
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setUnitSystem(system: UnitSystem) {
        _uiState.update { current ->
            if (current.unitSystem == system) return@update current

            // Convert inputs between metric and imperial on toggle
            val newWeight = if (system is UnitSystem.Imperial) {
                val kg = current.weightInput.toDoubleOrNull() ?: 75.0
                "%.1f".format(UnitConverter.kgToLb(kg))
            } else {
                val lb = current.weightInput.toDoubleOrNull() ?: 165.0
                "%.1f".format(UnitConverter.lbToKg(lb))
            }

            current.copy(
                unitSystem = system,
                weightInput = newWeight
            )
        }
    }

    fun updateWeight(value: String) {
        _uiState.update { it.copy(weightInput = value) }
    }

    fun updateHeightCm(value: String) {
        _uiState.update { it.copy(heightCmInput = value) }
    }

    fun updateHeightFeet(value: String) {
        _uiState.update { it.copy(heightFeetInput = value) }
    }

    fun updateHeightInches(value: String) {
        _uiState.update { it.copy(heightInchesInput = value) }
    }

    fun updateAge(value: String) {
        _uiState.update { it.copy(ageInput = value) }
    }

    fun setSex(sex: BiologicalSex) {
        _uiState.update { it.copy(biologicalSex = sex) }
    }

    fun setExperience(experience: TrainingExperience) {
        _uiState.update { it.copy(trainingExperience = experience) }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(2)) }
    }

    fun prevStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun markHealthConnectLinked() {
        _uiState.update { it.copy(isHealthConnectLinked = true) }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val weightKg = if (state.unitSystem is UnitSystem.Metric) {
                state.weightInput.toDoubleOrNull() ?: 75.0
            } else {
                val lb = state.weightInput.toDoubleOrNull() ?: 165.0
                UnitConverter.lbToKg(lb)
            }

            val heightCm = if (state.unitSystem is UnitSystem.Metric) {
                state.heightCmInput.toDoubleOrNull() ?: 175.0
            } else {
                val feet = state.heightFeetInput.toIntOrNull() ?: 5
                val inches = state.heightInchesInput.toIntOrNull() ?: 9
                UnitConverter.feetInchesToCm(feet, inches)
            }

            val age = state.ageInput.toIntOrNull() ?: 25

            val profile = UserProfile(
                id = UUID.randomUUID().toString(),
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                biologicalSex = state.biologicalSex,
                trainingExperience = state.trainingExperience,
                unitSystem = state.unitSystem
            )

            userProfileRepository.upsertProfile(profile)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
