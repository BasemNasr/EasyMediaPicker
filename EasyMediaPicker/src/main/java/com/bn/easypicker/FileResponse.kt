package com.driver.captainone.utils.mediaPicker

sealed class FileResponse<out T> {
    /**
     * Success file uri with body
     */
    data class Success<T>(val body: T) : FileResponse<T>()

    /**
     * For example, Can't get file or uri
     */
    data class UnknownError(val error: Throwable?) : FileResponse<Nothing>()

    /**
     * For example, Loading progress bar
     */
    object Loading: FileResponse<Nothing>()
}
