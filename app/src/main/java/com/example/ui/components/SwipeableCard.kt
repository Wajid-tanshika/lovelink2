package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SwipeType
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    user: UserProfile,
    onSwipe: (SwipeType) -> Unit,
    onInfoClick: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenWidthPx * 0.35f

    var photoIndex by remember(user.id) { mutableIntStateOf(0) }
    val photoUrls = if (user.photoUrls.isNotEmpty()) user.photoUrls else listOf("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=80")

    val rotationAngle = (offsetX.value / screenWidthPx) * 18f

    // Stamp alphas based on drag
    val likeAlpha = (offsetX.value / swipeThreshold).coerceIn(0f, 1f)
    val passAlpha = (-offsetX.value / swipeThreshold).coerceIn(0f, 1f)
    val superLikeAlpha = (-offsetY.value / swipeThreshold).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .rotate(rotationAngle)
            .pointerInput(user.id) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            when {
                                offsetX.value > swipeThreshold -> {
                                    offsetX.animateTo(screenWidthPx * 1.5f, tween(200))
                                    onSwipe(SwipeType.LIKE)
                                }
                                offsetX.value < -swipeThreshold -> {
                                    offsetX.animateTo(-screenWidthPx * 1.5f, tween(200))
                                    onSwipe(SwipeType.PASS)
                                }
                                offsetY.value < -swipeThreshold -> {
                                    offsetY.animateTo(-screenWidthPx * 1.5f, tween(200))
                                    onSwipe(SwipeType.SUPER_LIKE)
                                }
                                else -> {
                                    launch { offsetX.animateTo(0f, animationSpec = spring()) }
                                    launch { offsetY.animateTo(0f, animationSpec = spring()) }
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    }
                )
            },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Photo Image
            AsyncImage(
                model = photoUrls[photoIndex.coerceIn(photoUrls.indices)],
                contentDescription = "${user.name}'s photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Top Photo Index Switcher Taps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (photoIndex > 0) photoIndex--
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (photoIndex < photoUrls.size - 1) photoIndex++
                        }
                )
            }

            // Photo Indicator Dots
            if (photoUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    photoUrls.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (idx == photoIndex) Color.White else Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }

            // LIKE Stamp Overlay
            if (likeAlpha > 0.05f) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .align(Alignment.TopStart)
                        .rotate(-15f)
                        .border(4.dp, LikeGreen, RoundedCornerShape(12.dp))
                        .background(LikeGreen.copy(alpha = 0.2f * likeAlpha))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "LIKE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = LikeGreen.copy(alpha = likeAlpha)
                    )
                }
            }

            // PASS Stamp Overlay
            if (passAlpha > 0.05f) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .align(Alignment.TopEnd)
                        .rotate(15f)
                        .border(4.dp, PassRed, RoundedCornerShape(12.dp))
                        .background(PassRed.copy(alpha = 0.2f * passAlpha))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "PASS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = PassRed.copy(alpha = passAlpha)
                    )
                }
            }

            // SUPER LIKE Stamp Overlay
            if (superLikeAlpha > 0.05f) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                        .border(4.dp, SuperLikeBlue, RoundedCornerShape(12.dp))
                        .background(SuperLikeBlue.copy(alpha = 0.2f * superLikeAlpha))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "SUPER LIKE ⭐",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        ),
                        color = SuperLikeBlue.copy(alpha = superLikeAlpha)
                    )
                }
            }

            // Bottom Gradient & Profile Info Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Name, Age & Verified
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${user.name}, ${user.age}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = Color.White
                        )
                        if (user.isVerified) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified User",
                                tint = DiamondCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Profession & Distance Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (user.profession.isNotEmpty()) {
                            Text(
                                text = user.profession,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = RosePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${user.distanceKm} km away",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Bio excerpt
                    if (user.bio.isNotEmpty()) {
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 2
                        )
                    }

                    // Interests chips
                    OptInInterestRow(user.interests)

                    // Info button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { onInfoClick(user) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View Profile Info",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptInInterestRow(interests: List<String>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        interests.take(3).forEach { interest ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RosePrimary.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, RosePrimary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = interest,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}
