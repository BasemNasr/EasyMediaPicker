package com.bn.easymediapicker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.bn.easymediapicker.core.MediaPicker

/**
 * Creates and remembers a [MediaPickerState] for use in Compose.
 * 
 * This is the primary entry point for using the media picker in Compose Multiplatform.
 * 
 * Usage:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val pickerState = rememberMediaPickerState()
 *     
 *     Button(onClick = {
 *         pickerState.pickImage { result ->
 *             result?.let { 
 *                 // Handle selected image
 *                 println("Selected: ${it.uri}")
 *             }
 *         }
 *     }) {
 *         Text("Pick Image")
 *     }
 * }
 * ```
 * 
 * Note: On Android, you need to call [MediaPickerFactory.initialize] in your Activity's onCreate.
 * On iOS, call it from your UIViewController.
 * On Desktop, initialization is optional.
 * 
 * @return A [MediaPickerState] that can be used to launch picker operations
 */
@Composable
expect fun rememberMediaPickerState(): MediaPickerState

/**
 * Creates and remembers a [MediaPickerState] with a custom [MediaPicker] implementation.
 * 
 * Use this when you need to provide your own picker implementation or
 * have special initialization requirements.
 * 
 * @param picker The [MediaPicker] implementation to use
 * @return A [MediaPickerState] that can be used to launch picker operations
 */
@Composable
fun rememberMediaPickerState(picker: MediaPicker): MediaPickerState {
    val scope = rememberCoroutineScope()
    return remember(picker) {
        MediaPickerState(picker, scope)
    }
}

