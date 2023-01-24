package com.bn.easypicker.mutils

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object PermissionUtils {


    val NEW_IMAGE_PERMISSIONS =
        arrayOf(permission.READ_MEDIA_IMAGES, permission.CAMERA)

    val IMAGE_PERMISSIONS = arrayOf(permission.READ_EXTERNAL_STORAGE,permission.WRITE_EXTERNAL_STORAGE,
        permission.CAMERA)
    val MEDIA_LOCATION_PERMISSIONS = arrayOf(permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE, permission.ACCESS_MEDIA_LOCATION)

    val GPS_PERMISSION = arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION)
    const val BACKGROUND_LOCATION = permission.ACCESS_BACKGROUND_LOCATION
    const val COARSE_LOCATION = permission.ACCESS_COARSE_LOCATION
    const val FINE_LOCATION = permission.ACCESS_FINE_LOCATION

    val BACKGROUND_PERMISSION = arrayOf(permission.ACCESS_BACKGROUND_LOCATION)

    val CALL_PHONE = arrayOf(permission.CALL_PHONE)

    val IMAGE_PERMISSION_RESPONSE = 1

    val DOWNLOAD_PERMISSION_RESPONSE = 2

    fun isPermissionGranted(activity: Activity, permissionName: String, requestCode: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED) {
                Log.v("Tag", "Permission is granted")
                true
            } else {
                Log.v("tag", "Permission is revoked")
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permissionName), requestCode
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("tag", "Permission is granted")
            true
        }
    }

    /**
     * @return whether all the required permission for taking and picking an image are granted or not
     */
    fun areImagePermissionsGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return true
        }
        for (permission in IMAGE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun isAllPermissionGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult != 0) {
                return false
            }
        }
        return true
    }

    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("SSSs", permission)
                    return false
                }
            }
        }
        return true

    }

    fun canMakeSmores(BuildVersion: Int): Boolean {
        return Build.VERSION.SDK_INT > BuildVersion
        //        Build.VERSION_CODES.LOLLIPOP_MR1
    }
}