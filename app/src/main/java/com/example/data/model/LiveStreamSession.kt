package com.example.data.model

data class LiveComment(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val text: String,
    val isGiftNotice: Boolean = false,
    val giftEmoji: String? = null,
    val giftName: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class LiveGiftEvent(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val giftId: String,
    val giftName: String,
    val giftEmoji: String,
    val diamondCost: Int,
    val animationType: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class LiveStreamSession(
    val id: String,
    val hostId: String,
    val hostName: String,
    val hostAvatar: String,
    val isVerified: Boolean = false,
    val title: String,
    val category: String = "Casual Chat",
    val viewerCount: Int = 1,
    val totalLikes: Int = 0,
    val totalDiamondsEarned: Int = 0,
    val isLive: Boolean = true,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isFrontCamera: Boolean = true,
    val comments: List<LiveComment> = emptyList(),
    val giftEvents: List<LiveGiftEvent> = emptyList(),
    val bannedUserIds: List<String> = emptyList(),
    val mutedUserIds: List<String> = emptyList(),
    val viewers: List<UserProfile> = emptyList()
)

data class LiveSummary(
    val streamId: String,
    val hostName: String,
    val durationSeconds: Int,
    val peakViewers: Int,
    val totalLikes: Int,
    val diamondsEarned: Int,
    val newFollowersGained: Int
)
