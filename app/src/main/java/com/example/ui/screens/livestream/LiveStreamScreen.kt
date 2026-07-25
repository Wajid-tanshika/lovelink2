package com.example.ui.screens.livestream

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary
import com.example.util.PreventScreenshotProtection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamScreen(
    liveStreamViewModel: LiveStreamViewModel,
    currentUser: UserProfile,
    diamondBalance: Int = 500,
    onNavigateToDiamondStore: () -> Unit = {},
    onCloseClick: () -> Unit
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val context = LocalContext.current
    val stream by liveStreamViewModel.currentStream.collectAsState()
    val gifts by liveStreamViewModel.gifts.collectAsState()
    val summary by liveStreamViewModel.summary.collectAsState()
    val toastMsg by liveStreamViewModel.toastMessage.collectAsState()

    val live = stream ?: run {
        onCloseClick()
        return
    }

    val isHost = live.hostId == currentUser.id
    var commentInput by remember { mutableStateOf("") }
    var showGiftSheet by remember { mutableStateOf(false) }
    var selectedCommentForMod by remember { mutableStateOf<LiveComment?>(null) }
    var isFollowingHost by remember { mutableStateOf(false) }

    val chatListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            liveStreamViewModel.clearToast()
        }
    }

    // Auto scroll chat to bottom when comments arrive
    LaunchedEffect(live.comments.size) {
        if (live.comments.isNotEmpty()) {
            chatListState.animateScrollToItem(live.comments.size - 1)
        }
    }

    // Format duration MM:SS
    val minutes = live.durationSeconds / 60
    val seconds = live.durationSeconds % 60
    val durationFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Live Camera Stream Feed Simulation
        AsyncImage(
            model = live.hostAvatar,
            contentDescription = live.hostName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for readability
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

        // Top Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Host Avatar & Info Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = live.hostAvatar,
                            contentDescription = live.hostName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = live.hostName,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                if (live.isVerified) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(
                                text = "${live.totalLikes} Likes • $durationFormatted",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        if (!isHost) {
                            Button(
                                onClick = {
                                    isFollowingHost = !isFollowingHost
                                    Toast.makeText(context, if (isFollowingHost) "Followed ${live.hostName}" else "Unfollowed", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowingHost) Color.White.copy(alpha = 0.3f) else RosePrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (isFollowingHost) "Following" else "+ Follow",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Stats & Close Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Diamonds Earned Badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("💎", fontSize = 14.sp)
                            Text(
                                text = "${live.totalDiamondsEarned}",
                                color = GoldPremium,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Live Viewers Badge
                    Surface(
                        color = RosePrimary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Text(
                                text = "LIVE ${live.viewerCount}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // End or Close Button
                    IconButton(
                        onClick = {
                            liveStreamViewModel.endLiveStream(live.id)
                            onCloseClick()
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category & Title Banner
            Surface(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${live.category} • \"${live.title}\"",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Live Chat Overlay & Virtual Gift Notices
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.85f)
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
                .heightIn(max = 260.dp)
        ) {
            LazyColumn(
                state = chatListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(live.comments, key = { it.id }) { comment ->
                    if (comment.isGiftNotice) {
                        // Gift Announcement Banner in Chat
                        Surface(
                            color = VioletSecondary.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(comment.giftEmoji ?: "🎁", fontSize = 18.sp)
                                Text(
                                    text = "${comment.senderName} ${comment.text}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    } else {
                        // Regular Live Comment Bubble
                        Surface(
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable {
                                selectedCommentForMod = comment
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AsyncImage(
                                    model = comment.senderAvatar.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                )
                                Text(
                                    text = comment.senderName,
                                    color = DiamondCyan,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = comment.text,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Actions Bar (Input, Like ❤️, Gift 💎)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chat Input Box
            OutlinedTextField(
                value = commentInput,
                onValueChange = { commentInput = it },
                placeholder = { Text("Comment on live...", color = Color.White.copy(alpha = 0.6f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                ),
                trailingIcon = {
                    if (commentInput.isNotBlank()) {
                        IconButton(onClick = {
                            liveStreamViewModel.sendComment(live.id, currentUser, commentInput)
                            commentInput = ""
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = RosePrimary)
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            )

            // Gift Button 💎
            IconButton(
                onClick = { showGiftSheet = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(Brush.horizontalGradient(listOf(RosePrimary, VioletSecondary)), CircleShape)
            ) {
                Text("🎁", fontSize = 22.sp)
            }

            // Like Heart Button ❤️
            IconButton(
                onClick = { liveStreamViewModel.sendLike(live.id) },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
            ) {
                Text("❤️", fontSize = 22.sp)
            }
        }

        // Virtual Gifts Bottom Sheet
        if (showGiftSheet) {
            ModalBottomSheet(
                onDismissRequest = { showGiftSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Send Virtual Gift",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onNavigateToDiamondStore() }
                                .background(GoldPremium.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("💎 $diamondBalance Balance", fontWeight = FontWeight.Bold, color = GoldPremium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        gifts.chunked(4).forEach { rowGifts ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowGifts.forEach { gift ->
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp)
                                            .clickable {
                                                val success = liveStreamViewModel.sendGift(live.id, currentUser, gift, diamondBalance)
                                                if (success) showGiftSheet = false
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(gift.emoji, fontSize = 28.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(gift.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text("💎 ${gift.diamondCost}", style = MaterialTheme.typography.labelSmall, color = RosePrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Comment Moderation Sheet (For Host or Admin)
        val modComment = selectedCommentForMod
        if (modComment != null) {
            AlertDialog(
                onDismissRequest = { selectedCommentForMod = null },
                title = { Text("Moderate User (${modComment.senderName})") },
                text = { Text("Choose action for message: \"${modComment.text}\"") },
                confirmButton = {
                    TextButton(onClick = {
                        liveStreamViewModel.banUser(live.id, modComment.senderId)
                        selectedCommentForMod = null
                    }) {
                        Text("Ban User 🚫", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        liveStreamViewModel.muteUser(live.id, modComment.senderId)
                        selectedCommentForMod = null
                    }) {
                        Text("Mute User 🔇")
                    }
                }
            )
        }

        // Live End Summary Dialog
        val sum = summary
        if (sum != null) {
            AlertDialog(
                onDismissRequest = {
                    liveStreamViewModel.dismissSummary()
                    onCloseClick()
                },
                title = { Text("🎉 Live Stream Summary", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Host: ${sum.hostName}")
                        Text("Duration: ${sum.durationSeconds / 60} min ${sum.durationSeconds % 60} sec")
                        Text("Peak Viewers: ${sum.peakViewers} 👀")
                        Text("Total Likes: ${sum.totalLikes} ❤️")
                        Text("Diamonds Earned: 💎 ${sum.diamondsEarned}")
                        Text("New Followers: +${sum.newFollowersGained} 🌟")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            liveStreamViewModel.dismissSummary()
                            onCloseClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}
