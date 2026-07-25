package com.example.data.repository

import com.example.data.model.CommentItem
import com.example.data.model.NotificationType
import com.example.data.model.PostItem
import com.example.data.source.FirestoreFeedService
import com.example.data.source.SampleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedRepository(
    private val notificationRepository: NotificationRepository? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Pre-seeded rich sample posts for instant demo feed experience
    private val initialPosts = listOf(
        PostItem(
            id = "post_1",
            authorId = SampleData.PROFILES[0].id,
            authorName = SampleData.PROFILES[0].name,
            authorPhotoUrl = SampleData.PROFILES[0].photoUrls.firstOrNull() ?: "",
            isAuthorVerified = true,
            caption = "Sunset coffee date in Manhattan ✨ Loving the autumn vibes in the city! Who wants to grab a latte next weekend? #ManhattanVibes #CoffeeLover #NYC #LoveLink",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80"
            ),
            isVideo = false,
            location = "Greenwich Village, New York",
            hashtags = listOf("ManhattanVibes", "CoffeeLover", "NYC", "LoveLink"),
            mentions = listOf("alex_me", "maya_l"),
            likedBy = listOf("user_me", "prof_2", "prof_3"),
            commentCount = 4,
            savedBy = listOf("user_me"),
            timestamp = System.currentTimeMillis() - 1000 * 60 * 45
        ),
        PostItem(
            id = "post_2",
            authorId = SampleData.PROFILES[1].id,
            authorName = SampleData.PROFILES[1].name,
            authorPhotoUrl = SampleData.PROFILES[1].photoUrls.firstOrNull() ?: "",
            isAuthorVerified = true,
            caption = "Weekend hike up the Hudson valley trails 🏔️ Sunshine, fresh air, and good company are the best cure for a busy week. Mention someone who loves high peaks! #HikingAdventures #Nature #WeekendGetaway",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80"
            ),
            isVideo = false,
            location = "Hudson Highlands, NY",
            hashtags = listOf("HikingAdventures", "Nature", "WeekendGetaway"),
            mentions = listOf("sophiachen"),
            likedBy = listOf("prof_1", "prof_3"),
            commentCount = 2,
            savedBy = emptyList(),
            timestamp = System.currentTimeMillis() - 1000 * 60 * 180
        ),
        PostItem(
            id = "post_3",
            authorId = SampleData.PROFILES[2].id,
            authorName = SampleData.PROFILES[2].name,
            authorPhotoUrl = SampleData.PROFILES[2].photoUrls.firstOrNull() ?: "",
            isAuthorVerified = false,
            caption = "Live acoustic lounge performance tonight! 🎶 Music brings people together faster than words ever could. What is your favorite live concert memory? #LiveMusic #AcousticSessions #ArtAndSoul",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=800&q=80"
            ),
            isVideo = true,
            videoThumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=800&q=80",
            location = "SoHo Lounge, NYC",
            hashtags = listOf("LiveMusic", "AcousticSessions", "ArtAndSoul"),
            mentions = emptyList(),
            likedBy = listOf("user_me", "prof_1"),
            commentCount = 1,
            savedBy = listOf("user_me"),
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 6
        )
    )

    private val initialComments = mapOf(
        "post_1" to listOf(
            CommentItem(
                id = "comm_1",
                postId = "post_1",
                authorId = SampleData.PROFILES[1].id,
                authorName = SampleData.PROFILES[1].name,
                authorPhotoUrl = SampleData.PROFILES[1].photoUrls.firstOrNull() ?: "",
                isAuthorVerified = true,
                content = "That coffee shop has the best almond croissants in Greenwich! 🥐",
                likedBy = listOf("user_me"),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30
            ),
            CommentItem(
                id = "comm_2",
                postId = "post_1",
                authorId = "user_me",
                authorName = "Alex Rivera",
                authorPhotoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                isAuthorVerified = true,
                content = "Beautiful photos Sophia! Count me in for next weekend ☕",
                likedBy = listOf("prof_1"),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 15
            )
        )
    )

    private val _posts = MutableStateFlow<List<PostItem>>(initialPosts)
    val posts: StateFlow<List<PostItem>> = _posts.asStateFlow()

    private val _savedPostIds = MutableStateFlow<Set<String>>(setOf("post_1", "post_3"))
    val savedPostIds: StateFlow<Set<String>> = _savedPostIds.asStateFlow()

    private val _commentsMap = MutableStateFlow<Map<String, List<CommentItem>>>(initialComments)
    val commentsMap: StateFlow<Map<String, List<CommentItem>>> = _commentsMap.asStateFlow()

    fun createPost(
        currentUserId: String,
        currentUserName: String,
        currentUserPhoto: String,
        isVerified: Boolean,
        caption: String,
        mediaUrls: List<String>,
        isVideo: Boolean = false,
        location: String? = null,
        hashtags: List<String> = emptyList(),
        mentions: List<String> = emptyList()
    ) {
        val newPost = PostItem(
            id = "post_${System.currentTimeMillis()}",
            authorId = currentUserId,
            authorName = currentUserName,
            authorPhotoUrl = currentUserPhoto,
            isAuthorVerified = isVerified,
            caption = caption,
            mediaUrls = mediaUrls,
            isVideo = isVideo,
            videoThumbnailUrl = if (isVideo && mediaUrls.isNotEmpty()) mediaUrls.first() else null,
            location = location,
            hashtags = hashtags,
            mentions = mentions,
            likedBy = emptyList(),
            commentCount = 0,
            savedBy = emptyList(),
            timestamp = System.currentTimeMillis()
        )

        _posts.value = listOf(newPost) + _posts.value

        // Sync with Firestore in background
        scope.launch {
            FirestoreFeedService.createPostInFirestore(newPost)
        }

        // Notify mentioned users or followers
        if (mentions.isNotEmpty()) {
            mentions.forEach { mention ->
                notificationRepository?.addNotification(
                    title = "New Mention 🏷️",
                    body = "$currentUserName mentioned you in a post: \"${caption.take(30)}...\"",
                    type = NotificationType.MENTION,
                    avatarUrl = currentUserPhoto
                )
            }
        }
    }

    fun toggleLikePost(postId: String, currentUserId: String, currentUserName: String, currentUserPhoto: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val isLiked = post.likedBy.contains(currentUserId)
                val newLikedBy = if (isLiked) {
                    post.likedBy - currentUserId
                } else {
                    post.likedBy + currentUserId
                }

                if (!isLiked && post.authorId != currentUserId) {
                    notificationRepository?.addNotification(
                        title = "New Post Like ❤️",
                        body = "$currentUserName liked your post!",
                        type = NotificationType.LIKE,
                        avatarUrl = currentUserPhoto
                    )
                }

                val updatedPost = post.copy(likedBy = newLikedBy)
                scope.launch { FirestoreFeedService.createPostInFirestore(updatedPost) }
                updatedPost
            } else {
                post
            }
        }
    }

    fun toggleSavePost(postId: String, currentUserId: String) {
        val currentSaved = _savedPostIds.value.toMutableSet()
        val isSaved = currentSaved.contains(postId)
        if (isSaved) {
            currentSaved.remove(postId)
        } else {
            currentSaved.add(postId)
        }
        _savedPostIds.value = currentSaved

        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newSavedBy = if (isSaved) post.savedBy - currentUserId else post.savedBy + currentUserId
                post.copy(savedBy = newSavedBy)
            } else post
        }
    }

    fun addComment(
        postId: String,
        authorId: String,
        authorName: String,
        authorPhotoUrl: String,
        isVerified: Boolean,
        content: String,
        parentCommentId: String? = null
    ) {
        val newComment = CommentItem(
            id = "comm_${System.currentTimeMillis()}",
            postId = postId,
            authorId = authorId,
            authorName = authorName,
            authorPhotoUrl = authorPhotoUrl,
            isAuthorVerified = isVerified,
            content = content,
            parentCommentId = parentCommentId,
            timestamp = System.currentTimeMillis()
        )

        val currentList = _commentsMap.value[postId] ?: emptyList()
        val updatedMap = _commentsMap.value.toMutableMap()
        updatedMap[postId] = currentList + newComment
        _commentsMap.value = updatedMap

        // Update comment count on post
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedPost = post.copy(commentCount = post.commentCount + 1)
                if (post.authorId != authorId) {
                    notificationRepository?.addNotification(
                        title = "New Comment 💬",
                        body = "$authorName commented: \"${content.take(30)}...\"",
                        type = NotificationType.COMMENT,
                        avatarUrl = authorPhotoUrl
                    )
                }
                scope.launch { FirestoreFeedService.createPostInFirestore(updatedPost) }
                updatedPost
            } else post
        }

        scope.launch {
            FirestoreFeedService.createCommentInFirestore(newComment)
        }
    }

    fun toggleLikeComment(postId: String, commentId: String, currentUserId: String) {
        val currentList = _commentsMap.value[postId] ?: return
        val updatedList = currentList.map { comment ->
            if (comment.id == commentId) {
                val isLiked = comment.likedBy.contains(currentUserId)
                val newLikedBy = if (isLiked) comment.likedBy - currentUserId else comment.likedBy + currentUserId
                comment.copy(likedBy = newLikedBy)
            } else comment
        }
        val updatedMap = _commentsMap.value.toMutableMap()
        updatedMap[postId] = updatedList
        _commentsMap.value = updatedMap
    }

    fun deleteComment(postId: String, commentId: String) {
        val currentList = _commentsMap.value[postId] ?: return
        val updatedList = currentList.filterNot { it.id == commentId }
        val updatedMap = _commentsMap.value.toMutableMap()
        updatedMap[postId] = updatedList
        _commentsMap.value = updatedMap

        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(commentCount = (post.commentCount - 1).coerceAtLeast(0))
            } else post
        }

        scope.launch {
            FirestoreFeedService.deleteCommentFromFirestore(commentId)
        }
    }

    fun deletePost(postId: String) {
        _posts.value = _posts.value.filterNot { it.id == postId }
        val updatedMap = _commentsMap.value.toMutableMap()
        updatedMap.remove(postId)
        _commentsMap.value = updatedMap

        scope.launch {
            FirestoreFeedService.deletePostFromFirestore(postId)
        }
    }

    fun reportPost(postId: String, reason: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(isReported = true, reportReason = reason)
            } else post
        }
    }

    fun searchHashtags(tag: String): List<PostItem> {
        if (tag.isBlank()) return emptyList()
        val cleanTag = tag.trimStart('#')
        return _posts.value.filter { post ->
            post.hashtags.any { it.equals(cleanTag, ignoreCase = true) } ||
            post.caption.contains("#$cleanTag", ignoreCase = true)
        }
    }

    fun getReels(): List<PostItem> {
        return _posts.value.filter { it.isVideo }
    }

    fun getTrendingPosts(): List<PostItem> {
        return _posts.value.sortedByDescending { it.likeCount + it.commentCount }
    }

    fun getBookmarkedPosts(): List<PostItem> {
        val saved = _savedPostIds.value
        return _posts.value.filter { saved.contains(it.id) }
    }

    fun reportComment(postId: String, commentId: String) {
        val currentList = _commentsMap.value[postId] ?: return
        val updatedList = currentList.map { comment ->
            if (comment.id == commentId) {
                comment.copy(isReported = true)
            } else comment
        }
        val updatedMap = _commentsMap.value.toMutableMap()
        updatedMap[postId] = updatedList
        _commentsMap.value = updatedMap
    }
}
