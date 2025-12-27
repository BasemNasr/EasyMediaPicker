package com.bn.easymediapicker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.bn.easymediapicker.core.MediaPickerFactory

/**
 * Web-specific implementation of [rememberMediaPickerState].
 * 
 * Creates and remembers a MediaPickerState for web platform.
 */
@Composable
actual fun rememberMediaPickerState(): MediaPickerState {
    val scope = rememberCoroutineScope()
    val picker = remember { MediaPickerFactory.create() }
    
    return remember(picker, scope) {
        MediaPickerState(picker, scope)
    }
}
