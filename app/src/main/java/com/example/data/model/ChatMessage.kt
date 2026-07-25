package com.example.data.model

data class ChatMessage(
    val id: String = "",
    val matchId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDelivered: Boolean = true,
    val isSeen: Boolean = false,
    val isSystem: Boolean = false,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val deletedForUserIds: List<String> = emptyList(),
    val isDeletedForEveryone: Boolean = false
)
