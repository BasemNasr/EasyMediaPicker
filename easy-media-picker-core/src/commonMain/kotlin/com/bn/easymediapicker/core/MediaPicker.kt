package com.bn.easymediapicker.core

/**
 * Platform-agnostic interface for media picking operations.
 * 
 * Each platform (Android, iOS, Desktop) provides its own implementation
 * that handles the native picker UI and permission management.
 * 
 * Usage:
 * ```kotlin
 * val picker = MediaPicker.create(context) // Platform-specific context
 * 
 * // Pick a single image
 * val image = picker.pickImage()
 * 
 * // Pick multiple images
 * val images = picker.pickImages(maxSelection = 5)
 * 
 * // Pick a video
 * val video = picker.pickVideo()
 * 
 * // Pick any file
 * val file = picker.pickFile()
 * 
 * // Capture from camera
 * val photo = picker.captureImage()
 * ```
 */
interface MediaPicker {
    
    // ========== Image Operations ==========
    
    /**
     * Opens an image picker to select a single image.
     * 
     * @param config Configuration options for the picker
     * @return The selected image as [MediaResult], or null if cancelled
     */
    suspend fun pickImage(config: MediaPickerConfig = MediaPickerConfig.Default): MediaResult?
    
    /**
     * Opens an image picker to select multiple images.
     * 
     * @param maxSelection Maximum number of images to select
     * @param config Configuration options for the picker
     * @return List of selected images as [MediaResult], empty if cancelled
     */
    suspend fun pickImages(
        maxSelection: Int = 10,
        config: MediaPickerConfig = MediaPickerConfig.Default
    ): List<MediaResult>
    
    // ========== Video Operations ==========
    
    /**
     * Opens a video picker to select a single video.
     * 
     * @param config Configuration options for the picker
     * @return The selected video as [MediaResult], or null if cancelled
     */
    suspend fun pickVideo(config: MediaPickerConfig = MediaPickerConfig.Default): MediaResult?
    
    /**
     * Opens a video picker to select multiple videos.
     * 
     * @param maxSelection Maximum number of videos to select
     * @param config Configuration options for the picker
     * @return List of selected videos as [MediaResult], empty if cancelled
     */
    suspend fun pickVideos(
        maxSelection: Int = 10,
        config: MediaPickerConfig = MediaPickerConfig.Default
    ): List<MediaResult>
    
    // ========== File Operations ==========
    
    /**
     * Opens a file picker to select a single file of any type.
     * 
     * @param config Configuration options for the picker
     * @return The selected file as [MediaResult], or null if cancelled
     */
    suspend fun pickFile(config: MediaPickerConfig = MediaPickerConfig.Default): MediaResult?
    
    /**
     * Opens a file picker to select multiple files.
     * 
     * @param maxSelection Maximum number of files to select
     * @param config Configuration options for the picker
     * @return List of selected files as [MediaResult], empty if cancelled
     */
    suspend fun pickFiles(
        maxSelection: Int = 10,
        config: MediaPickerConfig = MediaPickerConfig.Default
    ): List<MediaResult>
    
    // ========== Camera Operations ==========
    
    /**
     * Opens the camera to capture a photo.
     * 
     * @param config Configuration options (e.g., compression settings)
     * @return The captured photo as [MediaResult], or null if cancelled
     */
    suspend fun captureImage(config: MediaPickerConfig = MediaPickerConfig.Default): MediaResult?
    
    /**
     * Opens the camera to record a video.
     * 
     * @param config Configuration options
     * @return The recorded video as [MediaResult], or null if cancelled
     */
    suspend fun captureVideo(config: MediaPickerConfig = MediaPickerConfig.Default): MediaResult?
    
    // ========== Media (Combined) Operations ==========
    
    /**
     * Opens a picker that allows selecting either images or videos.
     * 
     * @param config Configuration options for the picker
     * @return The selected media as [MediaResult], or null if cancelled
     */
    suspend fun pickMedia(config: MediaPickerConfig = MediaPickerConfig.Default): MediaResult?
    
    /**
     * Opens a picker that allows selecting multiple images and videos.
     * 
     * @param maxSelection Maximum number of items to select
     * @param config Configuration options for the picker
     * @return List of selected media as [MediaResult], empty if cancelled
     */
    suspend fun pickMultipleMedia(
        maxSelection: Int = 10,
        config: MediaPickerConfig = MediaPickerConfig.Default
    ): List<MediaResult>
    
    // ========== Permissions ==========
    
    /**
     * Checks if the app has the required permissions for media picking.
     * 
     * @return true if all required permissions are granted
     */
    suspend fun hasPermissions(): Boolean
    
    /**
     * Requests the required permissions for media picking.
     * 
     * @return true if permissions were granted
     */
    suspend fun requestPermissions(): Boolean
}

/**
 * Exception thrown when media picking fails.
 */
class MediaPickerException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when required permissions are not granted.
 */
class PermissionDeniedException(
    message: String = "Required permissions were not granted"
) : Exception(message)

