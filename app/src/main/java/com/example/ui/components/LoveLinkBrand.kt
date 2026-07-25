package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary

enum class LogoSize {
    SMALL,   // 32dp
    MEDIUM,  // 48dp
    LARGE,   // 80dp
    SPLASH   // 120dp
}

@Composable
fun LoveLinkLogoIcon(
    size: Dp = 48.dp,
    showGlow: Boolean = true,
    pulseAnimation: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale = if (pulseAnimation) {
        val infiniteTransition = rememberInfiniteTransition(label = "LogoPulse")
        val animatedScale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseScale"
        )
        animatedScale
    } else 1.0f

    Box(
        modifier = modifier
            .size(size)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        if (showGlow) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                RosePrimary.copy(alpha = 0.35f),
                                VioletSecondary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ic_lovelink_logo),
            contentDescription = "LoveLink Official Logo",
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showGlow) 4.dp else 0.dp)
        )
    }
}

@Composable
fun LoveLinkFullLogo(
    iconSize: Dp = 96.dp,
    showTagline: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LoveLinkLogoIcon(
            size = iconSize,
            showGlow = true,
            pulseAnimation = true
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Love",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = RosePrimary
                )
                Text(
                    text = "Link",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (showTagline) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VioletSecondary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, RosePrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "MEET • MATCH • CONNECT",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 11.sp
                        ),
                        color = RosePrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun LoveLinkHeaderLogo(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LoveLinkLogoIcon(
            size = 36.dp,
            showGlow = false
        )
        Row {
            Text(
                text = "Love",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                ),
                color = RosePrimary
            )
            Text(
                text = "Link",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
