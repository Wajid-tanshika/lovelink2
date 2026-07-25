package com.example.data.model

enum class StoryType {
    PHOTO, VIDEO, TEXT
}

data class StoryViewer(
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val viewedAtMillis: Long = System.currentTimeMillis()
)

data class StoryReaction(
    val userId: String,
    val userName: String,
    val emoji: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class StoryItem(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String,
    val isVerified: Boolean = false,
    val type: StoryType,
    val mediaUrl: String? = null,
    val caption: String? = null,
    val textContent: String? = null,
    val backgroundColorHex: String = "#FF1493",
    val textStyle: String = "NORMAL",
    val durationSeconds: Int = if (type == StoryType.VIDEO) 30 else 5,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val expiresAtMillis: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
    val viewers: List<StoryViewer> = emptyList(),
    val reactions: List<StoryReaction> = emptyList(),
    val isViewedByMe: Boolean = false
)

data class StoryGroup(
    val authorId: String,
    val authorName: String,
    val authorAvatar: String,
    val isVerified: Boolean = false,
    val isOwnGroup: Boolean = false,
    val stories: List<StoryItem> = emptyList(),
    val hasUnseen: Boolean = true
)
