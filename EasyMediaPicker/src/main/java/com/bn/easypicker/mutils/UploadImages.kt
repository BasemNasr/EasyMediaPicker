package com.bn.easypicker.mutils

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.*

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

    @RequiresApi(Build.VERSION_CODES.DONUT)
    fun resizeAndCompressImageBeforeSend(context: Context,
                                         filePath: String?,
                                         fileName: String): String {
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            when (orientation) {
                6 -> {
                    matrix.postRotate(90f)
                    Log.d("EXIF", "Exif: $orientation")
                }
                3 -> {
                    matrix.postRotate(180f)
                    Log.d("EXIF", "Exif: $orientation")
                }
                8 -> {
                    matrix.postRotate(270f)
                    Log.d("EXIF", "Exif: $orientation")
                }
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap!!, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var out: FileOutputStream? = null
        val filename: String = filePath + fileName
        try {
            out = FileOutputStream(filename)
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, out)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return filename
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
