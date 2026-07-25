package com.example.data.model

enum class CallType {
    VOICE, VIDEO
}

enum class CallState {
    IDLE, DIALING, INCOMING, ACTIVE, ENDED, MISSED, REJECTED
}

data class CallSession(
    val id: String,
    val callType: CallType,
    val callerId: String,
    val callerName: String,
    val callerAvatar: String,
    val receiverId: String,
    val receiverName: String,
    val receiverAvatar: String,
    val state: CallState = CallState.DIALING,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isBluetoothConnected: Boolean = false,
    val isFrontCamera: Boolean = true,
    val isVideoEnabled: Boolean = true,
    val isPipMode: Boolean = false,
    val startTimeMillis: Long = System.currentTimeMillis()
)

data class CallHistoryItem(
    val id: String,
    val callType: CallType,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserAvatar: String,
    val isOutgoing: Boolean,
    val isMissed: Boolean,
    val durationSeconds: Int,
    val timestampMillis: Long = System.currentTimeMillis()
)
