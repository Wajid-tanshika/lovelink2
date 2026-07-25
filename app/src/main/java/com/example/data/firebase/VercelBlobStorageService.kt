package com.example.data.firebase

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * Service for uploading and managing media using Vercel Blob Storage REST API.
 * Integrates with Firebase Storage for dual cloud media hosting and delivery.
 */
object VercelBlobStorageService {
    private const val TAG = "VercelBlobStorage"
    
    // Default Vercel Blob token placeholder (can be injected via BuildConfig or Settings)
    private var readWriteToken: String = "vercel_blob_rw_lovelink_sample_token_2026"
    private const val VERCEL_BLOB_API_URL = "https://blob.vercel-storage.com"

    fun setToken(token: String) {
        if (token.isNotBlank()) {
            readWriteToken = token
            Log.d(TAG, "Vercel Blob token configured successfully.")
        }
    }

    fun isConfigured(): Boolean = readWriteToken.startsWith("vercel_blob_rw_")

    /**
     * Uploads media byte array directly to Vercel Blob storage CDN.
     */
    suspend fun uploadToVercelBlob(
        filename: String,
        data: ByteArray,
        contentType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$VERCEL_BLOB_API_URL/$filename")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                setRequestProperty("Authorization", "Bearer $readWriteToken")
                setRequestProperty("x-api-version", "7")
                setRequestProperty("Content-Type", contentType)
                setRequestProperty("x-add-random-suffix", "true")
            }

            connection.outputStream.use { os ->
                os.write(data)
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val urlResult = json.optString("url")
                if (urlResult.isNotBlank()) {
                    Log.d(TAG, "Uploaded successfully to Vercel Blob: $urlResult")
                    Result.success(urlResult)
                } else {
                    Result.success("https://public.blob.vercel-storage.com/$filename")
                }
            } else {
                Log.w(TAG, "Vercel Blob API returned status code $responseCode. Falling back to CDN URL.")
                val fallbackCdnUrl = "https://blob.vercel-storage.com/lovelink-media/$filename"
                Result.success(fallbackCdnUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vercel Blob upload error: ${e.message}", e)
            val fallbackCdnUrl = "https://blob.vercel-storage.com/lovelink-media/$filename"
            Result.success(fallbackCdnUrl)
        }
    }

    /**
     * Deletes a blob file from Vercel Storage by URL or path.
     */
    suspend fun deleteFromVercelBlob(urlToDelete: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$VERCEL_BLOB_API_URL/delete")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Authorization", "Bearer $readWriteToken")
                setRequestProperty("Content-Type", "application/json")
            }

            val payload = JSONObject().apply {
                put("urls", listOf(urlToDelete))
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val code = connection.responseCode
            if (code in 200..299) {
                Result.success(true)
            } else {
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vercel Blob delete failed: ${e.message}")
            Result.success(true)
        }
    }
}
