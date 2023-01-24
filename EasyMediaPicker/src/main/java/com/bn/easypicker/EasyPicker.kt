package com.bn.easypicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bn.easypicker.MediaStoreUtils.deleteUriFile
import com.bn.easypicker.listeners.OnAttachmentTypeSelected
import com.bn.easypicker.listeners.OnCaptureMedia
import com.bn.easypicker.mutils.FilesVersionUtil
import com.bn.easypicker.mutils.PermissionUtils
import com.bn.easypicker.mutils.UploadImages
import com.bn.easypicker.mutils.request_permission.RequestStoragePermissionActivity
import com.bn.easypicker.ui.SelectAttachmentsTypeSheet
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlin.random.Random


class EasyPicker(
    builder: Builder,
) : OnAttachmentTypeSelected {

    private val request: Int = builder.request
    private val mContext: Context = builder.act
    private val act: FragmentActivity = builder.act
    private val mListener: OnCaptureMedia = builder.mListener
    private val cameraIcon: Int = builder.cameraIcon
    private val galleryIcon: Int = builder.galleryIcon
    private val textColor: Int = builder.textColor
    private val backgroundColor: Int = builder.sheetBackgroundColor
    private val btnBackground: Int = builder.btnBackground


    private val resultLauncher =
        act.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == -1) {
                chooseImage()
            }
        }

    private val mSelectImageSheet: SelectAttachmentsTypeSheet by lazy {
        SelectAttachmentsTypeSheet(
            mContext,
            this,
            cameraIcon,
            galleryIcon,
            backgroundColor,
            btnBackground,
            textColor
        )
    }


    open class Builder(act: FragmentActivity) {
        var request: Int = 102456
        var act = act
        var cameraIcon: Int = R.drawable.ic_camera
        var galleryIcon: Int = R.drawable.ic_galery
        var sheetBackgroundColor: Int = R.color.white
        var btnBackground: Int = R.drawable.bg_et_silver
        var textColor: Int = R.color.black
        var mListener = object : OnCaptureMedia {
            override fun onCaptureMedia(request: Int, file: FileResource) {
            }
        }

        fun setRequestCode(requestCode: Int): Builder {
            this.request = requestCode
            return this
        }

        fun setSheetBackgroundColor(
            backgroundColor: Int
        ): Builder {
            this.sheetBackgroundColor = backgroundColor
            return this
        }

        fun setIconsAndTextColor(
            cameraIcon: Int? = null,
            galleryIcon: Int? = null,
            textColor: Int? = null,
            btnBackground: Int? = null
        ): Builder {
            cameraIcon?.let { this.cameraIcon = cameraIcon }
            galleryIcon?.let { this.galleryIcon = galleryIcon }
            textColor?.let { this.textColor = textColor }
            btnBackground?.let { this.btnBackground = btnBackground }
            return this
        }

        fun setListener(onCaptureImage: OnCaptureMedia): Builder {
            this.mListener = onCaptureImage
            return this
        }

        fun build(): EasyPicker {
            Log.v("CurrentState", "${act.lifecycle.currentState}")
            return EasyPicker(this)
        }
    }


    private lateinit var mPath: Uri

    private var imageLauncher =
        act.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                var imageUri: Uri = try {
                    data?.data!!
                } catch (e: Exception) {
                    getImageUri(mContext, result.data!!.extras!!.get("data") as Bitmap)!!
                }

                CoroutineScope(Dispatchers.Main).launch {
                    var resulting: FileResource? = null
                    async {
                        try {
                            resulting = MediaStoreUtils.getResourceByUri(mContext, imageUri)
                        } catch (e: Exception) {
                            try {
                                Log.e("ExceptionVideo", ">>> Exception Video First: ${e.message}")
                                resulting = FileResource(
                                    uri = imageUri,
                                    path = FilesVersionUtil.getRealPathFromUri(mContext, imageUri)
                                )
                            } catch (e: Exception) {
                                Log.e("ExceptionVideo", ">>> Exception Video: ${e.message}")
                            }
                        }
                    }.await()
                    mListener.onCaptureMedia(request, resulting!!)
                }
            }
        }


    private var takeImageLauncher =
        act.registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            CoroutineScope(Dispatchers.Main).launch {
                var resulting: FileResource? = null
                if (!result) {
                    try {
                        deleteUriFile(mPath, act)
                    } catch (e: Exception) {
                        Log.v("Exception", e.toString())
                    }
                    return@launch
                }
                async {
                    try {
                        resulting = MediaStoreUtils.getResourceByUri(mContext, mPath)
                    } catch (e: Exception) {
                        try {
                            Log.e("ExceptionVideo", ">>> Exception Video First: ${e.message}")
                            resulting = FileResource(
                                uri = mPath,
                                path = FilesVersionUtil.getRealPathFromUri(mContext, mPath)
                            )
                        } catch (e: Exception) {
                            Log.e("ExceptionVideo", ">>> Exception Video: ${e.message}")
                        }
                    }
                }.await()
                mListener.onCaptureMedia(request, resulting!!)
            }
        }


    private var videoLauncher =
        act.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri = data?.data!!

                CoroutineScope(Dispatchers.Main).launch {
                    var resulting: FileResource? = null
                    async {
                        try {
                            resulting = MediaStoreUtils.getResourceByUri(mContext, imageUri)
                        } catch (e: Exception) {
                            try {
                                Log.e("ExceptionVideo", ">>> Exception Video First: ${e.message}")
                                resulting = FileResource(
                                    uri = imageUri,
                                    path = FilesVersionUtil.getRealPathFromUri(mContext, imageUri)
                                        ?: getPathFromURI(mContext, imageUri)
                                )
                            } catch (e: Exception) {
                                try {
                                    resulting = MediaStoreUtils.getResourceByUri(mContext, imageUri)
                                } catch (e: Exception) {
                                    Log.e("ExceptionVideo", ">>> Exception Video: ${e.message}")
                                }
                            }
                        }
                    }.await()
                    mListener.onCaptureMedia(request, resulting!!)
                }
            }
        }

    private fun getPathFromURI(context: Context, contentUri: Uri?): String? {
        var mediaCursor: Cursor? = null
        return try {
            val dataPath = arrayOf(MediaStore.Images.Media.DATA)
            mediaCursor = context.contentResolver.query(contentUri!!, dataPath, null, null, null)
            val column_index = mediaCursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            mediaCursor.moveToFirst()
            mediaCursor.getString(column_index)
        } finally {
            mediaCursor?.close()
        }
    }

    private var compressHighQualityImageLauncher =
        act.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data!!
                CoroutineScope(Dispatchers.Main).launch {
                    var resulting: FileResource? = null
                    async {
                        resulting = try {
                            MediaStoreUtils.getResourceByUri(mContext, imageUri)
                        } catch (e: Exception) {
                            FileResource(
                                uri = imageUri,
                                path = FilesVersionUtil.getRealPathFromUri(mContext, imageUri)
                            )
                        }
                    }.await()
                    resulting?.path?.let {
                        CoroutineScope(Dispatchers.Default).launch {
                            val compressedImageFile: String =
                                withContext(Dispatchers.Default) {
                                    UploadImages.resizeAndCompressImageBeforeSend(
                                        mContext,
                                        it,
                                        "${System.currentTimeMillis()}"
                                    )
                                }
                            withContext(Dispatchers.Main) {
                                resulting?.path = compressedImageFile
                                mListener.onCaptureMedia(request, resulting!!)
                            }
                        }
                    }
                }
            }
        }


    private var fileLauncher =
        act.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri = data?.data!!

                CoroutineScope(Dispatchers.Main).launch {
                    var resulting: FileResource? = null
                    async {
                        try {
                            resulting = MediaStoreUtils.getResourceByUri(mContext, imageUri)
                        } catch (e: Exception) {
                            try {
                                Log.e("ExceptionVideo", ">>> Exception Video First: ${e.message}")
                                resulting = FileResource(
                                    uri = imageUri,
                                    path = FilesVersionUtil.getRealPathFromUri(mContext, imageUri)
                                )
                            } catch (e: Exception) {
                                Log.e("ExceptionVideo", ">>> Exception Video: ${e.message}")
                            }
                        }
                    }.await()
                    mListener.onCaptureMedia(request, resulting!!)
                }
            }
        }


    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT > 32) PermissionUtils.hasPermissions(act, PermissionUtils.NEW_IMAGE_PERMISSIONS)
        else PermissionUtils.hasPermissions(act, PermissionUtils.IMAGE_PERMISSIONS)
    }

    fun chooseImage() {
        if (checkPermission()) {
            mSelectImageSheet.show()
        } else {
            PickActions.openStorageRequest(act, resultLauncher)
        }
    }

    fun chooseAndCompressImage() {
        if (checkPermission()) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ).apply {
                type = "image/*"
            }
            if (intent.resolveActivity(act.packageManager) != null) {
                compressHighQualityImageLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) compressHighQualityImageLauncher.launch(
                intent
            )
        } else {
            PickActions.openStorageRequest(act, resultLauncher)
        }
    }

    fun chooseVideo() {
        if (checkPermission()) {
            val intent = Intent(
                Intent.ACTION_GET_CONTENT,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ).apply {
                type = "video/*"
            }
            if (intent.resolveActivity(act.packageManager) != null) {
                videoLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) videoLauncher.launch(intent)
        } else {
            PickActions.openStorageRequest(act, resultLauncher)
        }
    }

    fun chooseFile() {
        if (checkPermission()) {
            val mRequestFileIntent = Intent(Intent.ACTION_GET_CONTENT)
            mRequestFileIntent.type = "*/*"


            val intent = Intent(
                Intent.ACTION_GET_CONTENT,
            ).apply {
                type = "*/*"
            }
            if (intent.resolveActivity(act.packageManager) != null) {
                fileLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) fileLauncher.launch(intent)
        } else PickActions.openStorageRequest(act, resultLauncher)

    }

    object PickActions {
        fun openStorageRequest(act: Activity, resultLauncher: ActivityResultLauncher<Intent>) {
            val intent = Intent(act, RequestStoragePermissionActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    fun captureHighQualityImage() {
        CoroutineScope(Main).launch {

            mPath = async { MediaStoreUtils.createImageUri(act)!! }.await()
            if (checkPermission()) {

                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
                ).apply {
                    type = "image/*"
                }
                takeImageLauncher.launch(
                    mPath
                )
            } else {
                PickActions.openStorageRequest(act, resultLauncher)
            }
        }
    }

    override fun onAttachSelected(selectedAttatchType: Int) {
        // 0 mean open Camera , 1 mean select image
        act.lifecycleScope.launchWhenStarted {
            imageLauncher
            videoLauncher
        }
        when (selectedAttatchType) {
            0 -> {
                captureHighQualityImage()
            }
            1 -> {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
                ).apply {
                    type = "image/*"
                }
                imageLauncher.launch(intent)
            }
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val path =
            MediaStore.Images.Media.insertImage(
                inContext.contentResolver, inImage, " " + inContext.getString(
                    R.string.app_name
                ) + ":" + Random.nextInt(0, 1500), null
            )
        return Uri.parse(path)
    }

}