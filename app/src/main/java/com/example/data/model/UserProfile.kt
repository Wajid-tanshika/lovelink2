package com.example.data.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val photoURL: String = "",
    val age: Int = 20,
    val dateOfBirth: String = "2002-05-15",
    val gender: String = "Woman", // Woman, Man, Non-Binary
    val lookingFor: String = "Long-term Relationship", // Long-term, Dating, Friendship, Casual
    val bio: String = "",
    val country: String = "United States",
    val state: String = "New York",
    val city: String = "New York",
    val interests: List<String> = emptyList(),
    val profession: String = "",
    val heightCm: Int = 170,
    val distanceKm: Double = 3.5,
    val photoUrls: List<String> = emptyList(),
    val accountType: String = "Free", // Free, Premium
    val isVerified: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isPremium: Boolean = false,
    val diamondBalance: Int = 100,
    val coins: Int = 500,
    val createdAt: Long = System.currentTimeMillis(),
    val profileCompleted: Boolean = true,
    val locationLat: Double = 40.7128,
    val locationLng: Double = -74.0060,
    val matches: List<String> = emptyList(),
    val likes: List<String> = emptyList(),
    val superLikes: List<String> = emptyList(),
    val passes: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val postsCount: Int = 3,
    val videoCount: Int = 2,
    val profileViews: Int = 1250,
    val isCreator: Boolean = false,
    val isVerifiedCreator: Boolean = false,
    val totalLikesReceived: Int = 42,
    val isBlocked: Boolean = false,
    val isMuted: Boolean = false,
    val suspensionUntil: String = "",
    val warningCount: Int = 0,
    val deviceInfo: String = "Android 14 • Pixel 8 Pro",
    val loginHistory: List<String> = listOf("2026-07-22 20:15 (New York, US)"),
    val isBoostActive: Boolean = false,
    val boostEndsTimestamp: Long = 0L,
    val isAdmin: Boolean = false,
    val education: String = "Bachelor's Degree",
    val instagramUsername: String = "",
    val hideOnlineStatus: Boolean = false,
    val hideLastSeen: Boolean = false,
    val isPrivateAccount: Boolean = false,
    val isHiddenFromSearch: Boolean = false,
    val verificationStatus: String = "UNVERIFIED", // UNVERIFIED, PENDING, VERIFIED, REJECTED
    val language: String = "English",
    val mutedUserIds: List<String> = emptyList(),
    val blockedUserIds: List<String> = emptyList()
) {
    fun is18Plus(): Boolean = age >= 18

    fun calculateProfileCompletionPercentage(): Int {
        var score = 0
        if (name.isNotBlank()) score += 15
        if (photoURL.isNotBlank()) score += 20
        if (bio.isNotBlank()) score += 15
        if (interests.isNotEmpty()) score += 15
        if (profession.isNotBlank()) score += 10
        if (education.isNotBlank()) score += 10
        if (photoUrls.size >= 2) score += 10
        if (instagramUsername.isNotBlank()) score += 5
        return score.coerceIn(0, 100)
    }
}

data class UserSwipeAction(
    val targetUserId: String,
    val actionType: SwipeType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SwipeType {
    LIKE, PASS, SUPER_LIKE
}
