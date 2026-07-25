package com.example.data.firebase

import android.util.Log
import com.example.data.model.AppSettings
import com.example.data.model.ChatMessage
import com.example.data.model.MatchItem
import com.example.data.model.NotificationItem
import com.example.data.model.PostItem
import com.example.data.model.ReportItem
import com.example.data.model.StoryItem
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Production-ready Firestore Database Service.
 * Provides CRUD and Realtime Streams for User Profiles, Feed Posts, Chat Messages,
 * Withdrawal Requests, and FCM Push Tokens without hardcoding configurations.
 */
object FirestoreService {
    private const val TAG = "FirestoreService"

    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_PROFILES = "profiles"
    private const val COLLECTION_POSTS = "posts"
    private const val COLLECTION_STORIES = "stories"
    private const val COLLECTION_CHATS = "chats"
    private const val COLLECTION_MESSAGES = "messages"
    private const val COLLECTION_MATCHES = "matches"
    private const val COLLECTION_NOTIFICATIONS = "notifications"
    private const val COLLECTION_REPORTS = "reports"
    private const val COLLECTION_BLOCKS = "blocks"
    private const val COLLECTION_PREMIUM = "premium"
    private const val COLLECTION_COINS = "coins"
    private const val COLLECTION_WALLET = "wallet"
    private const val COLLECTION_WITHDRAWALS = "withdrawal_requests"
    private const val COLLECTION_SETTINGS = "app_settings"
    private const val COLLECTION_ADMINS = "admins"

    val firestoreInstance: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseFirestore instance not available: ${e.message}")
            null
        }

    /* ------------------------------------------------------------------------- */
    /* USER PROFILES & FCM TOKEN                                                */
    /* ------------------------------------------------------------------------- */

    /**
     * Save or update user profile document in Firestore
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Boolean> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        val userId = profile.id.ifBlank { profile.email }
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID and Email are blank"))

        return try {
            db.collection(COLLECTION_USERS)
                .document(userId)
                .set(profile, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved user profile for $userId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "saveUserProfile error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get user profile by User ID
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val doc = db.collection(COLLECTION_USERS).document(userId).get().await()
            if (doc.exists()) {
                val profile = doc.toObject(UserProfile::class.java)
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUserProfile error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update FCM Registration Token for push notifications
     */
    suspend fun updateFcmToken(userId: String, token: String): Result<Boolean> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        if (userId.isBlank() || token.isBlank()) return Result.success(false)

        return try {
            val update = mapOf(
                "fcmToken" to token,
                "fcmTokenUpdatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_USERS)
                .document(userId)
                .set(update, SetOptions.merge())
                .await()
            Log.d(TAG, "Updated FCM token for $userId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "updateFcmToken error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /* ------------------------------------------------------------------------- */
    /* REALTIME CHAT MESSAGES                                                   */
    /* ------------------------------------------------------------------------- */

    /**
     * Realtime Stream of Chat Messages for a specific match/chat conversation
     */
    fun getChatMessagesStream(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val db = firestoreInstance
        if (db == null || chatId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection(COLLECTION_CHATS)
            .document(chatId)
            .collection(COLLECTION_MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getChatMessagesStream listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    trySend(messages)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Send a new Chat Message to Firestore
     */
    suspend fun sendChatMessage(chatId: String, message: ChatMessage): Result<Boolean> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val docRef = if (message.id.isNotBlank()) {
                db.collection(COLLECTION_CHATS).document(chatId).collection(COLLECTION_MESSAGES).document(message.id)
            } else {
                db.collection(COLLECTION_CHATS).document(chatId).collection(COLLECTION_MESSAGES).document()
            }
            val msgWithId = message.copy(id = docRef.id, matchId = chatId)
            docRef.set(msgWithId).await()
            Log.d(TAG, "Sent chat message ${docRef.id} to chat $chatId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "sendChatMessage error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /* ------------------------------------------------------------------------- */
    /* POSTS & FEED                                                             */
    /* ------------------------------------------------------------------------- */

    /**
     * Realtime Stream of Social Feed Posts
     */
    fun getPostsStream(): Flow<List<PostItem>> = callbackFlow {
        val db = firestoreInstance
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(COLLECTION_POSTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getPostsStream listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val posts = snapshot.toObjects(PostItem::class.java)
                    trySend(posts)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Create or update a Post item
     */
    suspend fun savePost(post: PostItem): Result<String> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val docRef = if (post.id.isNotBlank()) {
                db.collection(COLLECTION_POSTS).document(post.id)
            } else {
                db.collection(COLLECTION_POSTS).document()
            }
            val finalPost = post.copy(id = docRef.id)
            docRef.set(finalPost).await()
            Log.d(TAG, "Saved post ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "savePost error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /* ------------------------------------------------------------------------- */
    /* WITHDRAWALS & ADMIN CONFIGS                                              */
    /* ------------------------------------------------------------------------- */

    /**
     * Submit a Coin Withdrawal Request to Firestore
     */
    suspend fun submitWithdrawalRequest(request: WithdrawalRequest): Result<String> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val docRef = if (request.id.isNotBlank()) {
                db.collection(COLLECTION_WITHDRAWALS).document(request.id)
            } else {
                db.collection(COLLECTION_WITHDRAWALS).document()
            }
            val finalReq = request.copy(id = docRef.id)
            docRef.set(finalReq).await()
            Log.d(TAG, "Submitted withdrawal request ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "submitWithdrawalRequest error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch user's withdrawal requests
     */
    suspend fun getUserWithdrawalRequests(userId: String): Result<List<WithdrawalRequest>> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val query = db.collection(COLLECTION_WITHDRAWALS)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val list = query.toObjects(WithdrawalRequest::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "getUserWithdrawalRequests error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /* ------------------------------------------------------------------------- */
    /* STORIES, MATCHES, NOTIFICATIONS, REPORTS & BLOCKS                       */
    /* ------------------------------------------------------------------------- */

    /**
     * Realtime Stream of Stories
     */
    fun getStoriesStream(): Flow<List<StoryItem>> = callbackFlow {
        val db = firestoreInstance
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(COLLECTION_STORIES)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getStoriesStream error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val stories = snapshot.toObjects(StoryItem::class.java)
                    trySend(stories)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Save a Story document
     */
    suspend fun saveStory(story: StoryItem): Result<String> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val docRef = if (story.id.isNotBlank()) {
                db.collection(COLLECTION_STORIES).document(story.id)
            } else {
                db.collection(COLLECTION_STORIES).document()
            }
            val finalStory = story.copy(id = docRef.id)
            docRef.set(finalStory).await()
            Log.d(TAG, "Saved story ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "saveStory error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Save or update Match
     */
    suspend fun saveMatch(match: MatchItem): Result<Boolean> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            db.collection(COLLECTION_MATCHES).document(match.id).set(match, SetOptions.merge()).await()
            Log.d(TAG, "Saved match ${match.id}")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "saveMatch error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Save or record Notification
     */
    suspend fun saveNotification(notification: NotificationItem): Result<Boolean> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val docRef = if (notification.id.isNotBlank()) {
                db.collection(COLLECTION_NOTIFICATIONS).document(notification.id)
            } else {
                db.collection(COLLECTION_NOTIFICATIONS).document()
            }
            val finalNotif = notification.copy(id = docRef.id)
            docRef.set(finalNotif).await()
            Log.d(TAG, "Saved notification ${docRef.id}")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "saveNotification error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Submit User Report
     */
    suspend fun submitReport(report: ReportItem): Result<Boolean> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val docRef = if (report.id.isNotBlank()) {
                db.collection(COLLECTION_REPORTS).document(report.id)
            } else {
                db.collection(COLLECTION_REPORTS).document()
            }
            val finalReport = report.copy(id = docRef.id)
            docRef.set(finalReport).await()
            Log.d(TAG, "Submitted user report ${docRef.id}")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "submitReport error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Block user
     */
    suspend fun blockUser(blockerId: String, blockedId: String): Result<Boolean> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val blockData = mapOf(
                "blockerId" to blockerId,
                "blockedId" to blockedId,
                "timestamp" to System.currentTimeMillis()
            )
            val docId = "${blockerId}_${blockedId}"
            db.collection(COLLECTION_BLOCKS).document(docId).set(blockData, SetOptions.merge()).await()
            Log.d(TAG, "User $blockerId blocked $blockedId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "blockUser error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch App Settings from Firestore
     */
    suspend fun getAppSettings(): Result<AppSettings?> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val doc = db.collection(COLLECTION_SETTINGS).document("global").get().await()
            if (doc.exists()) {
                val settings = doc.toObject(AppSettings::class.java)
                Result.success(settings)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAppSettings error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Save App Settings to Firestore
     */
    suspend fun saveAppSettings(settings: AppSettings): Result<Boolean> {
        val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            db.collection(COLLECTION_SETTINGS).document("global").set(settings, SetOptions.merge()).await()
            Log.d(TAG, "Saved AppSettings to Firestore")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "saveAppSettings error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
