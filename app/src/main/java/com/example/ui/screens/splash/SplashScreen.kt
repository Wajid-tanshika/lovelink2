package com.example.ui.screens.splash

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.LoveLinkFullLogo
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    isProfileCompleted: Boolean,
    onNavigateNext: (String) -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    val currentIsLoggedIn by rememberUpdatedState(isLoggedIn)
    val currentIsProfileCompleted by rememberUpdatedState(isProfileCompleted)
    val currentOnNavigateNext by rememberUpdatedState(onNavigateNext)

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SplashScale"
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(800, easing = LinearOutSlowInEasing),
        label = "SplashAlpha"
    )

    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "SplashScreen effect started")
        startAnimation = true
        
        // Wait 1000ms for splash animation
        delay(1000)

        if (!hasNavigated) {
            hasNavigated = true
            val loggedIn = currentIsLoggedIn
            val profileCompleted = currentIsProfileCompleted
            Log.d("SplashScreen", "Navigating from Splash. isLoggedIn=$loggedIn, isProfileCompleted=$profileCompleted")
            try {
                if (loggedIn) {
                    if (profileCompleted) {
                        currentOnNavigateNext(com.example.ui.navigation.NavRoutes.Home.route)
                    } else {
                        currentOnNavigateNext(com.example.ui.navigation.NavRoutes.Onboarding.route)
                    }
                } else {
                    currentOnNavigateNext(com.example.ui.navigation.NavRoutes.Welcome.route)
                }
            } catch (e: Exception) {
                Log.e("SplashScreen", "Navigation failed, falling back to Welcome", e)
                currentOnNavigateNext(com.example.ui.navigation.NavRoutes.Welcome.route)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        RosePrimary.copy(alpha = 0.12f),
                        VioletSecondary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(scaleAnim)
                .alpha(alphaAnim)
        ) {
            LoveLinkFullLogo(
                iconSize = 120.dp,
                showTagline = true
            )
        }
    }
}
