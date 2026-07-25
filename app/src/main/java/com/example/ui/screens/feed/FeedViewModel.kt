package com.example.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CommentItem
import com.example.data.model.PostItem
import com.example.data.model.UserProfile
import com.example.data.repository.FeedRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FeedTab {
    EXPLORE, FOLLOWING, SAVED
}

data class CreatePostUiState(
    val caption: String = "",
    val mediaUrls: List<String> = emptyList(),
    val isVideo: Boolean = false,
    val location: String = "",
    val hashtagInput: String = "",
    val hashtagsList: List<String> = emptyList(),
    val mentionInput: String = "",
    val mentionsList: List<String> = emptyList(),
    val isPosting: Boolean = false,
    val postSuccess: Boolean = false
)

class FeedViewModel @JvmOverloads constructor(
    val feedRepository: FeedRepository = FeedRepository(),
    val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(FeedTab.EXPLORE)
    val selectedTab: StateFlow<FeedTab> = _selectedTab.asStateFlow()

    private val _activeCommentPostId = MutableStateFlow<String?>(null)
    val activeCommentPostId: StateFlow<String?> = _activeCommentPostId.asStateFlow()

    private val _createPostState = MutableStateFlow(CreatePostUiState())
    val createPostState: StateFlow<CreatePostUiState> = _createPostState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val posts: StateFlow<List<PostItem>> = feedRepository.posts
    val savedPostIds: StateFlow<Set<String>> = feedRepository.savedPostIds
    val commentsMap: StateFlow<Map<String, List<CommentItem>>> = feedRepository.commentsMap
    val currentUser: StateFlow<UserProfile> = userRepository.currentUserProfile

    val filteredPosts: StateFlow<List<PostItem>> = combine(
        posts,
        savedPostIds,
        _selectedTab,
        currentUser
    ) { allPosts: List<PostItem>, savedIds: Set<String>, tab: FeedTab, user: UserProfile ->
        when (tab) {
            FeedTab.EXPLORE -> allPosts
            FeedTab.FOLLOWING -> allPosts.filter { user.following.contains(it.authorId) || it.authorId == user.id }
            FeedTab.SAVED -> allPosts.filter { savedIds.contains(it.id) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: FeedTab) {
        _selectedTab.value = tab
    }

    fun openCommentsFor(postId: String) {
        _activeCommentPostId.value = postId
    }

    fun closeComments() {
        _activeCommentPostId.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun toggleLikePost(postId: String) {
        val user = currentUser.value
        feedRepository.toggleLikePost(
            postId = postId,
            currentUserId = user.id,
            currentUserName = user.name,
            currentUserPhoto = user.photoUrls.firstOrNull() ?: ""
        )
    }

    fun toggleSavePost(postId: String) {
        val user = currentUser.value
        feedRepository.toggleSavePost(postId, user.id)
        _snackbarMessage.value = "Saved post updated!"
    }

    fun addComment(postId: String, content: String, parentCommentId: String? = null) {
        if (content.isBlank()) return
        val user = currentUser.value
        feedRepository.addComment(
            postId = postId,
            authorId = user.id,
            authorName = user.name,
            authorPhotoUrl = user.photoUrls.firstOrNull() ?: "",
            isVerified = user.isVerified,
            content = content.trim(),
            parentCommentId = parentCommentId
        )
    }

    fun toggleLikeComment(postId: String, commentId: String) {
        val user = currentUser.value
        feedRepository.toggleLikeComment(postId, commentId, user.id)
    }

    fun deleteComment(postId: String, commentId: String) {
        feedRepository.deleteComment(postId, commentId)
        _snackbarMessage.value = "Comment deleted"
    }

    fun deletePost(postId: String) {
        feedRepository.deletePost(postId)
        _snackbarMessage.value = "Post deleted"
    }

    fun reportPost(postId: String, reason: String) {
        feedRepository.reportPost(postId, reason)
        _snackbarMessage.value = "Post reported for moderation review"
    }

    fun reportComment(postId: String, commentId: String) {
        feedRepository.reportComment(postId, commentId)
        _snackbarMessage.value = "Comment reported to admin moderation"
    }

    // Create Post helpers
    fun updateCaption(caption: String) {
        _createPostState.value = _createPostState.value.copy(caption = caption)
    }

    fun updateLocation(location: String) {
        _createPostState.value = _createPostState.value.copy(location = location)
    }

    fun addMediaUrl(url: String, isVideo: Boolean = false) {
        val currentMedia = _createPostState.value.mediaUrls
        if (currentMedia.size < 10) {
            _createPostState.value = _createPostState.value.copy(
                mediaUrls = currentMedia + url,
                isVideo = isVideo
            )
        }
    }

    fun removeMediaUrl(url: String) {
        val currentMedia = _createPostState.value.mediaUrls.filterNot { it == url }
        _createPostState.value = _createPostState.value.copy(mediaUrls = currentMedia)
    }

    fun addHashtag(tag: String) {
        val clean = tag.trim().removePrefix("#")
        if (clean.isNotEmpty() && !_createPostState.value.hashtagsList.contains(clean)) {
            _createPostState.value = _createPostState.value.copy(
                hashtagsList = _createPostState.value.hashtagsList + clean,
                hashtagInput = ""
            )
        }
    }

    fun removeHashtag(tag: String) {
        _createPostState.value = _createPostState.value.copy(
            hashtagsList = _createPostState.value.hashtagsList.filterNot { it == tag }
        )
    }

    fun addMention(username: String) {
        val clean = username.trim().removePrefix("@")
        if (clean.isNotEmpty() && !_createPostState.value.mentionsList.contains(clean)) {
            _createPostState.value = _createPostState.value.copy(
                mentionsList = _createPostState.value.mentionsList + clean,
                mentionInput = ""
            )
        }
    }

    fun removeMention(username: String) {
        _createPostState.value = _createPostState.value.copy(
            mentionsList = _createPostState.value.mentionsList.filterNot { it == username }
        )
    }

    fun submitPost() {
        val state = _createPostState.value
        val user = currentUser.value ?: return

        if (state.caption.isBlank() && state.mediaUrls.isEmpty()) {
            _snackbarMessage.value = "Please add text or photos to your post"
            return
        }

        viewModelScope.launch {
            _createPostState.value = state.copy(isPosting = true)

            feedRepository.createPost(
                currentUserId = user.id,
                currentUserName = user.name,
                currentUserPhoto = user.photoUrls.firstOrNull() ?: "",
                isVerified = user.isVerified,
                caption = state.caption.trim(),
                mediaUrls = state.mediaUrls.ifEmpty {
                    listOf("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80")
                },
                isVideo = state.isVideo,
                location = state.location.ifBlank { null },
                hashtags = state.hashtagsList,
                mentions = state.mentionsList
            )

            _createPostState.value = CreatePostUiState(postSuccess = true)
            _snackbarMessage.value = "Post created successfully! ✨"
        }
    }

    fun resetCreatePostState() {
        _createPostState.value = CreatePostUiState()
    }
}

class FeedViewModelFactory(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeedViewModel(feedRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
