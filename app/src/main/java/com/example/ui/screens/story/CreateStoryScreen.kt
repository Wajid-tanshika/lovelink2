package com.example.ui.screens.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
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
import com.example.data.model.StoryType
import com.example.data.model.UserProfile
import com.example.ui.theme.RosePrimary
import com.example.util.PreventScreenshotProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryScreen(
    storyViewModel: StoryViewModel,
    currentUser: UserProfile,
    onBackClick: () -> Unit,
    onStoryCreated: () -> Unit = onBackClick
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    var selectedType by remember { mutableStateOf(StoryType.TEXT) }
    var textContent by remember { mutableStateOf("") }
    var captionContent by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#FF1493") }

    val sampleMediaUrls = listOf(
        "https://images.unsplash.com/photo-1517841905240-472988babdf9",
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb"
    )
    var selectedMediaUrl by remember { mutableStateOf(sampleMediaUrls.first()) }

    val colorsHex = listOf("#FF1493", "#7B1FA2", "#00BCD4", "#4CAF50", "#FF9800", "#1E88E5", "#E91E63")

    val parsedColor = try {
        Color(android.graphics.Color.parseColor(selectedColorHex))
    } catch (e: Exception) {
        RosePrimary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Story", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            storyViewModel.createStory(
                                author = currentUser,
                                type = selectedType,
                                mediaUrl = if (selectedType != StoryType.TEXT) selectedMediaUrl else null,
                                caption = if (selectedType != StoryType.TEXT) captionContent else null,
                                textContent = if (selectedType == StoryType.TEXT) textContent else null,
                                backgroundColorHex = selectedColorHex
                            )
                            onBackClick()
                        },
                        enabled = (selectedType == StoryType.TEXT && textContent.isNotBlank()) || selectedType != StoryType.TEXT,
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Story Type Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedType == StoryType.TEXT,
                    onClick = { selectedType = StoryType.TEXT },
                    label = { Text("Text Story") },
                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) }
                )
                FilterChip(
                    selected = selectedType == StoryType.PHOTO,
                    onClick = { selectedType = StoryType.PHOTO },
                    label = { Text("Photo Story") },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                )
                FilterChip(
                    selected = selectedType == StoryType.VIDEO,
                    onClick = { selectedType = StoryType.VIDEO },
                    label = { Text("30s Video") },
                    leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) }
                )
            }

            // Preview Canvas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedType == StoryType.TEXT) parsedColor else Color.Black
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedType == StoryType.TEXT) {
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            placeholder = { Text("Type your story...", color = Color.White.copy(alpha = 0.7f), fontSize = 24.sp) },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    } else {
                        AsyncImage(
                            model = selectedMediaUrl,
                            contentDescription = "Story Media",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        OutlinedTextField(
                            value = captionContent,
                            onValueChange = { captionContent = it },
                            placeholder = { Text("Add caption...", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        )
                    }
                }
            }

            // Customization Options
            if (selectedType == StoryType.TEXT) {
                // Color Palette Picker
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Background Theme Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(colorsHex) { hex ->
                            val c = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (selectedColorHex == hex) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }
            } else {
                // Media Samples Picker
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Choose Sample Media", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(sampleMediaUrls) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (selectedMediaUrl == url) 3.dp else 0.dp,
                                        color = RosePrimary,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMediaUrl = url }
                            )
                        }
                    }
                }
            }
        }
    }
}
