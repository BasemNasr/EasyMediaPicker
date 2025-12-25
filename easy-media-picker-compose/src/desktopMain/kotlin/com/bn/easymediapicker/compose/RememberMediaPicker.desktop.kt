package com.bn.easymediapicker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.FrameWindowScope
import com.bn.easymediapicker.core.MediaPickerFactory

/**
 * Desktop-specific implementation of [rememberMediaPickerState].
 * 
 * This works automatically on desktop without any special initialization.
 * Optionally, you can call [MediaPickerFactory.initialize] with a Window
 * to have file dialogs positioned relative to your window.
 * 
 * Usage:
 * ```kotlin
 * @Composable
 * fun App() {
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
 * 
 * For better dialog positioning within a Window:
 * ```kotlin
 * fun main() = application {
 *     Window(onCloseRequest = ::exitApplication) {
 *         // Initialize with the current window for proper dialog positioning
 *         LaunchedEffect(Unit) {
 *             MediaPickerFactory.initialize(window)
 *         }
 *         
 *         App()
 *     }
 * }
 * ```
 */
@Composable
actual fun rememberMediaPickerState(): MediaPickerState {
    val scope = rememberCoroutineScope()
    
    val picker = remember {
        MediaPickerFactory.create()
    }
    
    return remember(picker) {
        MediaPickerState(picker, scope)
    }
}

/**
 * Desktop-specific variant that automatically uses the current window.
 * 
 * Use this inside a [FrameWindowScope] for proper dialog positioning.
 * 
 * ```kotlin
 * fun main() = application {
 *     Window(onCloseRequest = ::exitApplication) {
 *         val pickerState = rememberMediaPickerState(this)
 *         // ...
 *     }
 * }
 * ```
 */
@Composable
fun FrameWindowScope.rememberMediaPickerState(): MediaPickerState {
    val scope = rememberCoroutineScope()
    val window = window
    
    val picker = remember(window) {
        MediaPickerFactory.create(window)
    }
    
    return remember(picker) {
        MediaPickerState(picker, scope)
    }
}




