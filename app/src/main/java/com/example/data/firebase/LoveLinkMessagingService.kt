package com.example.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.LoveLinkApplication
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service for handling background/foreground push notifications.
 * Automatically handles push tokens and shows rich interactive notifications for match updates,
 * chat messages, and admin broadcasts.
 */
class LoveLinkMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Registration Token generated: $token")
        
        // Save FCM token locally / send to Firestore if user is authenticated
        FcmManager.handleNewFcmToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message Received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val messageType = data["type"] ?: remoteMessage.notification?.tag

        if (messageType == "incoming_call" || data.containsKey("caller_name")) {
            val callerName = data["caller_name"] ?: remoteMessage.notification?.title ?: "Incoming Call"
            val callType = data["call_type"] ?: "video"
            val callId = data["call_id"] ?: "call_${System.currentTimeMillis()}"
            showIncomingCallNotification(callerName, callType, callId, data)
        } else {
            // Check if message contains notification payload
            var title = remoteMessage.notification?.title
            var body = remoteMessage.notification?.body

            // Fallback to data payload if notification payload is empty
            if (title.isNullOrEmpty() && remoteMessage.data.isNotEmpty()) {
                title = remoteMessage.data["title"] ?: remoteMessage.data["subject"] ?: "LoveLink Notification"
            }
            if (body.isNullOrEmpty() && remoteMessage.data.isNotEmpty()) {
                body = remoteMessage.data["body"] ?: remoteMessage.data["message"] ?: ""
            }

            if (!title.isNullOrEmpty() || !body.isNullOrEmpty()) {
                showLocalHeadsUpNotification(
                    title = title ?: "LoveLink",
                    body = body ?: "",
                    data = remoteMessage.data
                )
            }
        }
    }

    private fun showIncomingCallNotification(
        callerName: String,
        callType: String,
        callId: String,
        data: Map<String, String>
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Incoming Call Notification Channel (High Importance & Ringtone/Vibration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val callChannel = NotificationChannel(
                CALL_CHANNEL_ID,
                "Incoming Video & Voice Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority incoming video/voice call alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                setSound(
                    android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE),
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(callChannel)
        }

        // Full screen / launch Intent for incoming call
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("screen", "call")
            putExtra("caller_name", callerName)
            putExtra("call_type", callType)
            putExtra("call_id", callId)
            data.forEach { (k, v) -> putExtra(k, v) }
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            callId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val callTypeLabel = if (callType.equals("video", ignoreCase = true)) "Incoming Video Call 📹" else "Incoming Voice Call 📞"

        val notificationBuilder = NotificationCompat.Builder(this, CALL_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(callerName)
            .setContentText(callTypeLabel)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)

        notificationManager.notify(INCOMING_CALL_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun showLocalHeadsUpNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel on Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "LoveLink Matches, Messages & System Notifications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to launch MainActivity when clicking notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "LoveLinkMessagingService"
        const val CHANNEL_ID = "lovelink_notifications_channel"
        const val CHANNEL_NAME = "LoveLink Push Notifications"
        const val CALL_CHANNEL_ID = "lovelink_incoming_call_channel"
        const val INCOMING_CALL_NOTIFICATION_ID = 2001
    }
}
