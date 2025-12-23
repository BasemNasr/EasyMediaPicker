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
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
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

