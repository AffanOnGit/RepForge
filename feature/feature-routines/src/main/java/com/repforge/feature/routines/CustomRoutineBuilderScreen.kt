package com.repforge.feature.routines

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.repforge.core.ui.theme.ErrorRed
import com.repforge.core.ui.theme.ForgeAmber
import com.repforge.core.ui.theme.KineticLime
import com.repforge.core.ui.theme.TextPrimary
import com.repforge.core.ui.theme.TextSecondary
import com.repforge.core.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRoutineBuilderScreen(
    onNavigateBack: () -> Unit,
    onRoutineSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomRoutineBuilderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val pickerExercises by viewModel.pickerExercises.collectAsState()

    // Navigate after save
    LaunchedEffect(state.savedRoutineId) {
        state.savedRoutineId?.let { onRoutineSaved(it) }
    }

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
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isEditMode) "EDIT ROUTINE" else "BUILD ROUTINE",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (state.isSaving || state.routineName.isBlank() || state.entries.isEmpty())
                                CarbonSlateSurface
                            else
                                ForgeAmber
                        )
                        .clickable(
                            enabled = !state.isSaving && state.routineName.isNotBlank() && state.entries.isNotEmpty(),
                            role = Role.Button,
                            onClick = { viewModel.saveRoutine() }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (state.isSaving) "Saving..." else "Save",
                        color = if (state.isSaving || state.routineName.isBlank() || state.entries.isEmpty())
                            TextTertiary
                        else
                            CarbonSlate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Name & description fields
            item {
                Spacer(modifier = Modifier.height(4.dp))
                ForgeTextField(
                    value = state.routineName,
                    onValueChange = viewModel::onNameChanged,
                    label = "Routine Name",
                    placeholder = "e.g. Upper Body Power"
                )
                Spacer(modifier = Modifier.height(8.dp))
                ForgeTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = "Description (optional)",
                    placeholder = "e.g. 3-day push/pull focus"
                )
            }

            // "Add Exercise" button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonSlateLight)
                        .border(1.dp, KineticLime.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable(role = Role.Button, onClick = { viewModel.openPicker() })
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = KineticLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Exercise",
                            color = KineticLime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Header for exercise list
            if (state.entries.isNotEmpty()) {
                item {
                    Text(
                        text = "${state.entries.size} EXERCISE${if (state.entries.size != 1) "S" else ""}",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Exercise rows
            items(state.entries, key = { it.id }) { entry ->
                BuilderExerciseRow(
                    entry = entry,
                    isFirst = state.entries.firstOrNull()?.id == entry.id,
                    isLast = state.entries.lastOrNull()?.id == entry.id,
                    onRemove = { viewModel.removeExercise(entry.id) },
                    onMoveUp = { viewModel.moveUp(entry.id) },
                    onMoveDown = { viewModel.moveDown(entry.id) },
                    onSetsPlus = { viewModel.updateSets(entry.id, +1) },
                    onSetsMinus = { viewModel.updateSets(entry.id, -1) },
                    onRepsMinPlus = { viewModel.updateRepsMin(entry.id, +1) },
                    onRepsMinMinus = { viewModel.updateRepsMin(entry.id, -1) },
                    onRepsMaxPlus = { viewModel.updateRepsMax(entry.id, +1) },
                    onRepsMaxMinus = { viewModel.updateRepsMax(entry.id, -1) }
                )
            }

            // Empty state
            if (state.entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No exercises added yet.\nTap \"Add Exercise\" to start building.",
                            color = TextTertiary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Exercise Picker Bottom Sheet
        if (state.isPickerOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { viewModel.closePicker() },
                sheetState = sheetState,
                containerColor = CarbonSlateSurface
            ) {
                ExercisePickerContent(
                    exercises = pickerExercises,
                    filter = state.pickerFilter,
                    onSearchChanged = viewModel::onPickerSearchChanged,
                    onMuscleChanged = viewModel::onPickerMuscleChanged,
                    onEquipmentChanged = viewModel::onPickerEquipmentChanged,
                    onSelectExercise = { ex -> viewModel.addExercise(ex) }
                )
            }
        }
    }
}

// ─── Builder Exercise Row ────────────────────────────────────────────────────

@Composable
private fun BuilderExerciseRow(
    entry: BuilderExerciseEntry,
    isFirst: Boolean,
    isLast: Boolean,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onSetsPlus: () -> Unit,
    onSetsMinus: () -> Unit,
    onRepsMinPlus: () -> Unit,
    onRepsMinMinus: () -> Unit,
    onRepsMaxPlus: () -> Unit,
    onRepsMaxMinus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateCard, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        // Top row: name + up/down/delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.exercise.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${entry.exercise.equipment.displayName} · ${entry.exercise.primarySubMuscle.displayName}",
                    color = ForgeAmber,
                    fontSize = 11.sp
                )
            }
            Row {
                if (!isFirst) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move up", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
                if (!isLast) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move down", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Volume controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VolumeControl(label = "Sets", value = entry.sets.toString(), onPlus = onSetsPlus, onMinus = onSetsMinus, modifier = Modifier.weight(1f))
            VolumeControl(label = "Min reps", value = entry.repsMin.toString(), onPlus = onRepsMinPlus, onMinus = onRepsMinMinus, modifier = Modifier.weight(1f))
            VolumeControl(label = "Max reps", value = entry.repsMax.toString(), onPlus = onRepsMaxPlus, onMinus = onRepsMaxMinus, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun VolumeControl(
    label: String,
    value: String,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextTertiary, fontSize = 10.sp, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CarbonSlateSurface)
                    .clickable(role = Role.Button, onClick = onMinus),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "−", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CarbonSlateSurface)
                    .clickable(role = Role.Button, onClick = onPlus),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Exercise Picker (bottom sheet content) ──────────────────────────────────

@Composable
private fun ExercisePickerContent(
    exercises: List<Exercise>,
    filter: ExercisePickerFilter,
    onSearchChanged: (String) -> Unit,
    onMuscleChanged: (SubMuscle?) -> Unit,
    onEquipmentChanged: (Equipment?) -> Unit,
    onSelectExercise: (Exercise) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "ADD EXERCISE",
            color = ForgeAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Search bar
        ForgeTextField(
            value = filter.searchQuery,
            onValueChange = onSearchChanged,
            label = "Search exercises",
            placeholder = "e.g. Squat, Lat Pulldown…"
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Muscle filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            item {
                FilterChip(
                    label = "All Muscles",
                    selected = filter.subMuscle == null,
                    onClick = { onMuscleChanged(null) }
                )
            }
            items(SubMuscle.entries.take(18)) { muscle ->
                FilterChip(
                    label = muscle.displayName,
                    selected = filter.subMuscle == muscle,
                    onClick = { onMuscleChanged(if (filter.subMuscle == muscle) null else muscle) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Equipment filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            item {
                FilterChip(
                    label = "All Equipment",
                    selected = filter.equipment == null,
                    onClick = { onEquipmentChanged(null) }
                )
            }
            items(Equipment.entries) { eq ->
                FilterChip(
                    label = eq.displayName,
                    selected = filter.equipment == eq,
                    onClick = { onEquipmentChanged(if (filter.equipment == eq) null else eq) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "${exercises.size} exercises",
            color = TextTertiary,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Results
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(320.dp)
        ) {
            if (exercises.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No exercises match your filters.", color = TextTertiary, fontSize = 13.sp)
                    }
                }
            }
            items(exercises, key = { it.id }) { exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CarbonSlateLight)
                        .clickable(role = Role.Button) { onSelectExercise(exercise) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${exercise.equipment.displayName} · ${exercise.primarySubMuscle.displayName}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = KineticLime,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) ForgeAmber.copy(alpha = 0.2f) else CarbonSlateLight)
            .border(
                1.dp,
                if (selected) ForgeAmber else CarbonSlateCard,
                RoundedCornerShape(20.dp)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) ForgeAmber else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
