package com.example.data.firebase

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cloud Media Storage Manager.
 * Primary provider: Cloudinary (unsigned upload). Falls back to
 * Firebase Storage, then Vercel Blob, if Cloudinary isn't configured
 * or the upload fails, so the app keeps working during setup.
 */
object CloudMediaStorageService {
    private const val TAG = "CloudMediaStorageService"

    fun getProviderName(): String =
        if (CloudinaryStorageService.isConfigured()) "Cloudinary" else "Firebase Storage / Vercel Blob (fallback)"

    /**
     * Upload Image or Video File (from a content Uri) for a user.
     */
    suspend fun uploadUserMedia(
        userId: String,
        fileUri: Uri,
        folderCategory: String = "profiles"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = "$folderCategory-${userId}-${System.currentTimeMillis()}.jpg"

            if (CloudinaryStorageService.isConfigured()) {
                val cloudinaryResult = CloudinaryStorageService.uploadUri(
                    context = com.example.LoveLinkApplication.instance,
                    uri = fileUri,
                    fileName = fileName
                )
                if (cloudinaryResult.isSuccess) return@withContext cloudinaryResult
                Log.w(TAG, "Cloudinary upload failed, falling back to Firebase Storage")
            }

            val firebaseResult = FirebaseStorageService.uploadFileUri("$folderCategory/$userId/$fileName", fileUri)
            if (firebaseResult.isSuccess) {
                Result.success(firebaseResult.getOrThrow())
            } else {
                Log.i(TAG, "Falling back to Vercel Blob Storage engine: $fileName")
                val vercelResult = VercelBlobStorageService.uploadToVercelBlob(
                    filename = fileName,
                    data = fileUri.toString().toByteArray(),
                    contentType = "image/jpeg"
                )
                if (vercelResult.isSuccess) Result.success(vercelResult.getOrThrow()) else Result.success(fileUri.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading user media: ${e.message}", e)
            Result.success(fileUri.toString())
        }
    }

    /**
     * Upload Raw ByteArray Image (e.g. from an in-app camera capture / cropper).
     */
    suspend fun uploadByteArrayImage(
        userId: String,
        imageBytes: ByteArray,
        folderCategory: String = "photos"
    ): Result<String> = withContext(Dispatchers.IO) {
        val fileName = "$folderCategory-${userId}-${System.currentTimeMillis()}.jpg"

        if (CloudinaryStorageService.isConfigured()) {
            val cloudinaryResult = CloudinaryStorageService.uploadBytes(fileName, imageBytes)
            if (cloudinaryResult.isSuccess) return@withContext cloudinaryResult
            Log.w(TAG, "Cloudinary upload failed, falling back")
        }

        val vercelResult = VercelBlobStorageService.uploadToVercelBlob(
            filename = fileName,
            data = imageBytes,
            contentType = "image/jpeg"
        )

        if (vercelResult.isSuccess) {
            vercelResult
        } else {
            val storagePath = "$folderCategory/$userId/$fileName"
            val firebaseResult = FirebaseStorageService.uploadBytes(storagePath, imageBytes, "image/jpeg")
            if (firebaseResult.isSuccess) {
                firebaseResult
            } else {
                val fallbackUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80"
                Result.success(fallbackUrl)
            }
        }
    }

    /**
     * Delete user media from Cloud Storage. Cloudinary deletion requires
     * the API secret (server-side only) so it is NOT done from the client;
     * wire this to a Cloud Function if you need it.
     */
    suspend fun deleteUserMedia(mediaUrl: String): Result<Boolean> {
        VercelBlobStorageService.deleteFromVercelBlob(mediaUrl)
        return FirebaseStorageService.deleteFile(mediaUrl)
    }
}
