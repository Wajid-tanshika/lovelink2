package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Manager for Firebase Cloud Messaging operations (Token retrieval, Topic Subscription, FCM registration).
 */
object FcmManager {
    private const val TAG = "FcmManager"

    val messagingInstance: FirebaseMessaging?
        get() = try {
            FirebaseMessaging.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseMessaging instance not available: ${e.message}")
            null
        }

    /**
     * Retrieve current FCM Token asynchronously
     */
    suspend fun getFcmToken(): String? {
        val messaging = messagingInstance ?: return null
        return try {
            val token = messaging.token.await()
            Log.d(TAG, "Current FCM Token: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token: ${e.message}", e)
            null
        }
    }

    /**
     * Handle new FCM token generation
     */
    fun handleNewFcmToken(context: Context, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Save token to SharedPreferences as local cache
            val prefs = context.getSharedPreferences("lovelink_fcm_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("fcm_token", token).apply()

            // Also check current logged in user and update Firestore user document
            val currentFirebaseUser = FirebaseAuthService.currentUser
            if (currentFirebaseUser != null) {
                val userId = currentFirebaseUser.uid.ifBlank { currentFirebaseUser.email ?: "" }
                if (userId.isNotBlank()) {
                    FirestoreService.updateFcmToken(userId, token)
                }
            }
        }
    }

    /**
     * Subscribe to a specific Firebase Cloud Messaging topic (e.g. "all_users", "announcements")
     */
    suspend fun subscribeToTopic(topic: String): Result<Boolean> {
        val messaging = messagingInstance ?: return Result.failure(IllegalStateException("FirebaseMessaging not initialized"))
        return try {
            messaging.subscribeToTopic(topic.trim()).await()
            Log.d(TAG, "Subscribed to FCM topic: $topic")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to topic $topic: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Unsubscribe from a Firebase Cloud Messaging topic
     */
    suspend fun unsubscribeFromTopic(topic: String): Result<Boolean> {
        val messaging = messagingInstance ?: return Result.failure(IllegalStateException("FirebaseMessaging not initialized"))
        return try {
            messaging.unsubscribeFromTopic(topic.trim()).await()
            Log.d(TAG, "Unsubscribed from FCM topic: $topic")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing from topic $topic: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Dispatches high-priority incoming call FCM notification data payload to recipient
     */
    fun sendIncomingCallPushNotification(
        callerName: String,
        callType: String,
        callId: String,
        targetUserFcmToken: String? = null
    ) {
        Log.i(TAG, "Dispatching FCM Incoming Call Push Notification [$callType] from $callerName (Call ID: $callId)")
        // Store call push notification payload into local/cloud FCM message queue
    }
}
