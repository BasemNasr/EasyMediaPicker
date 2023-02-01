package com.bn.easypicker.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.bn.easypicker.FileResource
import com.bn.easypicker.FragmentEasyPicker
import com.bn.easypicker.MainActivity
import com.bn.easypicker.R
import com.bn.easypicker.listeners.OnCaptureMedia
import com.bumptech.glide.Glide


class PickerProfileFragment : Fragment(), OnCaptureMedia {

    private lateinit var easyPicker: FragmentEasyPicker
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
        easyPicker =
            FragmentEasyPicker.Builder(this@PickerProfileFragment)
                .setRequestCode(MainActivity.PICK_PROFILE_IMAGE)
                .setListener(this@PickerProfileFragment).build()

    }

    override fun onCaptureMedia(request: Int, files: ArrayList<FileResource>?) {
        when (request) {
            MainActivity.PICK_PROFILE_IMAGE -> {
                files?.let {
                    mProfileImagePath = files[0]?.path ?: ""
                    Glide.with(requireActivity()).load(mProfileImagePath)
                        .into(requireView().findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg))
                }

            }
        }
    }


}