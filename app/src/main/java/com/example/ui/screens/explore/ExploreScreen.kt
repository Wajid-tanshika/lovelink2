package com.example.ui.screens.explore

import androidx.compose.runtime.Composable
import com.example.data.model.UserProfile
import com.example.ui.screens.interests.InterestSelectionScreen
import com.example.util.PreventScreenshotProtection

@Composable
fun ExploreScreen(
    currentUser: UserProfile,
    onNavigateToHomeFeed: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    InterestSelectionScreen(
        currentUser = currentUser,
        isEditMode = onBackClick != null,
        onSaveSuccess = { _ ->
            onNavigateToHomeFeed()
        },
        onBackClick = onBackClick
    )
}
