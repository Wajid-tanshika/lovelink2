package com.example.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.data.model.MatchItem
import com.example.data.model.UserProfile
import com.example.data.model.VirtualGift
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RosePrimary
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.PreventScreenshotProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    match: MatchItem,
    currentUser: UserProfile,
    onStartCallClick: (Boolean) -> Unit = {},
    onNavigateToCallHistory: () -> Unit = {},
    onBackClick: () -> Unit
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    LaunchedEffect(match.id) {
        chatViewModel.loadChat(match)
    }

    val messages by chatViewModel.messages.collectAsState()
    val snackMessage by chatViewModel.snackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            chatViewModel.clearSnackMessage()
        }
    }

    val otherUser = match.getOtherUser(currentUser.id)

    var inputMessage by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    val quickEmojis = listOf("❤️", "😍", "✨", "☕", "🌹", "😂", "🔥", "🥂")

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box {
                            AsyncImage(
                                model = otherUser.photoUrls.firstOrNull() ?: "",
                                contentDescription = otherUser.name,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (otherUser.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = otherUser.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (otherUser.isOnline) "Online now" else "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (otherUser.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onStartCallClick(false) }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = RosePrimary)
                    }

                    IconButton(onClick = { onStartCallClick(true) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = RosePrimary)
                    }

                    IconButton(onClick = { showGiftSheet = true }) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = "Send Gift", tint = RosePrimary)
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Call History") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToCallHistory()
                                },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Chat") },
                                onClick = {
                                    showMenu = false
                                    chatViewModel.deleteChat(match.id)
                                    onBackClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Block User") },
                                onClick = {
                                    showMenu = false
                                    chatViewModel.blockUser(otherUser)
                                    onBackClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Report User") },
                                onClick = {
                                    showMenu = false
                                    showReportDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    ChatBubbleItem(msg = msg, isMe = msg.senderId == currentUser.id)
                }
            }

            // Quick Emoji Selector Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quickEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                inputMessage += emoji
                            }
                            .padding(6.dp)
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }

            // Bottom Message Input Row
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Send Image Attachment button
                    IconButton(
                        onClick = {
                            chatViewModel.sendMessage(
                                senderId = currentUser.id,
                                text = "",
                                imageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80"
                            )
                        }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Send Photo", tint = RosePrimary)
                    }

                    // Voice note simulation button
                    IconButton(
                        onClick = {
                            chatViewModel.sendMessage(
                                senderId = currentUser.id,
                                text = "🎙️ Voice Note (0:12)",
                                audioUrl = "sample_voice.mp3"
                            )
                        }
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Note", tint = RosePrimary)
                    }

                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )

                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank()) {
                                chatViewModel.sendMessage(currentUser.id, inputMessage)
                                inputMessage = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RosePrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }

    // Gift Modal Sheet
    if (showGiftSheet) {
        ModalBottomSheet(onDismissRequest = { showGiftSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Send Virtual Gift to ${otherUser.name} 🎁",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(VirtualGift.DEFAULT_GIFTS) { gift ->
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable {
                                    chatViewModel.sendVirtualGift(
                                        currentUser.id,
                                        gift.name,
                                        gift.emoji,
                                        gift.diamondCost
                                    )
                                    showGiftSheet = false
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(gift.emoji, fontSize = 36.sp)
                                Text(gift.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Surface(
                                    shape = CircleShape,
                                    color = DiamondCyan.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "${gift.diamondCost} 💎",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DiamondCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report ${otherUser.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Help us keep LoveLink safe. Why are you reporting this user?")
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Reason / Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        chatViewModel.reportUser(currentUser, otherUser, "Safety Report", reportReason)
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ChatBubbleItem(msg: ChatMessage, isMe: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMe) 18.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) RosePrimary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (msg.imageUrl != null) {
                    AsyncImage(
                        model = msg.imageUrl,
                        contentDescription = "Attached Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "10:42 AM",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
