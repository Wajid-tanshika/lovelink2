package com.example.data.repository

import com.example.data.model.MatchItem
import com.example.data.model.UserProfile
import com.example.data.source.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MatchRepository {

    private val initialMatches = listOf(
        MatchItem(
            id = "match_1",
            users = listOf("user_me", "user_1"),
            userProfiles = mapOf("user_1" to SampleData.PROFILES[0]),
            lastMessage = "Hey Alex! Loved your photography photos 📸",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 15, // 15 mins ago
            unreadCounts = mapOf("user_me" to 1)
        ),
        MatchItem(
            id = "match_2",
            users = listOf("user_me", "user_3"),
            userProfiles = mapOf("user_3" to SampleData.PROFILES[2]),
            lastMessage = "You matched! Say hi 👋",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2, // 2 hours ago
            isSuperLikeMatch = true
        ),
        MatchItem(
            id = "match_3",
            users = listOf("user_me", "user_5"),
            userProfiles = mapOf("user_5" to SampleData.PROFILES[4]),
            lastMessage = "Are you going to the gallery opening this Saturday?",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 // 1 day ago
        )
    )

    private val _matches = MutableStateFlow<List<MatchItem>>(initialMatches)
    val matches: StateFlow<List<MatchItem>> = _matches.asStateFlow()

    fun createMatch(currentUser: UserProfile, targetUser: UserProfile, isSuperLike: Boolean = false): MatchItem {
        val newMatch = MatchItem(
            id = "match_${System.currentTimeMillis()}",
            users = listOf(currentUser.id, targetUser.id),
            userProfiles = mapOf(targetUser.id to targetUser, currentUser.id to currentUser),
            lastMessage = if (isSuperLike) "Super Liked! You matched! ⭐" else "It's a Match! Say hi 👋",
            lastMessageTimestamp = System.currentTimeMillis(),
            isSuperLikeMatch = isSuperLike
        )
        _matches.value = listOf(newMatch) + _matches.value
        return newMatch
    }

    fun updateLastMessage(matchId: String, text: String) {
        _matches.value = _matches.value.map { match ->
            if (match.id == matchId) {
                match.copy(
                    lastMessage = text,
                    lastMessageTimestamp = System.currentTimeMillis()
                )
            } else match
        }
    }
}
