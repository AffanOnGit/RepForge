package com.repforge.feature.session

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.core.domain.model.Equipment
import com.repforge.core.ui.components.ExerciseCard
import com.repforge.core.ui.components.ForgeButton
import com.repforge.core.ui.components.ForgeButtonVariant
import com.repforge.core.ui.components.PlateMathVisualizer
import com.repforge.core.ui.components.SetRow
import com.repforge.core.ui.theme.CarbonSlate
import com.repforge.core.ui.theme.CarbonSlateCard
import com.repforge.core.ui.theme.CarbonSlateLight
import com.repforge.core.ui.theme.CarbonSlateSurface
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.KineticLime
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary
import com.repforge.core.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    onFinishWorkout: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveSessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val minutes = state.elapsedSeconds / 60
    val seconds = state.elapsedSeconds % 60
    val formattedDuration = "%02d:%02d".format(minutes, seconds)

    Scaffold(
        containerColor = CarbonSlate,
        bottomBar = {
            RestTimerBar(
                isVisible = state.isRestTimerVisible,
                remainingSeconds = state.restTimerRemainingSeconds,
                totalSeconds = state.restTimerTotalSeconds,
                onAdd30s = { viewModel.add30sRest() },
                onSubtract15s = { viewModel.subtract15sRest() },
                onSkip = { viewModel.skipRestTimer() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Session Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSlateLight)
                    .border(1.dp, CarbonSlateCard)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = state.routineName.uppercase(),
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = ForgeAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedDuration,
                                color = ForgeAmber,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = KineticLime,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "~${state.estimatedCalories} kcal",
                                color = KineticLime,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    ForgeButton(
                        text = "Finish",
                        onClick = { viewModel.finishWorkout { onFinishWorkout(state.sessionId) } },
                        variant = ForgeButtonVariant.PRIMARY
                    )
                }
            }

            // Exercise Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                itemsIndexed(state.exercises) { exIndex, exUi ->
                    ExerciseCard(
                        exerciseName = exUi.exercise.name,
                        equipment = exUi.exercise.equipment,
                        primarySubMuscle = exUi.exercise.primarySubMuscle,
                        secondarySubMuscles = exUi.exercise.secondarySubMuscles,
                        personalRecordDisplay = exUi.bestPRDisplay,
                        onSwapClick = { viewModel.openExerciseSwap(exIndex) },
                        onPlateMathClick = if (exUi.exercise.equipment == Equipment.BARBELL) {
                            {
                                val weight = exUi.sets.firstOrNull()?.set?.weightKg ?: 60.0
                                viewModel.openPlateMath(weight)
                            }
                        } else null,
                        onAddSetClick = { viewModel.addSet(exIndex) }
                    ) {
                        exUi.sets.forEachIndexed { setIndex, setUi ->
                            SetRow(
                                setNumber = setUi.set.setNumber,
                                setType = setUi.set.setType,
                                weightDisplay = if (setUi.set.weightKg % 1.0 == 0.0) {
                                    "%.0f".format(setUi.set.weightKg)
                                } else {
                                    "%.1f".format(setUi.set.weightKg)
                                },
                                repsDisplay = "${setUi.set.repsCompleted}",
                                isCompleted = setUi.set.isCompleted,
                                onWeightChange = { viewModel.updateWeight(exIndex, setIndex, it) },
                                onRepsChange = { viewModel.updateReps(exIndex, setIndex, it) },
                                onSetTypeClick = { viewModel.cycleSetType(exIndex, setIndex) },
                                onToggleComplete = { viewModel.toggleSetComplete(exIndex, setIndex) },
                                ghostText = setUi.ghostText
                            )
                        }
                    }
                }
            }
        }

        // Barbell Plate Math Bottom Sheet
        if (state.isPlateMathOpen) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { viewModel.closePlateMath() },
                sheetState = sheetState,
                containerColor = CarbonSlateSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    PlateMathVisualizer(
                        totalWeightKg = state.plateMathWeightKg,
                        barWeightKg = state.plateMathBarKg,
                        unitSystem = state.unitSystem
                    )
                }
            }
        }

        // Live Exercise Swap Bottom Sheet
        if (state.isSwapSheetOpen && state.exerciseToSwapIndex >= 0) {
            val currentExercise = state.exercises.getOrNull(state.exerciseToSwapIndex)?.exercise
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeExerciseSwap() },
                sheetState = sheetState,
                containerColor = CarbonSlateSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "MID-WORKOUT EXERCISE SWAP",
                        color = ForgeAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Alternatives for ${currentExercise?.name}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Same primary sub-muscle: ${currentExercise?.primarySubMuscle?.displayName}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.swapSuggestions.isEmpty()) {
                        Text(
                            text = "No direct sub-muscle alternatives found.",
                            color = TextTertiary,
                            fontSize = 14.sp
                        )
                    } else {
                        state.swapSuggestions.forEach { altEx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CarbonSlateLight)
                                    .clickable { viewModel.swapExercise(altEx) }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = altEx.name,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = altEx.equipment.displayName,
                                        color = ForgeAmber,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = "Swap ⇄",
                                    color = KineticLime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
