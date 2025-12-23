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
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
 */
class AndroidMediaPicker private constructor(
    private val context: Context,
    private val activityResultRegistry: androidx.activity.result.ActivityResultRegistry,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner
) : MediaPicker {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    companion object {
        private const val KEY_PICK_IMAGE = "pick_image"
        private const val KEY_PICK_IMAGES = "pick_images"
        private const val KEY_PICK_VIDEO = "pick_video"
        private const val KEY_PICK_VIDEOS = "pick_videos"
        private const val KEY_PICK_FILE = "pick_file"
        private const val KEY_PICK_FILES = "pick_files"
        private const val KEY_CAPTURE_IMAGE = "capture_image"
        private const val KEY_CAPTURE_VIDEO = "capture_video"
        private const val KEY_PICK_MEDIA = "pick_media"
        private const val KEY_PICK_MULTIPLE_MEDIA = "pick_multiple_media"
        private const val KEY_REQUEST_PERMISSIONS = "request_permissions"
        
        /**
         * Create from a ComponentActivity.
         */
        fun create(activity: ComponentActivity): AndroidMediaPicker {
            return AndroidMediaPicker(
                context = activity.applicationContext,
                activityResultRegistry = activity.activityResultRegistry,
                lifecycleOwner = activity
            )
        }
        
        /**
         * Create from a Fragment.
         */
        fun create(fragment: Fragment): AndroidMediaPicker {
            return AndroidMediaPicker(
                context = fragment.requireContext().applicationContext,
                activityResultRegistry = fragment.requireActivity().activityResultRegistry,
                lifecycleOwner = fragment.viewLifecycleOwner
            )
        }
    }
    
    // ========== Image Operations ==========
    
    override suspend fun pickImage(config: MediaPickerConfig): MediaResult? {
        return if (isPhotoPickerAvailable()) {
            pickImageWithPhotoPicker(config)
        } else {
            pickImageWithIntent(config)
        }
    }
    
    private suspend fun pickImageWithPhotoPicker(config: MediaPickerConfig): MediaResult? {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_IMAGE + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val result = uriToMediaResult(uri, config)
                            continuation.resume(result)
                        } catch (e: Exception) {
                            continuation.resumeWithException(MediaPickerException("Failed to process image", e))
                        }
                    }
                } else {
                    continuation.resume(null)
                }
            }
            
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    private suspend fun pickImageWithIntent(config: MediaPickerConfig): MediaResult? {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_IMAGE + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val uri = result.data?.data
                if (result.resultCode == Activity.RESULT_OK && uri != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val mediaResult = uriToMediaResult(uri, config)
                            continuation.resume(mediaResult)
                        } catch (e: Exception) {
                            continuation.resumeWithException(MediaPickerException("Failed to process image", e))
                        }
                    }
                } else {
                    continuation.resume(null)
                }
            }
            
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            launcher.launch(intent)
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    override suspend fun pickImages(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return if (isPhotoPickerAvailable()) {
            pickImagesWithPhotoPicker(maxSelection, config)
        } else {
            pickImagesWithIntent(maxSelection, config)
        }
    }
    
    private suspend fun pickImagesWithPhotoPicker(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_IMAGES + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.PickMultipleVisualMedia(maxSelection)
            ) { uris ->
                if (uris.isNotEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val results = uris.mapNotNull { uri ->
                                try {
                                    uriToMediaResult(uri, config)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            continuation.resume(results)
                        } catch (e: Exception) {
                            continuation.resumeWithException(MediaPickerException("Failed to process images", e))
                        }
                    }
                } else {
                    continuation.resume(emptyList())
                }
            }
            
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    private suspend fun pickImagesWithIntent(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_IMAGES + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uris = mutableListOf<Uri>()
                    
                    // Check for single selection
                    result.data?.data?.let { uris.add(it) }
                    
                    // Check for multiple selection
                    result.data?.clipData?.let { clipData ->
                        for (i in 0 until minOf(clipData.itemCount, maxSelection)) {
                            clipData.getItemAt(i)?.uri?.let { uris.add(it) }
                        }
                    }
                    
                    if (uris.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val results = uris.mapNotNull { uri ->
                                    try {
                                        uriToMediaResult(uri, config)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                continuation.resume(results)
                            } catch (e: Exception) {
                                continuation.resumeWithException(MediaPickerException("Failed to process images", e))
                            }
                        }
                    } else {
                        continuation.resume(emptyList())
                    }
                } else {
                    continuation.resume(emptyList())
                }
            }
            
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            launcher.launch(intent)
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    // ========== Video Operations ==========
    
    override suspend fun pickVideo(config: MediaPickerConfig): MediaResult? {
        return if (isPhotoPickerAvailable()) {
            pickVideoWithPhotoPicker(config)
        } else {
            pickVideoWithIntent(config)
        }
    }
    
    private suspend fun pickVideoWithPhotoPicker(config: MediaPickerConfig): MediaResult? {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_VIDEO + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val result = uriToMediaResult(uri, config)
                            continuation.resume(result)
                        } catch (e: Exception) {
                            continuation.resumeWithException(MediaPickerException("Failed to process video", e))
                        }
                    }
                } else {
                    continuation.resume(null)
                }
            }
            
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    private suspend fun pickVideoWithIntent(config: MediaPickerConfig): MediaResult? {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_VIDEO + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val uri = result.data?.data
                if (result.resultCode == Activity.RESULT_OK && uri != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val mediaResult = uriToMediaResult(uri, config)
                            continuation.resume(mediaResult)
                        } catch (e: Exception) {
                            continuation.resumeWithException(MediaPickerException("Failed to process video", e))
                        }
                    }
                } else {
                    continuation.resume(null)
                }
            }
            
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
            }
            launcher.launch(intent)
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    override suspend fun pickVideos(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return if (isPhotoPickerAvailable()) {
            pickVideosWithPhotoPicker(maxSelection, config)
        } else {
            pickVideosWithIntent(maxSelection, config)
        }
    }
    
    private suspend fun pickVideosWithPhotoPicker(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_VIDEOS + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.PickMultipleVisualMedia(maxSelection)
            ) { uris ->
                if (uris.isNotEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val results = uris.mapNotNull { uri ->
                                try {
                                    uriToMediaResult(uri, config)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            continuation.resume(results)
                        } catch (e: Exception) {
                            continuation.resumeWithException(MediaPickerException("Failed to process videos", e))
                        }
                    }
                } else {
                    continuation.resume(emptyList())
                }
            }
            
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    private suspend fun pickVideosWithIntent(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_VIDEOS + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uris = mutableListOf<Uri>()
                    
                    result.data?.data?.let { uris.add(it) }
                    result.data?.clipData?.let { clipData ->
                        for (i in 0 until minOf(clipData.itemCount, maxSelection)) {
                            clipData.getItemAt(i)?.uri?.let { uris.add(it) }
                        }
                    }
                    
                    if (uris.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val results = uris.mapNotNull { uri ->
                                    try {
                                        uriToMediaResult(uri, config)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                continuation.resume(results)
                            } catch (e: Exception) {
                                continuation.resumeWithException(MediaPickerException("Failed to process videos", e))
                            }
                        }
                    } else {
                        continuation.resume(emptyList())
                    }
                } else {
                    continuation.resume(emptyList())
                }
            }
            
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            launcher.launch(intent)
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    // ========== File Operations ==========
    
    override suspend fun pickFile(config: MediaPickerConfig): MediaResult? {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_FILE + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val uri = result.data?.data
                if (result.resultCode == Activity.RESULT_OK && uri != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val mediaResult = uriToMediaResult(uri, config)
                            continuation.resume(mediaResult)
                        } catch (e: Exception) {
                            continuation.resumeWithException(MediaPickerException("Failed to process file", e))
                        }
                    }
                } else {
                    continuation.resume(null)
                }
            }
            
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = if (config.allowedMimeTypes.isNotEmpty()) {
                    config.allowedMimeTypes.first()
                } else {
                    "*/*"
                }
                if (config.allowedMimeTypes.size > 1) {
                    putExtra(Intent.EXTRA_MIME_TYPES, config.allowedMimeTypes.toTypedArray())
                }
            }
            launcher.launch(intent)
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    override suspend fun pickFiles(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_PICK_FILES + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uris = mutableListOf<Uri>()
                    
                    result.data?.data?.let { uris.add(it) }
                    result.data?.clipData?.let { clipData ->
                        for (i in 0 until minOf(clipData.itemCount, maxSelection)) {
                            clipData.getItemAt(i)?.uri?.let { uris.add(it) }
                        }
                    }
                    
                    if (uris.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val results = uris.mapNotNull { uri ->
                                    try {
                                        uriToMediaResult(uri, config)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                continuation.resume(results)
                            } catch (e: Exception) {
                                continuation.resumeWithException(MediaPickerException("Failed to process files", e))
                            }
                        }
                    } else {
                        continuation.resume(emptyList())
                    }
                } else {
                    continuation.resume(emptyList())
                }
            }
            
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = if (config.allowedMimeTypes.isNotEmpty()) {
                    config.allowedMimeTypes.first()
                } else {
                    "*/*"
                }
                if (config.allowedMimeTypes.size > 1) {
                    putExtra(Intent.EXTRA_MIME_TYPES, config.allowedMimeTypes.toTypedArray())
                }
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            launcher.launch(intent)
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    // ========== Camera Operations ==========
    
    private var captureUri: Uri? = null
    
    override suspend fun captureImage(config: MediaPickerConfig): MediaResult? {
        val uri = withContext(Dispatchers.IO) { createImageUri() }
            ?: throw MediaPickerException("Failed to create image URI")
        
        captureUri = uri
        
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_CAPTURE_IMAGE + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.TakePicture()
            ) { success ->
                if (success && captureUri != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val result = uriToMediaResult(captureUri!!, config)
                            continuation.resume(result)
                        } catch (e: Exception) {
                            continuation.resumeWithException(MediaPickerException("Failed to process captured image", e))
                        }
                    }
                } else {
                    // Clean up the empty file
                    captureUri?.let { deleteUri(it) }
                    continuation.resume(null)
                }
            }
            
            launcher.launch(uri)
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    override suspend fun captureVideo(config: MediaPickerConfig): MediaResult? {
        val uri = withContext(Dispatchers.IO) { createVideoUri() }
            ?: throw MediaPickerException("Failed to create video URI")
        
        captureUri = uri
        
        return suspendCancellableCoroutine { continuation ->
            val launcher = activityResultRegistry.register(
                KEY_CAPTURE_VIDEO + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.CaptureVideo()
            ) { success ->
                if (success && captureUri != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val result = uriToMediaResult(captureUri!!, config)
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
            
            launcher.launch(uri)
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    // ========== Media (Combined) Operations ==========
    
    override suspend fun pickMedia(config: MediaPickerConfig): MediaResult? {
        return if (isPhotoPickerAvailable()) {
            suspendCancellableCoroutine { continuation ->
                val launcher = activityResultRegistry.register(
                    KEY_PICK_MEDIA + System.currentTimeMillis(),
                    lifecycleOwner,
                    ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val result = uriToMediaResult(uri, config)
                                continuation.resume(result)
                            } catch (e: Exception) {
                                continuation.resumeWithException(MediaPickerException("Failed to process media", e))
                            }
                        }
                    } else {
                        continuation.resume(null)
                    }
                }
                
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                
                continuation.invokeOnCancellation {
                    launcher.unregister()
                }
            }
        } else {
            // Fall back to intent-based picker
            pickImage(config) ?: pickVideo(config)
        }
    }
    
    override suspend fun pickMultipleMedia(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return if (isPhotoPickerAvailable()) {
            suspendCancellableCoroutine { continuation ->
                val launcher = activityResultRegistry.register(
                    KEY_PICK_MULTIPLE_MEDIA + System.currentTimeMillis(),
                    lifecycleOwner,
                    ActivityResultContracts.PickMultipleVisualMedia(maxSelection)
                ) { uris ->
                    if (uris.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val results = uris.mapNotNull { uri ->
                                    try {
                                        uriToMediaResult(uri, config)
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
                
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                
                continuation.invokeOnCancellation {
                    launcher.unregister()
                }
            }
        } else {
            pickImages(maxSelection, config)
        }
    }
    
    // ========== Permissions ==========
    
    override suspend fun hasPermissions(): Boolean {
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
        return suspendCancellableCoroutine { continuation ->
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
            
            val launcher = activityResultRegistry.register(
                KEY_REQUEST_PERMISSIONS + System.currentTimeMillis(),
                lifecycleOwner,
                ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                val allGranted = results.values.all { it }
                continuation.resume(allGranted)
            }
            
            launcher.launch(permissions)
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
    
    // ========== Helper Functions ==========
    
    private fun isPhotoPickerAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
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
