package com.repforge.feature.heatmap

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
fun HeatmapScreen(
    modifier: Modifier = Modifier,
    viewModel: HeatmapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(containerColor = CarbonSlate) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ANATOMY HEATMAP",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )

                // Front / Rear toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CarbonSlateSurface)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (state.isFrontView) ForgeAmber else Color.Transparent)
                            .clickable { viewModel.toggleView(true) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Front",
                            color = if (state.isFrontView) Color.Black else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!state.isFrontView) ForgeAmber else Color.Transparent)
                            .clickable { viewModel.toggleView(false) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Rear",
                            color = if (!state.isFrontView) Color.Black else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Selector: Weekly Overall vs Per Workout Session
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CarbonSlateLight)
                    .border(1.dp, CarbonSlateCard, RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (state.scope == HeatmapScope.WEEKLY) ForgeAmber else Color.Transparent)
                        .clickable { viewModel.setScope(HeatmapScope.WEEKLY) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Weekly Overall",
                        color = if (state.scope == HeatmapScope.WEEKLY) Color.Black else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (state.scope == HeatmapScope.SESSION) ForgeAmber else Color.Transparent)
                        .clickable { viewModel.setScope(HeatmapScope.SESSION) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Per Workout Session",
                        color = if (state.scope == HeatmapScope.SESSION) Color.Black else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (state.scope == HeatmapScope.WEEKLY) {
                // Time Window Selector (7d, 14d, 30d)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7 to "Last 7 Days", 14 to "Last 14 Days", 30 to "Last 30 Days").forEach { (days, label) ->
                        val isSelected = state.selectedTimeWindowDays == days
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CarbonSlateLight else CarbonSlateSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) ForgeAmber else CarbonSlateCard,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setTimeWindow(days) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) ForgeAmber else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                // Per Workout Session Selector
                if (state.recentSessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CarbonSlateSurface)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No completed workouts yet. Log a session to see its heatmap!",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.recentSessions) { session ->
                            val isSelected = state.selectedSessionId == session.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) CarbonSlateLight else CarbonSlateSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) ForgeAmber else CarbonSlateCard,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.selectSession(session.id) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = session.routineName,
                                        color = if (isSelected) ForgeAmber else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = session.formattedDate,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2D Interactive Anatomy Body Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CarbonSlateLight)
                    .border(1.dp, CarbonSlateCard, RoundedCornerShape(18.dp))
                    .padding(vertical = 12.dp)
            ) {
                AnatomyMapComposable(
                    isFrontView = state.isFrontView,
                    heatData = state.heatMapData,
                    onSubMuscleClick = { viewModel.selectSubMuscle(it) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heat Gradient Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(CarbonSlateCard)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Untrained", color = TextTertiary, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(ForgeAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Moderate", color = TextTertiary, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(KineticLime)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Peak Volume", color = TextTertiary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sub-Muscle Volume Ranking
            Text(
                text = "TOP TRAINED SUB-MUSCLES",
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            val sortedList = state.heatMapData.values
                .filter { it.totalSets > 0 }
                .sortedByDescending { it.totalTonnageKg }

            if (sortedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonSlateLight)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No workout volume recorded in this time window.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                sortedList.take(6).forEach { muscleHeat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonSlateLight)
                            .clickable { viewModel.selectSubMuscle(muscleHeat.subMuscle) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = muscleHeat.subMuscle.displayName,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${muscleHeat.totalSets} sets • ${muscleHeat.daysSinceLastTrained?.let { "$it days ago" } ?: "Recently"}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "%.0f kg".format(muscleHeat.totalTonnageKg),
                            color = ForgeAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Sub-Muscle Detail Modal Bottom Sheet
        if (state.selectedSubMuscle != null) {
            val muscleData = state.selectedSubMuscle!!
            val sheetState = rememberModalBottomSheetState()

            ModalBottomSheet(
                onDismissRequest = { viewModel.selectSubMuscle(null) },
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
                        text = muscleData.subMuscle.parentGroup.displayName.uppercase(),
                        color = ForgeAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = muscleData.subMuscle.displayName,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailTile(
                            title = "TOTAL SETS",
                            value = "${muscleData.totalSets}",
                            modifier = Modifier.weight(1f)
                        )
                        DetailTile(
                            title = "TONNAGE",
                            value = "%.0f kg".format(muscleData.totalTonnageKg),
                            modifier = Modifier.weight(1f)
                        )
                        DetailTile(
                            title = "LAST TRAINED",
                            value = muscleData.daysSinceLastTrained?.let { "$it d ago" } ?: "None",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (muscleData.topExercises.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "TOP PERFORMED EXERCISES",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        muscleData.topExercises.forEach { exName ->
                            Text(
                                text = "• $exName",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CarbonSlateLight)
            .padding(12.dp)
    ) {
        Text(text = title, color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
