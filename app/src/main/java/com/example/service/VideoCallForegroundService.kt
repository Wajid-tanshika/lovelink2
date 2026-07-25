package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

/**
 * Foreground Service that manages active video and voice calls in the background,
 * ensuring real-time Agora media connection remains active while user navigates through the app.
 */
class VideoCallForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "VideoCallForegroundService onStartCommand action: $action")

        when (action) {
            ACTION_START_CALL_SERVICE -> {
                val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Matched Connection"
                val callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "Video Call"
                startForegroundWithNotification(callerName, callType)
            }
            ACTION_STOP_CALL_SERVICE -> {
                stopForegroundService()
            }
            else -> {
                val callerName = intent?.getStringExtra(EXTRA_CALLER_NAME) ?: "Matched Connection"
                val callType = intent?.getStringExtra(EXTRA_CALL_TYPE) ?: "Video Call"
                startForegroundWithNotification(callerName, callType)
            }
        }

        return START_STICKY
    }

    private fun startForegroundWithNotification(callerName: String, callType: String) {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("screen", "call")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, VideoCallForegroundService::class.java).apply {
            this.action = ACTION_STOP_CALL_SERVICE
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            102,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subTitle = if (callType.equals("VIDEO", ignoreCase = true) || callType.contains("Video")) {
            "Active Video Call in Progress 📹"
        } else {
            "Active Voice Call in Progress 📞"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("LoveLink • $callerName")
            .setContentText(subTitle)
            .setSubText("Tap to return to call screen")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(R.mipmap.ic_launcher, "End Call", stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
            }
            try {
                startForeground(NOTIFICATION_ID, notification, foregroundServiceType)
            } catch (e: Exception) {
                Log.w(TAG, "Fallback to default startForeground: ${e.message}")
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ongoing Video & Voice Calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent status while video or voice call is active in background"
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "VideoCallService"
        private const val CHANNEL_ID = "lovelink_active_call_channel"
        private const val NOTIFICATION_ID = 3001

        const val ACTION_START_CALL_SERVICE = "com.example.service.START_CALL"
        const val ACTION_STOP_CALL_SERVICE = "com.example.service.STOP_CALL"
        const val EXTRA_CALLER_NAME = "extra_caller_name"
        const val EXTRA_CALL_TYPE = "extra_call_type"

        fun startCallService(context: Context?, callerName: String, callType: String) {
            val validContext = context ?: com.example.LoveLinkApplication.instance ?: return
            try {
                val intent = Intent(validContext, VideoCallForegroundService::class.java).apply {
                    action = ACTION_START_CALL_SERVICE
                    putExtra(EXTRA_CALLER_NAME, callerName)
                    putExtra(EXTRA_CALL_TYPE, callType)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    validContext.startForegroundService(intent)
                } else {
                    validContext.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start call foreground service: ${e.message}")
            }
        }

        fun stopCallService(context: Context?) {
            val validContext = context ?: com.example.LoveLinkApplication.instance ?: return
            try {
                val intent = Intent(validContext, VideoCallForegroundService::class.java).apply {
                    action = ACTION_STOP_CALL_SERVICE
                }
                validContext.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop call foreground service: ${e.message}")
            }
        }
    }
}
