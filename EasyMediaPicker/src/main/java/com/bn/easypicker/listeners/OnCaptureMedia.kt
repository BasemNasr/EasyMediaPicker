package com.bn.easypicker.listeners

import com.bn.easypicker.FileResource

interface OnCaptureMedia {
    fun onCaptureMedia(request: Int, file: FileResource)
}