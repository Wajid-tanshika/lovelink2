package com.example.data.model

data class AdminNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val targetUserId: String? = null, // null means broadcast to ALL users
    val targetUserName: String? = null,
    val targetUserEmail: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val sentByEmail: String = "admin@lovelink.com"
)
