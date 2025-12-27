package com.bn.easymediapicker.core

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume

/**
 * Web implementation of [MediaPicker] using HTML5 File API.
 */
class WebMediaPicker : MediaPicker {
    
    override suspend fun pickImage(config: MediaPickerConfig): MediaResult? {
        return pickFile("image/*", false, config).firstOrNull()
    }
    
    override suspend fun pickImages(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickFile("image/*", true, config)
    }
    
    override suspend fun pickVideo(config: MediaPickerConfig): MediaResult? {
        return pickFile("video/*", false, config).firstOrNull()
    }
    
    override suspend fun pickVideos(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickFile("video/*", true, config)
    }
    
    override suspend fun pickFile(config: MediaPickerConfig): MediaResult? {
        val accept = if (config.allowedMimeTypes.isNotEmpty()) {
            config.allowedMimeTypes.joinToString(",")
        } else {
            "*/*"
        }
        return pickFile(accept, false, config).firstOrNull()
    }
    
    override suspend fun pickFiles(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        val accept = if (config.allowedMimeTypes.isNotEmpty()) {
            config.allowedMimeTypes.joinToString(",")
        } else {
            "*/*"
        }
        return pickFile(accept, true, config)
    }
    
    override suspend fun captureImage(config: MediaPickerConfig): MediaResult? {
        return pickFile("image/*", false, config, capture = true).firstOrNull()
    }
    
    override suspend fun captureVideo(config: MediaPickerConfig): MediaResult? {
        return pickFile("video/*", false, config, capture = true).firstOrNull()
    }
    
    override suspend fun pickMedia(config: MediaPickerConfig): MediaResult? {
        return pickFile("image/*,video/*", false, config).firstOrNull()
    }
    
    override suspend fun pickMultipleMedia(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickFile("image/*,video/*", true, config)
    }
    
    override suspend fun hasPermissions(): Boolean {
        // Web doesn't require explicit permissions for file picking
        return true
    }
    
    override suspend fun requestPermissions(): Boolean {
        // Web doesn't require explicit permissions for file picking
        return true
    }
    
    /**
     * Core file picking implementation using HTML5 input element.
     */
    private suspend fun pickFile(
        accept: String,
        multiple: Boolean,
        config: MediaPickerConfig,
        capture: Boolean = false
    ): List<MediaResult> = suspendCancellableCoroutine { continuation ->
        try {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = accept
            input.multiple = multiple
            
            if (capture) {
                input.setAttribute("capture", "environment")
            }
            
            input.onchange = {
                val files = input.files
                if (files != null && files.length > 0) {
                    val results = mutableListOf<MediaResult>()
                    var processedCount = 0
                    val totalFiles = files.length
                    
                    for (i in 0 until totalFiles) {
                        val file = files.item(i)
                        if (file != null) {
                            processFile(file, config) { result ->
                                result?.let { results.add(it) }
                                processedCount++
                                if (processedCount == totalFiles) {
                                    continuation.resume(results)
                                }
                            }
                        } else {
                            processedCount++
                            if (processedCount == totalFiles) {
                                continuation.resume(results)
                            }
                        }
                    }
                } else {
                    continuation.resume(emptyList())
                }
            }
            
            input.oncancel = {
                continuation.resume(emptyList())
            }
            
            continuation.invokeOnCancellation {
                input.onchange = null
                input.oncancel = null
            }
            
            input.click()
        } catch (e: Exception) {
            continuation.resume(emptyList())
        }
    }
    
    /**
     * Process a web File object into MediaResult.
     */
    private fun processFile(file: File, config: MediaPickerConfig, callback: (MediaResult?) -> Unit) {
        try {
            val type = when {
                file.type.startsWith("image/") -> MediaType.IMAGE
                file.type.startsWith("video/") -> MediaType.VIDEO
                file.type.startsWith("audio/") -> MediaType.AUDIO
                else -> MediaType.FILE
            }
            
            // Create object URL for the file
            val objectUrl = URL.createObjectURL(file)
            
            val result = MediaResult(
                uri = objectUrl,
                name = file.name,
                size = file.size.toLong(),
                mimeType = file.type,
                type = type,
                duration = null, // Duration extraction requires media element, skip for now
                platformData = file
            )
            
            callback(result)
        } catch (e: Exception) {
            console.error("Error processing file: ${e.message}")
            callback(null)
        }
    }
}
