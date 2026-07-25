package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CallRepository {

    private val _activeCall = MutableStateFlow<CallSession?>(null)
    val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()

    private val _callHistory = MutableStateFlow<List<CallHistoryItem>>(sampleCallHistory())
    val callHistory: StateFlow<List<CallHistoryItem>> = _callHistory.asStateFlow()

    fun startCall(
        currentUser: UserProfile,
        targetUser: UserProfile,
        callType: CallType
    ): CallSession {
        val session = CallSession(
            id = "call_${UUID.randomUUID().toString().take(8)}",
            callType = callType,
            callerId = currentUser.id,
            callerName = currentUser.name,
            callerAvatar = currentUser.photoUrls.firstOrNull() ?: "",
            receiverId = targetUser.id,
            receiverName = targetUser.name,
            receiverAvatar = targetUser.photoUrls.firstOrNull() ?: "",
            state = CallState.DIALING
        )
        _activeCall.value = session
        return session
    }

    fun receiveCall(
        caller: UserProfile,
        currentUser: UserProfile,
        callType: CallType
    ): CallSession {
        val session = CallSession(
            id = "call_${UUID.randomUUID().toString().take(8)}",
            callType = callType,
            callerId = caller.id,
            callerName = caller.name,
            callerAvatar = caller.photoUrls.firstOrNull() ?: "",
            receiverId = currentUser.id,
            receiverName = currentUser.name,
            receiverAvatar = currentUser.photoUrls.firstOrNull() ?: "",
            state = CallState.INCOMING
        )
        _activeCall.value = session
        return session
    }

    fun acceptCall() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(state = CallState.ACTIVE)
    }

    fun declineCall(isMissed: Boolean = false) {
        val current = _activeCall.value ?: return
        val newState = if (isMissed) CallState.MISSED else CallState.REJECTED
        addToHistory(current.copy(state = newState))
        _activeCall.value = current.copy(state = newState)
        _activeCall.value = null
    }

    fun endCall(durationSeconds: Int) {
        val current = _activeCall.value ?: return
        val endedCall = current.copy(state = CallState.ENDED, durationSeconds = durationSeconds)
        addToHistory(endedCall)
        _activeCall.value = endedCall
        _activeCall.value = null
    }

    fun toggleMute() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isMuted = !current.isMuted)
    }

    fun toggleSpeaker() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isSpeakerOn = !current.isSpeakerOn)
    }

    fun switchCamera() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isFrontCamera = !current.isFrontCamera)
    }

    fun toggleVideo() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isVideoEnabled = !current.isVideoEnabled)
    }

    fun togglePip() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isPipMode = !current.isPipMode)
    }

    private fun addToHistory(session: CallSession) {
        val historyItem = CallHistoryItem(
            id = session.id,
            callType = session.callType,
            otherUserId = if (session.callerId == "user_me") session.receiverId else session.callerId,
            otherUserName = if (session.callerId == "user_me") session.receiverName else session.callerName,
            otherUserAvatar = if (session.callerId == "user_me") session.receiverAvatar else session.callerAvatar,
            isOutgoing = session.callerId == "user_me",
            isMissed = session.state == CallState.MISSED,
            durationSeconds = session.durationSeconds,
            timestampMillis = System.currentTimeMillis()
        )
        _callHistory.value = listOf(historyItem) + _callHistory.value
    }

    fun clearHistory() {
        _callHistory.value = emptyList()
    }

    private fun sampleCallHistory(): List<CallHistoryItem> {
        return listOf(
            CallHistoryItem("h1", CallType.VIDEO, "u2", "Sophia Martinez", "https://images.unsplash.com/photo-1534528741775-53994a69daeb", isOutgoing = false, isMissed = false, durationSeconds = 345, timestampMillis = System.currentTimeMillis() - 3600000),
            CallHistoryItem("h2", CallType.VOICE, "u3", "Liam Chen", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d", isOutgoing = true, isMissed = false, durationSeconds = 120, timestampMillis = System.currentTimeMillis() - 14400000),
            CallHistoryItem("h3", CallType.VIDEO, "u4", "Emma Watson", "https://images.unsplash.com/photo-1517841905240-472988babdf9", isOutgoing = false, isMissed = true, durationSeconds = 0, timestampMillis = System.currentTimeMillis() - 86400000)
        )
    }
}
