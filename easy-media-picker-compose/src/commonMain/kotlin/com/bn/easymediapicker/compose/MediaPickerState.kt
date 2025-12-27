package com.bn.easymediapicker.compose

import com.bn.easymediapicker.core.MediaPicker
import com.bn.easymediapicker.core.MediaPickerConfig
import com.bn.easymediapicker.core.MediaResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State holder for media picker operations in Compose.
 * 
 * This class provides convenient methods to launch picker operations
 * and collect results, designed to work seamlessly with Compose.
 * 
 * @property picker The underlying [MediaPicker] implementation
 * @property scope The [CoroutineScope] for launching picker operations
 */
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MediaPickerState(
    internal val picker: MediaPicker,
    private val scope: CoroutineScope
) {
    /**
     * Whether a picker operation is currently in progress.
     */
    var isLoading: Boolean by mutableStateOf(false)
        private set

    /**
     * The last error that occurred during a picker operation.
     */
    var error: Throwable? by mutableStateOf(null)
        private set
    
    // ========== Image Operations ==========
    
    /**
     * Launches an image picker and calls [onResult] with the selected image.
     */
    fun pickImage(
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (MediaResult?) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val result = picker.pickImage(config)
                onResult(result)
            } catch (e: Exception) {
                error = e
                onResult(null)
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Launches an image picker for multiple selection.
     */
    fun pickImages(
        maxSelection: Int = 10,
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (List<MediaResult>) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val results = picker.pickImages(maxSelection, config)
                onResult(results)
            } catch (e: Exception) {
                error = e
                onResult(emptyList())
            } finally {
                isLoading = false
            }
        }
    }
    
    // ========== Video Operations ==========
    
    /**
     * Launches a video picker and calls [onResult] with the selected video.
     */
    fun pickVideo(
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (MediaResult?) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val result = picker.pickVideo(config)
                onResult(result)
            } catch (e: Exception) {
                error = e
                onResult(null)
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Launches a video picker for multiple selection.
     */
    fun pickVideos(
        maxSelection: Int = 10,
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (List<MediaResult>) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val results = picker.pickVideos(maxSelection, config)
                onResult(results)
            } catch (e: Exception) {
                error = e
                onResult(emptyList())
            } finally {
                isLoading = false
            }
        }
    }
    
    // ========== File Operations ==========
    
    /**
     * Launches a file picker and calls [onResult] with the selected file.
     */
    fun pickFile(
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (MediaResult?) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val result = picker.pickFile(config)
                onResult(result)
            } catch (e: Exception) {
                error = e
                onResult(null)
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Launches a file picker for multiple selection.
     */
    fun pickFiles(
        maxSelection: Int = 10,
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (List<MediaResult>) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val results = picker.pickFiles(maxSelection, config)
                onResult(results)
            } catch (e: Exception) {
                error = e
                onResult(emptyList())
            } finally {
                isLoading = false
            }
        }
    }
    
    // ========== Camera Operations ==========
    
    /**
     * Launches the camera to capture a photo.
     */
    fun captureImage(
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (MediaResult?) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val result = picker.captureImage(config)
                onResult(result)
            } catch (e: Exception) {
                error = e
                onResult(null)
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Launches the camera to record a video.
     */
    fun captureVideo(
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (MediaResult?) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val result = picker.captureVideo(config)
                onResult(result)
            } catch (e: Exception) {
                error = e
                onResult(null)
            } finally {
                isLoading = false
            }
        }
    }
    
    // ========== Media (Combined) Operations ==========
    
    /**
     * Launches a picker for images and videos.
     */
    fun pickMedia(
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (MediaResult?) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val result = picker.pickMedia(config)
                onResult(result)
            } catch (e: Exception) {
                error = e
                onResult(null)
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Launches a picker for multiple images and videos.
     */
    fun pickMultipleMedia(
        maxSelection: Int = 10,
        config: MediaPickerConfig = MediaPickerConfig.Default,
        onResult: (List<MediaResult>) -> Unit
    ) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val results = picker.pickMultipleMedia(maxSelection, config)
                onResult(results)
            } catch (e: Exception) {
                error = e
                onResult(emptyList())
            } finally {
                isLoading = false
            }
        }
    }
    
    // ========== Permissions ==========
    
    /**
     * Checks if required permissions are granted.
     */
    fun checkPermissions(onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                val granted = picker.hasPermissions()
                onResult(granted)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    
    /**
     * Requests required permissions.
     */
    fun requestPermissions(onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                val granted = picker.requestPermissions()
                onResult(granted)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}




