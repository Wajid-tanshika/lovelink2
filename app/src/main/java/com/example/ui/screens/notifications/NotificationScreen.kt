package com.example.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.repository.NotificationRepository
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SuperLikeBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notificationRepository: NotificationRepository = NotificationRepository(),
    onBackClick: () -> Unit
) {
    val notifications by notificationRepository.notifications.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredList = notifications.filter { notif ->
        when (selectedFilter) {
            "Matches" -> notif.type == NotificationType.MATCH
            "Super Likes" -> notif.type == NotificationType.SUPER_LIKE
            "Visits" -> notif.type == NotificationType.PROFILE_VISIT
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { notificationRepository.markAllAsRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Mark All Read", tint = RosePrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Matches", "Super Likes", "Visits").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RosePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredList.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredList) { item ->
                        NotificationCard(item = item)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notifications in this category yet. 🔔")
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead) RosePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                if (item.senderAvatarUrl != null) {
                    AsyncImage(
                        model = item.senderAvatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(RosePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = RosePrimary)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            when (item.type) {
                                NotificationType.MATCH -> RosePrimary
                                NotificationType.SUPER_LIKE -> SuperLikeBlue
                                NotificationType.PROFILE_VISIT -> DiamondCyan
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.type) {
                            NotificationType.MATCH -> Icons.Default.Favorite
                            NotificationType.SUPER_LIKE -> Icons.Default.Star
                            NotificationType.PROFILE_VISIT -> Icons.Default.Visibility
                            else -> Icons.Default.CheckCircle
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
