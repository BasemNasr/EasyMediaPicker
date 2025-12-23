package com.bn.easymediapicker.core

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

/**
 * Android implementation of [MediaPickerFactory].
 * 
 * Before using the picker, you must initialize it with an Activity or Fragment:
 * 
 * ```kotlin
 * // In your Activity's onCreate:
 * MediaPickerFactory.initialize(this)
 * 
 * // Or with a Fragment:
 * MediaPickerFactory.initialize(fragment)
 * ```
 */
actual object MediaPickerFactory {
    private var activityRef: WeakReference<ComponentActivity>? = null
    private var fragmentRef: WeakReference<Fragment>? = null
    
    /**
     * Initialize with a ComponentActivity.
     * Call this in onCreate before using the picker.
     */
    fun initialize(activity: ComponentActivity) {
        activityRef = WeakReference(activity)
        fragmentRef = null
    }
    
    /**
     * Initialize with a Fragment.
     * Call this in onViewCreated before using the picker.
     */
    fun initialize(fragment: Fragment) {
        fragmentRef = WeakReference(fragment)
        activityRef = null
    }
    
    /**
     * Creates a [MediaPicker] for the registered Activity/Fragment.
     * Make sure to call [initialize] first.
     */
    actual fun create(): MediaPicker {
        fragmentRef?.get()?.let { fragment ->
            return AndroidMediaPicker.create(fragment)
        }
        
        activityRef?.get()?.let { activity ->
            return AndroidMediaPicker.create(activity)
        }
        
        throw IllegalStateException(
            "MediaPickerFactory not initialized. Call MediaPickerFactory.initialize(activity) " +
            "or MediaPickerFactory.initialize(fragment) before using the picker."
        )
    }
    
    /**
     * Creates a [MediaPicker] directly from a ComponentActivity.
     */
    fun create(activity: ComponentActivity): MediaPicker {
        return AndroidMediaPicker.create(activity)
    }
    
    /**
     * Creates a [MediaPicker] directly from a Fragment.
     */
    fun create(fragment: Fragment): MediaPicker {
        return AndroidMediaPicker.create(fragment)
    }
}

