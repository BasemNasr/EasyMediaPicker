package com.bn.easymediapicker.core

/**
 * Web implementation of [MediaPickerFactory].
 */
actual object MediaPickerFactory {
    /**
     * Creates a [MediaPicker] instance for web platform.
     */
    actual fun create(): MediaPicker {
        return WebMediaPicker()
    }
}
