package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SwipeType
import com.example.data.model.UserProfile
import com.example.ui.components.MatchDialog
import com.example.ui.components.SwipeableCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeViewModel
import com.example.util.PreventScreenshotProtection

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    currentUser: UserProfile,
    onNavigateToChat: (String) -> Unit,
    onNavigateToDiamondStore: () -> Unit,
    onNavigateToProfileInfo: (UserProfile) -> Unit
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val feed by homeViewModel.feed.collectAsState()
    val showMatchDialog by homeViewModel.showMatchDialog.collectAsState()
    val matchEvent by homeViewModel.newMatchEvent.collectAsState()
    val snackMessage by homeViewModel.snackMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            homeViewModel.clearSnackMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (feed.isNotEmpty()) {
                val currentTopUser = feed.first()

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Card Deck Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Background card stack effect for 3D depth
                        if (feed.size > 1) {
                            val secondUser = feed[1]
                            SwipeableCard(
                                user = secondUser,
                                onSwipe = { },
                                onInfoClick = { },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 10.dp)
                            )
                        }

                        // Top Active Card
                        SwipeableCard(
                            user = currentTopUser,
                            onSwipe = { type ->
                                homeViewModel.swipe(currentTopUser, type, currentUser)
                            },
                            onInfoClick = { onNavigateToProfileInfo(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Action Controls Bottom Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Undo Button (⏪)
                        FloatingActionButton(
                            onClick = { homeViewModel.undo(currentUser) },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = GoldPremium,
                            shape = CircleShape,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo Swipe")
                        }

                        // Pass Button (❌)
                        FloatingActionButton(
                            onClick = { homeViewModel.swipe(currentTopUser, SwipeType.PASS, currentUser) },
                            containerColor = PassRed.copy(alpha = 0.15f),
                            contentColor = PassRed,
                            shape = CircleShape,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Pass", modifier = Modifier.size(32.dp))
                        }

                        // Super Like Button (⭐)
                        FloatingActionButton(
                            onClick = { homeViewModel.swipe(currentTopUser, SwipeType.SUPER_LIKE, currentUser) },
                            containerColor = SuperLikeBlue.copy(alpha = 0.15f),
                            contentColor = SuperLikeBlue,
                            shape = CircleShape,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Super Like", modifier = Modifier.size(26.dp))
                        }

                        // Like Button (💖)
                        FloatingActionButton(
                            onClick = { homeViewModel.swipe(currentTopUser, SwipeType.LIKE, currentUser) },
                            containerColor = RosePrimary,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = "Like", modifier = Modifier.size(32.dp))
                        }

                        // Boost Button (⚡)
                        FloatingActionButton(
                            onClick = { onNavigateToDiamondStore() },
                            containerColor = GoldPremium.copy(alpha = 0.15f),
                            contentColor = GoldPremium,
                            shape = CircleShape,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = "Profile Boost")
                        }
                    }
                }
            } else {
                // Empty Feed View
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(RosePrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = RosePrimary, modifier = Modifier.size(36.dp))
                        }

                        Text(
                            text = "No More Profiles Nearby",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Expand your distance or age filters in Explore, or activate a Profile Boost to get 10x more views!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = onNavigateToDiamondStore,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                        ) {
                            Text("Activate Profile Boost ⚡")
                        }
                    }
                }
            }

            // Match Dialog Popup
            val currentMatch = matchEvent
            if (showMatchDialog && currentMatch != null) {
                MatchDialog(
                    match = currentMatch,
                    currentUser = currentUser,
                    onSendMessage = { matchId ->
                        homeViewModel.dismissMatchDialog()
                        onNavigateToChat(matchId)
                    },
                    onKeepSwiping = { homeViewModel.dismissMatchDialog() }
                )
            }
        }
    }
}
