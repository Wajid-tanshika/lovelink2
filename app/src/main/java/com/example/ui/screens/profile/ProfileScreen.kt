package com.example.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.data.source.SampleData
import com.example.ui.components.LoveLinkHeaderLogo
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary
import com.example.ui.viewmodel.ProfileViewModel
import com.example.util.PreventScreenshotProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToDiamondStore: () -> Unit,
    onNavigateToPremiumPlans: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToOnboarding: (() -> Unit)? = null
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val currentUserState by profileViewModel.currentUser.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val snackMessage by profileViewModel.snackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showChangePhotoDialog by remember { mutableStateOf(false) }

    // Fallback profile if user data is missing or loading
    val user: UserProfile = currentUserState ?: SampleData.CURRENT_USER

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.clearSnackMessage()
        }
    }

    // Profile loaded cleanly without forced auto-redirection

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                // Loading Animation
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoveLinkHeaderLogo()
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(
                        color = RosePrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading profile...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Profile Header & Logo Branding
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LoveLinkHeaderLogo()

                        Spacer(modifier = Modifier.height(4.dp))

                        // Avatar with Rose Primary Border, Camera Change Icon & Verification Checkmark
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.clickable { showChangePhotoDialog = true }
                        ) {
                            val photo = user.photoUrls.firstOrNull()
                                ?: user.photoURL.ifBlank { "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80" }

                            AsyncImage(
                                model = photo,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .border(3.5.dp, RosePrimary, CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            // Camera Edit Badge Icon
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(RosePrimary)
                                    .align(Alignment.BottomStart)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Profile Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (user.isVerified) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(DiamondCyan)
                                        .align(Alignment.BottomEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (showChangePhotoDialog) {
                            val currentPhoto = user.photoUrls.firstOrNull() ?: user.photoURL
                            ChangeProfilePhotoDialog(
                                currentPhotoUrl = currentPhoto,
                                onDismiss = { showChangePhotoDialog = false },
                                onPhotoSelected = { photoUrl ->
                                    profileViewModel.updateProfileImage(photoUrl)
                                }
                            )
                        }

                        // Display Name, Age, & Admin Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${user.name.ifBlank { "LoveLink Member" }}, ${if (user.age > 0) user.age else 24}",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (user.isAdmin) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldPremium.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, GoldPremium)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AdminPanelSettings,
                                            contentDescription = "Admin Badge",
                                            tint = GoldPremium,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            "ADMIN",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPremium
                                        )
                                    }
                                }
                            }
                        }

                        // Profession & Location
                        Text(
                            text = "${user.profession.ifBlank { "Single & Looking" }} • ${user.city.ifBlank { "Nearby" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        // Action Buttons: Edit Profile, Settings, Admin Panel
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onNavigateToEditProfile,
                                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Edit Profile")
                                }
                            }

                            OutlinedButton(
                                onClick = onNavigateToSettings,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            }

                            if (user.isAdmin) {
                                OutlinedButton(
                                    onClick = onNavigateToAdmin,
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, GoldPremium)
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Panel", tint = GoldPremium, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Incomplete Profile Banner Alert
                    if (!user.profileCompleted) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Profile Incomplete ⚠️",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        "Complete your profile information to start getting matches!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Button(
                                    onClick = { onNavigateToOnboarding?.invoke() ?: onNavigateToEditProfile() },
                                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                                ) {
                                    Text("Complete Now", color = Color.White)
                                }
                            }
                        }
                    }

                    // Stats Counter Row (Social & Dating Stats)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${user.postsCount}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = RosePrimary)
                                Text("Posts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Divider(modifier = Modifier.height(30.dp).width(1.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${user.followers.size + 142}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = VioletSecondary)
                                Text("Followers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Divider(modifier = Modifier.height(30.dp).width(1.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${user.following.size + 58}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = DiamondCyan)
                                Text("Following", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Divider(modifier = Modifier.height(30.dp).width(1.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${user.totalLikesReceived}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GoldPremium)
                                Text("Likes Recv", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }

                    // Diamond Wallet Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDiamondStore() },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(DiamondCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Diamond, contentDescription = "Diamonds", tint = DiamondCyan)
                                }

                                Column {
                                    Text("Diamond Wallet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("${user.diamondBalance} Diamonds Available 💎", style = MaterialTheme.typography.bodySmall, color = DiamondCyan)
                                }
                            }

                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Go to Diamond Store")
                        }
                    }

                    // Upgrade VIP Premium Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPremiumPlans() },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(RosePrimary, VioletSecondary)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldPremium)
                                    Text("LoveLink VIP Premium", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                                Text(
                                    text = "Get Unlimited Likes, See Who Liked You, Undo Swipes & 5 Free Super Likes daily!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GoldPremium
                                ) {
                                    Text("Upgrade Now ✨", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Blue Verification Badge Card
                    if (!user.isVerified) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = DiamondCyan.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Get Blue Verification Badge 💙", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Verified profiles get 3x more matches and higher trust.", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = { profileViewModel.requestVerification() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan)
                                ) {
                                    Text("Verify", color = Color.White)
                                }
                            }
                        }
                    }

                    // Photos Gallery Preview
                    val photoList = user.photoUrls.ifEmpty {
                        listOf("https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80")
                    }

                    Text("Photos (${photoList.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(photoList) { url ->
                            Box {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Photo",
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Logout Option Button
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

