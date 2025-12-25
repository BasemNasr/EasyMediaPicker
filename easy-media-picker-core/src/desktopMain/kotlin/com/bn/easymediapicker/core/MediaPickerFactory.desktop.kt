package com.bn.easymediapicker.core

import java.awt.Window
import java.lang.ref.WeakReference

/**
 * Desktop (JVM) implementation of [MediaPickerFactory].
 * 
 * You can optionally initialize with a parent window for proper dialog positioning:
 * 
 * ```kotlin
 * // With a parent window:
 * MediaPickerFactory.initialize(window)
 * 
 * // Or without (uses screen center):
 * val picker = MediaPickerFactory.create()
 * ```
 */
actual object MediaPickerFactory {
    private var windowRef: WeakReference<Window>? = null
    
    /**
     * Initialize with a parent Window.
     * The file dialogs will be centered on this window.
     */
    fun initialize(window: Window) {
        windowRef = WeakReference(window)
    }
    
    /**
     * Creates a [MediaPicker] for desktop.
     * Uses the registered window as parent if available.
     */
    actual fun create(): MediaPicker {
        return DesktopMediaPicker(windowRef?.get())
    }
    
    /**
     * Creates a [MediaPicker] with a specific parent window.
     */
    fun create(window: Window?): MediaPicker {
        return DesktopMediaPicker(window)
    }
}




