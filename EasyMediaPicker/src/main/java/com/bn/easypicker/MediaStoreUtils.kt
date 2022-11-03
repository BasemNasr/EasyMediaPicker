package com.bn.easypicker
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns.DATE_ADDED
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.coroutines.resume


object MediaStoreUtils {
    /**
     * Check if the app can read the shared storage
     */
    fun canReadInMediaStore(context: Context) =
        checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    /**
     * Check if the app can writes on the shared storage
     *
     * On Android 10 (API 29), we can add media to MediaStore without having to request the
     * [WRITE_EXTERNAL_STORAGE] permission, so we only check on pre-API 29 devices
     */
    fun canWriteInMediaStore(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            checkSelfPermission(
                context,
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * We create a MediaStore [Uri] where an image will be stored
     */
    suspend fun createImageUri(context: Context, filename: String): Uri? {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        return withContext(Dispatchers.IO) {
            val newImage = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            }

            // This method will perform a binder transaction which is better to execute off the main
            // thread
            return@withContext context.contentResolver.insert(imageCollection, newImage)
        }
    }

    suspend fun createImageUri(context: Context): Uri? {
        val filename = "camera-${System.currentTimeMillis()}.jpg"
        return createImageUri(context, filename)
    }

    /**
     * We create a MediaStore [Uri] where a video will be stored
     */
    suspend fun createVideoUri(context: Context, filename: String): Uri? {
        val videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        return withContext(Dispatchers.IO) {
            val newVideo = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            }

            // This method will perform a binder transaction which is better to execute off the main
            // thread
            return@withContext context.contentResolver.insert(videoCollection, newVideo)
        }
    }

    /**
     * We create a MediaStore [Uri] where an image will be stored
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun createDownloadUri(context: Context, filename: String): Uri? {
        val downloadsCollection =
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        return withContext(Dispatchers.IO) {
            val newImage = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
            }

            // This method will perform a binder transaction which is better to execute off the main
            // thread
            return@withContext context.contentResolver.insert(downloadsCollection, newImage)
        }
    }

    /**
     * Convert a media [Uri] to a content [Uri] to be used when requesting
     * [MediaStore.Files.FileColumns] values.
     *
     * Some columns are only available on the [MediaStore.Files] collection and this method converts
     * [Uri] from other MediaStore collections (e.g. [MediaStore.Images])
     *
     * @param uri [Uri] representing the MediaStore entry.
     */
    private fun convertMediaUriToContentUri(uri: Uri): Uri? {
        val entryId = uri.lastPathSegment ?: return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.getVolumeName(uri), entryId.toLong())
        } else {
            MediaStore.Files.getContentUri(uri.pathSegments[0], entryId.toLong())
        }
    }

    suspend fun scanPath(context: Context, path: String, mimeType: String): Uri? {
        return suspendCancellableCoroutine { continuation ->
            MediaScannerConnection.scanFile(
                context,
                arrayOf(path),
                arrayOf(mimeType)
            ) { _, scannedUri ->
                if (scannedUri == null) {
                    continuation.cancel(Exception("File $path could not be scanned"))
                } else {
                    continuation.resume(scannedUri)
                }
            }
        }
    }

    suspend fun scanUri(context: Context, uri: Uri, mimeType: String): Uri? {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(MediaStore.Files.FileColumns.DATA),
            null,
            null,
            null
        ) ?: throw Exception("Uri $uri could not be found")

        val path = cursor.use {
            if (!cursor.moveToFirst()) {
                throw Exception("Uri $uri could not be found")
            }

            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
        }

        return suspendCancellableCoroutine { continuation ->
            MediaScannerConnection.scanFile(
                context,
                arrayOf(path),
                arrayOf(mimeType)
            ) { _, scannedUri ->
                if (scannedUri == null) {
                    continuation.cancel(Exception("File $path could not be scanned"))
                } else {
                    continuation.resume(scannedUri)
                }
            }
        }
    }

    /**
     * Returns a [FileResource] if it finds its [Uri] in MediaStore.
     */
    suspend fun getResourceByUri(context: Context, contentUri: Uri): FileResource {
        return withContext(Dispatchers.IO) {
            // Convert generic media uri to content uri to get FileColumns.MEDIA_TYPE value
            var content: Uri? = null
            try {
                content = convertMediaUriToContentUri(contentUri) ?: contentUri
            } catch (e: Exception) {
                Log.e("Nothing", "getResourceByUri: >> Nothing", )
            }

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATA,
            )

            val cursor = content?.let {
                context.contentResolver.query(
                    it,
                    projection,
                    null,
                    null,
                    null
                )
            } ?: throw Exception("Uri $contentUri could not be found")

            cursor.use {
                if (!cursor.moveToFirst()) {
                    throw Exception("Uri $contentUri could not be found")
                }

                val idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val displayNameColumn =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val mediaTypeColumn =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val mimeTypeColumn =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
//                val dataColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)

                FileResource(
                    uri = contentUri,
                    filename = cursor.getString(displayNameColumn),
                    size = humanReadableByteCountBin(cursor.getLong(sizeColumn)),
                    type = FileType.getEnum(cursor.getInt(mediaTypeColumn)),
                    mimeType = cursor.getString(mimeTypeColumn),
                    path = cursor.getString(dataColumn),
                    duration = if (FileType.getEnum(cursor.getInt(mediaTypeColumn)) == FileType.VIDEO)
                        getDuration(context, cursor.getString(dataColumn)).toString() else null
                )

            }
        }
    }
    /**
     * Returns a [FileResource] if it finds its [Uri] in MediaStore.
     */
    suspend fun getFileByUri(context: Context, contentUri: Uri): FileResource {
        return withContext(Dispatchers.IO) {
            // Convert generic media uri to content uri to get FileColumns.MEDIA_TYPE value
            var content: Uri? = contentUri

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATA,
            )

            val cursor = content?.let {
                context.contentResolver.query(
                    it,
                    projection,
                    null,
                    null,
                    null
                )
            } ?: throw Exception("Uri $contentUri could not be found")

            cursor.use {
                if (!cursor.moveToFirst()) {
                    throw Exception("Uri $contentUri could not be found")
                }

                val idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val displayNameColumn =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val mediaTypeColumn =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val mimeTypeColumn =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
//                val dataColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)

                FileResource(
                    uri = contentUri,
                    filename = cursor.getString(displayNameColumn),
                    size = humanReadableByteCountBin(cursor.getLong(sizeColumn)),
                    type = FileType.getEnum(cursor.getInt(mediaTypeColumn)),
                    mimeType = cursor.getString(mimeTypeColumn),
                    path = cursor.getString(dataColumn),
                    duration = if (FileType.getEnum(cursor.getInt(mediaTypeColumn)) == FileType.VIDEO)
                        getDuration(context, cursor.getString(dataColumn)).toString() else null
                )

            }
        }
    }


    /**
     * Returns a [FileResource] if it finds its [Uri] in MediaStore.
     */
    suspend fun getMediaResources(context: Context, type: FileType? = null): List<FileResource> {
        return withContext(Dispatchers.IO) {
            val mediaList = mutableListOf<FileResource>()
            val externalContentUri = MediaStore.Files.getContentUri("external")
                ?: throw Exception("External Storage not available")

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATA,
            )

            val cursor = context.contentResolver.query(
                externalContentUri,
                projection,
                null,
                null,
                "$DATE_ADDED DESC"
            ) ?: throw Exception("Query could not be executed")

            cursor.use {
                while (cursor.moveToNext()) {
                    val idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                    val displayNameColumn =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                    val mediaTypeColumn =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                    val mediaDurationColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            cursor.getColumnIndex(MediaStore.Files.FileColumns.DURATION)
                    } else null
                    val mimeTypeColumn =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                    val dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                    val id = cursor.getInt(idColumn)
                    val contentUri: Uri = ContentUris.withAppendedId(
                        externalContentUri,
                        id.toLong()
                    )

                    if (type != null && cursor.getInt(mediaTypeColumn) == type.value && cursor.getLong(sizeColumn) != 0L) {
                        Log.d("FileType", "Value: >>> ${type.value}")
                        Log.d("FileType", "Cursor: >>> ${cursor.getInt(mediaTypeColumn)}")
                        Log.d("FileType", "FileType: >>> ${FileType.getEnum(type.value).value}")
                        mediaList += FileResource(
                            uri = contentUri,
                            filename = cursor.getString(displayNameColumn),
                            size = humanReadableByteCountBin(cursor.getLong(sizeColumn)),
                            type = FileType.getEnum(cursor.getInt(mediaTypeColumn)),
                            mimeType = cursor.getString(mimeTypeColumn),
                            path = cursor.getString(dataColumn)
                        )
                    }
                }
            }

            return@withContext mediaList
        }
    }

    private fun getDuration(requireContext: Context, vPath: String): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(vPath)
        val duration = java.lang.Long.parseLong(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        ) / 1000
        retriever.release()
        return duration
    }

    private fun humanReadableByteCountBin(bytes: Long): String? {
        val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
        if (absB < 1024) {
            return "$bytes B"
        }
        var value = absB
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 40
        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }
        value *= java.lang.Long.signum(bytes).toLong()
        return String.format("%.1f %cB", value / 1024.0, ci.current())
    }

    suspend fun createVideoUri(context: Context): Uri? {
        val filename = "camera-${System.currentTimeMillis()}.mp4"
        return createVideoUri(context, filename)
    }

    fun deleteUriFile(mPath: Uri, context: Context) {
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.contentResolver.delete(mPath, null)
            }
        }catch (e:Exception){
            Log.v("DeletePathEx",e.toString())
        }
    }
}