package com.example.ui.screens.call

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.CallHistoryItem
import com.example.data.model.CallType
import com.example.data.model.UserProfile
import com.example.ui.theme.RosePrimary
import com.example.util.PreventScreenshotProtection
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    callViewModel: CallViewModel,
    currentUser: UserProfile,
    onBackClick: () -> Unit,
    onStartCall: (targetUserId: String, isVideo: Boolean) -> Unit
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val history by callViewModel.callHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { callViewModel.clearHistory() }) {
                            Text("Clear", color = RosePrimary)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No recent calls",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    CallHistoryCard(
                        item = item,
                        onCallVoiceClick = { onStartCall(item.otherUserId, false) },
                        onCallVideoClick = { onStartCall(item.otherUserId, true) }
                    )
                }
            }
        }
    }
}

@Composable
fun CallHistoryCard(
    item: CallHistoryItem,
    onCallVoiceClick: () -> Unit,
    onCallVideoClick: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val dateStr = sdf.format(Date(item.timestampMillis))

    val minutes = item.durationSeconds / 60
    val seconds = item.durationSeconds % 60
    val durationStr = if (item.isMissed) "Missed Call" else String.format("%02d:%02d", minutes, seconds)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = item.otherUserAvatar,
                    contentDescription = item.otherUserName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                )

                Column {
                    Text(
                        text = item.otherUserName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                item.isMissed -> Icons.Default.CallMissed
                                item.isOutgoing -> Icons.Default.CallMade
                                else -> Icons.Default.CallReceived
                            },
                            contentDescription = null,
                            tint = if (item.isMissed) Color.Red else Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$durationStr • $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.isMissed) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onCallVoiceClick) {
                    Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = RosePrimary)
                }
                IconButton(onClick = onCallVideoClick) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = RosePrimary)
                }
            }
        }
    }
}
