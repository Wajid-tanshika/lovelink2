package com.example.ui.screens.interests

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.source.InterestCard
import com.example.data.source.UserInterestManager
import com.example.ui.components.LoveLinkLogoIcon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary
import com.example.util.PreventScreenshotProtection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestSelectionScreen(
    currentUser: UserProfile,
    isEditMode: Boolean = false,
    onSaveSuccess: (List<String>) -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    // 🔒 Mandatory Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize pre-selected interests from user profile or local storage
    val initialInterests = remember(currentUser.interests) {
        if (currentUser.interests.isNotEmpty()) {
            currentUser.interests.toSet()
        } else {
            UserInterestManager.getLocalInterests(context)
        }
    }

    val selectedInterests = remember { mutableStateListOf<String>().apply { addAll(initialInterests) } }
    var isSaving by remember { mutableStateOf(false) }

    val minSelection = 2
    val maxSelection = 10
    val selectedCount = selectedInterests.size
    val isValidSelection = selectedCount in minSelection..maxSelection

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LoveLinkLogoIcon(size = 32.dp)
                        Text(
                            text = if (isEditMode) "Edit Your Interests" else "Discover Passions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    if (isEditMode && onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Back", modifier = Modifier.scale(-1f, 1f))
                        }
                    }
                },
                actions = {
                    Surface(
                        color = if (isValidSelection) RosePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "$selectedCount / $maxSelection",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isValidSelection) RosePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedCount < minSelection) {
                        Text(
                            text = "Select at least ${minSelection - selectedCount} more interest${if (minSelection - selectedCount > 1) "s" else ""} to continue",
                            style = MaterialTheme.typography.bodySmall,
                            color = RosePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (selectedCount == maxSelection) {
                        Text(
                            text = "Maximum of $maxSelection interests selected ✨",
                            style = MaterialTheme.typography.bodySmall,
                            color = DiamondCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            if (!isValidSelection || isSaving) return@Button
                            isSaving = true
                            scope.launch {
                                val list = selectedInterests.toList()
                                UserInterestManager.saveInterestsToFirestore(
                                    context = context,
                                    userId = currentUser.id.ifEmpty { "user_1" },
                                    interests = list
                                )
                                isSaving = false
                                Toast.makeText(context, "Interests saved successfully! ✨", Toast.LENGTH_SHORT).show()
                                onSaveSuccess(list)
                            }
                        },
                        enabled = isValidSelection && !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RosePrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isEditMode) "Save Changes ✨" else "Continue to Feed ✨",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "🎨", fontSize = 32.sp)
                    Column {
                        Text(
                            text = "What are you passionate about?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pick 2 to 10 interests to get personalized profile recommendations and social feed stories.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Interest Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(UserInterestManager.ALL_INTERESTS, key = { it.id }) { item ->
                    val isSelected = selectedInterests.contains(item.name)
                    
                    InterestCardView(
                        item = item,
                        isSelected = isSelected,
                        onToggle = {
                            if (isSelected) {
                                selectedInterests.remove(item.name)
                            } else {
                                if (selectedInterests.size < maxSelection) {
                                    selectedInterests.add(item.name)
                                } else {
                                    Toast.makeText(context, "Maximum 10 interests allowed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InterestCardView(
    item: InterestCard,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    // Selection Animation Scale Effect
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "CardScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) RosePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        label = "CardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) RosePrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "CardBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .scale(scaleAnim)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = item.icon, fontSize = 28.sp)
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) RosePrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Selection Checkmark Badge
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(RosePrimary)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
