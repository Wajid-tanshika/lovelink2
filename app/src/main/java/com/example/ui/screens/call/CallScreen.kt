package com.example.ui.screens.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.firebase.AgoraCallService
import com.example.data.model.CallSession
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.data.model.CallState
import com.example.data.model.CallType
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary
import com.example.util.PreventScreenshotProtection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CallScreen(
    callViewModel: CallViewModel,
    currentUser: com.example.data.model.UserProfile = com.example.data.source.SampleData.CURRENT_USER,
    partner: com.example.data.model.UserProfile = com.example.data.source.SampleData.PROFILES.first(),
    isVideo: Boolean = false,
    onEndCallClick: () -> Unit
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val neededPermissions = if (isVideo) {
        listOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA)
    } else {
        listOf(android.Manifest.permission.RECORD_AUDIO)
    }
    val permissionsState = rememberMultiplePermissionsState(neededPermissions)

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(partner.id, isVideo, permissionsState.allPermissionsGranted) {
        if (!permissionsState.allPermissionsGranted) return@LaunchedEffect
        val callType = if (isVideo) com.example.data.model.CallType.VIDEO else com.example.data.model.CallType.VOICE
        callViewModel.startCall(currentUser, partner, callType)
    }

    if (!permissionsState.allPermissionsGranted) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Microphone${if (isVideo) " & Camera" else ""} permission is needed to place this call.", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(24.dp))
                Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) { Text("Grant Permission") }
            }
        }
        return
    }

    val activeCall by callViewModel.activeCall.collectAsState()
    val durationSeconds by callViewModel.callDuration.collectAsState()
    val networkQuality by callViewModel.networkQuality.collectAsState()
    val remoteUid by AgoraCallService.remoteUid.collectAsState()
    val context = LocalContext.current

    val call = activeCall

    if (call == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("No active call", color = Color.White)
        }
        return
    }

    // Format call duration MM:SS
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    // Pulse animation for dialing/incoming
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        if (call.callType == CallType.VIDEO && call.isVideoEnabled) {
            if (AgoraCallService.isAgoraConfigured() && remoteUid != null) {
                AndroidView(
                    factory = { ctx ->
                        AgoraCallService.createRendererView(ctx).also { surfaceView ->
                            AgoraCallService.setupRemoteVideo(surfaceView, remoteUid!!)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Simulated video preview shown until the remote participant's
                // Agora video stream connects (or when Agora isn't configured yet)
                AsyncImage(
                    model = if (call.isFrontCamera) call.receiverAvatar else "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
                    contentDescription = "Video Stream",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dark overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // PIP Preview Window (Front Camera Self View)
            if (call.isPipMode || call.state == CallState.ACTIVE) {
                Box(
                    modifier = Modifier
                        .padding(top = 48.dp, end = 20.dp)
                        .size(width = 110.dp, height = 160.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                ) {
                    if (AgoraCallService.isAgoraConfigured()) {
                        AndroidView(
                            factory = { ctx ->
                                AgoraCallService.createRendererView(ctx).also { surfaceView ->
                                    AgoraCallService.setupLocalVideo(surfaceView)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = call.callerAvatar,
                            contentDescription = "Self Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = "You",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            // Voice Call Audio Canvas
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.scale(if (call.state == CallState.DIALING) pulseScale else 1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(RosePrimary.copy(alpha = 0.2f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(VioletSecondary.copy(alpha = 0.3f), CircleShape)
                    )
                    AsyncImage(
                        model = call.receiverAvatar,
                        contentDescription = call.receiverName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, RosePrimary, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = call.receiverName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                val statusText = when (call.state) {
                    CallState.DIALING -> "Ringing..."
                    CallState.INCOMING -> "Incoming Voice Call..."
                    CallState.ACTIVE -> "Encrypted • $timeFormatted"
                    CallState.ENDED -> "Call Ended"
                    else -> "Connecting..."
                }

                Surface(
                    color = if (call.state == CallState.ACTIVE) Color(0xFF1B5E20) else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (call.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Top Navigation & Call Type Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { callViewModel.togglePip() },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.PictureInPictureAlt, contentDescription = "PiP", tint = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (call.callType == CallType.VIDEO) "HD Video Call (Agora RTC)" else "Voice Call (Agora RTC)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = if (call.state == CallState.ACTIVE) "Agora Connected • $timeFormatted" else "Agora Channel: ${com.example.data.firebase.AgoraCallService.getChannelNameForSession(call.id)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF81D4FA)
                )
                if (call.state == CallState.ACTIVE) {
                    Spacer(modifier = Modifier.height(6.dp))
                    com.example.ui.components.NetworkQualityIndicator(networkState = networkQuality)
                }
            }

            IconButton(
                onClick = { callViewModel.switchCamera() },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White)
            }
        }

        // Bottom Controls Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 20.dp, end = 20.dp)
        ) {
            if (call.state == CallState.INCOMING) {
                // Incoming Accept / Decline controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline Button
                    FloatingActionButton(
                        onClick = {
                            callViewModel.declineCall()
                            onEndCallClick()
                        },
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Decline", modifier = Modifier.size(32.dp))
                    }

                    // Accept Button
                    FloatingActionButton(
                        onClick = { callViewModel.acceptCall() },
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Accept", modifier = Modifier.size(32.dp))
                    }
                }
            } else {
                // Active / Dialing Call Actions Bar
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute Mic Toggle
                        IconButton(
                            onClick = { callViewModel.toggleMute() },
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (call.isMuted) Color.White else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (call.isMuted) Color.Black else Color.White
                            )
                        }

                        // Speaker / Bluetooth Toggle
                        IconButton(
                            onClick = { callViewModel.toggleSpeaker() },
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (call.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (call.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                contentDescription = "Speaker",
                                tint = if (call.isSpeakerOn) Color.Black else Color.White
                            )
                        }

                        // Video On/Off Toggle
                        if (call.callType == CallType.VIDEO) {
                            IconButton(
                                onClick = { callViewModel.toggleVideo() },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        if (!call.isVideoEnabled) Color.White else Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (call.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = "Video Toggle",
                                    tint = if (!call.isVideoEnabled) Color.Black else Color.White
                                )
                            }
                        }

                        // End Call Red Button
                        FloatingActionButton(
                            onClick = {
                                callViewModel.endCall()
                                onEndCallClick()
                            },
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "End Call", modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}
