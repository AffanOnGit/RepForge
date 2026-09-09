package com.repforge.feature.ingestion

import java.util.regex.Pattern

object YouTubeUrlHelper {

    private val VIDEO_ID_PATTERN = Pattern.compile(
        "^.*(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/|shorts\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*).*"
    )

    fun extractVideoId(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.length == 11 && !trimmed.contains("/")) {
            return trimmed
        }

        val matcher = VIDEO_ID_PATTERN.matcher(trimmed)
        return if (matcher.matches()) {
            val id = matcher.group(1)
            if (id?.length == 11) id else null
        } else {
            null
        }
    }
}
