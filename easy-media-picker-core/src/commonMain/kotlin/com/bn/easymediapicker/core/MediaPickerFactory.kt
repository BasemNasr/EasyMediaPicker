package com.bn.easymediapicker.core

/**
 * Factory for creating platform-specific [MediaPicker] instances.
 * 
 * Each platform provides its own implementation through the expect/actual mechanism.
 * 
 * Usage varies by platform:
 * 
 * **Android:**
 * ```kotlin
 * val picker = MediaPickerFactory.create(activity)
 * // or
 * val picker = MediaPickerFactory.create(fragment)
 * ```
 * 
 * **iOS:**
 * ```kotlin
 * val picker = MediaPickerFactory.create(viewController)
 * ```
 * 
 * **Desktop:**
 * ```kotlin
 * val picker = MediaPickerFactory.create(window)
 * // or
 * val picker = MediaPickerFactory.create() // Uses default window
 * ```
 */
expect object MediaPickerFactory {
    /**
     * Creates a [MediaPicker] instance for the current platform.
     * The actual signature varies by platform - see platform-specific documentation.
     */
    fun create(): MediaPicker
}

