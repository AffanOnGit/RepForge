package com.repforge.feature.ingestion

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
fun AIIngestionScreen(
    onNavigateBack: () -> Unit,
    onRoutineImported: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AIIngestionViewModel = hiltViewModel()
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
                    text = "AI WORKOUT INGESTION",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
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
            Spacer(modifier = Modifier.height(12.dp))

            // Headline
            Text(
                text = "YouTube to Workout Routine",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Paste any YouTube workout video URL. Our multimodal AI extracts exercises, sets, reps, and sub-muscles in seconds.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // URL input & Import button
            ForgeTextField(
                value = state.urlInput,
                onValueChange = { viewModel.updateUrl(it) },
                label = "YOUTUBE VIDEO LINK",
                placeholder = "https://youtube.com/watch?v=..."
            )

            Spacer(modifier = Modifier.height(14.dp))

            ForgeButton(
                text = "Ingest Workout",
                onClick = { viewModel.startIngestion() },
                variant = ForgeButtonVariant.PRIMARY,
                isFullWidth = true,
                isLoading = state.stage != IngestionStage.IDLE && state.stage != IngestionStage.READY_FOR_REVIEW,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            // Pipeline Progress State
            AnimatedVisibility(visible = state.stage != IngestionStage.IDLE && state.stage != IngestionStage.READY_FOR_REVIEW) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CarbonSlateLight)
                        .border(1.dp, CarbonSlateCard, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = ForgeAmber,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (state.stage) {
                            IngestionStage.CHECKING_GLOBAL_CACHE -> "Checking Global Video Cache (0 AI Tokens)..."
                            IngestionStage.ANALYZING_METADATA -> "Extracting YouTube chapters & metadata..."
                            IngestionStage.GEMINI_PARSING -> "Gemini AI structuring exercise sequence..."
                            else -> "Processing..."
                        },
                        color = ForgeAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Error display
            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ErrorRed.copy(alpha = 0.15f))
                        .border(1.dp, ErrorRed, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = state.error?.message ?: "",
                            color = ErrorRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.error?.recoveryAction ?: "",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Curated Creator Hub Quick Picks
            Text(
                text = "CURATED CREATOR PRESETS (0 TOKENS)",
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CuratedPresetCard(
                title = "Scientific Push Workout",
                creator = "Jeff Nippard",
                tags = "Chest • Shoulders • Triceps",
                onClick = {
                    viewModel.updateUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    viewModel.startIngestion()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            CuratedPresetCard(
                title = "High Volume Chest & Triceps",
                creator = "Dr. Mike Israetel (RP)",
                tags = "Upper Chest • Mid Chest • Triceps",
                onClick = {
                    viewModel.updateUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    viewModel.startIngestion()
                }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // HITL (Human-in-the-loop) Review Sheet
        if (state.stage == IngestionStage.READY_FOR_REVIEW && state.parsedData != null) {
            val parsed = state.parsedData!!
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { viewModel.reset() },
                sheetState = sheetState,
                containerColor = CarbonSlateSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HUMAN-IN-THE-LOOP VERIFICATION",
                            color = ForgeAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        if (parsed.isFromGlobalCache) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KineticLime.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "VERIFIED CACHE",
                                    color = KineticLime,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = parsed.videoTitle,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Creator: ${parsed.creatorName}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "EXTRACTED EXERCISES (${state.editedExercises.size})",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    state.editedExercises.forEachIndexed { index, ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CarbonSlateLight)
                                .border(1.dp, CarbonSlateCard, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${index + 1}. ${ex.name}",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${ex.sets} sets × ${ex.repsMin}-${ex.repsMax} reps • ${ex.equipment.displayName} • ${ex.primarySubMuscle.displayName}",
                                    color = ForgeAmber,
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(onClick = { viewModel.removeExercise(ex.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove exercise",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ForgeButton(
                        text = "Confirm & Save Routine",
                        onClick = { viewModel.confirmAndSaveRoutine(onRoutineImported) },
                        variant = ForgeButtonVariant.KINETIC,
                        isFullWidth = true,
                        isLoading = state.isSaving
                    )
                }
            }
        }
    }
}

@Composable
private fun CuratedPresetCard(
    title: String,
    creator: String,
    tags: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonSlateLight)
            .border(1.dp, CarbonSlateCard, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CarbonSlateSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = ForgeAmber,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$creator • $tags",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "Import ⚡",
            color = ForgeAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
