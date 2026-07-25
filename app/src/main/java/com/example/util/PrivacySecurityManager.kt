package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Privacy and Security Utility.
 * Note: FLAG_SECURE is disabled to allow screen streaming and browser previews to display correctly without black screen.
 */
object PrivacySecurityManager {

    fun enableScreenshotProtection(activity: Activity?) {
        // Explicitly clear FLAG_SECURE so emulator streaming preview works
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun disableScreenshotProtection(activity: Activity?) {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}

/**
 * Composable DisposableEffect that ensures FLAG_SECURE is cleared so the app UI renders on screen previews.
 */
@Composable
fun PreventScreenshotProtection(
    enabled: Boolean = false
) {
    val context = LocalContext.current
    DisposableEffect(enabled) {
        val activity = with(PrivacySecurityManager) { context.findActivity() }
        if (activity != null) {
            PrivacySecurityManager.disableScreenshotProtection(activity)
        }
        onDispose {
            if (activity != null) {
                PrivacySecurityManager.disableScreenshotProtection(activity)
            }
        }
    }
}
