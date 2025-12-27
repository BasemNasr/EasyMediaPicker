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
    val scope = rememberCoroutineScope()
    
    // Create the picker instance directly
    // We cast to AndroidMediaPicker to register launchers, but return as MediaPickerState
    val picker = remember(context) { 
        com.bn.easymediapicker.core.AndroidMediaPicker(context)
    }
    
    // Create launchers
    val pickVisualMediaLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri -> picker.onPickVisualMediaResult(uri) }
    
    val pickMultipleVisualMediaLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> picker.onPickMultipleVisualMediaResult(uris) }
    
    val startActivityLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result -> picker.onActivityResult(result) }
    
    val takePictureLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success -> picker.onTakePictureResult(success) }
    
    val captureVideoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CaptureVideo()
    ) { success -> picker.onCaptureVideoResult(success) }
    
    val requestPermissionsLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results -> picker.onRequestPermissionsResult(results) }
    
    // Register launchers with the picker
    // This allows the AndroidMediaPicker to launch activities using these safe launchers
    androidx.compose.runtime.SideEffect {
        picker.registerLaunchers(
            pickVisualMediaLauncher,
            pickMultipleVisualMediaLauncher,
            startActivityLauncher,
            takePictureLauncher,
            captureVideoLauncher,
            requestPermissionsLauncher
        )
    }
    
    return remember(picker, scope) {
        MediaPickerState(picker, scope)
    }
}




