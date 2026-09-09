package com.repforge.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.core.domain.model.BiologicalSex
import com.repforge.core.domain.model.PlateMath
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
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CarbonSlate
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Identity
            item {
                ProfileHeader(
                    email = state.userEmail,
                    isGuest = state.isGuest,
                    onSignOut = { viewModel.signOut(onNavigateToLogin) }
                )
            }

            // Save Confirmation Notification
            if (state.saveSuccessMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KineticLime.copy(alpha = 0.15f))
                            .border(1.dp, KineticLime, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "✓ ${state.saveSuccessMessage}",
                            color = KineticLime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Section: Unit Preferences
            item {
                UnitPreferenceCard(
                    selectedSystem = state.unitSystem,
                    onSelect = { viewModel.toggleUnitSystem(it) }
                )
            }

            // Section: Biometrics
            item {
                BiometricsCard(
                    state = state,
                    onWeightChange = { viewModel.onWeightChanged(it) },
                    onHeightChange = { viewModel.onHeightChanged(it) },
                    onAgeChange = { viewModel.onAgeChanged(it) },
                    onBodyFatChange = { viewModel.onBodyFatChanged(it) },
                    onSexSelect = { viewModel.onSexSelected(it) }
                )
            }

            // Section: Training Experience Level
            item {
                TrainingExperienceCard(
                    selectedExp = state.trainingExperience,
                    onSelect = { viewModel.onExperienceSelected(it) }
                )
            }

            // Section: Default Barbell Calibration
            item {
                BarbellCalibrationCard(
                    selectedBarKg = state.defaultBarWeightKg,
                    unitSystem = state.unitSystem,
                    onSelectBar = { viewModel.onDefaultBarWeightSelected(it) }
                )
            }

            // Section: Health Connect Integration
            item {
                HealthConnectStatusCard(
                    isAvailable = state.isHealthConnectAvailable,
                    isConnected = state.isHealthConnectConnected,
                    isSyncing = state.isSyncingHealthConnect,
                    onSync = { viewModel.syncFromHealthConnect() }
                )
            }

            // Save Changes CTA Button
            item {
                ForgeButton(
                    text = if (state.isSaving) "Saving..." else "Save Profile & Preferences",
                    onClick = { viewModel.saveProfile() },
                    variant = ForgeButtonVariant.Primary,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // RepForge 100% Free Guarantee Card
            item {
                FreeAppManifestoCard()
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    email: String,
    isGuest: Boolean,
    onSignOut: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(ForgeAmber.copy(alpha = 0.2f))
                    .border(2.dp, ForgeAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = email.take(1).uppercase(),
                    color = ForgeAmber,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column {
                Text(
                    text = "ATHLETE PROFILE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ForgeAmber,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = email,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isGuest) "Offline Guest Mode" else "Cloud Sync Enabled",
                    color = if (isGuest) TextTertiary else KineticLime,
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(CarbonSlateLight)
                .border(1.dp, CarbonSlateSurface, RoundedCornerShape(10.dp))
                .clickable(role = Role.Button, onClick = onSignOut)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isGuest) "Sign In" else "Sign Out",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun UnitPreferenceCard(
    selectedSystem: UnitSystem,
    onSelect: (UnitSystem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "MEASUREMENT UNIT SYSTEM",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "All database records are stored in pure metric. Selecting Imperial converts display values seamlessly.",
            color = TextTertiary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Metric
            val isMetric = selectedSystem is UnitSystem.Metric
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isMetric) ForgeAmber.copy(alpha = 0.15f) else CarbonSlateCard)
                    .border(1.dp, if (isMetric) ForgeAmber else CarbonSlateSurface, RoundedCornerShape(12.dp))
                    .clickable(role = Role.RadioButton, onClick = { onSelect(UnitSystem.Metric) })
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "METRIC",
                        color = if (isMetric) ForgeAmber else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "kg • cm", color = TextTertiary, fontSize = 11.sp)
                }
            }

            // Imperial
            val isImperial = selectedSystem is UnitSystem.Imperial
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isImperial) ForgeAmber.copy(alpha = 0.15f) else CarbonSlateCard)
                    .border(1.dp, if (isImperial) ForgeAmber else CarbonSlateSurface, RoundedCornerShape(12.dp))
                    .clickable(role = Role.RadioButton, onClick = { onSelect(UnitSystem.Imperial) })
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "IMPERIAL",
                        color = if (isImperial) ForgeAmber else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "lb • ft-in", color = TextTertiary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun BiometricsCard(
    state: ProfileUiState,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onBodyFatChange: (String) -> Unit,
    onSexSelect: (BiologicalSex) -> Unit
) {
    val weightUnit = if (state.unitSystem is UnitSystem.Metric) "kg" else "lb"
    val heightUnit = if (state.unitSystem is UnitSystem.Metric) "cm" else "ft'in\""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "BIOMETRICS & METABOLISM",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Used exclusively for on-device caloric burn calculations (Mifflin-St Jeor formula).",
            color = TextTertiary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Weight & Height Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ForgeTextField(
                value = state.weightInput,
                onValueChange = onWeightChange,
                label = "Body Weight ($weightUnit)",
                placeholder = if (state.unitSystem is UnitSystem.Metric) "80.0" else "176.4",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )

            ForgeTextField(
                value = state.heightInput,
                onValueChange = onHeightChange,
                label = "Height ($heightUnit)",
                placeholder = if (state.unitSystem is UnitSystem.Metric) "180" else "5'11\"",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Age & Body Fat Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ForgeTextField(
                value = state.ageInput,
                onValueChange = onAgeChange,
                label = "Age (years)",
                placeholder = "28",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            ForgeTextField(
                value = state.bodyFatInput,
                onValueChange = onBodyFatChange,
                label = "Body Fat % (opt)",
                placeholder = "15.0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Biological Sex Selection
        Text(
            text = "Biological Sex (for BMR baseline)",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isMale = state.biologicalSex == BiologicalSex.MALE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isMale) ForgeAmber.copy(alpha = 0.15f) else CarbonSlateCard)
                    .border(1.dp, if (isMale) ForgeAmber else CarbonSlateSurface, RoundedCornerShape(10.dp))
                    .clickable(role = Role.RadioButton, onClick = { onSexSelect(BiologicalSex.MALE) })
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Male",
                    color = if (isMale) ForgeAmber else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            val isFemale = state.biologicalSex == BiologicalSex.FEMALE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isFemale) ForgeAmber.copy(alpha = 0.15f) else CarbonSlateCard)
                    .border(1.dp, if (isFemale) ForgeAmber else CarbonSlateSurface, RoundedCornerShape(10.dp))
                    .clickable(role = Role.RadioButton, onClick = { onSexSelect(BiologicalSex.FEMALE) })
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Female",
                    color = if (isFemale) ForgeAmber else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TrainingExperienceCard(
    selectedExp: TrainingExperience,
    onSelect: (TrainingExperience) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "TRAINING EXPERIENCE",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Calibrates post-exercise oxygen consumption (EPOC) and recovery efficiency.",
            color = TextTertiary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrainingExperience.values().forEach { exp ->
                val isSelected = selectedExp == exp
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) KineticLime.copy(alpha = 0.15f) else CarbonSlateCard)
                        .border(1.dp, if (isSelected) KineticLime else CarbonSlateSurface, RoundedCornerShape(10.dp))
                        .clickable(role = Role.RadioButton, onClick = { onSelect(exp) })
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exp.displayName,
                        color = if (isSelected) KineticLime else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BarbellCalibrationCard(
    selectedBarKg: Double,
    unitSystem: UnitSystem,
    onSelectBar: (Double) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "DEFAULT BARBELL CALIBRATION",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Used as the baseline empty bar weight in Plate Math visualizers across all barbell lifts.",
            color = TextTertiary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        PlateMath.standardBarWeights.forEach { (barName, weightKg) ->
            val isSelected = (selectedBarKg == weightKg)
            val displayWeight = when (unitSystem) {
                is UnitSystem.Metric -> "%.0f kg".format(weightKg)
                is UnitSystem.Imperial -> "%.0f lb".format(weightKg * 2.20462)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) ForgeAmber.copy(alpha = 0.12f) else CarbonSlateCard)
                    .border(1.dp, if (isSelected) ForgeAmber else CarbonSlateSurface, RoundedCornerShape(10.dp))
                    .clickable(role = Role.RadioButton, onClick = { onSelectBar(weightKg) })
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = barName,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = displayWeight,
                    color = if (isSelected) ForgeAmber else TextTertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun HealthConnectStatusCard(
    isAvailable: Boolean,
    isConnected: Boolean,
    isSyncing: Boolean,
    onSync: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HEALTH CONNECT INTEGRATION",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (!isAvailable) "Not Installed on Device" else if (isConnected) "Connected & Syncing" else "Permissions Pending",
                    color = if (isConnected) KineticLime else ForgeAmber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isAvailable) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CarbonSlateSurface)
                        .clickable(role = Role.Button, onClick = onSync)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = KineticLime, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = "Sync Now",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Bidirectionally syncs finished strength workouts and calculated calories to Health Connect. Reads body weight logs to keep Mifflin-St Jeor formulas accurate.",
            color = TextTertiary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun FreeAppManifestoCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "🛡️", fontSize = 18.sp)
            Text(
                text = "100% FREE & OPEN PHILOSOPHY",
                color = ForgeAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "RepForge is built for lifters who respect honest software. No premium tiers, no locked routine slots, no ads, and no monthly subscriptions. All 250+ exercises, Plate Math, and AI ingestion are free forever.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "RepForge v1.0.0 (Release Candidate) • Build 1",
            color = TextTertiary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
