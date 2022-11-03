package com.bn.easypicker.mutils.request_permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.bn.easypicker.R
import com.bn.easypicker.listeners.OnPermissionDialogListener
import com.bn.easypicker.mutils.PermissionUtils
import com.bn.easypicker.ui.StoragePermissionDialog

class RequestStoragePermissionActivity : AppCompatActivity(), OnPermissionDialogListener<String> {
    private val mPermission by lazy {
        StoragePermissionDialog(
            R.layout.media_permission_dialog,
            this
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_generic_permission)
        getStorageAccess()

        val message =
            getString(R.string.app_name) + " " + resources.getString(R.string.want_to_access_files_video_image)
        findViewById<AppCompatTextView>(R.id.locMessageText).text = message
        findViewById<AppCompatButton>(R.id.locOkButton).setOnClickListener { getStorageAccess() }
        findViewById<AppCompatButton>(R.id.locNegativeButton).setOnClickListener { finish() }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 3001) {
            if (!PermissionUtils.hasPermissions(this, PermissionUtils.IMAGE_PERMISSIONS)) {
                if (!mPermission.isAdded) mPermission.show(supportFragmentManager, "tag")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !PermissionUtils.hasPermissions(
                    this,
                    PermissionUtils.MEDIA_LOCATION_PERMISSIONS
                )
            ) {
                if (!mPermission.isAdded) mPermission.show(supportFragmentManager, "tag")
            } else finish()
        }
    }

    private fun getStorageAccess() {
        if (!PermissionUtils.hasPermissions(this, PermissionUtils.IMAGE_PERMISSIONS)) {
            requestPermissions(PermissionUtils.IMAGE_PERMISSIONS, 3001)
        } else finish()
    }

    companion object {
        fun start(act: AppCompatActivity) {
            act.startActivity(Intent(act, RequestStoragePermissionActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPermission.isAdded) mPermission.dismiss()
    }

    override fun onPermissionDialogClicked(data: String) {
        when (data) {
            "setting" -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                mPermission.dismiss()
            }
            "cancel" -> mPermission.dismiss()
        }
    }
}