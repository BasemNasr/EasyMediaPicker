package com.bn.easypicker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import com.bn.easypicker.listeners.OnCaptureMedia
import com.bn.easypicker.mutils.Constants.MEDIA_PERMISSION_DONE
import com.bn.easypicker.mutils.UploadImages
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity(), OnCaptureMedia {

    private lateinit var easyPicker: EasyPicker
    var mProfileImagePath = ""


    companion object {
        const val PICK_PROFILE_IMAGE = 4195
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpImagePicker()
        setOnClicks()
    }

    private fun setOnClicks() {
        findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg).setOnClickListener {
            easyPicker.chooseImage()
        }
        findViewById<AppCompatButton>(R.id.btnNext).setOnClickListener {
            val intent = Intent(this@MainActivity
                ,MainActivity2::class.java)
            startActivity(intent)

        }
    }

    private fun setUpImagePicker() {
        lifecycleScope.launch {
            easyPicker = EasyPicker.Builder(this@MainActivity)
                .setRequestCode(PICK_PROFILE_IMAGE)
                .setIconsAndTextColor(R.drawable.camera,R.drawable.gallery,R.color.black)
                .setSheetBackgroundColor(R.color.white)
                .setListener(this@MainActivity)
                .build()
        }
    }

    override fun onCaptureMedia(request: Int, file: FileResource) {
        when (request) {
            PICK_PROFILE_IMAGE -> {
                val imagePath = if (file.path!!.isNotEmpty()) {
                    UploadImages.resizeAndCompressImageBeforeSend(
                        this@MainActivity, file.path, File(file.path).name
                    )
                } else file.path

                mProfileImagePath = imagePath!!
                Glide.with(this@MainActivity).load(mProfileImagePath)
                    .into(findViewById<AppCompatImageView>(R.id.ivCaptainProfileImg))
            }
            MEDIA_PERMISSION_DONE -> {
                easyPicker.chooseImage()
            }
        }
    }
}