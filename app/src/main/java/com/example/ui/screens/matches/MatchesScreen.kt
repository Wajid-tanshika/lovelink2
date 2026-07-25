package com.example.ui.screens.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MatchItem
import com.example.data.model.UserProfile
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SuperLikeBlue
import com.example.ui.viewmodel.MatchesViewModel
import com.example.util.PreventScreenshotProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    matchesViewModel: MatchesViewModel,
    currentUser: UserProfile,
    isPremium: Boolean = true,
    onMatchClick: (MatchItem) -> Unit,
    onNavigateToPremium: () -> Unit = {}
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val searchQuery by matchesViewModel.searchQuery.collectAsState()
    val matches = matchesViewModel.getFilteredMatches(currentUser.id)
    var showLimitDialog by remember { mutableStateOf(false) }

    val activeChatsCount = matches.count { it.lastMessage.isNotBlank() }

    fun handleMatchClick(match: MatchItem) {
        if (!isPremium && activeChatsCount >= 5 && match.lastMessage.isBlank()) {
            showLimitDialog = true
        } else {
            onMatchClick(match)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Free user chat limit banner
        if (!isPremium) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPremium() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RosePrimary.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RosePrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Free Plan Chat Limit: $activeChatsCount/5 Active Chats",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = RosePrimary
                        )
                        Text(
                            text = "Upgrade to LoveLink VIP for Unlimited Chats & Swipes ✨",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = onNavigateToPremium,
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("VIP", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Top Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { matchesViewModel.setSearchQuery(it) },
            placeholder = { Text("Search matches & chats...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        )

        // New Matches horizontal row
        if (matches.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "New Matches (${matches.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(matches) { match ->
                        val otherUser = match.getOtherUser(currentUser.id)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { handleMatchClick(match) }
                        ) {
                            Box {
                                AsyncImage(
                                    model = otherUser.photoUrls.firstOrNull() ?: "",
                                    contentDescription = otherUser.name,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .border(2.5.dp, RosePrimary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                if (otherUser.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(OnlineGreen)
                                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }

                                if (match.isSuperLikeMatch) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(SuperLikeBlue)
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Super Like",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = otherUser.name.split(" ").firstOrNull() ?: otherUser.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Conversations List
            Text(
                text = "Messages",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(matches) { match ->
                    val otherUser = match.getOtherUser(currentUser.id)
                    val unread = match.unreadCounts[currentUser.id] ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { handleMatchClick(match) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box {
                            AsyncImage(
                                model = otherUser.photoUrls.firstOrNull() ?: "",
                                contentDescription = otherUser.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (otherUser.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = otherUser.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = match.lastMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (unread > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (unread > 0) {
                            Badge(containerColor = RosePrimary) {
                                Text("$unread", color = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No active matches yet. Keep swiping on Home! ❤️")
            }
        }
    }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text("Chat Limit Reached (5/5) 💬") },
            text = {
                Text("Free users can start up to 5 active chats. Upgrade to LoveLink VIP for Unlimited Chats, Unlimited Swipes, Unlimited Likes, and Ad-Free Experience!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLimitDialog = false
                        onNavigateToPremium()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Upgrade to VIP ✨")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
