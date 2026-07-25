package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class StoryRepository {

    private val _storyGroups = MutableStateFlow<List<StoryGroup>>(sampleStories())
    val storyGroups: StateFlow<List<StoryGroup>> = _storyGroups.asStateFlow()

    fun createStory(
        author: UserProfile,
        type: StoryType,
        mediaUrl: String? = null,
        caption: String? = null,
        textContent: String? = null,
        backgroundColorHex: String = "#FF1493"
    ): StoryItem {
        val newStory = StoryItem(
            id = "story_${UUID.randomUUID().toString().take(8)}",
            authorId = author.id,
            authorName = author.name,
            authorAvatar = author.photoUrls.firstOrNull() ?: "",
            isVerified = author.isVerified,
            type = type,
            mediaUrl = mediaUrl,
            caption = caption,
            textContent = textContent,
            backgroundColorHex = backgroundColorHex
        )

        val currentGroups = _storyGroups.value.toMutableList()
        val ownGroupIndex = currentGroups.indexOfFirst { it.authorId == author.id }

        if (ownGroupIndex != -1) {
            val ownGroup = currentGroups[ownGroupIndex]
            val updatedStories = ownGroup.stories + newStory
            currentGroups[ownGroupIndex] = ownGroup.copy(stories = updatedStories)
        } else {
            val newOwnGroup = StoryGroup(
                authorId = author.id,
                authorName = author.name,
                authorAvatar = author.photoUrls.firstOrNull() ?: "",
                isVerified = author.isVerified,
                isOwnGroup = true,
                stories = listOf(newStory),
                hasUnseen = false
            )
            currentGroups.add(0, newOwnGroup)
        }

        _storyGroups.value = currentGroups
        return newStory
    }

    fun markStoryAsViewed(storyId: String, currentUserId: String) {
        val updatedGroups = _storyGroups.value.map { group ->
            val updatedStories = group.stories.map { story ->
                if (story.id == storyId) {
                    val alreadyViewed = story.viewers.any { it.userId == currentUserId }
                    val newViewers = if (!alreadyViewed) {
                        story.viewers + StoryViewer(
                            userId = currentUserId,
                            userName = "Me",
                            userAvatar = ""
                        )
                    } else story.viewers
                    story.copy(viewers = newViewers, isViewedByMe = true)
                } else story
            }
            val hasUnseen = updatedStories.any { !it.isViewedByMe }
            group.copy(stories = updatedStories, hasUnseen = hasUnseen)
        }
        _storyGroups.value = updatedGroups
    }

    fun reactToStory(storyId: String, currentUserId: String, currentUserName: String, emoji: String) {
        val updatedGroups = _storyGroups.value.map { group ->
            val updatedStories = group.stories.map { story ->
                if (story.id == storyId) {
                    val reaction = StoryReaction(currentUserId, currentUserName, emoji)
                    story.copy(reactions = story.reactions + reaction)
                } else story
            }
            group.copy(stories = updatedStories)
        }
        _storyGroups.value = updatedGroups
    }

    fun deleteStory(storyId: String) {
        val updatedGroups = _storyGroups.value.mapNotNull { group ->
            val filteredStories = group.stories.filterNot { it.id == storyId }
            if (filteredStories.isEmpty() && !group.isOwnGroup) {
                null
            } else {
                group.copy(stories = filteredStories)
            }
        }
        _storyGroups.value = updatedGroups
    }

    private fun sampleStories(): List<StoryGroup> {
        val now = System.currentTimeMillis()
        val expiry = now + (24 * 60 * 60 * 1000)

        val storyMe = StoryGroup(
            authorId = "user_me",
            authorName = "Your Story",
            authorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
            isVerified = true,
            isOwnGroup = true,
            stories = listOf(
                StoryItem("s_me_1", "user_me", "Alex Johnson", "https://images.unsplash.com/photo-1534528741775-53994a69daeb", true, StoryType.TEXT, textContent = "Sunset vibes at the beach tonight! 🌅✨", backgroundColorHex = "#E91E63", createdAtMillis = now - 3600000, expiresAtMillis = expiry)
            ),
            hasUnseen = false
        )

        val storySophia = StoryGroup(
            authorId = "u2",
            authorName = "Sophia",
            authorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
            isVerified = true,
            isOwnGroup = false,
            stories = listOf(
                StoryItem("s_s1", "u2", "Sophia Martinez", "https://images.unsplash.com/photo-1534528741775-53994a69daeb", true, StoryType.PHOTO, mediaUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9", caption = "Weekend coffee date spot ☕💛", createdAtMillis = now - 7200000, expiresAtMillis = expiry)
            ),
            hasUnseen = true
        )

        val storyLiam = StoryGroup(
            authorId = "u3",
            authorName = "Liam",
            authorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
            isVerified = false,
            isOwnGroup = false,
            stories = listOf(
                StoryItem("s_l1", "u3", "Liam Chen", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d", false, StoryType.PHOTO, mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb", caption = "Hiking up Yosemite Valley 🏔️🌲", createdAtMillis = now - 10800000, expiresAtMillis = expiry)
            ),
            hasUnseen = true
        )

        return listOf(storyMe, storySophia, storyLiam)
    }
}
