package com.example.data.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await

/**
 * Production-ready Firebase Storage Service.
 * Handles photo, video, and audio asset uploads/downloads to Firebase Cloud Storage.
 * Uses default storage bucket configured by google-services.json or runtime instance.
 */
object FirebaseStorageService {
    private const val TAG = "FirebaseStorageService"

    val storageInstance: FirebaseStorage?
        get() = try {
            FirebaseStorage.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseStorage instance not available: ${e.message}")
            null
        }

    /**
     * Upload ByteArray data to specified path in Firebase Storage and return the public Download URL string.
     */
    suspend fun uploadBytes(
        storagePath: String,
        data: ByteArray,
        mimeType: String = "image/jpeg"
    ): Result<String> {
        val storage = storageInstance ?: return Result.failure(IllegalStateException("Firebase Storage not initialized"))
        return try {
            val ref = storage.reference.child(storagePath)
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .build()

            ref.putBytes(data, metadata).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(TAG, "Upload successful to $storagePath -> $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "uploadBytes error for $storagePath: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Upload local file Uri to specified path in Firebase Storage
     */
    suspend fun uploadFileUri(
        storagePath: String,
        fileUri: Uri
    ): Result<String> {
        val storage = storageInstance ?: return Result.failure(IllegalStateException("Firebase Storage not initialized"))
        return try {
            val ref = storage.reference.child(storagePath)
            ref.putFile(fileUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(TAG, "File Uri upload successful to $storagePath -> $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "uploadFileUri error for $storagePath: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Helper to upload User Profile picture
     */
    suspend fun uploadProfilePhoto(userId: String, imageBytes: ByteArray): Result<String> {
        val path = "profile_pictures/$userId/profile_${System.currentTimeMillis()}.jpg"
        return uploadBytes(path, imageBytes, "image/jpeg")
    }

    /**
     * Helper to upload Feed Post picture
     */
    suspend fun uploadPostPhoto(userId: String, imageBytes: ByteArray): Result<String> {
        val path = "post_pictures/$userId/post_${System.currentTimeMillis()}.jpg"
        return uploadBytes(path, imageBytes, "image/jpeg")
    }

    /**
     * Helper to upload Story picture
     */
    suspend fun uploadStoryPhoto(userId: String, imageBytes: ByteArray): Result<String> {
        val path = "story_pictures/$userId/story_${System.currentTimeMillis()}.jpg"
        return uploadBytes(path, imageBytes, "image/jpeg")
    }

    /**
     * Helper to upload Chat Attachment image or audio
     */
    suspend fun uploadChatMedia(
        chatId: String,
        mediaBytes: ByteArray,
        isAudio: Boolean = false
    ): Result<String> {
        val ext = if (isAudio) "aac" else "jpg"
        val mime = if (isAudio) "audio/aac" else "image/jpeg"
        val path = "chat_attachments/$chatId/media_${System.currentTimeMillis()}.$ext"
        return uploadBytes(path, mediaBytes, mime)
    }

    /**
     * Delete file from Storage by full URL or storage path
     */
    suspend fun deleteFile(fileUrlOrPath: String): Result<Boolean> {
        val storage = storageInstance ?: return Result.failure(IllegalStateException("Firebase Storage not initialized"))
        return try {
            val ref = if (fileUrlOrPath.startsWith("http://") || fileUrlOrPath.startsWith("https://")) {
                storage.getReferenceFromUrl(fileUrlOrPath)
            } else {
                storage.reference.child(fileUrlOrPath)
            }
            ref.delete().await()
            Log.d(TAG, "Successfully deleted file: $fileUrlOrPath")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "deleteFile error for $fileUrlOrPath: ${e.message}", e)
            Result.failure(e)
        }
    }
}
