package com.bn.easypicker.mutils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object UploadImages {
    fun start(): String {
        val options = BitmapFactory.Options()

        options.inSampleSize = 4
        options.inPurgeable = true
        val bm = BitmapFactory.decodeFile("your path of image", options)

        val baos = ByteArrayOutputStream()

        bm.compress(Bitmap.CompressFormat.JPEG, 95, baos)

        // bitmap object
        val byteImagePhoto = baos.toByteArray()

        //generate base64 string of image
        return Base64.encodeToString(byteImagePhoto, Base64.DEFAULT)

    }

    interface UploadCallbacks {
        fun onProgressUpdate(percentage: Int)
    }

    private val multiPart = "multipart/form-data".toMediaType()

    @WorkerThread
    fun uploadFile(
        key: String,
        path: String,
        onProgress: UploadCallbacks
    ) = flow {
        val file = File(path)
        val requestFile = file.asRequestBodyWithProgress(multiPart, onProgress)
        val requestBody = MultipartBody.Part.createFormData(key, file.name, requestFile)
        emit(requestBody)
    }

    fun File.asRequestBodyWithProgress(
        contentType: MediaType? = null,
        progressCallback: UploadCallbacks
    ): RequestBody {
        return object : RequestBody() {
            override fun contentType() = contentType

            override fun contentLength() = length()

            override fun writeTo(sink: BufferedSink) {
                val fileLength = contentLength()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                val inSt = FileInputStream(this@asRequestBodyWithProgress)
                var uploaded = 0L
                inSt.use {
                    var read: Int = inSt.read(buffer)
                    val handler = Handler(Looper.getMainLooper())
                    while (read != -1) {
                        progressCallback.let {
                            uploaded += read
                            handler.post { progressCallback.onProgressUpdate((100 * uploaded / fileLength).toInt()) }

                            sink.write(buffer, 0, read)
                        }
                        read = inSt.read(buffer)
                    }
                }
            }
        }
    }

    fun resizeAndCompressImageBeforeSend(
        context: Context,
        filePath: String?,
        fileName: String
    ): String {
        val MAX_IMAGE_SIZE = 700 * 1024 // max final file size in kilobytes
        // First decode with inJustDecodeBounds=true to check dimensions of image
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        // Calculate inSampleSize(First we are going to resize the image to 800x800 image, in order to not have a big but very low quality image.
        //resizing the image will already reduce the file size, but after resizing we will check the file size and start to compress image
        options.inSampleSize = calculateInSampleSize(options, 800, 800)
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bmpPic = BitmapFactory.decodeFile(filePath, options)
        var compressQuality = 100 // quality decreasing by 5 every loop.
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            Log.d("compressBitmap", "Quality: $compressQuality")
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
            Log.d("compressBitmap", "Size: " + streamLength / 1024 + " kb")
        } while (streamLength >= MAX_IMAGE_SIZE)
        try {
            //save the resized and compressed file to disk cache
            Log.d("compressBitmap", "cacheDir: " + context.cacheDir)
            val bmpFile = FileOutputStream(context.cacheDir.toString() + fileName)
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpFile)
            bmpFile.flush()
            bmpFile.close()
        } catch (e: Exception) {
            Log.e("compressBitmap", "Error on saving file")
        }
        return context.cacheDir.toString() + fileName
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val debugTag = "MemoryInformation"
        val height = options.outHeight
        val width = options.outWidth
        Log.d(debugTag, "image height: $height---image width: $width")
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        Log.d(debugTag, "inSampleSize: $inSampleSize")
        return inSampleSize
    }
}
