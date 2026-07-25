package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.MatchItem
import com.example.data.model.UserProfile
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary

@Composable
fun MatchDialog(
    match: MatchItem,
    currentUser: UserProfile,
    onSendMessage: (String) -> Unit,
    onKeepSwiping: () -> Unit
) {
    val otherUser = match.getOtherUser(currentUser.id)

    // Heart pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Dialog(onDismissRequest = onKeepSwiping) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Heart Pulse Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(RosePrimary, VioletSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Match Heart",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = if (match.isSuperLikeMatch) "SUPER MATCH! ⭐" else "IT'S A MATCH!",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp
                    ),
                    color = RosePrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "You and ${otherUser.name} liked each other!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Overlapping Avatars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current user photo
                    AsyncImage(
                        model = currentUser.photoUrls.firstOrNull() ?: "",
                        contentDescription = "My avatar",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(3.dp, RosePrimary, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width((-20).dp))

                    // Other user photo
                    AsyncImage(
                        model = otherUser.photoUrls.firstOrNull() ?: "",
                        contentDescription = "${otherUser.name}'s avatar",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(3.dp, DiamondCyan, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Action Buttons
                Button(
                    onClick = { onSendMessage(match.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RosePrimary
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message", tint = Color.White)
                        Text(text = "Send a Message", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }

                OutlinedButton(
                    onClick = onKeepSwiping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(text = "Keep Swiping", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
