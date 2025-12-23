package com.bn.easymediapicker.core

/**
 * Represents a media file result from picking operations.
 * This is a platform-agnostic representation that works across Android, iOS, and Desktop.
 *
 * @property uri The platform-specific URI/path as a string
 * @property name The display name of the file (if available)
 * @property size The file size in bytes (if available)
 * @property mimeType The MIME type of the file (if available)
 * @property type The type of media (image, video, file, etc.)
 * @property duration Duration in seconds for video/audio files (if applicable)
 * @property platformData Platform-specific data that can be cast on each platform
 */
data class MediaResult(
    val uri: String,
    val name: String? = null,
    val size: Long? = null,
    val mimeType: String? = null,
    val type: MediaType = MediaType.FILE,
    val duration: Long? = null,
    val platformData: Any? = null
) {
    /**
     * Returns a human-readable size string (e.g., "1.5 MB")
     */
    fun formattedSize(): String? {
        val bytes = size ?: return null
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${(bytes.toDouble() / 1024.0).roundToSingleDecimal()} KB"
            bytes < 1024 * 1024 * 1024 -> "${(bytes.toDouble() / (1024.0 * 1024)).roundToSingleDecimal()} MB"
            else -> "${(bytes.toDouble() / (1024.0 * 1024 * 1024)).roundToSingleDecimal()} GB"
        }
    }
    
    /**
     * Returns a human-readable duration string for video/audio (e.g., "1:30")
     */
    fun formattedDuration(): String? {
        val durationSec = duration ?: return null
        val minutes = durationSec / 60
        val seconds = durationSec % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

/**
 * Helper to round a Double to a single decimal place and format as string,
 * without relying on JVM-only String.format.
 */
private fun Double.roundToSingleDecimal(): String {
    val scaled = kotlin.math.round(this * 10.0) / 10.0
    val longPart = scaled.toLong()
    // If it's an integer like 2.0, show "2.0"; otherwise keep one decimal
    return if (scaled == longPart.toDouble()) {
        "${longPart}.0"
    } else {
        // Ensure exactly one decimal digit
        val sign = if (scaled < 0) "-" else ""
        val abs = kotlin.math.abs(scaled)
        val intPart = abs.toInt()
        val fracPart = ((abs - intPart) * 10).toInt()
        "$sign$intPart.$fracPart"
    }
}

/**
 * Represents the type of media content.
 */
enum class MediaType {
    /** Unknown or generic file */
    FILE,
    
    /** Image file (jpg, png, gif, etc.) */
    IMAGE,
    
    /** Video file (mp4, mov, etc.) */
    VIDEO,
    
    /** Audio file (mp3, wav, etc.) */
    AUDIO,
    
    /** Document file (pdf, doc, etc.) */
    DOCUMENT
}

