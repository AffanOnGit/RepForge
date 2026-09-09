package com.repforge.feature.routines

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.core.domain.model.MuscleGroup
import com.repforge.core.domain.model.SubMuscle
import com.repforge.core.ui.components.ForgeButton
import com.repforge.core.ui.components.ForgeButtonVariant
import com.repforge.core.ui.components.ForgeTextField
import com.repforge.core.ui.components.StepperCounter
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
fun RapidTemplateWizardScreen(
    onNavigateBack: () -> Unit,
    onRoutineSaved: (String) -> Unit,
    onStartWorkout: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RapidTemplateWizardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CarbonSlate,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RAPID TEMPLATE WIZARD",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSlateLight)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.currentStep > 0) {
                        ForgeButton(
                            text = "Back",
                            onClick = { viewModel.prevStep() },
                            variant = ForgeButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (state.currentStep < 3) {
                        ForgeButton(
                            text = "Next",
                            onClick = { viewModel.nextStep() },
                            variant = ForgeButtonVariant.PRIMARY,
                            modifier = Modifier.weight(1.5f),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    } else {
                        ForgeButton(
                            text = "Save Routine",
                            onClick = { viewModel.saveRoutine(onRoutineSaved) },
                            variant = ForgeButtonVariant.PRIMARY,
                            modifier = Modifier.weight(1f),
                            isLoading = state.isSaving
                        )
                        ForgeButton(
                            text = "Save & Start",
                            onClick = { viewModel.saveRoutine(onStartWorkout) },
                            variant = ForgeButtonVariant.KINETIC,
                            modifier = Modifier.weight(1.2f),
                            isLoading = state.isSaving,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
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
            // Step progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0..3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i <= state.currentStep) ForgeAmber else CarbonSlateCard)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "wizardStepAnimation"
            ) { step ->
                when (step) {
                    0 -> StepTargetMuscles(state, viewModel)
                    1 -> StepExerciseSelection(state, viewModel)
                    2 -> StepVolumeTuning(state, viewModel)
                    3 -> StepProjectionSummary(state)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepTargetMuscles(
    state: RapidTemplateWizardUiState,
    viewModel: RapidTemplateWizardViewModel
) {
    Column {
        Text(
            text = "Step 1: Target Muscles",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Select 2 to 5 sub-muscles to program your workout.",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        ForgeTextField(
            value = state.routineName,
            onValueChange = { viewModel.updateRoutineName(it) },
            label = "ROUTINE NAME",
            placeholder = "e.g. Upper Chest & Side Delt Hypertrophy"
        )

        Spacer(modifier = Modifier.height(20.dp))

        MuscleGroup.entries.forEach { group ->
            Text(
                text = group.displayName.uppercase(),
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.subMuscles.forEach { subMuscle ->
                    val isSelected = state.selectedSubMuscles.contains(subMuscle)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ForgeAmber else CarbonSlateSurface)
                            .border(
                                1.dp,
                                if (isSelected) ForgeAmber else CarbonSlateCard,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(role = Role.Checkbox) {
                                viewModel.toggleSubMuscle(subMuscle)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = subMuscle.displayName,
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepExerciseSelection(
    state: RapidTemplateWizardUiState,
    viewModel: RapidTemplateWizardViewModel
) {
    Column {
        Text(
            text = "Step 2: Exercise Selection",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Toggle exercises to include. Tap the Link icon to create a Superset.",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.candidateExercises.forEachIndexed { index, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (item.isSelected) CarbonSlateLight else CarbonSlateSurface.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        if (item.isSelected) CarbonSlateCard else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.toggleExerciseSelection(item.exercise.id) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (item.isSelected) ForgeAmber else CarbonSlateCard),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.exercise.name,
                                color = if (item.isSelected) TextPrimary else TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.exercise.equipment.displayName} • ${item.exercise.primarySubMuscle.displayName}",
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Superset link toggle (if not last item)
                    if (index < state.candidateExercises.size - 1) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (item.isSupersetWithNext) KineticLime.copy(alpha = 0.2f) else CarbonSlateSurface)
                                .border(
                                    1.dp,
                                    if (item.isSupersetWithNext) KineticLime else CarbonSlateCard,
                                    CircleShape
                                )
                                .clickable { viewModel.toggleSuperset(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Link as Superset",
                                tint = if (item.isSupersetWithNext) KineticLime else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (item.isSupersetWithNext) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚡ SUPERSET with next exercise",
                        color = KineticLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun StepVolumeTuning(
    state: RapidTemplateWizardUiState,
    viewModel: RapidTemplateWizardViewModel
) {
    Column {
        Text(
            text = "Step 3: Volume & Rest",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Prescribe working sets, rep ranges, and rest periods.",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.candidateExercises.filter { it.isSelected }.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CarbonSlateLight)
                    .border(1.dp, CarbonSlateCard, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = item.exercise.name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sets Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Working Sets", color = TextSecondary, fontSize = 14.sp)
                    StepperCounter(
                        value = item.prescribedSets.toDouble(),
                        onValueChange = { viewModel.updateSets(item.exercise.id, it.toInt()) },
                        step = 1.0,
                        minValue = 1.0,
                        maxValue = 10.0,
                        unitLabel = "sets",
                        isInteger = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rep Range Stepper (Reps Max)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Target Reps", color = TextSecondary, fontSize = 14.sp)
                    StepperCounter(
                        value = item.prescribedRepsMax.toDouble(),
                        onValueChange = {
                            viewModel.updateRepsMax(item.exercise.id, it.toInt())
                            viewModel.updateRepsMin(item.exercise.id, (it.toInt() - 2).coerceAtLeast(1))
                        },
                        step = 1.0,
                        minValue = 1.0,
                        maxValue = 50.0,
                        unitLabel = "reps",
                        isInteger = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rest Period Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Rest Interval", color = TextSecondary, fontSize = 14.sp)
                    StepperCounter(
                        value = item.restSeconds.toDouble(),
                        onValueChange = { viewModel.updateRest(item.exercise.id, it.toInt()) },
                        step = 15.0,
                        minValue = 15.0,
                        maxValue = 300.0,
                        unitLabel = "sec",
                        isInteger = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StepProjectionSummary(state: RapidTemplateWizardUiState) {
    Column {
        Text(
            text = "Step 4: Real-Time Projection",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Calculated metrics based on your programming volume.",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Projection Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "EST. DURATION",
                value = "~${state.estimatedDurationMinutes} min",
                accentColor = ForgeAmber,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "EST. BURN (±15%)",
                value = "${state.estimatedCaloriesRange.first}-${state.estimatedCaloriesRange.second} kcal",
                accentColor = KineticLime,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        MetricCard(
            title = "TOTAL WORKING VOLUME",
            value = "${state.totalSets} sets across ${state.candidateExercises.count { it.isSelected }} exercises",
            accentColor = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "TARGETED SUB-MUSCLES",
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonSlateLight)
                .border(1.dp, CarbonSlateCard, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            state.selectedSubMuscles.forEach { muscle ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = muscle.displayName, color = TextPrimary, fontSize = 14.sp)
                    Text(
                        text = "${muscle.parentGroup.displayName}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateCard, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = accentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
