package com.example.data.repository

import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.source.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationRepository {

    private val initialNotifications = listOf(
        NotificationItem(
            id = "notif_1",
            userId = "user_me",
            title = "New Match! 🎉",
            body = "You and Sophia Chen matched with each other!",
            type = NotificationType.MATCH,
            senderAvatarUrl = SampleData.PROFILES[0].photoUrls.firstOrNull(),
            timestamp = System.currentTimeMillis() - 1000 * 60 * 20,
            isRead = false
        ),
        NotificationItem(
            id = "notif_2",
            userId = "user_me",
            title = "Super Like Received ⭐",
            body = "Maya Lin super liked your profile!",
            type = NotificationType.SUPER_LIKE,
            senderAvatarUrl = SampleData.PROFILES[2].photoUrls.firstOrNull(),
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 3,
            isRead = true
        ),
        NotificationItem(
            id = "notif_3",
            userId = "user_me",
            title = "Profile Visit 👀",
            body = "Elena Rostova checked out your profile photo.",
            type = NotificationType.PROFILE_VISIT,
            senderAvatarUrl = SampleData.PROFILES[1].photoUrls.firstOrNull(),
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 8,
            isRead = true
        )
    )

    private val _notifications = MutableStateFlow<List<NotificationItem>>(initialNotifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun addNotification(title: String, body: String, type: NotificationType, avatarUrl: String? = null) {
        val newNotif = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            userId = "user_me",
            title = title,
            body = body,
            type = type,
            senderAvatarUrl = avatarUrl,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }
}
