package com.repforge.feature.routines

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.repforge.core.domain.model.Equipment
import com.repforge.core.domain.model.Exercise
import com.repforge.core.domain.model.SubMuscle
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
fun ExerciseDictionaryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseDictionaryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val exercises by viewModel.exercises.collectAsState()

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
                    text = "EXERCISE DICTIONARY",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateCustomDialog() },
                containerColor = ForgeAmber,
                contentColor = CarbonSlate,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create custom exercise",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search input
            ForgeTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Search 250+ canonical exercises & creator tags...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Equipment Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        label = "All Equipment",
                        isSelected = state.selectedEquipment == null,
                        onClick = { viewModel.selectEquipment(null) }
                    )
                }
                items(Equipment.entries) { eq ->
                    FilterChip(
                        label = eq.displayName,
                        isSelected = state.selectedEquipment == eq,
                        onClick = {
                            viewModel.selectEquipment(if (state.selectedEquipment == eq) null else eq)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-Muscle Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        label = "All Muscles",
                        isSelected = state.selectedSubMuscle == null,
                        onClick = { viewModel.selectSubMuscle(null) }
                    )
                }
                items(SubMuscle.entries) { muscle ->
                    FilterChip(
                        label = muscle.displayName,
                        isSelected = state.selectedSubMuscle == muscle,
                        onClick = {
                            viewModel.selectSubMuscle(if (state.selectedSubMuscle == muscle) null else muscle)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Exercise count banner
            Text(
                text = "${exercises.size} EXERCISES FOUND",
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(exercises, key = { it.id }) { exercise ->
                    ExerciseDictionaryItem(exercise = exercise)
                }
            }
        }

        // Custom Exercise Dialog
        if (state.isCustomExerciseDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.closeCreateCustomDialog() },
                containerColor = CarbonSlateSurface,
                title = {
                    Text(
                        text = "New Custom Exercise",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ForgeTextField(
                            value = state.newExerciseName,
                            onValueChange = { viewModel.updateNewExerciseName(it) },
                            label = "EXERCISE NAME",
                            placeholder = "e.g. Plate-Loaded Belt Squat"
                        )

                        Text(
                            text = "EQUIPMENT",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(Equipment.entries) { eq ->
                                FilterChip(
                                    label = eq.displayName,
                                    isSelected = state.newExerciseEquipment == eq,
                                    onClick = { viewModel.updateNewExerciseEquipment(eq) }
                                )
                            }
                        }

                        Text(
                            text = "PRIMARY SUB-MUSCLE",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(SubMuscle.entries) { muscle ->
                                FilterChip(
                                    label = muscle.displayName,
                                    isSelected = state.newExercisePrimaryMuscle == muscle,
                                    onClick = { viewModel.updateNewExercisePrimaryMuscle(muscle) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.saveCustomExercise() }
                    ) {
                        Text(text = "Save Exercise", color = ForgeAmber, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeCreateCustomDialog() }) {
                        Text(text = "Cancel", color = TextTertiary)
                    }
                }
            )
        }
    }
}

@Composable
private fun ExerciseDictionaryItem(exercise: Exercise) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateCard, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${exercise.equipment.displayName} • ${exercise.primarySubMuscle.displayName}",
                    color = ForgeAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (exercise.isCustom) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(KineticLime.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CUSTOM",
                        color = KineticLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (exercise.creatorTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = exercise.creatorTags.joinToString(" "),
                color = TextTertiary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ForgeAmber else CarbonSlateSurface)
            .border(1.dp, if (isSelected) ForgeAmber else CarbonSlateCard, RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
