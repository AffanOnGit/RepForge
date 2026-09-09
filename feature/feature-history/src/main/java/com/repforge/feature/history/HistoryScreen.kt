package com.repforge.feature.history

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.core.domain.model.PRType
import com.repforge.core.domain.model.UnitConverter
import com.repforge.core.domain.model.UnitSystem
import com.repforge.core.ui.components.ForgeButton
import com.repforge.core.ui.components.ForgeButtonVariant
import com.repforge.core.ui.components.SetTypeBadge
import com.repforge.core.ui.theme.CarbonSlate
import com.repforge.core.ui.theme.CarbonSlateCard
import com.repforge.core.ui.theme.CarbonSlateLight
import com.repforge.core.ui.theme.CarbonSlateSurface
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.KineticLime
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary
import com.repforge.core.ui.theme.TextTertiary
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = CarbonSlate
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ForgeAmber)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header & Action Bar
                item {
                    HistoryHeader(
                        prCount = state.personalRecords.size,
                        onOpenTrophyRoom = { viewModel.toggleTrophyRoom(true) },
                        onOpenExport = { viewModel.toggleExportSheet(true) }
                    )
                }

                // High-Level Lifetime KPIs
                item {
                    KpiStatsRow(
                        totalWorkouts = state.totalWorkouts,
                        streakWeeks = state.streakWeeks,
                        totalTonnage = state.totalTonnageDisplay,
                        prCount = state.personalRecords.size
                    )
                }

                // GitHub-style Frequency Matrix (Consistency Heatmap)
                item {
                    ConsistencyMatrixCard(frequencyGrid = state.frequencyGrid)
                }

                // Section Title: Workout Logbook
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "WORKOUT LOGBOOK",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                if (state.monthlySessions.isEmpty()) {
                    item {
                        EmptyHistoryPlaceholder()
                    }
                } else {
                    // Grouped monthly feed
                    state.monthlySessions.forEach { (monthName, sessionItems) ->
                        item {
                            MonthHeader(monthName = monthName, workoutCount = sessionItems.size)
                        }

                        items(sessionItems, key = { it.session.id }) { item ->
                            val isExpanded = state.expandedSessionId == item.session.id
                            SessionHistoryCard(
                                item = item,
                                isExpanded = isExpanded,
                                expandedSets = if (isExpanded) state.expandedSets else emptyList(),
                                isLoadingSets = isExpanded && state.isLoadingSets,
                                unitSystem = state.unitSystem,
                                onClick = { viewModel.toggleSessionExpand(item.session.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: PR Trophy Room
    if (state.showTrophyRoom) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleTrophyRoom(false) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CarbonSlateCard
        ) {
            TrophyRoomSheetContent(
                records = state.personalRecords,
                unitSystem = state.unitSystem,
                onClose = { viewModel.toggleTrophyRoom(false) }
            )
        }
    }

    // Modal Bottom Sheet: Data Export
    if (state.showExportSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleExportSheet(false) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CarbonSlateCard
        ) {
            ExportSheetContent(
                workoutCount = state.totalWorkouts,
                onExportCsv = {
                    viewModel.exportWorkoutsAsCsv(context)
                    viewModel.toggleExportSheet(false)
                },
                onExportJson = {
                    viewModel.exportWorkoutsAsJson(context)
                    viewModel.toggleExportSheet(false)
                }
            )
        }
    }
}

@Composable
private fun HistoryHeader(
    prCount: Int,
    onOpenTrophyRoom: () -> Unit,
    onOpenExport: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "LOGBOOK & HISTORY",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = ForgeAmber,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Training Archive",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // Trophy Room Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonSlateLight)
                    .border(1.dp, if (prCount > 0) ForgeAmber else CarbonSlateSurface, RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onOpenTrophyRoom)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "🏆", fontSize = 14.sp)
                    Text(
                        text = "$prCount PRs",
                        color = if (prCount > 0) ForgeAmber else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Export Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonSlateLight)
                    .border(1.dp, CarbonSlateSurface, RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onOpenExport),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📤", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun KpiStatsRow(
    totalWorkouts: Int,
    streakWeeks: Int,
    totalTonnage: String,
    prCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiCard(
            title = "WORKOUTS",
            value = "$totalWorkouts",
            subtitle = "completed",
            accentColor = ForgeAmber,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = "STREAK",
            value = "$streakWeeks wks",
            subtitle = "active",
            accentColor = KineticLime,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = "TONNAGE",
            value = totalTonnage,
            subtitle = "lifted",
            accentColor = Color(0xFF38BDF8),
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateSurface, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = TextTertiary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = subtitle,
            color = accentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ConsistencyMatrixCard(frequencyGrid: List<DayContribution>) {
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
            Text(
                text = "CONSISTENCY MATRIX (LAST 10 WEEKS)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "Less", color = TextTertiary, fontSize = 9.sp)
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(CarbonSlateSurface))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(KineticLime.copy(alpha = 0.4f)))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(KineticLime.copy(alpha = 0.75f)))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(KineticLime))
                Text(text = "More", color = TextTertiary, fontSize = 9.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 10 columns (weeks) x 7 rows (days Mon-Sun)
        val weeks = frequencyGrid.chunked(7)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeks.forEach { weekDays ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    weekDays.forEach { day ->
                        val cellColor = when (day.intensityLevel) {
                            1 -> KineticLime.copy(alpha = 0.4f)
                            2 -> KineticLime.copy(alpha = 0.75f)
                            3 -> KineticLime
                            else -> CarbonSlateSurface
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(cellColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(monthName: String, workoutCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthName.uppercase(),
            color = ForgeAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "$workoutCount ${if (workoutCount == 1) "workout" else "workouts"}",
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SessionHistoryCard(
    item: SessionCardItem,
    isExpanded: Boolean,
    expandedSets: List<WorkoutSetDetail>,
    isLoadingSets: Boolean,
    unitSystem: UnitSystem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSlateLight)
            .border(1.dp, if (isExpanded) ForgeAmber.copy(alpha = 0.6f) else CarbonSlateSurface, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.formattedDate,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.session.routineName.ifBlank { "Freestyle Workout" },
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Expand Chevron
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CarbonSlateSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Badges: Duration, Tonnage, Calories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SessionPill(label = "⏱ ${item.formattedDuration}")
            SessionPill(label = "⚡ ${item.formattedTonnage}")
            SessionPill(label = "🔥 ${item.formattedCalories}")
        }

        // Expanded Sets Details
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                HorizontalDivider(color = CarbonSlateSurface, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoadingSets) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ForgeAmber, modifier = Modifier.size(24.dp))
                    }
                } else if (expandedSets.isEmpty()) {
                    Text(
                        text = "No individual sets logged in this session.",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                } else {
                    // Group sets by exercise name
                    val groupedSets = expandedSets.groupBy { it.exerciseName }
                    groupedSets.forEach { (exerciseName, sets) ->
                        Text(
                            text = exerciseName,
                            color = KineticLime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        sets.forEach { set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SetTypeBadge(setType = set.setType, setNumber = set.setNumber)
                                    Text(
                                        text = "Set ${set.setNumber}",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val weightDisplay = when (unitSystem) {
                                        is UnitSystem.Metric -> "%.1f kg".format(set.weightKg)
                                        is UnitSystem.Imperial -> "%.1f lb".format(UnitConverter.kgToLb(set.weightKg))
                                    }
                                    Text(
                                        text = "$weightDisplay × ${set.repsCompleted}",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    if (set.rpe != null) {
                                        Text(
                                            text = "@${set.rpe}",
                                            color = TextTertiary,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    if (set.isPR) {
                                        Text(text = "🏆", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionPill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonSlateSurface)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun EmptyHistoryPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateSurface, RoundedCornerShape(16.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📋", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No Workouts Logged Yet",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Complete your first workout session from the Today tab or Routine Builder to begin tracking consistency.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun TrophyRoomSheetContent(
    records: List<com.repforge.core.domain.model.PersonalRecord>,
    unitSystem: UnitSystem,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TROPHY ROOM",
                    color = ForgeAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Personal Records",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "${records.size} Records",
                color = KineticLime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = CarbonSlateSurface, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No personal records achieved yet. Keep pushing overload!",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    val dateStr = java.time.Instant.ofEpochMilli(record.achievedAtMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .format(dateFormatter)

                    val valueStr = when (record.type) {
                        PRType.WEIGHT -> when (unitSystem) {
                            is UnitSystem.Metric -> "%.1f kg".format(record.value)
                            is UnitSystem.Imperial -> "%.1f lb".format(UnitConverter.kgToLb(record.value))
                        }
                        PRType.REPS -> "%.0f reps".format(record.value)
                        PRType.VOLUME -> when (unitSystem) {
                            is UnitSystem.Metric -> "%,d kg volume".format(record.value.toLong())
                            is UnitSystem.Imperial -> "%,d lb volume".format(UnitConverter.kgToLb(record.value).toLong())
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonSlateLight)
                            .border(1.dp, CarbonSlateSurface, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🥇", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = record.exerciseName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$dateStr • ${record.type.name.lowercase().capitalize(Locale.ROOT)} PR",
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = valueStr,
                            color = ForgeAmber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ForgeButton(
            text = "Done",
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ExportSheetContent(
    workoutCount: Int,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "DATA PORTABILITY",
            color = ForgeAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Export Training History",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "You own 100% of your training data. Export all $workoutCount logged sessions without watermarks or paywalls.",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // CSV Option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CarbonSlateLight)
                .border(1.dp, CarbonSlateSurface, RoundedCornerShape(14.dp))
                .clickable(role = Role.Button, onClick = onExportCsv)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "📊 Export as CSV (Spreadsheet)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "Compatible with Excel, Google Sheets, Numbers", color = TextTertiary, fontSize = 11.sp)
            }
            Text(text = "➔", color = ForgeAmber, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // JSON Option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CarbonSlateLight)
                .border(1.dp, CarbonSlateSurface, RoundedCornerShape(14.dp))
                .clickable(role = Role.Button, onClick = onExportJson)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "💾 Export as JSON (Raw Schema)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "Ideal for custom scripts, backups, and migrations", color = TextTertiary, fontSize = 11.sp)
            }
            Text(text = "➔", color = KineticLime, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
