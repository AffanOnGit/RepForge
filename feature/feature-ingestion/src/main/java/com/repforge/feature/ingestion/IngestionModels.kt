package com.repforge.feature.ingestion

import com.repforge.core.domain.model.Equipment
import com.repforge.core.domain.model.SubMuscle

sealed class IngestionError(val message: String, val recoveryAction: String) {
    data object InvalidYouTubeUrl : IngestionError(
        message = "Invalid YouTube link. Please provide a standard watch, share, or shorts link.",
        recoveryAction = "Check and paste the URL again"
    )

    data object NotAWorkoutVideo : IngestionError(
        message = "This doesn't appear to be a resistance training or workout video.",
        recoveryAction = "Try a dedicated fitness or hypertrophy video"
    )

    data class QuotaExceeded(val daysRemaining: Int = 3) : IngestionError(
        message = "You've used your 3 weekly import credits. Complete 3 workouts this week to earn +1 bonus credit!",
        recoveryAction = "Earn credits by logging workouts"
    )

    data object NetworkUnavailable : IngestionError(
        message = "No internet connection. YouTube ingestion requires connectivity.",
        recoveryAction = "Connect to Wi-Fi/data or build with Rapid Wizard offline"
    )

    data class ParsingFailed(val detail: String) : IngestionError(
        message = "AI parsing could not extract structured exercises: $detail",
        recoveryAction = "Retry or create manually"
    )
}

data class ParsedExerciseItem(
    val id: String,
    val name: String,
    val equipment: Equipment,
    val primarySubMuscle: SubMuscle,
    val sets: Int = 3,
    val repsMin: Int = 8,
    val repsMax: Int = 12,
    val restSeconds: Int = 90,
    val isSuperset: Boolean = false,
    val notes: String = ""
)

data class ParsedWorkoutData(
    val videoId: String,
    val videoTitle: String,
    val creatorName: String,
    val thumbnailUrl: String,
    val exercises: List<ParsedExerciseItem>,
    val isFromGlobalCache: Boolean = false
)

enum class IngestionStage {
    IDLE,
    CHECKING_GLOBAL_CACHE,
    ANALYZING_METADATA,
    GEMINI_PARSING,
    READY_FOR_REVIEW
}
