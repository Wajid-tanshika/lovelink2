package com.example.data.model

data class MatchItem(
    val id: String = "",
    val users: List<String> = emptyList(),
    val matchedAt: Long = System.currentTimeMillis(),
    val userProfiles: Map<String, UserProfile> = emptyMap(),
    val lastMessage: String = "You matched! Say hi 👋",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCounts: Map<String, Int> = emptyMap(),
    val isSuperLikeMatch: Boolean = false
) {
    fun getOtherUser(currentUserId: String): UserProfile {
        val otherId = users.firstOrNull { it != currentUserId } ?: ""
        return userProfiles[otherId] ?: UserProfile(id = otherId, name = "LoveLink User")
    }
}
