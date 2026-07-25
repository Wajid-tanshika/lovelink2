package com.example.ui.screens.feed

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostItem
import com.example.data.source.SampleData
import com.example.ui.components.LoveLinkHeaderLogo
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary
import com.example.util.PreventScreenshotProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel,
    storyViewModel: com.example.ui.screens.story.StoryViewModel? = null,
    currentUser: com.example.data.model.UserProfile,
    onCreatePostClick: () -> Unit,
    onAddStoryClick: () -> Unit = {},
    onStoryClick: (String) -> Unit = {},
    onStartLiveClick: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onUserProfileClick: (String) -> Unit = {},
    onHeaderVisibilityChange: (Boolean) -> Unit = {}
) {
    val viewModel = feedViewModel
    // 🔒 Mandatory Privacy & Screenshot Protection across Feed Screen
    PreventScreenshotProtection(enabled = true)

    val storyGroups = storyViewModel?.storyGroups?.collectAsState()?.value ?: emptyList()

    val context = LocalContext.current
    val posts by viewModel.filteredPosts.collectAsState()
    val savedIds by viewModel.savedPostIds.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeCommentPostId by viewModel.activeCommentPostId.collectAsState()
    val commentsMap by viewModel.commentsMap.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    var sharePostItem by remember { mutableStateOf<PostItem?>(null) }
    var isHeaderVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -12f && isHeaderVisible) {
                    isHeaderVisible = false
                    onHeaderVisibilityChange(false)
                } else if (delta > 12f && !isHeaderVisible) {
                    isHeaderVisible = true
                    onHeaderVisibilityChange(true)
                }
                return Offset.Zero
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePostClick,
                containerColor = RosePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Post", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .nestedScroll(nestedScrollConnection)
        ) {
            // 24h Stories Header Bar (Auto Hides on Scroll Down)
            AnimatedVisibility(
                visible = isHeaderVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                com.example.ui.components.StoryHeaderBar(
                    storyGroups = storyGroups,
                    currentUser = currentUser,
                    onStoryGroupClick = { group -> onStoryClick(group.authorId) },
                    onCreateStoryClick = onAddStoryClick,
                    onStartLiveClick = onStartLiveClick
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Feed Tabs (Explore, Following, Saved)
            SecondaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = RosePrimary
            ) {
                Tab(
                    selected = selectedTab == FeedTab.EXPLORE,
                    onClick = { viewModel.selectTab(FeedTab.EXPLORE) },
                    text = { Text("Explore", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )

                Tab(
                    selected = selectedTab == FeedTab.FOLLOWING,
                    onClick = { viewModel.selectTab(FeedTab.FOLLOWING) },
                    text = { Text("Following", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )

                Tab(
                    selected = selectedTab == FeedTab.SAVED,
                    onClick = { viewModel.selectTab(FeedTab.SAVED) },
                    text = { Text("Saved", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Posts Feed List
            if (posts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = when (selectedTab) {
                                FeedTab.SAVED -> Icons.Default.BookmarkBorder
                                FeedTab.FOLLOWING -> Icons.Default.PersonAdd
                                else -> Icons.Default.RssFeed
                            },
                            contentDescription = null,
                            tint = VioletSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = when (selectedTab) {
                                FeedTab.SAVED -> "No Saved Posts"
                                FeedTab.FOLLOWING -> "No Posts from Following"
                                else -> "Social Feed is Quiet"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = when (selectedTab) {
                                FeedTab.SAVED -> "Tap the bookmark icon on any post to save it here!"
                                FeedTab.FOLLOWING -> "Follow members from Explore to see their posts here."
                                else -> "Be the first to share a moment with LoveLink singles!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Button(
                            onClick = onCreatePostClick,
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create a Post")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Suggested Users Carousel based on Interests
                    if (selectedTab == FeedTab.EXPLORE) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Suggested Matches ✨",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Based on Passions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RosePrimary
                                    )
                                }

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(SampleData.PROFILES) { user ->
                                        Card(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .clickable { onUserProfileClick(user.id) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clip(CircleShape)
                                                ) {
                                                    AsyncImage(
                                                        model = user.photoUrls.firstOrNull() ?: "",
                                                        contentDescription = user.name,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }

                                                Text(
                                                    text = "${user.name}, ${user.age}",
                                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1
                                                )

                                                Surface(
                                                    color = RosePrimary.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = user.interests.firstOrNull() ?: "Passionate",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = RosePrimary,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Trending Posts Banner
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🔥", fontSize = 18.sp)
                                    Text(
                                        text = "Trending Posts in Your Favorite Communities",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            currentUserId = currentUser.id,
                            onLikeClick = { viewModel.toggleLikePost(post.id) },
                            onCommentClick = { viewModel.openCommentsFor(post.id) },
                            onSaveClick = { viewModel.toggleSavePost(post.id) },
                            onReportClick = { reason -> viewModel.reportPost(post.id, reason) },
                            onDeletePostClick = { viewModel.deletePost(post.id) },
                            onShareInsideAppClick = { sharePostItem = post },
                            isSaved = savedIds.contains(post.id)
                        )
                    }
                }
            }
        }
    }

    // Comments Sheet Modal
    val currentCommentPostId = activeCommentPostId
    if (currentCommentPostId != null) {
        val comments = commentsMap[currentCommentPostId] ?: emptyList()
        CommentsSheet(
            postId = currentCommentPostId,
            comments = comments,
            currentUserId = currentUser.id,
            onAddComment = { content, parentId ->
                viewModel.addComment(currentCommentPostId, content, parentId)
            },
            onLikeComment = { commentId ->
                viewModel.toggleLikeComment(currentCommentPostId, commentId)
            },
            onDeleteComment = { commentId ->
                viewModel.deleteComment(currentCommentPostId, commentId)
            },
            onReportComment = { commentId ->
                viewModel.reportComment(currentCommentPostId, commentId)
            },
            onDismiss = { viewModel.closeComments() }
        )
    }

    // Share Post Inside App Dialog (Select Match to send post to)
    if (sharePostItem != null) {
        AlertDialog(
            onDismissRequest = { sharePostItem = null },
            title = { Text("Share Post in Chat", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a match to send this post in chat:", style = MaterialTheme.typography.bodyMedium)
                    SampleData.PROFILES.take(3).forEach { profile ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val sharedPost = sharePostItem
                                    sharePostItem = null
                                    Toast.makeText(context, "Post shared with ${profile.name}! 💌", Toast.LENGTH_SHORT).show()
                                    onNavigateToChat(profile.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(profile.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Send, contentDescription = null, tint = RosePrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { sharePostItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
