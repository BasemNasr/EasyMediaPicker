package com.bn.easymediapicker.demo

import androidx.compose.ui.window.ComposeUIViewController
import com.bn.easymediapicker.core.MediaPickerFactory
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val viewController = ComposeUIViewController {
        App()
    }
    
    // Initialize MediaPickerFactory with the view controller
    MediaPickerFactory.initialize(viewController)
    
    return viewController
}
