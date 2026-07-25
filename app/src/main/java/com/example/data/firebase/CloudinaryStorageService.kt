package com.example.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Cloudinary Media Storage Service (Unsigned Upload).
 *
 * Requires a Cloudinary account with an UNSIGNED upload preset created
 * in Settings -> Upload -> Upload presets (mode: Unsigned). This lets
 * the app upload directly from the device without exposing the API
 * secret. Configure via [configure] at app startup (see
 * LoveLinkApplication) using values from your .env / BuildConfig.
 */
object CloudinaryStorageService {
    private const val TAG = "CloudinaryStorage"

    private var cloudName: String = ""
    private var uploadPreset: String = ""

    fun configure(cloudName: String, uploadPreset: String) {
        this.cloudName = cloudName.trim()
        this.uploadPreset = uploadPreset.trim()
        Log.d(TAG, "Cloudinary configured. Cloud name set: ${this.cloudName.isNotBlank()}")
    }

    fun isConfigured(): Boolean = cloudName.isNotBlank() && uploadPreset.isNotBlank()

    /**
     * Upload raw bytes (already-compressed JPEG/PNG) using an unsigned
     * upload preset. Returns the secure_url of the uploaded asset.
     */
    suspend fun uploadBytes(
        fileName: String,
        data: ByteArray,
        resourceType: String = "image" // "image" or "video"
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(IllegalStateException("Cloudinary is not configured. Call CloudinaryStorageService.configure(cloudName, uploadPreset) first."))
        }
        try {
            val boundary = "LoveLinkBoundary${UUID.randomUUID()}"
            val url = URL("https://api.cloudinary.com/v1_1/$cloudName/$resourceType/upload")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                connectTimeout = 20000
                readTimeout = 30000
            }

            val body = ByteArrayOutputStream()
            fun writeField(name: String, value: String) {
                body.write("--$boundary\r\n".toByteArray())
                body.write("Content-Disposition: form-data; name=\"$name\"\r\n\r\n".toByteArray())
                body.write("$value\r\n".toByteArray())
            }

            writeField("upload_preset", uploadPreset)
            body.write("--$boundary\r\n".toByteArray())
            body.write("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n".toByteArray())
            body.write("Content-Type: application/octet-stream\r\n\r\n".toByteArray())
            body.write(data)
            body.write("\r\n--$boundary--\r\n".toByteArray())

            connection.outputStream.use { it.write(body.toByteArray()) }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream.bufferedReader().use { it.readText() }

            if (responseCode in 200..299) {
                val json = JSONObject(responseText)
                val secureUrl = json.optString("secure_url")
                if (secureUrl.isNotBlank()) {
                    Log.d(TAG, "Cloudinary upload success: $secureUrl")
                    Result.success(secureUrl)
                } else {
                    Result.failure(Exception("Cloudinary response missing secure_url"))
                }
            } else {
                Log.w(TAG, "Cloudinary upload failed ($responseCode): $responseText")
                Result.failure(Exception("Cloudinary upload failed: $responseText"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cloudinary upload error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Convenience: upload from a content:// Uri (reads the whole file into memory,
     * fine for profile photos / short clips; for large videos consider chunked upload).
     */
    suspend fun uploadUri(
        context: Context,
        uri: Uri,
        fileName: String,
        resourceType: String = "image"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Could not read file from Uri"))
            uploadBytes(fileName, bytes, resourceType)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Best-effort delete via the Cloudinary Admin API is intentionally NOT
     * done here because it requires the API secret (server-side only).
     * Deletion should be handled by a backend/Cloud Function using the
     * Admin API with signed authentication, not from the client.
     */
}
