package com.example.ui.screens.livestream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.LiveStreamRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveStreamViewModel @JvmOverloads constructor(
    private val liveStreamRepository: LiveStreamRepository = LiveStreamRepository()
) : ViewModel() {

    val activeStreams: StateFlow<List<LiveStreamSession>> = liveStreamRepository.activeStreams
    val currentStream: StateFlow<LiveStreamSession?> = liveStreamRepository.currentStream
    val gifts: StateFlow<List<VirtualGift>> = liveStreamRepository.gifts

    private val _summary = MutableStateFlow<LiveSummary?>(null)
    val summary: StateFlow<LiveSummary?> = _summary.asStateFlow()

    private val _floatingHearts = MutableStateFlow<List<Long>>(emptyList())
    val floatingHearts: StateFlow<List<Long>> = _floatingHearts.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var durationTimerJob: Job? = null

    fun startLiveStream(host: UserProfile, title: String, category: String) {
        val stream = liveStreamRepository.startLiveStream(host, title, category)
        startStreamTimer(stream.id)
    }

    fun joinLiveStream(streamId: String, user: UserProfile) {
        liveStreamRepository.joinLiveStream(streamId, user)
    }

    fun leaveLiveStream(streamId: String) {
        liveStreamRepository.leaveLiveStream(streamId)
    }

    fun sendComment(streamId: String, sender: UserProfile, text: String) {
        if (text.isBlank()) return
        val current = currentStream.value
        if (current?.mutedUserIds?.contains(sender.id) == true) {
            _toastMessage.value = "You are muted in this live stream."
            return
        }
        if (current?.bannedUserIds?.contains(sender.id) == true) {
            _toastMessage.value = "You have been banned from this live stream."
            return
        }
        liveStreamRepository.sendComment(streamId, sender, text)
    }

    fun sendGift(streamId: String, sender: UserProfile, gift: VirtualGift, currentDiamonds: Int): Boolean {
        if (currentDiamonds < gift.diamondCost) {
            _toastMessage.value = "Insufficient diamonds! Need ${gift.diamondCost - currentDiamonds} more."
            return false
        }
        liveStreamRepository.sendGift(streamId, sender, gift)
        _toastMessage.value = "Sent ${gift.emoji} ${gift.name}!"
        return true
    }

    fun sendLike(streamId: String) {
        liveStreamRepository.sendLike(streamId)
        _floatingHearts.value = _floatingHearts.value + System.currentTimeMillis()
    }

    fun endLiveStream(streamId: String) {
        durationTimerJob?.cancel()
        val sum = liveStreamRepository.endLiveStream(streamId)
        _summary.value = sum
    }

    fun dismissSummary() {
        _summary.value = null
    }

    fun banUser(streamId: String, userId: String) {
        liveStreamRepository.banUserFromLive(streamId, userId)
        _toastMessage.value = "User banned from stream."
    }

    fun muteUser(streamId: String, userId: String) {
        liveStreamRepository.muteUserInLive(streamId, userId)
        _toastMessage.value = "User muted in stream."
    }

    fun adminEndLive(streamId: String) {
        liveStreamRepository.endLiveStream(streamId)
        _toastMessage.value = "Live stream terminated by Admin."
    }

    fun adminAddOrUpdateGift(gift: VirtualGift) {
        liveStreamRepository.adminAddOrUpdateGift(gift)
        _toastMessage.value = "Gift configuration updated!"
    }

    fun adminDeleteGift(giftId: String) {
        liveStreamRepository.adminDeleteGift(giftId)
        _toastMessage.value = "Gift removed."
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun startStreamTimer(streamId: String) {
        durationTimerJob?.cancel()
        durationTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val stream = currentStream.value
                if (stream != null && stream.id == streamId) {
                    val updated = stream.copy(durationSeconds = stream.durationSeconds + 1)
                    // Occasionally simulate random viewers & comments
                    val newViewers = if (updated.durationSeconds % 8 == 0) updated.viewerCount + (1..3).random() else updated.viewerCount
                    val newLikes = if (updated.durationSeconds % 3 == 0) updated.totalLikes + (1..5).random() else updated.totalLikes
                    // update internal state
                } else {
                    break
                }
            }
        }
    }
}

class LiveStreamViewModelFactory(
    private val liveStreamRepository: LiveStreamRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LiveStreamViewModel(liveStreamRepository) as T
    }
}
