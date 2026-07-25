package com.example.ui.screens.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CallRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallViewModel @JvmOverloads constructor(
    private val callRepository: CallRepository = CallRepository()
) : ViewModel() {

    val activeCall: StateFlow<CallSession?> = callRepository.activeCall
    val callHistory: StateFlow<List<CallHistoryItem>> = callRepository.callHistory

    private val _callDuration = MutableStateFlow(0)
    val callDuration: StateFlow<Int> = _callDuration.asStateFlow()

    private val _networkQuality = MutableStateFlow(NetworkQualityState())
    val networkQuality: StateFlow<NetworkQualityState> = _networkQuality.asStateFlow()

    private var timerJob: Job? = null
    private var networkMonitorJob: Job? = null

    init {
        viewModelScope.launch {
            activeCall.collect { call ->
                val appContext = com.example.LoveLinkApplication.instance
                if (call?.state == CallState.ACTIVE) {
                    startTimer()
                    startNetworkMonitoring()
                    val otherUserName = if (call.callerId == "current_user_1") call.receiverName else call.callerName
                    com.example.service.VideoCallForegroundService.startCallService(
                        context = appContext,
                        callerName = otherUserName,
                        callType = call.callType.name
                    )
                } else if (call == null || call.state == CallState.ENDED) {
                    stopTimer()
                    stopNetworkMonitoring()
                    com.example.service.VideoCallForegroundService.stopCallService(appContext)
                }
            }
        }
    }

    private fun startNetworkMonitoring() {
        if (networkMonitorJob?.isActive == true) return
        networkMonitorJob = viewModelScope.launch {
            var counter = 0
            while (true) {
                delay(2500)
                counter++
                // Realistic dynamic network metric fluctuations
                val rtt = (20..42).random()
                val uplink = (2100..2900).random()
                val downlink = (2800..3800).random()
                val fps = if (counter % 7 == 0) 28 else 30
                val loss = (0..15).random() / 100f
                val level = when {
                    rtt < 30 && loss < 0.1f -> NetworkQualityLevel.EXCELLENT
                    rtt < 55 -> NetworkQualityLevel.GOOD
                    rtt < 100 -> NetworkQualityLevel.FAIR
                    else -> NetworkQualityLevel.POOR
                }
                _networkQuality.value = NetworkQualityState(
                    level = level,
                    rttMs = rtt,
                    uplinkKbps = uplink,
                    downlinkKbps = downlink,
                    fps = fps,
                    resolution = if (activeCall.value?.callType == CallType.VIDEO) "1080p HD" else "Audio 48kHz",
                    packetLossPercent = loss
                )
            }
        }
    }

    private fun stopNetworkMonitoring() {
        networkMonitorJob?.cancel()
        networkMonitorJob = null
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        _callDuration.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _callDuration.value += 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun startCall(currentUser: UserProfile, targetUser: UserProfile, callType: CallType) {
        val session = callRepository.startCall(currentUser, targetUser, callType)
        // Initialize Agora RTC Session
        com.example.data.firebase.AgoraCallService.initializeCallSession(
            context = com.example.LoveLinkApplication.instance,
            session = session
        )
        // Send FCM Push Notification for incoming video/voice call to target user
        com.example.data.firebase.FcmManager.sendIncomingCallPushNotification(
            callerName = currentUser.name,
            callType = callType.name,
            callId = session.id
        )
        // Simulate auto accept after 3 seconds for demo
        viewModelScope.launch {
            delay(3000)
            if (activeCall.value?.state == CallState.DIALING) {
                callRepository.acceptCall()
            }
        }
    }

    fun receiveCall(caller: UserProfile, currentUser: UserProfile, callType: CallType) {
        val session = callRepository.receiveCall(caller, currentUser, callType)
        com.example.data.firebase.AgoraCallService.initializeCallSession(
            context = com.example.LoveLinkApplication.instance,
            session = session
        )
        com.example.data.firebase.FcmManager.sendIncomingCallPushNotification(
            callerName = caller.name,
            callType = callType.name,
            callId = session.id
        )
    }

    fun acceptCall() {
        callRepository.acceptCall()
    }

    fun declineCall(isMissed: Boolean = false) {
        com.example.data.firebase.AgoraCallService.leaveChannel(activeCall.value)
        stopTimer()
        callRepository.declineCall(isMissed)
    }

    fun endCall() {
        com.example.data.firebase.AgoraCallService.leaveChannel(activeCall.value)
        val duration = _callDuration.value
        stopTimer()
        callRepository.endCall(duration)
    }

    fun toggleMute() {
        callRepository.toggleMute()
        val muted = activeCall.value?.isMuted ?: false
        com.example.data.firebase.AgoraCallService.muteLocalAudio(muted)
    }

    fun toggleSpeaker() {
        callRepository.toggleSpeaker()
        val speakerOn = activeCall.value?.isSpeakerOn ?: false
        com.example.data.firebase.AgoraCallService.setSpeakerphoneOn(speakerOn)
    }

    fun switchCamera() {
        callRepository.switchCamera()
        com.example.data.firebase.AgoraCallService.switchCameraFacing()
    }

    fun toggleVideo() {
        callRepository.toggleVideo()
        val enabled = activeCall.value?.isVideoEnabled ?: true
        com.example.data.firebase.AgoraCallService.enableLocalVideo(enabled)
    }

    fun togglePip() = callRepository.togglePip()
    fun clearHistory() = callRepository.clearHistory()
}

class CallViewModelFactory(
    private val callRepository: CallRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CallViewModel(callRepository) as T
    }
}
