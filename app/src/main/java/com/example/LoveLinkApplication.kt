package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoveLinkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Setup Global Uncaught Exception Handler to catch unexpected crashes gracefully
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("LoveLinkApp", "Unhandled exception in thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Initialize Firebase safely
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("LoveLinkApp", "Firebase initialized successfully.")
            }
            // Configure FCM topics
            CoroutineScope(Dispatchers.IO).launch {
                com.example.data.firebase.FcmManager.subscribeToTopic("global_announcements")
                com.example.data.firebase.FcmManager.subscribeToTopic("lovelink_matches")
            }
        } catch (e: Throwable) {
            Log.w("LoveLinkApp", "Firebase initialization skipped or unavailable: ${e.message}")
        }

        // Load saved Agora credentials if available
        val savedCallingConfig = com.example.util.EncryptedStorageManager.getCallingConfig(this)
        if (savedCallingConfig != null && savedCallingConfig.agoraAppId.isNotBlank()) {
            com.example.data.firebase.AgoraCallService.configure(
                appId = savedCallingConfig.agoraAppId,
                token = savedCallingConfig.agoraTempToken,
                channel = savedCallingConfig.agoraChannelName
            )
        } else {
            // Configure default Agora Call Engine
            com.example.data.firebase.AgoraCallService.configure(
                appId = "agora_lovelink_app_id_demo_2026",
                token = "agora_token_sample",
                channel = "lovelink_live_channel"
            )
        }

        // Load saved Cloudinary media storage credentials if available
        val savedStorageConfig = com.example.util.EncryptedStorageManager.getStorageConfig(this)
        if (savedStorageConfig != null &&
            savedStorageConfig.selectedProvider == com.example.data.model.StorageProvider.CLOUDINARY &&
            savedStorageConfig.cloudinaryCloudName.isNotBlank()
        ) {
            com.example.data.firebase.CloudinaryStorageService.configure(
                cloudName = savedStorageConfig.cloudinaryCloudName,
                uploadPreset = savedStorageConfig.cloudinaryUploadPreset
            )
        }
    }

    companion object {
        var instance: LoveLinkApplication? = null
            private set
    }
}
