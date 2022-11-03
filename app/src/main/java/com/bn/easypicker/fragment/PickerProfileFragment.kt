package com.bn.easypicker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bn.easypicker.EasyPicker
import com.bn.easypicker.FileResource
import com.bn.easypicker.MainActivity
import com.bn.easypicker.R
import com.bn.easypicker.listeners.OnCaptureMedia
import com.bn.easypicker.mutils.Constants
import com.bn.easypicker.mutils.UploadImages
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File


class PickerProfileFragment : Fragment(), OnCaptureMedia {

    private lateinit var easyPicker: EasyPicker
    var mProfileImagePath = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_picker_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpImagePicker()
        view.findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg).setOnClickListener {
            easyPicker.chooseImage()
        }
    }

    private fun setUpImagePicker() {
        lifecycleScope.launch {
            easyPicker =
                EasyPicker.Builder(requireActivity() as AppCompatActivity)
                    .setRequestCode(MainActivity.PICK_PROFILE_IMAGE)
                    .setListener(this@PickerProfileFragment).build()
        }
    }

    override fun onCaptureMedia(request: Int, file: FileResource) {
        when (request) {
            MainActivity.PICK_PROFILE_IMAGE -> {
                val imagePath = if (file.path!!.isNotEmpty()) {
                    UploadImages.resizeAndCompressImageBeforeSend(
                        requireActivity(), file.path, File(file.path).name
                    )
                } else file.path

                mProfileImagePath = imagePath!!
                Glide.with(requireActivity()).load(mProfileImagePath)
                    .into(view!!.findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg))
            }
            Constants.MEDIA_PERMISSION_DONE -> {
                easyPicker.chooseImage()
            }
        }
    }


}