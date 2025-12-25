package com.bn.easymediapicker.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Window
import java.io.File
import java.nio.file.Files
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop (JVM) implementation of [MediaPicker].
 * 
 * Uses AWT FileDialog for native look-and-feel on macOS,
 * and JFileChooser as fallback on other platforms.
 */
class DesktopMediaPicker(
    private val parentWindow: Window? = null
) : MediaPicker {
    
    private val isMacOS: Boolean
        get() = System.getProperty("os.name").lowercase().contains("mac")
    
    // ========== Image Operations ==========
    
    override suspend fun pickImage(config: MediaPickerConfig): MediaResult? {
        val results = pickFiles(
            title = "Select Image",
            extensions = IMAGE_EXTENSIONS,
            multiSelect = false,
            config = config
        )
        return results.firstOrNull()
    }
    
    override suspend fun pickImages(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickFiles(
            title = "Select Images",
            extensions = IMAGE_EXTENSIONS,
            multiSelect = true,
            config = config
        ).take(maxSelection)
    }
    
    // ========== Video Operations ==========
    
    override suspend fun pickVideo(config: MediaPickerConfig): MediaResult? {
        val results = pickFiles(
            title = "Select Video",
            extensions = VIDEO_EXTENSIONS,
            multiSelect = false,
            config = config
        )
        return results.firstOrNull()
    }
    
    override suspend fun pickVideos(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickFiles(
            title = "Select Videos",
            extensions = VIDEO_EXTENSIONS,
            multiSelect = true,
            config = config
        ).take(maxSelection)
    }
    
    // ========== File Operations ==========
    
    override suspend fun pickFile(config: MediaPickerConfig): MediaResult? {
        val extensions = mimeTypesToExtensions(config.allowedMimeTypes)
        val results = pickFiles(
            title = "Select File",
            extensions = extensions,
            multiSelect = false,
            config = config
        )
        return results.firstOrNull()
    }
    
    override suspend fun pickFiles(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        val extensions = mimeTypesToExtensions(config.allowedMimeTypes)
        return pickFiles(
            title = "Select Files",
            extensions = extensions,
            multiSelect = true,
            config = config
        ).take(maxSelection)
    }
    
    // ========== Camera Operations ==========
    
    override suspend fun captureImage(config: MediaPickerConfig): MediaResult? {
        // Camera capture is not supported on desktop
        throw MediaPickerException("Camera capture is not supported on desktop. Use pickImage() instead.")
    }
    
    override suspend fun captureVideo(config: MediaPickerConfig): MediaResult? {
        // Camera capture is not supported on desktop
        throw MediaPickerException("Video capture is not supported on desktop. Use pickVideo() instead.")
    }
    
    // ========== Media (Combined) Operations ==========
    
    override suspend fun pickMedia(config: MediaPickerConfig): MediaResult? {
        val results = pickFiles(
            title = "Select Media",
            extensions = IMAGE_EXTENSIONS + VIDEO_EXTENSIONS,
            multiSelect = false,
            config = config
        )
        return results.firstOrNull()
    }
    
    override suspend fun pickMultipleMedia(maxSelection: Int, config: MediaPickerConfig): List<MediaResult> {
        return pickFiles(
            title = "Select Media",
            extensions = IMAGE_EXTENSIONS + VIDEO_EXTENSIONS,
            multiSelect = true,
            config = config
        ).take(maxSelection)
    }
    
    // ========== Permissions ==========
    
    override suspend fun hasPermissions(): Boolean {
        // Desktop doesn't require runtime permissions
        return true
    }
    
    override suspend fun requestPermissions(): Boolean {
        // Desktop doesn't require runtime permissions
        return true
    }
    
    // ========== Private Implementation ==========
    
    private suspend fun pickFiles(
        title: String,
        extensions: List<String>,
        multiSelect: Boolean,
        config: MediaPickerConfig
    ): List<MediaResult> = withContext(Dispatchers.IO) {
        val files = if (isMacOS) {
            pickWithFileDialog(title, extensions, multiSelect)
        } else {
            pickWithJFileChooser(title, extensions, multiSelect)
        }
        
        files.map { file ->
            fileToMediaResult(file, config)
        }
    }
    
    private fun pickWithFileDialog(
        title: String,
        extensions: List<String>,
        multiSelect: Boolean
    ): List<File> {
        val frame = parentWindow as? Frame ?: Frame()
        val dialog = FileDialog(frame, title, FileDialog.LOAD)
        
        dialog.isMultipleMode = multiSelect
        
        // Set file filter
        if (extensions.isNotEmpty()) {
            dialog.setFilenameFilter { _, name ->
                extensions.any { ext ->
                    name.lowercase().endsWith(".$ext")
                }
            }
        }
        
        dialog.isVisible = true
        
        val result = if (multiSelect) {
            dialog.files?.toList() ?: emptyList()
        } else {
            val dir = dialog.directory
            val file = dialog.file
            if (dir != null && file != null) {
                listOf(File(dir, file))
            } else {
                emptyList()
            }
        }
        
        // Dispose frame if we created it
        if (parentWindow == null) {
            frame.dispose()
        }
        
        return result
    }
    
    private fun pickWithJFileChooser(
        title: String,
        extensions: List<String>,
        multiSelect: Boolean
    ): List<File> {
        val chooser = JFileChooser()
        chooser.dialogTitle = title
        chooser.isMultiSelectionEnabled = multiSelect
        chooser.fileSelectionMode = JFileChooser.FILES_ONLY
        
        // Set file filter
        if (extensions.isNotEmpty()) {
            val filter = FileNameExtensionFilter(
                "Allowed files (${extensions.joinToString(", ") { "*.$it" }})",
                *extensions.toTypedArray()
            )
            chooser.fileFilter = filter
        }
        
        val result = chooser.showOpenDialog(parentWindow)
        
        return if (result == JFileChooser.APPROVE_OPTION) {
            if (multiSelect) {
                chooser.selectedFiles?.toList() ?: emptyList()
            } else {
                chooser.selectedFile?.let { listOf(it) } ?: emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    private fun fileToMediaResult(file: File, config: MediaPickerConfig): MediaResult {
        val extension = file.extension.lowercase()
        val mimeType = getMimeType(extension)
        val type = getMediaType(extension)
        
        return MediaResult(
            uri = file.toURI().toString(),
            name = file.name,
            size = file.length(),
            mimeType = mimeType,
            type = type,
            duration = null, // TODO: Could use FFmpeg or similar for video duration
            platformData = file
        )
    }
    
    private fun getMimeType(extension: String): String {
        return when (extension) {
            // Images
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            
            // Videos
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "wmv" -> "video/x-ms-wmv"
            
            // Audio
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "flac" -> "audio/flac"
            "aac" -> "audio/aac"
            
            // Documents
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            
            else -> try {
                Files.probeContentType(java.io.File(".dummy.$extension").toPath()) ?: "application/octet-stream"
            } catch (e: Exception) {
                "application/octet-stream"
            }
        }
    }
    
    private fun getMediaType(extension: String): MediaType {
        return when (extension) {
            in IMAGE_EXTENSIONS -> MediaType.IMAGE
            in VIDEO_EXTENSIONS -> MediaType.VIDEO
            in AUDIO_EXTENSIONS -> MediaType.AUDIO
            in DOCUMENT_EXTENSIONS -> MediaType.DOCUMENT
            else -> MediaType.FILE
        }
    }
    
    private fun mimeTypesToExtensions(mimeTypes: List<String>): List<String> {
        if (mimeTypes.isEmpty()) return emptyList()
        
        return mimeTypes.flatMap { mimeType ->
            when {
                mimeType == "*/*" -> emptyList()
                mimeType == "image/*" -> IMAGE_EXTENSIONS
                mimeType == "video/*" -> VIDEO_EXTENSIONS
                mimeType == "audio/*" -> AUDIO_EXTENSIONS
                mimeType.startsWith("image/") -> IMAGE_EXTENSIONS.filter { getMimeType(it) == mimeType }
                mimeType.startsWith("video/") -> VIDEO_EXTENSIONS.filter { getMimeType(it) == mimeType }
                mimeType.startsWith("audio/") -> AUDIO_EXTENSIONS.filter { getMimeType(it) == mimeType }
                else -> DOCUMENT_EXTENSIONS.filter { getMimeType(it) == mimeType }
            }
        }
    }
    
    companion object {
        private val IMAGE_EXTENSIONS = listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg")
        private val VIDEO_EXTENSIONS = listOf("mp4", "mov", "avi", "mkv", "webm", "wmv")
        private val AUDIO_EXTENSIONS = listOf("mp3", "wav", "ogg", "flac", "aac")
        private val DOCUMENT_EXTENSIONS = listOf("pdf", "doc", "docx", "xls", "xlsx", "txt")
    }
}




