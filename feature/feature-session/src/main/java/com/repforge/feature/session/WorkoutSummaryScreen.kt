package com.repforge.feature.session

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.repforge.core.ui.components.ForgeButton
import com.repforge.core.ui.components.ForgeButtonVariant
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
fun WorkoutSummaryScreen(
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutSummaryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCalorieInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CarbonSlate,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSlateLight)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                ForgeButton(
                    text = "Done & Back to Home",
                    onClick = onDoneClick,
                    variant = ForgeButtonVariant.PRIMARY,
                    isFullWidth = true
                )
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
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "WORKOUT COMPLETE",
                color = KineticLime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = state.session?.routineName?.uppercase() ?: "COMPLETED WORKOUT",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PR Celebration Banner (if any)
            if (state.newPersonalRecords.isNotEmpty()) {
                state.newPersonalRecords.forEach { pr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(KineticLime.copy(alpha = 0.15f))
                            .border(1.dp, KineticLime, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(KineticLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "NEW PERSONAL RECORD!",
                                color = KineticLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${pr.exerciseName}: ${pr.value.toInt()} kg",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatTile(
                    title = "DURATION",
                    value = "${state.durationMinutes} min",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatTile(
                    title = "TOTAL TONNAGE",
                    value = if (state.totalTonnageKg >= 1000) {
                        "%.1f t".format(state.totalTonnageKg / 1000.0)
                    } else {
                        "%.0f kg".format(state.totalTonnageKg)
                    },
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatTile(
                    title = "SETS LOGGED",
                    value = "${state.totalSetsCount}",
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bio-Energetic Caloric Burn Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonSlateLight)
                    .border(1.dp, CarbonSlateCard, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = ForgeAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ESTIMATED CALORIC EXPENDITURE",
                            color = ForgeAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CarbonSlateSurface)
                            .clickable { showCalorieInfoDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Rough Estimate ℹ",
                            color = TextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val cal = state.calorieEstimate
                if (cal != null) {
                    Text(
                        text = "~${cal.totalKcal.toInt()} kcal",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Confidence Range: ${cal.lowBoundKcal.toInt()} – ${cal.highBoundKcal.toInt()} kcal (±15%)",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-Muscle Distribution
            if (state.subMuscleDistribution.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CarbonSlateLight)
                        .border(1.dp, CarbonSlateCard, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SUB-MUSCLE VOLUME BREAKDOWN",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    state.subMuscleDistribution.take(5).forEach { volume ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = volume.subMuscle.displayName,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "%.0f%%".format(volume.percentage),
                                    color = ForgeAmber,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (volume.percentage / 100.0).toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ForgeAmber,
                                trackColor = CarbonSlateSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Connect Sync Badge
            if (state.isHealthConnectSynced) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CarbonSlateSurface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = KineticLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Synced strength session & calories to Health Connect",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Diff Resolver Dialog
        if (state.showDiffDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDiffDialog() },
                containerColor = CarbonSlateSurface,
                title = {
                    Text(
                        text = "Workout Changes Detected",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = state.diffSummary,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "How would you like to save these adjustments?",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ForgeButton(
                            text = "Update Base Routine",
                            onClick = { viewModel.updateBaseRoutine() },
                            variant = ForgeButtonVariant.PRIMARY,
                            isFullWidth = true
                        )
                        ForgeButton(
                            text = "Save as New Variation",
                            onClick = { viewModel.saveAsNewVariation() },
                            variant = ForgeButtonVariant.SECONDARY,
                            isFullWidth = true
                        )
                        ForgeButton(
                            text = "Log for Today Only",
                            onClick = { viewModel.dismissDiffDialog() },
                            variant = ForgeButtonVariant.GHOST,
                            isFullWidth = true
                        )
                    }
                },
                dismissButton = {}
            )
        }

        // Calorie Explanation Dialog
        if (showCalorieInfoDialog) {
            AlertDialog(
                onDismissRequest = { showCalorieInfoDialog = false },
                containerColor = CarbonSlateSurface,
                title = {
                    Text(
                        text = "How We Estimate Calories",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Resistance training calorie expenditure varies based on muscle mass, rest periods, and failure intensity. We use a 3-factor model (Mifflin-St Jeor BMR + Tonnage Load Scaling + EPOC fatigue factor). We display a scientifically defensible ±15% range rather than misleading precision.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showCalorieInfoDialog = false }) {
                        Text(text = "Got It", color = ForgeAmber, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun SummaryStatTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateCard, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ForgeAmber,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}
