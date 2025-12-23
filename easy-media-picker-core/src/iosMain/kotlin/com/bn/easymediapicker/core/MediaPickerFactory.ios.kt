package com.bn.easymediapicker.core

import platform.UIKit.UIViewController
import kotlin.native.ref.WeakReference

/**
 * iOS implementation of [MediaPickerFactory].
 * 
 * Before using the picker, you must initialize it with a UIViewController:
 * 
 * ```kotlin
 * // In your view controller:
 * MediaPickerFactory.initialize(viewController)
 * ```
 */
actual object MediaPickerFactory {
    private var viewControllerRef: WeakReference<UIViewController>? = null
    
    /**
     * Initialize with a UIViewController.
     * This view controller will be used to present the picker UI.
     */
    fun initialize(viewController: UIViewController) {
        viewControllerRef = WeakReference(viewController)
    }
    
    /**
     * Creates a [MediaPicker] for the registered UIViewController.
     * Make sure to call [initialize] first.
     */
    actual fun create(): MediaPicker {
        val viewController = viewControllerRef?.get()
            ?: throw IllegalStateException(
                "MediaPickerFactory not initialized. Call MediaPickerFactory.initialize(viewController) " +
                "before using the picker."
            )
        return IOSMediaPicker(viewController)
    }
    
    /**
     * Creates a [MediaPicker] directly from a UIViewController.
     */
    fun create(viewController: UIViewController): MediaPicker {
        return IOSMediaPicker(viewController)
    }
}

