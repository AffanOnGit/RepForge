package com.repforge.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.core.domain.model.BiologicalSex
import com.repforge.core.domain.model.TrainingExperience
import com.repforge.core.domain.model.UnitSystem
import com.repforge.core.ui.components.ForgeButton
import com.repforge.core.ui.components.ForgeButtonVariant
import com.repforge.core.ui.components.ForgeTextField
import com.repforge.core.ui.theme.CarbonSlate
import com.repforge.core.ui.theme.CarbonSlateCard
import com.repforge.core.ui.theme.CarbonSlateLight
import com.repforge.core.ui.theme.CarbonSlateSurface
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.KineticLime
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary
import com.repforge.core.ui.theme.TextTertiary

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CarbonSlate,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSlateLight)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                when (state.currentStep) {
                    0 -> {
                        ForgeButton(
                            text = "Get Started",
                            onClick = { viewModel.nextStep() },
                            variant = ForgeButtonVariant.PRIMARY,
                            isFullWidth = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    1 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ForgeButton(
                                text = "Back",
                                onClick = { viewModel.prevStep() },
                                variant = ForgeButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            ForgeButton(
                                text = "Continue",
                                onClick = { viewModel.nextStep() },
                                variant = ForgeButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }
                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ForgeButton(
                                text = if (state.isHealthConnectLinked) "Finish Setup" else "Connect Health Connect",
                                onClick = {
                                    if (state.isHealthConnectLinked) {
                                        viewModel.completeOnboarding(onOnboardingFinished)
                                    } else {
                                        viewModel.markHealthConnectLinked()
                                        viewModel.completeOnboarding(onOnboardingFinished)
                                    }
                                },
                                variant = ForgeButtonVariant.KINETIC,
                                isFullWidth = true,
                                isLoading = state.isSaving
                            )
                            ForgeButton(
                                text = "Skip for Now",
                                onClick = { viewModel.completeOnboarding(onOnboardingFinished) },
                                variant = ForgeButtonVariant.GHOST,
                                isFullWidth = true,
                                enabled = !state.isSaving
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (i <= state.currentStep) ForgeAmber else CarbonSlateCard
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboardingStepAnimation"
            ) { step ->
                when (step) {
                    0 -> WelcomeStepContent()
                    1 -> BiometricsStepContent(state, viewModel)
                    2 -> HealthConnectStepContent(state, viewModel)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WelcomeStepContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))

        // Brand Icon / Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(ForgeAmber.copy(alpha = 0.15f))
                .border(2.dp, ForgeAmber, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = ForgeAmber,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "REPFORGE",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Built for the iron, not the desk.",
            color = ForgeAmber,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Zero friction in-gym tracking. 19 sub-muscle precision. 100% offline-first reliability in any basement gym.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Key Value Pillars
        PillarItem(
            icon = Icons.Default.Speed,
            title = "Sweaty-Hands UX",
            description = "Huge 48dp+ tap targets, haptic feedback pulses, and instant ghost text."
        )
        Spacer(modifier = Modifier.height(12.dp))
        PillarItem(
            icon = Icons.Default.FitnessCenter,
            title = "19 Sub-Muscle Taxonomy",
            description = "Gym-friendly targeting: Upper/Mid/Lower Chest, Side/Rear Delts, and more."
        )
        Spacer(modifier = Modifier.height(12.dp))
        PillarItem(
            icon = Icons.Default.OfflinePin,
            title = "100% Offline-First",
            description = "No spinner wheels mid-workout. Everything runs locally on device."
        )
    }
}

@Composable
private fun PillarItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateCard, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CarbonSlateSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ForgeAmber,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun BiometricsStepContent(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column {
        Text(
            text = "Your Profile & Units",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Calibrates caloric burn formulas and sets your preferred measurement system.",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Unit System Toggle
        Text(
            text = "MEASUREMENT SYSTEM",
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonSlateSurface)
                .border(1.dp, CarbonSlateCard, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val isMetric = state.unitSystem is UnitSystem.Metric
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isMetric) ForgeAmber else Color.Transparent)
                    .clickable(role = Role.RadioButton) { viewModel.setUnitSystem(UnitSystem.Metric) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Metric (kg / cm)",
                    color = if (isMetric) Color.Black else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isMetric) ForgeAmber else Color.Transparent)
                    .clickable(role = Role.RadioButton) { viewModel.setUnitSystem(UnitSystem.Imperial) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Imperial (lb / ft-in)",
                    color = if (!isMetric) Color.Black else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Weight Input
        ForgeTextField(
            value = state.weightInput,
            onValueChange = { viewModel.updateWeight(it) },
            label = if (state.unitSystem is UnitSystem.Metric) "BODY WEIGHT (KG)" else "BODY WEIGHT (LB)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Height Input
        if (state.unitSystem is UnitSystem.Metric) {
            ForgeTextField(
                value = state.heightCmInput,
                onValueChange = { viewModel.updateHeightCm(it) },
                label = "HEIGHT (CM)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ForgeTextField(
                    value = state.heightFeetInput,
                    onValueChange = { viewModel.updateHeightFeet(it) },
                    label = "FEET",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ForgeTextField(
                    value = state.heightInchesInput,
                    onValueChange = { viewModel.updateHeightInches(it) },
                    label = "INCHES",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Age Input
        ForgeTextField(
            value = state.ageInput,
            onValueChange = { viewModel.updateAge(it) },
            label = "AGE",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Biological Sex
        Text(
            text = "BIOLOGICAL SEX",
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isMale = state.biologicalSex == BiologicalSex.MALE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isMale) ForgeAmber.copy(alpha = 0.2f) else CarbonSlateSurface)
                    .border(1.5.dp, if (isMale) ForgeAmber else CarbonSlateCard, RoundedCornerShape(12.dp))
                    .clickable(role = Role.RadioButton) { viewModel.setSex(BiologicalSex.MALE) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Male",
                    color = if (isMale) ForgeAmber else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!isMale) ForgeAmber.copy(alpha = 0.2f) else CarbonSlateSurface)
                    .border(1.5.dp, if (!isMale) ForgeAmber else CarbonSlateCard, RoundedCornerShape(12.dp))
                    .clickable(role = Role.RadioButton) { viewModel.setSex(BiologicalSex.FEMALE) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Female",
                    color = if (!isMale) ForgeAmber else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Experience Level
        Text(
            text = "TRAINING EXPERIENCE",
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrainingExperience.entries.forEach { exp ->
                val isSelected = state.trainingExperience == exp
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ForgeAmber.copy(alpha = 0.2f) else CarbonSlateSurface)
                        .border(1.5.dp, if (isSelected) ForgeAmber else CarbonSlateCard, RoundedCornerShape(12.dp))
                        .clickable(role = Role.RadioButton) { viewModel.setExperience(exp) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exp.displayName,
                        color = if (isSelected) ForgeAmber else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthConnectStepContent(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(KineticLime.copy(alpha = 0.15f))
                .border(2.dp, KineticLime, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = KineticLime,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Health Connect Sync",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "RepForge communicates bidirectionally with Android Health Connect to keep your health hub in sync.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        HealthFeatureRow(
            title = "Auto-sync Body Weight",
            subtitle = "Reads your smart scale weigh-ins automatically to calibrate tonnage scaling."
        )
        Spacer(modifier = Modifier.height(12.dp))
        HealthFeatureRow(
            title = "Workout Session Export",
            subtitle = "Writes strength training duration, total tonnage, and estimated calories to your health hub."
        )
        Spacer(modifier = Modifier.height(12.dp))
        HealthFeatureRow(
            title = "Heart Rate Calibration",
            subtitle = "Uses live heart rate telemetry to tighten caloric burn confidence intervals."
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isHealthConnectLinked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KineticLime.copy(alpha = 0.15f))
                    .border(1.dp, KineticLime, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = KineticLime,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Connect Ready",
                    color = KineticLime,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HealthFeatureRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateCard, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CarbonSlateSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = KineticLime,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
