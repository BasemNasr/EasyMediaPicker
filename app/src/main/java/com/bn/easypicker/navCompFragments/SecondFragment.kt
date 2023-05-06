package com.bn.easypicker.navCompFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bn.easypicker.FileResource
import com.bn.easypicker.FragmentEasyPicker
import com.bn.easypicker.MainActivity
import com.bn.easypicker.R
import com.bn.easypicker.databinding.FragmentSecondBinding
import com.bn.easypicker.listeners.OnCaptureMedia
import com.bumptech.glide.Glide

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(), OnCaptureMedia {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var easyPicker: FragmentEasyPicker
    private lateinit var multiImagesEasyPicker: FragmentEasyPicker
    var mProfileImagePath = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpImagePicker()
        setUpMultiImagesPicker()

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.ivCaptainProfileImg.setOnClickListener {
            easyPicker.chooseAndCompressImage()
        }
        binding.btnSelectMultiImages.setOnClickListener {
            multiImagesEasyPicker.chooseMultipleImages()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpImagePicker() {
        easyPicker =
            FragmentEasyPicker.Builder(this@SecondFragment)
                .setRequestCode(MainActivity.PICK_PROFILE_IMAGE)
                .setListener(this@SecondFragment)
                .build()
    }
    private fun setUpMultiImagesPicker() {
        multiImagesEasyPicker =
            FragmentEasyPicker.Builder(this@SecondFragment)
                .setRequestCode(MainActivity.PICK_IMAGES)
                .setListener(this@SecondFragment)
                .setMaxSelectionLimit(5)
                .build()


    }

    override fun onCaptureMedia(request: Int, files: ArrayList<FileResource>?) {
        when (request) {
            MainActivity.PICK_PROFILE_IMAGE -> {
                files?.let {
                    mProfileImagePath = files?.get(0)?.path ?: ""
                    Glide.with(requireActivity()).load(mProfileImagePath)
                        .into(requireView().findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg))
                }
            }
            MainActivity.PICK_IMAGES -> {
                files?.let {
                    Toast.makeText(requireContext(),"Images Size : ${files.size}",Toast.LENGTH_LONG).show()
                    mProfileImagePath = files?.get(0)?.path ?: ""
                    Glide.with(requireActivity()).load(mProfileImagePath)
                        .into(requireView().findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg))
                }
            }
        }
    }


}