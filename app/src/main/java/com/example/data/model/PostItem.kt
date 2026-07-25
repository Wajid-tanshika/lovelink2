package com.example.data.model

data class PostItem(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val caption: String = "",
    val mediaUrls: List<String> = emptyList(),
    val isVideo: Boolean = false,
    val videoThumbnailUrl: String? = null,
    val location: String? = null,
    val hashtags: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val likedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
    val savedBy: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isReported: Boolean = false,
    val reportReason: String? = null
) {
    val likeCount: Int get() = likedBy.size
}

data class CommentItem(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val content: String = "",
    val parentCommentId: String? = null,
    val likedBy: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isReported: Boolean = false
) {
    val likeCount: Int get() = likedBy.size
}
