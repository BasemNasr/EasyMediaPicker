package com.bn.easypicker.multiChoose

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bn.easypicker.R
import com.bn.easypicker.databinding.ActivityGalleryBinding
import com.bn.easypicker.multiChoose.adapter.ImagesAdapter
import com.bn.easypicker.multiChoose.model.ImageModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class GalleryActivity : AppCompatActivity() {

    private var maxSelectionLimit: Int = 0
    private var showAlbums: Boolean = false

    private lateinit var layoutManager: GridLayoutManager

    private lateinit var binding: ActivityGalleryBinding

    private lateinit var imagesAdapter: ImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initParams()
        initToolbar()
        initRecyclerView()
    }

    private fun initToolbar() {

        binding.layoutToolbar.toolbar.apply {
            title = getString(R.string.select_photos)
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setNavigationIcon(R.drawable.icon_header_back)
            setNavigationOnClickListener {
                onBackPressed()
            }
        }

        binding.layoutToolbar.btnGalleryPickDone.setOnClickListener {
            setSelectedImageResult()
        }
    }

    private fun initRecyclerView() {
        layoutManager = GridLayoutManager(this, resources.getInteger(R.integer.span_count_images))
        binding.recyclerViewImages.layoutManager = layoutManager
        imagesAdapter = object : ImagesAdapter(this, maxSelectionLimit) {
            override fun updateNextButton() {
                updateFAButtonAndTitle()
            }

            override fun showMessageForMaxSelection() {
                Toast.makeText(
                    this@GalleryActivity,
                    getString(R.string.select_max_photo_error).format(maxSelectionLimit),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.recyclerViewImages.adapter = imagesAdapter

        fetchImagesFromDevice()
    }

    private fun setSelectedImageResult() {
        val selectedImages = imagesAdapter.getSelectedUris().toCollection(arrayListOf())
        val intent = Intent()
        intent.putParcelableArrayListExtra(Constants.BUNDLE_SELECTED_IMAGE_RESULT, selectedImages)
        intent.putExtra(Constants.BUNDLE_IMAGE_PICKED_SUCCESS, true)
        setResult(Activity.RESULT_OK, intent)
        onBackPressed()
    }

    private fun fetchImagesFromDevice() {
        Observable.fromCallable {
            return@fromCallable getImagesUri()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                imagesAdapter.updateList(it)
            }
    }

    private fun getImagesUri(): MutableList<ImageModel> {

        val imageList: MutableList<ImageModel> = mutableListOf()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        val query = contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                imageList.add(ImageModel(contentUri))
            }
        }

        return imageList

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutManager.spanCount = resources.getInteger(R.integer.span_count_images)
        } else {
            layoutManager.spanCount = resources.getInteger(R.integer.span_count_images)
        }
    }

    private fun updateFAButtonAndTitle() {
        val selectedImages = imagesAdapter.getSelectedImageCount()

        if (selectedImages > 0) {
            binding.layoutToolbar.btnGalleryPickDone.visibility = View.VISIBLE
            binding.layoutToolbar.toolbar.title =
                getString(R.string.title_image_selected).format(selectedImages)
        } else {
            binding.layoutToolbar.btnGalleryPickDone.visibility = View.GONE
            binding.layoutToolbar.toolbar.title = getString(R.string.select_photos)
        }
    }

    private fun initParams() {
        maxSelectionLimit = intent.getIntExtra(Constants.BUNDLE_MAX_SELECTION_LIMIT, 0)
        showAlbums = intent.getBooleanExtra(Constants.BUNDLE_SHOW_ALBUMS, false)
    }
}