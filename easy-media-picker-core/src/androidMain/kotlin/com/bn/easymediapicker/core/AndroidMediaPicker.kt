package com.bn.easymediapicker.core

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android implementation of [MediaPicker].
 *
 * Uses the modern Activity Result API and PhotoPicker when available.
 * Falls back to traditional intents on older devices.
 * 
 * Note: Launchers must be registered via [registerLaunchers] before use.
 */
class AndroidMediaPicker(
    private val context: Context
) : MediaPicker {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Active continuation for the current operation
    private var activeContinuation: CancellableContinuation<*>? = null
    
    // Launchers
    private var pickVisualMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickMultipleVisualMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var startActivityLauncher: ActivityResultLauncher<Intent>? = null
    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null
    private var captureVideoLauncher: ActivityResultLauncher<Uri>? = null
    private var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>? = null
    
    // Captured URI for camera operations
    private var captureUri: Uri? = null
    
    // Current config for processing results
    private var currentConfig: MediaPickerConfig = MediaPickerConfig.Default

    fun registerLaunchers(
        pickVisualMedia: ActivityResultLauncher<PickVisualMediaRequest>,
        pickMultipleVisualMedia: ActivityResultLauncher<PickVisualMediaRequest>,
        startActivity: ActivityResultLauncher<Intent>,
        takePicture: ActivityResultLauncher<Uri>,
        captureVideo: ActivityResultLauncher<Uri>,
        requestPermissions: ActivityResultLauncher<Array<String>>
    ) {
        this.pickVisualMediaLauncher = pickVisualMedia
        this.pickMultipleVisualMediaLauncher = pickMultipleVisualMedia
        this.startActivityLauncher = startActivity
        this.takePictureLauncher = takePicture
        this.captureVideoLauncher = captureVideo
        this.requestPermissionsLauncher = requestPermissions
    }

    // ========== Result Handlers ==========

    fun onPickVisualMediaResult(uri: Uri?) {
        val continuation = activeContinuation as? CancellableContinuation<MediaResult?> ?: return
        activeContinuation = null
        
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val result = uriToMediaResult(uri, currentConfig)
                    continuation.resume(result)
                } catch (e: Exception) {
                    continuation.resumeWithException(MediaPickerException("Failed to process media", e))
                }
            }
        } else {
            continuation.resume(null)
        }
    }

    fun onPickMultipleVisualMediaResult(uris: List<Uri>) {
        val continuation = activeContinuation as? CancellableContinuation<List<MediaResult>> ?: return
        activeContinuation = null
        
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                try {
                    val results = uris.mapNotNull { uri ->
                        try {
                            uriToMediaResult(uri, currentConfig)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    continuation.resume(results)
                } catch (e: Exception) {
                    continuation.resumeWithException(MediaPickerException("Failed to process media", e))
                }
            }
        } else {
            continuation.resume(emptyList())
        }
    }

    fun onActivityResult(result: ActivityResult) {
        // Handle StartActivityForResult (Fallback & Files)
        // We need to differentiate between single and multiple selection based on activeContinuation type/context?
        // Or we check both types.
        
        // Try single result first
        val singleContinuation = activeContinuation as? CancellableContinuation<MediaResult?>
        if (singleContinuation != null) {
            activeContinuation = null
             val uri = result.data?.data
             if (result.resultCode == Activity.RESULT_OK && uri != null) {
                scope.launch(Dispatchers.IO) {
                    try {
                        val mediaResult = uriToMediaResult(uri, currentConfig)
                        singleContinuation.resume(mediaResult)
                    } catch (e: Exception) {
                        singleContinuation.resumeWithException(MediaPickerException("Failed to process result", e))
                    }
                }
            } else {
                singleContinuation.resume(null)
            }
            return
        }

        // Try multiple result
        val multipleContinuation = activeContinuation as? CancellableContinuation<List<MediaResult>>
        if (multipleContinuation != null) {
            activeContinuation = null
            if (result.resultCode == Activity.RESULT_OK) {
                val uris = mutableListOf<Uri>()
                result.data?.data?.let { uris.add(it) }
                result.data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                         clipData.getItemAt(i)?.uri?.let { uris.add(it) }
                    }
                }
                
                 if (uris.isNotEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        try {
                             val results = uris.mapNotNull { uri ->
                                try {
                                    uriToMediaResult(uri, currentConfig)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            // Apply max selection limit if needed? 
                            // Current impl relies on picker UI or just returns all.
                            multipleContinuation.resume(results)
                        } catch (e: Exception) {
                            multipleContinuation.resumeWithException(MediaPickerException("Failed to process results", e))
                        }
                    }
                } else {
                    multipleContinuation.resume(emptyList())
                }
            } else {
                multipleContinuation.resume(emptyList())
            }
            return
        }
    }

    fun onTakePictureResult(success: Boolean) {
        val continuation = activeContinuation as? CancellableContinuation<MediaResult?> ?: return
        activeContinuation = null
        
        if (success && captureUri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val result = uriToMediaResult(captureUri!!, currentConfig)
                    continuation.resume(result)
                } catch (e: Exception) {
                    continuation.resumeWithException(MediaPickerException("Failed to process captured image", e))
                }
            }
        } else {
            captureUri?.let { deleteUri(it) }
            continuation.resume(null)
        }
    }

    fun onCaptureVideoResult(success: Boolean) {
        val continuation = activeContinuation as? CancellableContinuation<MediaResult?> ?: return
        activeContinuation = null
        
        if (success && captureUri != null) {
             scope.launch(Dispatchers.IO) {
                try {
                    val result = uriToMediaResult(captureUri!!, currentConfig)
                    continuation.resume(result)
                } catch (e: Exception) {
                    continuation.resumeWithException(MediaPickerException("Failed to process captured video", e))
                }
            }
        } else {
            captureUri?.let { deleteUri(it) }
            continuation.resume(null)
        }
    }

    fun onRequestPermissionsResult(results: Map<String, Boolean>) {
        val continuation = activeContinuation as? CancellableContinuation<Boolean> ?: return
        activeContinuation = null
        val allGranted = results.values.all { it }
        continuation.resume(allGranted)
    }

    // ========== Image Operations ==========
    
    override suspend fun pickImage(config: MediaPickerConfig): MediaResult? {
        currentConfig = config
        return if (isPhotoPickerAvailable()) {
            suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                pickVisualMediaLauncher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        } else {
             suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                }
                startActivityLauncher?.launch(intent)
                     ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        }
    }
    
    override suspend fun pickImages(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        currentConfig = config
        return if (isPhotoPickerAvailable()) {
            suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                pickMultipleVisualMediaLauncher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                     ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        } else {
            suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                startActivityLauncher?.launch(intent)
                     ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        }
    }
    
    // ========== Video Operations ==========
    
    override suspend fun pickVideo(config: MediaPickerConfig): MediaResult? {
        currentConfig = config
        return if (isPhotoPickerAvailable()) {
            suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                pickVisualMediaLauncher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                     ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        } else {
            suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "video/*"
                }
                startActivityLauncher?.launch(intent)
                     ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        }
    }
    
    override suspend fun pickVideos(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        currentConfig = config
        return if (isPhotoPickerAvailable()) {
            suspendCancellableCoroutine { cont ->
                 activeContinuation = cont
                 pickMultipleVisualMediaLauncher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                     ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        } else {
             suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                startActivityLauncher?.launch(intent)
                     ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        }
    }
    
    // ========== File Operations ==========
    
    override suspend fun pickFile(config: MediaPickerConfig): MediaResult? {
        currentConfig = config
        return suspendCancellableCoroutine { cont ->
            activeContinuation = cont
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = if (config.allowedMimeTypes.isNotEmpty()) config.allowedMimeTypes.first() else "*/*"
                if (config.allowedMimeTypes.size > 1) {
                    putExtra(Intent.EXTRA_MIME_TYPES, config.allowedMimeTypes.toTypedArray())
                }
            }
            startActivityLauncher?.launch(intent)
                 ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
        }
    }
    
    override suspend fun pickFiles(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        currentConfig = config
        return suspendCancellableCoroutine { cont ->
            activeContinuation = cont
             val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = if (config.allowedMimeTypes.isNotEmpty()) config.allowedMimeTypes.first() else "*/*"
                if (config.allowedMimeTypes.size > 1) {
                    putExtra(Intent.EXTRA_MIME_TYPES, config.allowedMimeTypes.toTypedArray())
                }
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityLauncher?.launch(intent)
                 ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
        }
    }
    
    // ========== Camera Operations ==========
    
    override suspend fun captureImage(config: MediaPickerConfig): MediaResult? {
        currentConfig = config
        val uri = withContext(Dispatchers.IO) { createImageUri() }
            ?: throw MediaPickerException("Failed to create image URI")
        captureUri = uri
        
        return suspendCancellableCoroutine { cont ->
            activeContinuation = cont
            takePictureLauncher?.launch(uri)
                 ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
        }
    }
    
    override suspend fun captureVideo(config: MediaPickerConfig): MediaResult? {
        currentConfig = config
        val uri = withContext(Dispatchers.IO) { createVideoUri() }
            ?: throw MediaPickerException("Failed to create video URI")
        captureUri = uri
        
        return suspendCancellableCoroutine { cont ->
            activeContinuation = cont
            captureVideoLauncher?.launch(uri)
                 ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
        }
    }
    
    // ========== Media (Combined) Operations ==========
    
    override suspend fun pickMedia(config: MediaPickerConfig): MediaResult? {
        currentConfig = config
        return if (isPhotoPickerAvailable()) {
            suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                pickVisualMediaLauncher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                     ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        } else {
            // Fallback not perfectly supported for combined without custom logic, defaulting to image
            pickImage(config)
        }
    }
    
    override suspend fun pickMultipleMedia(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        currentConfig = config
        return if (isPhotoPickerAvailable()) {
            suspendCancellableCoroutine { cont ->
                activeContinuation = cont
                pickMultipleVisualMediaLauncher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                     ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
            }
        } else {
            pickImages(maxSelection, config)
        }
    }
    
    // ========== Permissions ==========
    
    override suspend fun hasPermissions(): Boolean {
        // Implementation remains the same
         return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    override suspend fun requestPermissions(): Boolean {
        return suspendCancellableCoroutine { cont ->
            activeContinuation = cont
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.CAMERA
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            }
            requestPermissionsLauncher?.launch(permissions)
                 ?: cont.resumeWithException(IllegalStateException("Launcher not registered"))
        }
    }
    
    // ========== Helper Functions ==========
    
    private fun isPhotoPickerAvailable(): Boolean {
        return ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)
    }
    
    private suspend fun uriToMediaResult(uri: Uri, config: MediaPickerConfig): MediaResult {
        return withContext(Dispatchers.IO) {
            val mimeType = context.contentResolver.getType(uri)
            val type = when {
                mimeType?.startsWith("image/") == true -> MediaType.IMAGE
                mimeType?.startsWith("video/") == true -> MediaType.VIDEO
                mimeType?.startsWith("audio/") == true -> MediaType.AUDIO
                else -> MediaType.FILE
            }
            
            var name: String? = null
            var size: Long? = null
            
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    
                    if (nameIndex >= 0) {
                        name = cursor.getString(nameIndex)
                    }
                    if (sizeIndex >= 0) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
            
            // Get duration for video files
            val duration = if (type == MediaType.VIDEO) {
                getVideoDuration(uri)
            } else null
            
            // Copy to cache if requested
            val finalUri = if (config.copyToCache) {
                copyToCache(uri, name ?: "file_${System.currentTimeMillis()}")?.toString() ?: uri.toString()
            } else {
                uri.toString()
            }
            
            MediaResult(
                uri = finalUri,
                name = name,
                size = size,
                mimeType = mimeType,
                type = type,
                duration = duration,
                platformData = uri  // Keep original Android Uri as platform data
            )
        }
    }
    
    private fun getVideoDuration(uri: Uri): Long? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLongOrNull()?.div(1000)  // Convert to seconds
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createImageUri(): Uri? {
        val filename = "camera_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        
        return context.contentResolver.insert(collection, contentValues)
    }
    
    private fun createVideoUri(): Uri? {
        val filename = "video_${System.currentTimeMillis()}.mp4"
        val contentValues = ContentValues().apply {
             put(MediaStore.Video.Media.DISPLAY_NAME, filename)
             put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        }
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        
        return context.contentResolver.insert(collection, contentValues)
    }
    
    private fun deleteUri(uri: Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            // Ignore deletion failures
        }
    }
    
    private fun copyToCache(uri: Uri, filename: String): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "media_picker")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val file = File(cacheDir, filename)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}
