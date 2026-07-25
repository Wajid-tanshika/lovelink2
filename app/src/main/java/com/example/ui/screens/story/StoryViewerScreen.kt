package com.example.ui.screens.story

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StoryGroup
import com.example.data.model.StoryItem
import com.example.data.model.StoryType
import com.example.data.model.UserProfile
import com.example.ui.theme.RosePrimary
import com.example.util.PreventScreenshotProtection
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewerScreen(
    storyViewModel: StoryViewModel,
    initialGroupIndex: Int = 0,
    currentUser: UserProfile = com.example.data.source.SampleData.CURRENT_USER,
    onCloseClick: () -> Unit
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val currentUserId = currentUser.id

    val storyGroups by storyViewModel.storyGroups.collectAsState()

    LaunchedEffect(initialGroupIndex, storyGroups) {
        val groupToOpen = storyGroups.getOrNull(initialGroupIndex) ?: storyGroups.firstOrNull()
        if (groupToOpen != null) {
            storyViewModel.openStoryGroup(groupToOpen)
        }
    }

    val selectedGroup by storyViewModel.selectedGroup.collectAsState()
    val selectedIndex by storyViewModel.selectedStoryIndex.collectAsState()

    val group = selectedGroup
    if (group == null || group.stories.isEmpty()) {
        onCloseClick()
        return
    }

    val story = group.stories.getOrNull(selectedIndex) ?: group.stories.first()
    var replyText by remember { mutableStateOf("") }
    var showViewersSheet by remember { mutableStateOf(false) }

    // Story progress animation timer
    var progress by remember(story.id) { mutableStateOf(0f) }

    LaunchedEffect(story.id) {
        progress = 0f
        val stepMs = 50L
        val totalMs = story.durationSeconds * 1000L
        val increment = stepMs.toFloat() / totalMs.toFloat()

        while (progress < 1f) {
            delay(stepMs)
            progress += increment
        }
        storyViewModel.nextStory(currentUserId)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 50, easing = LinearEasing),
        label = "StoryProgress"
    )

    // Background color for text story
    val bgColor = try {
        Color(android.graphics.Color.parseColor(story.backgroundColorHex))
    } catch (e: Exception) {
        Color(0xFFFF1493)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (story.type == StoryType.TEXT) bgColor else Color.Black)
    ) {
        // Media or Text Content
        when (story.type) {
            StoryType.PHOTO, StoryType.VIDEO -> {
                if (!story.mediaUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = story.mediaUrl,
                        contentDescription = "Story Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (!story.caption.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 120.dp, start = 20.dp, end = 20.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = story.caption ?: "",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            StoryType.TEXT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = story.textContent ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Tap Left / Right Navigation Overlay
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { storyViewModel.previousStory(currentUserId) }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { storyViewModel.nextStory(currentUserId) }
            )
        }

        // Top Overlay Controls (Progress bars & Header)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            // Segmented Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                group.stories.forEachIndexed { idx, _ ->
                    val segmentProgress = when {
                        idx < selectedIndex -> 1f
                        idx == selectedIndex -> animatedProgress
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segmentProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.35f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Author Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = story.authorAvatar,
                        contentDescription = story.authorName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = story.authorName,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (story.isVerified) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            text = "24h Story",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (group.isOwnGroup || story.authorId == currentUserId) {
                        IconButton(onClick = { showViewersSheet = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.Visibility, contentDescription = "Viewers", tint = Color.White)
                                Text(
                                    text = "${story.viewers.size}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        IconButton(onClick = { storyViewModel.deleteStory(story.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Story", tint = Color.White)
                        }
                    }
                    IconButton(onClick = onCloseClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // Bottom Controls (Reply & Emoji Reactions)
        if (!group.isOwnGroup && story.authorId != currentUserId) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                // Emoji Reaction Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("❤️", "🔥", "😍", "😂", "😮", "👏").forEach { emoji ->
                        Surface(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable {
                                    storyViewModel.reactToStory(story.id, currentUserId, "Me", emoji)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = emoji, fontSize = 22.sp)
                            }
                        }
                    }
                }

                // Reply Text Box
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Reply to ${story.authorName}...", color = Color.White.copy(alpha = 0.6f)) },
                    trailingIcon = {
                        if (replyText.isNotBlank()) {
                            IconButton(onClick = {
                                storyViewModel.replyToStory(story, currentUserId, replyText)
                                replyText = ""
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Send Reply", tint = RosePrimary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = RosePrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Viewers Bottom Sheet
    if (showViewersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showViewersSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Story Viewers (${story.viewers.size})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (story.viewers.isEmpty()) {
                    Text(
                        text = "No views yet. Share your story with friends!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(story.viewers) { viewer ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = viewer.userAvatar.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                        contentDescription = viewer.userName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )
                                    Text(
                                        text = viewer.userName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = RosePrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNull_or_Blank(): Boolean {
    return this == null || this.isBlank()
}
