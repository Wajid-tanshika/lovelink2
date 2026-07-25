package com.example.ui.screens.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.NotificationRepository
import com.example.data.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoryViewModel @JvmOverloads constructor(
    private val storyRepository: StoryRepository = StoryRepository(),
    private val notificationRepository: NotificationRepository? = null
) : ViewModel() {

    val storyGroups: StateFlow<List<StoryGroup>> = storyRepository.storyGroups

    private val _selectedGroup = MutableStateFlow<StoryGroup?>(null)
    val selectedGroup: StateFlow<StoryGroup?> = _selectedGroup.asStateFlow()

    private val _selectedStoryIndex = MutableStateFlow(0)
    val selectedStoryIndex: StateFlow<Int> = _selectedStoryIndex.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun openStoryGroup(group: StoryGroup, initialIndex: Int = 0) {
        _selectedGroup.value = group
        _selectedStoryIndex.value = initialIndex
        if (group.stories.isNotEmpty() && initialIndex < group.stories.size) {
            val story = group.stories[initialIndex]
            storyRepository.markStoryAsViewed(story.id, "user_me")
        }
    }

    fun nextStory(currentUserId: String = "user_me") {
        val group = _selectedGroup.value ?: return
        if (_selectedStoryIndex.value < group.stories.size - 1) {
            _selectedStoryIndex.value += 1
            val story = group.stories[_selectedStoryIndex.value]
            storyRepository.markStoryAsViewed(story.id, currentUserId)
        } else {
            // Next group
            val groups = storyGroups.value
            val currentGroupIndex = groups.indexOfFirst { it.authorId == group.authorId }
            if (currentGroupIndex != -1 && currentGroupIndex < groups.size - 1) {
                openStoryGroup(groups[currentGroupIndex + 1], 0)
            } else {
                closeStoryViewer()
            }
        }
    }

    fun previousStory(currentUserId: String = "user_me") {
        val group = _selectedGroup.value ?: return
        if (_selectedStoryIndex.value > 0) {
            _selectedStoryIndex.value -= 1
            val story = group.stories[_selectedStoryIndex.value]
            storyRepository.markStoryAsViewed(story.id, currentUserId)
        } else {
            // Previous group
            val groups = storyGroups.value
            val currentGroupIndex = groups.indexOfFirst { it.authorId == group.authorId }
            if (currentGroupIndex > 0) {
                val prevGroup = groups[currentGroupIndex - 1]
                openStoryGroup(prevGroup, (prevGroup.stories.size - 1).coerceAtLeast(0))
            } else {
                closeStoryViewer()
            }
        }
    }

    fun closeStoryViewer() {
        _selectedGroup.value = null
        _selectedStoryIndex.value = 0
    }

    fun createStory(
        author: UserProfile,
        type: StoryType,
        mediaUrl: String? = null,
        caption: String? = null,
        textContent: String? = null,
        backgroundColorHex: String = "#FF1493"
    ) {
        storyRepository.createStory(author, type, mediaUrl, caption, textContent, backgroundColorHex)
        _toastMessage.value = "Story posted successfully!"
    }

    fun reactToStory(storyId: String, currentUserId: String, currentUserName: String, emoji: String) {
        storyRepository.reactToStory(storyId, currentUserId, currentUserName, emoji)
        _toastMessage.value = "Reacted with $emoji"
    }

    fun replyToStory(story: StoryItem, currentUserId: String, messageText: String) {
        if (messageText.isBlank()) return
        notificationRepository?.addNotification(
            title = "Story Reply",
            body = "New reply on your story: \"$messageText\"",
            type = com.example.data.model.NotificationType.MESSAGE,
            avatarUrl = story.authorAvatar
        )
        _toastMessage.value = "Reply sent to ${story.authorName}!"
    }

    fun deleteStory(storyId: String) {
        storyRepository.deleteStory(storyId)
        _toastMessage.value = "Story deleted"
        closeStoryViewer()
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

class StoryViewModelFactory(
    private val storyRepository: StoryRepository,
    private val notificationRepository: NotificationRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StoryViewModel(storyRepository, notificationRepository) as T
    }
}
