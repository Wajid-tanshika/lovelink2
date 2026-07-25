package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class LiveStreamRepository(
    private val notificationRepository: NotificationRepository? = null
) {

    private val _activeStreams = MutableStateFlow<List<LiveStreamSession>>(sampleLiveStreams())
    val activeStreams: StateFlow<List<LiveStreamSession>> = _activeStreams.asStateFlow()

    private val _currentStream = MutableStateFlow<LiveStreamSession?>(null)
    val currentStream: StateFlow<LiveStreamSession?> = _currentStream.asStateFlow()

    private val _gifts = MutableStateFlow<List<VirtualGift>>(VirtualGift.DEFAULT_GIFTS)
    val gifts: StateFlow<List<VirtualGift>> = _gifts.asStateFlow()

    fun startLiveStream(
        host: UserProfile,
        title: String,
        category: String
    ): LiveStreamSession {
        val newStream = LiveStreamSession(
            id = "live_${UUID.randomUUID().toString().take(8)}",
            hostId = host.id,
            hostName = host.name,
            hostAvatar = host.photoUrls.firstOrNull() ?: "",
            isVerified = host.isVerified,
            title = title,
            category = category,
            viewerCount = 1,
            totalLikes = 0,
            totalDiamondsEarned = 0,
            isLive = true
        )
        _activeStreams.value = listOf(newStream) + _activeStreams.value
        _currentStream.value = newStream

        // Send live notification to followers
        notificationRepository?.addNotification(
            title = "🔴 ${host.name} is Live!",
            body = "Join ${host.name}'s live stream now: \"$title\"",
            type = com.example.data.model.NotificationType.SYSTEM,
            avatarUrl = host.photoUrls.firstOrNull()
        )

        return newStream
    }

    fun joinLiveStream(streamId: String, user: UserProfile) {
        val stream = _activeStreams.value.find { it.id == streamId } ?: return
        val updatedViewers = stream.viewers + user
        val updated = stream.copy(
            viewerCount = stream.viewerCount + 1,
            viewers = updatedViewers
        )
        updateStreamInList(updated)
        _currentStream.value = updated
    }

    fun leaveLiveStream(streamId: String) {
        val stream = _currentStream.value ?: return
        if (stream.id == streamId) {
            val updated = stream.copy(
                viewerCount = (stream.viewerCount - 1).coerceAtLeast(0)
            )
            updateStreamInList(updated)
            _currentStream.value = null
        }
    }

    fun sendComment(streamId: String, sender: UserProfile, text: String) {
        val stream = _currentStream.value ?: return
        if (stream.id != streamId) return

        val comment = LiveComment(
            id = "c_${UUID.randomUUID().toString().take(6)}",
            senderId = sender.id,
            senderName = sender.name,
            senderAvatar = sender.photoUrls.firstOrNull() ?: "",
            text = text
        )

        val updated = stream.copy(
            comments = stream.comments + comment
        )
        updateStreamInList(updated)
        _currentStream.value = updated
    }

    fun sendGift(
        streamId: String,
        sender: UserProfile,
        gift: VirtualGift
    ): LiveGiftEvent? {
        val stream = _currentStream.value ?: return null
        if (stream.id != streamId) return null

        val giftEvent = LiveGiftEvent(
            id = "g_${UUID.randomUUID().toString().take(6)}",
            senderId = sender.id,
            senderName = sender.name,
            senderAvatar = sender.photoUrls.firstOrNull() ?: "",
            giftId = gift.id,
            giftName = gift.name,
            giftEmoji = gift.emoji,
            diamondCost = gift.diamondCost,
            animationType = gift.animationType
        )

        val giftNoticeComment = LiveComment(
            id = "c_g_${UUID.randomUUID().toString().take(6)}",
            senderId = sender.id,
            senderName = sender.name,
            senderAvatar = sender.photoUrls.firstOrNull() ?: "",
            text = "sent ${gift.emoji} ${gift.name}!",
            isGiftNotice = true,
            giftEmoji = gift.emoji,
            giftName = gift.name
        )

        val updated = stream.copy(
            totalDiamondsEarned = stream.totalDiamondsEarned + gift.diamondCost,
            giftEvents = stream.giftEvents + giftEvent,
            comments = stream.comments + giftNoticeComment
        )
        updateStreamInList(updated)
        _currentStream.value = updated
        return giftEvent
    }

    fun sendLike(streamId: String) {
        val stream = _currentStream.value ?: return
        if (stream.id != streamId) return
        val updated = stream.copy(totalLikes = stream.totalLikes + 1)
        updateStreamInList(updated)
        _currentStream.value = updated
    }

    fun endLiveStream(streamId: String): LiveSummary? {
        val stream = _activeStreams.value.find { it.id == streamId } ?: _currentStream.value ?: return null
        val summary = LiveSummary(
            streamId = stream.id,
            hostName = stream.hostName,
            durationSeconds = stream.durationSeconds,
            peakViewers = stream.viewerCount.coerceAtLeast(12),
            totalLikes = stream.totalLikes,
            diamondsEarned = stream.totalDiamondsEarned,
            newFollowersGained = (stream.totalLikes / 15).coerceAtLeast(2)
        )

        val updatedStreams = _activeStreams.value.filterNot { it.id == streamId }
        _activeStreams.value = updatedStreams
        if (_currentStream.value?.id == streamId) {
            _currentStream.value = null
        }
        return summary
    }

    fun banUserFromLive(streamId: String, userId: String) {
        val stream = _currentStream.value ?: return
        val updated = stream.copy(
            bannedUserIds = stream.bannedUserIds + userId,
            viewerCount = (stream.viewerCount - 1).coerceAtLeast(0)
        )
        updateStreamInList(updated)
        _currentStream.value = updated
    }

    fun muteUserInLive(streamId: String, userId: String) {
        val stream = _currentStream.value ?: return
        val updated = stream.copy(
            mutedUserIds = stream.mutedUserIds + userId
        )
        updateStreamInList(updated)
        _currentStream.value = updated
    }

    // Admin Panel Management
    fun adminAddOrUpdateGift(gift: VirtualGift) {
        val currentGifts = _gifts.value.toMutableList()
        val index = currentGifts.indexOfFirst { it.id == gift.id }
        if (index != -1) {
            currentGifts[index] = gift
        } else {
            currentGifts.add(gift)
        }
        _gifts.value = currentGifts
    }

    fun adminDeleteGift(giftId: String) {
        _gifts.value = _gifts.value.filterNot { it.id == giftId }
    }

    private fun updateStreamInList(updated: LiveStreamSession) {
        _activeStreams.value = _activeStreams.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    private fun sampleLiveStreams(): List<LiveStreamSession> {
        return listOf(
            LiveStreamSession(
                id = "live_1",
                hostId = "u2",
                hostName = "Sophia Martinez",
                hostAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                isVerified = true,
                title = "Late Night Romance Q&A 💕 | Ask Me Anything!",
                category = "Romance & Chill",
                viewerCount = 142,
                totalLikes = 890,
                totalDiamondsEarned = 1250,
                isLive = true
            ),
            LiveStreamSession(
                id = "live_2",
                hostId = "u3",
                hostName = "Liam Chen",
                hostAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                isVerified = false,
                title = "Acoustic Guitar Sessions 🎸 Send your song requests!",
                category = "Music & Vibes",
                viewerCount = 88,
                totalLikes = 430,
                totalDiamondsEarned = 600,
                isLive = true
            )
        )
    }
}
