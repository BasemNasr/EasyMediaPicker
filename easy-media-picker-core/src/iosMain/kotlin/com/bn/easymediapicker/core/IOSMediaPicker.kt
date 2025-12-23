package com.bn.easymediapicker.core

import kotlinx.coroutines.*
import platform.Foundation.*
import platform.Photos.*
import platform.PhotosUI.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * iOS implementation of [MediaPicker].
 * 
 * Uses PHPickerViewController for images/videos (iOS 14+) and
 * UIDocumentPickerViewController for files.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
class IOSMediaPicker(
    private val viewController: UIViewController
) : MediaPicker {
    
    // ========== Image Operations ==========
    
    override suspend fun pickImage(config: MediaPickerConfig): MediaResult? {
        val results = pickVisualMedia(
            filterType = "image",
            selectionLimit = 1,
            config = config
        )
        return results.firstOrNull()
    }
    
    override suspend fun pickImages(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickVisualMedia(
            filterType = "image",
            selectionLimit = maxSelection,
            config = config
        )
    }
    
    // ========== Video Operations ==========
    
    override suspend fun pickVideo(config: MediaPickerConfig): MediaResult? {
        val results = pickVisualMedia(
            filterType = "video",
            selectionLimit = 1,
            config = config
        )
        return results.firstOrNull()
    }
    
    override suspend fun pickVideos(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickVisualMedia(
            filterType = "video",
            selectionLimit = maxSelection,
            config = config
        )
    }
    
    // ========== File Operations ==========
    
    override suspend fun pickFile(config: MediaPickerConfig): MediaResult? {
        val results = pickDocuments(
            selectionLimit = 1,
            config = config
        )
        return results.firstOrNull()
    }
    
    override suspend fun pickFiles(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickDocuments(
            selectionLimit = maxSelection,
            config = config
        )
    }
    
    // ========== Camera Operations ==========
    
    override suspend fun captureImage(config: MediaPickerConfig): MediaResult? {
        return captureMedia(
            isVideo = false,
            config = config
        )
    }
    
    override suspend fun captureVideo(config: MediaPickerConfig): MediaResult? {
        return captureMedia(
            isVideo = true,
            config = config
        )
    }
    
    // ========== Media (Combined) Operations ==========
    
    override suspend fun pickMedia(config: MediaPickerConfig): MediaResult? {
        val results = pickVisualMedia(
            filterType = "all",
            selectionLimit = 1,
            config = config
        )
        return results.firstOrNull()
    }
    
    override suspend fun pickMultipleMedia(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickVisualMedia(
            filterType = "all",
            selectionLimit = maxSelection,
            config = config
        )
    }
    
    // ========== Permissions ==========
    
    override suspend fun hasPermissions(): Boolean {
        val status = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelReadWrite)
        return status == PHAuthorizationStatusAuthorized || 
               status == PHAuthorizationStatusLimited
    }
    
    override suspend fun requestPermissions(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelReadWrite) { status ->
                val granted = status == PHAuthorizationStatusAuthorized || 
                              status == PHAuthorizationStatusLimited
                continuation.resume(granted)
            }
        }
    }
    
    // ========== Private Implementation ==========
    
    private suspend fun pickVisualMedia(
        filterType: String,
        selectionLimit: Int,
        config: MediaPickerConfig
    ): List<MediaResult> = suspendCancellableCoroutine { continuation ->
        val configuration = PHPickerConfiguration(PHPhotoLibrary.sharedPhotoLibrary())
        configuration.selectionLimit = selectionLimit.toLong()
        
        // Set filter based on type
        configuration.filter = when (filterType) {
            "image" -> PHPickerFilter.imagesFilter
            "video" -> PHPickerFilter.videosFilter
            else -> PHPickerFilter.anyFilterMatchingSubfilters(
                listOf(PHPickerFilter.imagesFilter, PHPickerFilter.videosFilter)
            )
        }
        
        val picker = PHPickerViewController(configuration)
        
        val delegate = PHPickerDelegate { results ->
            picker.dismissViewControllerAnimated(true, null)
            
            if (results.isEmpty()) {
                continuation.resume(emptyList())
                return@PHPickerDelegate
            }
            
            // Process results on background thread
            CoroutineScope(Dispatchers.Default).launch {
                val mediaResults = mutableListOf<MediaResult>()
                
                results.forEach { result ->
                    val provider = result.itemProvider
                    
                    // Determine type and load accordingly
                    val mediaResult = when {
                        provider.hasItemConformingToTypeIdentifier(UTTypeImage.identifier) -> {
                            loadFileFromProvider(provider, UTTypeImage.identifier, MediaType.IMAGE, config)
                        }
                        provider.hasItemConformingToTypeIdentifier(UTTypeMovie.identifier) -> {
                            loadFileFromProvider(provider, UTTypeMovie.identifier, MediaType.VIDEO, config)
                        }
                        else -> null
                    }
                    
                    mediaResult?.let { mediaResults.add(it) }
                }
                
                withContext(Dispatchers.Main) {
                    continuation.resume(mediaResults)
                }
            }
        }
        
        picker.delegate = delegate
        
        viewController.presentViewController(picker, animated = true, completion = null)
        
        continuation.invokeOnCancellation {
            picker.dismissViewControllerAnimated(true, null)
        }
    }
    
    private suspend fun loadFileFromProvider(
        provider: NSItemProvider,
        typeIdentifier: String,
        type: MediaType,
        config: MediaPickerConfig
    ): MediaResult? = suspendCancellableCoroutine { continuation ->
        provider.loadFileRepresentationForTypeIdentifier(typeIdentifier) { url, error ->
            if (error != null || url == null) {
                continuation.resume(null)
                return@loadFileRepresentationForTypeIdentifier
            }
            
            // Copy to cache directory since the URL is temporary
            val result = copyToCache(url, type, config)
            continuation.resume(result)
        }
    }
    
    private suspend fun pickDocuments(
        selectionLimit: Int,
        config: MediaPickerConfig
    ): List<MediaResult> = suspendCancellableCoroutine { continuation ->
        val contentTypes = listOf(UTTypeItem)
        
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = contentTypes,
            asCopy = true
        )
        picker.allowsMultipleSelection = selectionLimit > 1
        
        val delegate = DocumentPickerDelegate(
            onPicked = { urls ->
                val results = urls.mapNotNull { url ->
                    urlToMediaResult(url, MediaType.FILE, config)
                }
                continuation.resume(results)
            },
            onCancelled = {
                continuation.resume(emptyList())
            }
        )
        
        picker.delegate = delegate
        
        viewController.presentViewController(picker, animated = true, completion = null)
        
        continuation.invokeOnCancellation {
            picker.dismissViewControllerAnimated(true, null)
        }
    }
    
    private suspend fun captureMedia(
        isVideo: Boolean,
        config: MediaPickerConfig
    ): MediaResult? {
        if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceTypeCamera)) {
            throw MediaPickerException("Camera is not available on this device")
        }
        
        return suspendCancellableCoroutine { continuation ->
            val picker = UIImagePickerController()
            picker.sourceType = UIImagePickerControllerSourceTypeCamera
            
            if (isVideo) {
                picker.mediaTypes = listOf("public.movie")
            } else {
                picker.mediaTypes = listOf("public.image")
            }
            
            val delegate = ImagePickerDelegate(
                onImagePicked = { info ->
                    picker.dismissViewControllerAnimated(true, null)
                    
                    // Handle video URL
                    val mediaUrl = info[UIImagePickerControllerMediaURL] as? NSURL
                    if (mediaUrl != null && isVideo) {
                        val result = copyToCache(mediaUrl, MediaType.VIDEO, config)
                        continuation.resume(result)
                        return@ImagePickerDelegate
                    }
                    
                    // Handle image
                    val image = info[UIImagePickerControllerOriginalImage] as? UIImage
                    if (image != null && !isVideo) {
                        val result = saveImageToCache(image, config)
                        continuation.resume(result)
                        return@ImagePickerDelegate
                    }
                    
                    continuation.resume(null)
                },
                onCancelled = {
                    picker.dismissViewControllerAnimated(true, null)
                    continuation.resume(null)
                }
            )
            
            picker.delegate = delegate
            
            viewController.presentViewController(picker, animated = true, completion = null)
            
            continuation.invokeOnCancellation {
                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }
    
    // ========== Helper Functions ==========
    
    private fun copyToCache(
        sourceUrl: NSURL,
        type: MediaType,
        config: MediaPickerConfig
    ): MediaResult? {
        val fileManager = NSFileManager.defaultManager
        val cacheUrls = fileManager.URLsForDirectory(
            NSCachesDirectory,
            NSUserDomainMask
        )
        
        val cacheDir = cacheUrls.firstOrNull() as? NSURL ?: return null
        val mediaPickerDir = cacheDir.URLByAppendingPathComponent("media_picker") ?: return null
        
        // Create directory if needed
        mediaPickerDir.path?.let { path ->
            if (!fileManager.fileExistsAtPath(path)) {
                fileManager.createDirectoryAtPath(
                    path,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }
        }
        
        val filename = sourceUrl.lastPathComponent ?: "file_${currentTimeMillis()}"
        val destUrl = mediaPickerDir.URLByAppendingPathComponent(filename) ?: return null
        
        // Remove existing file
        destUrl.path?.let { path ->
            if (fileManager.fileExistsAtPath(path)) {
                fileManager.removeItemAtPath(path, null)
            }
        }
        
        // Copy file
        val success = fileManager.copyItemAtURL(sourceUrl, destUrl, null)
        if (!success) return null
        
        return urlToMediaResult(destUrl, type, config)
    }
    
    private fun urlToMediaResult(
        url: NSURL,
        type: MediaType,
        config: MediaPickerConfig
    ): MediaResult? {
        val path = url.path ?: return null
        val filename = url.lastPathComponent
        
        // Get file size
        val fileManager = NSFileManager.defaultManager
        val attributes = fileManager.attributesOfItemAtPath(path, null)
        val size = (attributes?.get(NSFileSize) as? NSNumber)?.longValue
        
        // Determine mime type
        val mimeType = when (type) {
            MediaType.IMAGE -> "image/jpeg"
            MediaType.VIDEO -> "video/mp4"
            MediaType.AUDIO -> "audio/mpeg"
            else -> "application/octet-stream"
        }
        
        return MediaResult(
            uri = url.absoluteString ?: path,
            name = filename,
            size = size,
            mimeType = mimeType,
            type = type,
            duration = null, // TODO: Implement using AVFoundation
            platformData = url
        )
    }
    
    private fun saveImageToCache(
        image: UIImage,
        config: MediaPickerConfig
    ): MediaResult? {
        val fileManager = NSFileManager.defaultManager
        val cacheUrls = fileManager.URLsForDirectory(
            NSCachesDirectory,
            NSUserDomainMask
        )
        
        val cacheDir = cacheUrls.firstOrNull() as? NSURL ?: return null
        val mediaPickerDir = cacheDir.URLByAppendingPathComponent("media_picker") ?: return null
        
        // Create directory if needed
        mediaPickerDir.path?.let { path ->
            if (!fileManager.fileExistsAtPath(path)) {
                fileManager.createDirectoryAtPath(
                    path,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }
        }
        
        val filename = "camera_${currentTimeMillis()}.jpg"
        val fileUrl = mediaPickerDir.URLByAppendingPathComponent(filename) ?: return null
        
        // Save image with quality
        val quality = config.imageQuality / 100.0
        val imageData = UIImageJPEGRepresentation(image, quality) ?: return null
        
        val path = fileUrl.path ?: return null
        val success = imageData.writeToFile(path, atomically = true)
        if (!success) return null
        
        return urlToMediaResult(fileUrl, MediaType.IMAGE, config)
    }
    
    private fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}

// ========== Delegate Classes ==========

private class PHPickerDelegate(
    private val onResults: (List<PHPickerResult>) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        @Suppress("UNCHECKED_CAST")
        val results = didFinishPicking as? List<PHPickerResult> ?: emptyList()
        onResults(results)
    }
}

private class DocumentPickerDelegate(
    private val onPicked: (List<NSURL>) -> Unit,
    private val onCancelled: () -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        @Suppress("UNCHECKED_CAST")
        val urls = didPickDocumentsAtURLs as? List<NSURL> ?: emptyList()
        onPicked(urls)
    }
    
    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onCancelled()
    }
}

private class ImagePickerDelegate(
    private val onImagePicked: (Map<Any?, *>) -> Unit,
    private val onCancelled: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        onImagePicked(didFinishPickingMediaWithInfo)
    }
    
    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onCancelled()
    }
}
