package com.example.data.firebase

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import com.example.data.model.AppSettings
import com.example.data.model.CallSession
import com.example.data.model.CallType
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Agora Voice & Video Calling Manager backed by the real Agora RTC
 * Engine (io.agora.rtc:full-sdk). Configure App ID / temp token via
 * [configure] (wired from the Admin Panel -> Calling Provider config,
 * or from LoveLinkApplication on startup).
 *
 * IMPORTANT (production): temporary tokens expire quickly and are only
 * meant for testing. For a Play Store release you need a small backend
 * (e.g. a Firebase Cloud Function) that mints Agora tokens on demand
 * using your App Certificate, and this service should fetch a fresh
 * token from that endpoint before each joinChannel call instead of
 * using a hardcoded token.
 */
object AgoraCallService {
    private const val TAG = "AgoraCallService"

    private var agoraAppId: String = ""
    private var agoraToken: String = ""
    private var defaultChannel: String = "lovelink_call_channel"

    private var rtcEngine: RtcEngine? = null

    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid: StateFlow<Int?> = _remoteUid.asStateFlow()

    private val _isJoined = MutableStateFlow(false)
    val isJoined: StateFlow<Boolean> = _isJoined.asStateFlow()

    private val eventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "Joined Agora channel $channel as uid=$uid")
            _isJoined.value = true
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "Remote user joined: $uid")
            _remoteUid.value = uid
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "Remote user left: $uid (reason=$reason)")
            if (_remoteUid.value == uid) _remoteUid.value = null
        }

        override fun onError(err: Int) {
            Log.w(TAG, "Agora RTC error code: $err")
        }
    }

    fun configure(appId: String, token: String, channel: String = "lovelink_call_channel") {
        agoraAppId = appId.trim()
        agoraToken = token.trim()
        if (channel.isNotBlank()) defaultChannel = channel.trim()
        Log.d(TAG, "Agora RTC configured. AppId set: ${agoraAppId.isNotBlank()}, Channel: $defaultChannel")
    }

    fun configureFromSettings(settings: AppSettings) {
        configure(appId = settings.agoraAppId, token = settings.agoraToken, channel = settings.agoraChannelName)
    }

    fun getChannelNameForSession(sessionId: String): String =
        if (sessionId.isNotBlank()) "lovelink_$sessionId" else defaultChannel

    fun isAgoraConfigured(): Boolean = agoraAppId.isNotBlank()

    fun getAppId(): String = agoraAppId
    fun getToken(): String = agoraToken

    /** Lazily creates the RtcEngine instance. Call once you have a valid App ID. */
    private fun ensureEngine(context: Context): RtcEngine? {
        if (!isAgoraConfigured()) {
            Log.w(TAG, "Agora not configured - set an App ID in Admin Panel > Calling Provider before starting calls.")
            return null
        }
        if (rtcEngine != null) return rtcEngine
        return try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = agoraAppId
                mEventHandler = eventHandler
            }
            rtcEngine = RtcEngine.create(config)
            rtcEngine
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create RtcEngine: ${e.message}", e)
            null
        }
    }

    /**
     * Initialize the engine and join the channel for this call session.
     * Uses uid = 0 so Agora auto-assigns a unique numeric uid per user.
     */
    fun initializeCallSession(context: Context?, session: CallSession): Boolean {
        val ctx = context ?: return false
        val engine = ensureEngine(ctx) ?: return false

        engine.enableAudio()
        if (session.callType == CallType.VIDEO) {
            engine.enableVideo()
        } else {
            engine.disableVideo()
        }

        val channelName = getChannelNameForSession(session.id)
        val options = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            publishMicrophoneTrack = true
            publishCameraTrack = session.callType == CallType.VIDEO
            autoSubscribeAudio = true
            autoSubscribeVideo = true
        }

        val tokenToUse = agoraToken.ifBlank { null }
        val result = engine.joinChannel(tokenToUse, channelName, 0, options)
        Log.d(TAG, "joinChannel($channelName) result code: $result (0 = success)")
        return result == 0
    }

    /** Bind the local camera preview to a SurfaceView. */
    fun setupLocalVideo(surfaceView: SurfaceView) {
        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        rtcEngine?.startPreview()
    }

    /** Bind a remote participant's video to a SurfaceView. */
    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    fun createRendererView(context: Context): SurfaceView = RtcEngine.CreateRendererView(context)

    fun muteLocalAudio(muted: Boolean) {
        rtcEngine?.muteLocalAudioStream(muted)
    }

    fun enableLocalVideo(enabled: Boolean) {
        rtcEngine?.enableLocalVideo(enabled)
        rtcEngine?.muteLocalVideoStream(!enabled)
    }

    fun setSpeakerphoneOn(on: Boolean) {
        rtcEngine?.setEnableSpeakerphone(on)
    }

    fun switchCameraFacing() {
        rtcEngine?.switchCamera()
    }

    /** Leave the current call channel (engine instance is kept alive for reuse). */
    fun leaveChannel(session: CallSession?) {
        rtcEngine?.stopPreview()
        rtcEngine?.leaveChannel()
        _isJoined.value = false
        _remoteUid.value = null
        if (session != null) {
            Log.d(TAG, "Left Agora channel for session: ${session.id}")
        }
    }

    /** Fully tear down the engine, e.g. on app process exit. */
    fun destroyEngine() {
        RtcEngine.destroy()
        rtcEngine = null
        _isJoined.value = false
        _remoteUid.value = null
    }
}
