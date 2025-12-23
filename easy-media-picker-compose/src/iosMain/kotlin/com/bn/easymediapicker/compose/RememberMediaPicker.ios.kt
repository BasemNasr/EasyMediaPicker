package com.bn.easymediapicker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.bn.easymediapicker.core.MediaPickerFactory

/**
 * iOS-specific implementation of [rememberMediaPickerState].
 * 
 * **Important:** You must call [MediaPickerFactory.initialize] with your 
 * UIViewController before using this composable.
 * 
 * Example setup in your Swift code:
 * ```swift
 * import EasyMediaPickerCore
 * 
 * class MyViewController: UIViewController {
 *     override func viewDidLoad() {
 *         super.viewDidLoad()
 *         MediaPickerFactory.shared.initialize(viewController: self)
 *     }
 * }
 * ```
 * 
 * Then in Compose:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val pickerState = rememberMediaPickerState()
 *     
 *     Button(onClick = {
 *         pickerState.pickImage { result ->
 *             result?.let { 
 *                 println("Selected: ${it.uri}")
 *             }
 *         }
 *     }) {
 *         Text("Pick Image")
 *     }
 * }
 * ```
 */
@Composable
actual fun rememberMediaPickerState(): MediaPickerState {
    val scope = rememberCoroutineScope()
    
    val picker = remember {
        try {
            MediaPickerFactory.create()
        } catch (e: IllegalStateException) {
            throw IllegalStateException(
                "MediaPickerFactory not initialized. " +
                "Call MediaPickerFactory.initialize(viewController) from your UIViewController " +
                "before using rememberMediaPickerState().",
                e
            )
        }
    }
    
    return remember(picker) {
        MediaPickerState(picker, scope)
    }
}

