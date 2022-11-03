package com.bn.easypicker

import android.net.Uri

/**
 * @suppress Getting and Creating any file from any type
 */

data class FileResource(
    var uri: Uri? = null,
    val filename: String? = null,
    val size: String? = null,
    val duration: String? = null,
    val type: FileType? = null,
    val mimeType: String? = null,
    var path: String? = null,

    var isSelected: Boolean = false,
    var position: Int? = null,
) : java.io.Serializable
