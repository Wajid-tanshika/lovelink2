package com.example.ui.screens.feed

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    feedViewModel: FeedViewModel,
    currentUser: com.example.data.model.UserProfile,
    onBackClick: () -> Unit
) {
    val viewModel = feedViewModel
    val context = LocalContext.current
    val state by viewModel.createPostState.collectAsState()

    var hashtagInput by remember { mutableStateOf("") }
    var mentionInput by remember { mutableStateOf("") }
    var showLocationDialog by remember { mutableStateOf(false) }

    val presetImages = listOf(
        "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=800&q=80"
    )

    LaunchedEffect(state.postSuccess) {
        if (state.postSuccess) {
            viewModel.resetCreatePostState()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.submitPost() },
                        enabled = !state.isPosting && (state.caption.isNotBlank() || state.mediaUrls.isNotEmpty()),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (state.isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Post", fontWeight = FontWeight.Bold)
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = currentUser.photoUrls.firstOrNull() ?: "",
                    contentDescription = currentUser.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, RosePrimary, CircleShape)
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (currentUser.isVerified) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (state.location.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VioletSecondary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(12.dp))
                                Text(state.location, style = MaterialTheme.typography.labelSmall, color = RosePrimary)
                            }
                        }
                    }
                }
            }

            // Main Caption Input
            OutlinedTextField(
                value = state.caption,
                onValueChange = { viewModel.updateCaption(it) },
                placeholder = { Text("What's on your mind? Add #hashtags or @mentions...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RosePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Emoji Toolbar Shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("❤️", "🔥", "✨", "☕", "🌊", "🥂", "🎶").forEach { emoji ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { viewModel.updateCaption(state.caption + emoji) }
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // Selected Media Preview Grid
            if (state.mediaUrls.isNotEmpty()) {
                Text(
                    text = "Attached Media (${state.mediaUrls.size}/10)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.mediaUrls) { url ->
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Attached Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            IconButton(
                                onClick = { viewModel.removeMediaUrl(url) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Media attachment options
            Text(
                text = "Add Media to Post",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pick photo preset button
                OutlinedButton(
                    onClick = {
                        val nextPhoto = presetImages[(state.mediaUrls.size) % presetImages.size]
                        viewModel.addMediaUrl(nextPhoto, isVideo = false)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = RosePrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Photo")
                }

                // Pick short video button
                OutlinedButton(
                    onClick = {
                        viewModel.addMediaUrl(
                            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=800&q=80",
                            isVideo = true
                        )
                        Toast.makeText(context, "Short Video added (max 60s)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = VioletSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Short Video")
                }
            }

            // Hashtags Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Hashtags & Mentions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hashtagInput,
                        onValueChange = { hashtagInput = it },
                        placeholder = { Text("#Hashtag") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.addHashtag(hashtagInput)
                            hashtagInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                    ) {
                        Text("Add")
                    }
                }

                // Hashtag Chips
                if (state.hashtagsList.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.hashtagsList) { tag ->
                            InputChip(
                                selected = true,
                                onClick = { viewModel.removeHashtag(tag) },
                                label = { Text("#$tag") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = mentionInput,
                        onValueChange = { mentionInput = it },
                        placeholder = { Text("@Mention user") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.addMention(mentionInput)
                            mentionInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletSecondary)
                    ) {
                        Text("Mention")
                    }
                }

                // Mention Chips
                if (state.mentionsList.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.mentionsList) { user ->
                            InputChip(
                                selected = true,
                                onClick = { viewModel.removeMention(user) },
                                label = { Text("@$user") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }
            }

            // Add Location button
            OutlinedButton(
                onClick = { showLocationDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RosePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.location.isBlank()) "Add Location" else "Location: ${state.location}")
            }
        }
    }

    if (showLocationDialog) {
        var tempLoc by remember { mutableStateOf(state.location) }
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Set Location", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempLoc,
                    onValueChange = { tempLoc = it },
                    placeholder = { Text("e.g. SoHo, Manhattan, NYC") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateLocation(tempLoc)
                        showLocationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
