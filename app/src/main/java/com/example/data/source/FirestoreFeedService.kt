package com.example.data.source

import android.util.Log
import com.example.data.model.CommentItem
import com.example.data.model.PostItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreFeedService {
    private const val TAG = "FirestoreFeedService"
    private const val COLLECTION_POSTS = "posts"
    private const val COLLECTION_COMMENTS = "post_comments"

    suspend fun createPostInFirestore(post: PostItem): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = if (post.id.isNotEmpty()) {
                firestore.collection(COLLECTION_POSTS).document(post.id)
            } else {
                firestore.collection(COLLECTION_POSTS).document()
            }
            val finalPost = post.copy(id = docRef.id)
            docRef.set(finalPost).await()
            Log.d(TAG, "Post saved to Firestore with id: ${docRef.id}")
            true
        } catch (e: Throwable) {
            Log.w(TAG, "Failed saving post to Firestore, using memory fallback: ${e.message}")
            false
        }
    }

    suspend fun deletePostFromFirestore(postId: String): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection(COLLECTION_POSTS).document(postId).delete().await()
            Log.d(TAG, "Post deleted from Firestore: $postId")
            true
        } catch (e: Throwable) {
            Log.w(TAG, "Failed deleting post from Firestore: ${e.message}")
            false
        }
    }

    suspend fun createCommentInFirestore(comment: CommentItem): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = if (comment.id.isNotEmpty()) {
                firestore.collection(COLLECTION_COMMENTS).document(comment.id)
            } else {
                firestore.collection(COLLECTION_COMMENTS).document()
            }
            val finalComment = comment.copy(id = docRef.id)
            docRef.set(finalComment).await()
            true
        } catch (e: Throwable) {
            Log.w(TAG, "Failed saving comment to Firestore: ${e.message}")
            false
        }
    }

    suspend fun deleteCommentFromFirestore(commentId: String): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection(COLLECTION_COMMENTS).document(commentId).delete().await()
            true
        } catch (e: Throwable) {
            Log.w(TAG, "Failed deleting comment from Firestore: ${e.message}")
            false
        }
    }
}
