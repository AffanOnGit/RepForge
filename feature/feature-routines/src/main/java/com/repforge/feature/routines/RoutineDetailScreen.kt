package com.repforge.feature.routines

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.ExerciseGroupType
import com.repforge.core.ui.components.ForgeButton
import com.repforge.core.ui.components.ForgeButtonVariant
import com.repforge.core.ui.theme.CarbonSlate
import com.repforge.core.ui.theme.CarbonSlateCard
import com.repforge.core.ui.theme.CarbonSlateLight
import com.repforge.core.ui.theme.CarbonSlateSurface
import com.repforge.core.ui.theme.ErrorRed
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.KineticLime
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary
import com.repforge.core.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    onNavigateBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    onEditRoutine: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: RoutineDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val routine = state.routine

    Scaffold(
        containerColor = CarbonSlate,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROUTINE DETAIL",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(onClick = { viewModel.deleteRoutine(onNavigateBack) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete routine",
                        tint = ErrorRed
                    )
                }
                IconButton(onClick = { routine?.id?.let(onEditRoutine) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit routine",
                        tint = ForgeAmber
                    )
                }
            }
        },
        bottomBar = {
            if (routine != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonSlateLight)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    ForgeButton(
                        text = "Start Workout Now",
                        onClick = { onStartWorkout(routine.id) },
                        variant = ForgeButtonVariant.KINETIC,
                        isFullWidth = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (routine == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading routine...", color = TextSecondary)
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = routine.name,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                if (routine.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = routine.description,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = ForgeAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Est. ${routine.estimatedDurationMinutes ?: 45} minutes",
                        color = ForgeAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (routine.creatorName != null) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Created by ${routine.creatorName}",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "EXERCISE SEQUENCE",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                state.groupsWithExercises.forEachIndexed { groupIndex, groupUi ->
                    val isSuperset = groupUi.group.groupType == ExerciseGroupType.SUPERSET
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CarbonSlateLight)
                            .border(
                                1.dp,
                                if (isSuperset) KineticLime.copy(alpha = 0.5f) else CarbonSlateCard,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(16.dp)
                    ) {
                        if (isSuperset) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = "⚡ SUPERSET GROUP",
                                    color = KineticLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(No rest between exercises)",
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        groupUi.exercises.forEachIndexed { exIndex, exDetail ->
                            val ex = exDetail.exercise
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${groupIndex + 1}${if (groupUi.exercises.size > 1) ('A' + exIndex) else ""}. ${ex?.name ?: "Unknown Exercise"}",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${exDetail.routineExercise.prescribedSets} sets × ${exDetail.routineExercise.prescribedRepsMin}-${exDetail.routineExercise.prescribedRepsMax} reps • ${exDetail.routineExercise.restSeconds}s rest",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                // Swap button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CarbonSlateSurface)
                                        .clickable { viewModel.openSwapSheet(exDetail) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Swap exercise",
                                        tint = ForgeAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (exIndex < groupUi.exercises.size - 1) {
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Exercise Swap Bottom Sheet
        if (state.isSwapSheetOpen && state.exerciseToSwap != null) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeSwapSheet() },
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
                        text = "SWAP EXERCISE",
                        color = ForgeAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Alternatives for ${state.exerciseToSwap?.exercise?.name}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Targeting the same primary sub-muscle: ${state.exerciseToSwap?.exercise?.primarySubMuscle?.displayName}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.swapSuggestions.isEmpty()) {
                        Text(
                            text = "No direct sub-muscle alternatives found in dictionary.",
                            color = TextTertiary,
                            fontSize = 14.sp
                        )
                    } else {
                        state.swapSuggestions.forEach { altExercise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CarbonSlateLight)
                                    .clickable { viewModel.swapExercise(altExercise) }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = altExercise.name,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = altExercise.equipment.displayName,
                                        color = ForgeAmber,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = "Select ⇄",
                                    color = ForgeAmber,
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
