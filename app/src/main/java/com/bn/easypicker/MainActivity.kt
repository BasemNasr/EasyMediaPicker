package com.bn.easypicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import com.bn.easypicker.listeners.OnCaptureMedia
import com.bn.easypicker.mutils.UploadImages
import com.bumptech.glide.Glide
import java.io.File

class MainActivity : AppCompatActivity(), OnCaptureMedia {

    private lateinit var easyPicker: EasyPicker
    var mProfileImagePath = ""


    companion object {
        const val PICK_PROFILE_IMAGE = 4195
        const val PICK_IMAGES = 5687
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpImagePicker()
        setUpMultipleImagePicker()
        setOnClicks()
    }

    private fun setOnClicks() {
        findViewById<AppCompatButton>(R.id.btnPickMultiImage).setOnClickListener {
            easyPicker.chooseMultipleImages()
        }
        findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg).setOnClickListener {
            easyPicker.chooseImage()
        }
        findViewById<AppCompatButton>(R.id.btnNext).setOnClickListener {
            val intent = Intent(
                this@MainActivity, MainActivity2::class.java
            )
            startActivity(intent)

        }
        findViewById<AppCompatButton>(R.id.btnNavComponents).setOnClickListener {
            val intent = Intent(
                this@MainActivity, MainActivity3::class.java
            )
            startActivity(intent)
        }
    }

    private fun setUpImagePicker() {
        easyPicker = EasyPicker.Builder(this@MainActivity)
            .setRequestCode(PICK_PROFILE_IMAGE)
            .setIconsAndTextColor(
                R.drawable.camera,
                R.drawable.gallery,
                R.color.black,
                R.drawable.bg_et_red
            )
            .setSheetBackgroundColor(R.color.white)
            .setListener(this@MainActivity)
            .build()

    }
    private fun setUpMultipleImagePicker() {
        easyPicker = EasyPicker.Builder(this@MainActivity)
            .setRequestCode(PICK_IMAGES)
            .setSheetBackgroundColor(R.color.white)
            .setListener(this@MainActivity)
            .build()

    }

    override fun onCaptureMedia(request: Int, files: ArrayList<FileResource>?) {
        when (request) {
            PICK_PROFILE_IMAGE -> {
                val imagePath = if (files?.get(0)?.path!!.isNotEmpty()) {
                    UploadImages.resizeAndCompressImageBeforeSend(
                        this@MainActivity, files[0]?.path, File(files[0].path).name
                    )
                } else files[0].path

                mProfileImagePath = imagePath!!
                Glide.with(this@MainActivity).load(mProfileImagePath)
                    .into(findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg))
            }
            PICK_IMAGES -> {
                files?.let {
                    Glide.with(this@MainActivity).load((files[0].path)).into(findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg))
                }
            }
        }
    }

}