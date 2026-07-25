package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StoryGroup
import com.example.data.model.UserProfile
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary

@Composable
fun StoryHeaderBar(
    storyGroups: List<StoryGroup>,
    currentUser: UserProfile,
    onStoryGroupClick: (StoryGroup) -> Unit,
    onCreateStoryClick: () -> Unit,
    onStartLiveClick: () -> Unit = {}
) {
    val unvisitedBorder = Brush.linearGradient(
        colors = listOf(RosePrimary, VioletSecondary, Color(0xFFFFD700))
    )
    val visitedBorder = Brush.linearGradient(
        colors = listOf(Color.LightGray, Color.Gray)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Own Story Item
        val ownGroup = storyGroups.find { it.isOwnGroup || it.authorId == currentUser.id }
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    if (ownGroup != null && ownGroup.stories.isNotEmpty()) {
                        onStoryGroupClick(ownGroup)
                    } else {
                        onCreateStoryClick()
                    }
                }
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = currentUser.photoUrls.firstOrNull() ?: "",
                        contentDescription = "My Story",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (ownGroup?.stories?.isNotEmpty() == true) 2.5.dp else 1.dp,
                                brush = if (ownGroup?.hasUnseen == true) unvisitedBorder else visitedBorder,
                                shape = CircleShape
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                    )

                    Surface(
                        color = RosePrimary,
                        shape = CircleShape,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onCreateStoryClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.White,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Story",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Other Users' Stories
        items(storyGroups.filter { !it.isOwnGroup && it.authorId != currentUser.id }) { group ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStoryGroupClick(group) }
            ) {
                AsyncImage(
                    model = group.authorAvatar,
                    contentDescription = group.authorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (group.hasUnseen) 3.dp else 1.5.dp,
                            brush = if (group.hasUnseen) unvisitedBorder else visitedBorder,
                            shape = CircleShape
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.authorName.split(" ").firstOrNull() ?: group.authorName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = if (group.hasUnseen) FontWeight.Bold else FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
