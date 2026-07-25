package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

/**
 * Central Manager to check Firebase readiness and coordinate Auth, Firestore, Storage, and FCM.
 * Ensures the app works gracefully whether google-services.json is present or pending.
 */
object FirebaseManager {
    private const val TAG = "FirebaseManager"

    /**
     * Checks if Firebase is properly initialized and available in the current runtime environment.
     */
    fun isFirebaseAvailable(context: Context? = null): Boolean {
        return try {
            val apps = if (context != null) FirebaseApp.getApps(context) else FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext)
            apps.isNotEmpty()
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Safe initialization call on Application startup.
     */
    fun initializeIfPossible(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.d(TAG, "Firebase initialized via FirebaseManager")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase initialization skipped (google-services.json may not be added yet): ${e.message}")
        }
    }
}
