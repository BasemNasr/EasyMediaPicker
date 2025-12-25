package com.bn.easymediapicker.core

/**
 * Configuration options for media picking operations.
 */
data class MediaPickerConfig(
    /**
     * Maximum number of items that can be selected (for multi-selection)
     */
    val maxSelection: Int = 1,
    
    /**
     * Allowed MIME types for file picking.
     * Empty list means all types are allowed.
     */
    val allowedMimeTypes: List<String> = emptyList(),
    
    /**
     * Whether to copy selected files to app's cache directory.
     * Recommended for iOS and when you need persistent access.
     */
    val copyToCache: Boolean = false,
    
    /**
     * Quality for image compression (0-100).
     * Only applies when compressImages is true.
     */
    val imageQuality: Int = 80,
    
    /**
     * Whether to compress images after selection.
     */
    val compressImages: Boolean = false,
    
    /**
     * Maximum image dimension (width or height) when compressing.
     * Images larger than this will be resized proportionally.
     */
    val maxImageDimension: Int = 1920
) {
    companion object {
        val Default = MediaPickerConfig()
        
        /**
         * Configuration optimized for picking images
         */
        val Images = MediaPickerConfig(
            allowedMimeTypes = listOf("image/*")
        )
        
        /**
         * Configuration optimized for picking videos
         */
        val Videos = MediaPickerConfig(
            allowedMimeTypes = listOf("video/*")
        )
        
        /**
         * Configuration optimized for picking documents
         */
        val Documents = MediaPickerConfig(
            allowedMimeTypes = listOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain"
            )
        )
    }
}




