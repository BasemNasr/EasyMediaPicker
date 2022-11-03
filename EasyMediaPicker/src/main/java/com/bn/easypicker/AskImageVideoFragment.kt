package com.bn.easypicker
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bn.easypicker.listeners.OnCaptureMedia
import com.bn.easypicker.mutils.FilesVersionUtil
import com.bn.easypicker.mutils.PermissionUtils
import com.bn.easypicker.mutils.UploadImages
import kotlinx.coroutines.*

@SuppressLint("QueryPermissionsNeeded")
class AskImageVideoFragment(
    private val request: Int,
    private val mContext: Context,
    private val act: Fragment,
    private val mListener: OnCaptureMedia
)  {

    private var imageLauncher =
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
                    Log.e("Resulting", ": ${resulting?.path}", )
//                    mListener.onGettingResult(request, Intent(), resulting!!)
                    mListener.onCaptureMedia(request, resulting!!)
                }
            }
        }

    private var compressImageLauncher =
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
//                                mListener.onGettingResult(request, Intent(), resulting!!)
                                mListener.onCaptureMedia(request,  resulting!!)
                            }
                        }
                    }
                }
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
                        resulting = try {
                            MediaStoreUtils.getResourceByUri(mContext, imageUri)
                        } catch (e: Exception) {
                            FileResource(
                                uri = imageUri,
                                path = FilesVersionUtil.getRealPathFromUri(mContext, imageUri)
                            )
                        }
                    }.await()
//                    mListener.onGettingResult(request, data, resulting!!)
                    mListener.onCaptureMedia(request, resulting!!)
                }
            }
        }


    private fun checkPermission(): Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return PermissionUtils.hasPermissions(act.requireContext(), PermissionUtils.MEDIA_LOCATION_PERMISSIONS)
        } else return PermissionUtils.hasPermissions(act.requireContext(), PermissionUtils.IMAGE_PERMISSIONS)
    }
    fun chooseImage() {
        if (checkPermission()) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ).apply {
                type = "image/*"
            }
            if (intent.resolveActivity(act.requireActivity().packageManager) != null) {
                imageLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) imageLauncher.launch(intent)
        } else act.startActivity(PickActions.openStorageRequest(act.requireActivity() as AppCompatActivity))
    }
    fun chooseCompressImage() {
        if (checkPermission()) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ).apply {
                type = "image/*"
            }
            if (intent.resolveActivity(act.requireActivity().packageManager) != null) {
                compressImageLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) compressImageLauncher.launch(intent)
        } else act.startActivity(PickActions.openStorageRequest(act.requireActivity() as AppCompatActivity))
    }

    fun chooseVideo() {
        if (checkPermission()) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.INTERNAL_CONTENT_URI
            ).apply {
                type = "video/*"
            }
            if (intent.resolveActivity(act.requireActivity().packageManager) != null) {
                videoLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) videoLauncher.launch(intent)
        } else act.startActivity(PickActions.openStorageRequest(act.requireActivity() as AppCompatActivity))
    }

    object PickActions{
        fun openStorageRequest(act: AppCompatActivity) = Intent(act, Class.forName("com.bn.easypicker.mutils.RequestStoragePermissionActivity"))
    }

}