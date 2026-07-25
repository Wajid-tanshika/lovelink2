package com.example.data.model

enum class NotificationType {
    MATCH, MESSAGE, SUPER_LIKE, PROFILE_VISIT, SYSTEM, LIKE, COMMENT, FOLLOW, MENTION, NEW_POST
}

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: NotificationType = NotificationType.SYSTEM,
    val senderAvatarUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetId: String? = null
)
