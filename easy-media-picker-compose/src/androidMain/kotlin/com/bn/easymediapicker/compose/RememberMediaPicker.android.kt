package com.bn.easymediapicker.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bn.easymediapicker.core.MediaPickerFactory

/**
 * Android-specific implementation of [rememberMediaPickerState].
 * 
 * This composable automatically handles initialization with the current Activity
 * and lifecycle management.
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    // Initialize the factory with the current activity
    DisposableEffect(context) {
        val activity = context as? ComponentActivity
        if (activity != null) {
            MediaPickerFactory.initialize(activity)
        }
        
        onDispose { }
    }
    
    val picker = remember(context) {
        val activity = context as? ComponentActivity
            ?: throw IllegalStateException(
                "rememberMediaPickerState() must be called from a ComponentActivity context"
            )
        MediaPickerFactory.create(activity)
    }
    
    return remember(picker) {
        MediaPickerState(picker, scope)
    }
}

