package com.bn.easypicker

import android.provider.MediaStore.Files.FileColumns

enum class FileType(val value: Int) : java.io.Serializable {
    /**
     * Representing [FileColumns.MEDIA_TYPE_NONE]
     */
    NONE(0),

    /**
     * Representing [FileColumns.MEDIA_TYPE_IMAGE]
     */
    IMAGE(1),

    /**
     * Representing [FileColumns.MEDIA_TYPE_AUDIO]
     */
    AUDIO(2),

    /**
     * Representing [FileColumns.MEDIA_TYPE_VIDEO]
     */
    VIDEO(3),

    /**
     * Representing [FileColumns.MEDIA_TYPE_DOCUMENT]
     */
    DOCUMENT(6);

    companion object {
        fun getEnum(value: Int) = values().find {
            it.value == value
        } ?: throw IllegalArgumentException("Unknown MediaStoreType value")
    }
}