package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.example.data.model.CallingProviderConfig
import com.example.data.model.FirebaseAppConfig
import com.example.data.model.StorageProviderConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Secure Encrypted Local Storage Manager.
 * Uses AES encryption for local persistence of sensitive API keys, tokens, and configuration.
 */
object EncryptedStorageManager {
    private const val TAG = "EncryptedStorageManager"
    private const val PREF_NAME = "lovelink_secure_prefs"
    private const val AES_KEY_ALIAS = "LoveLinkSecureKey2026AES" // 16 bytes for AES-128
    
    private const val KEY_FIREBASE_CONFIG = "enc_firebase_config"
    private const val KEY_CALLING_CONFIG = "enc_calling_config"
    private const val KEY_STORAGE_CONFIG = "enc_storage_config"
    private const val KEY_AUTH_TOKEN = "enc_auth_token"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val aesKeySpec: Key = SecretKeySpec("LoveLinkAppKey26".toByteArray(Charsets.UTF_8), "AES")

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun encrypt(data: String): String {
        return try {
            if (data.isBlank()) return ""
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption error", e)
            data
        }
    }

    private fun decrypt(encryptedData: String): String {
        return try {
            if (encryptedData.isBlank()) return ""
            val decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, aesKeySpec)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption error", e)
            encryptedData
        }
    }

    fun saveFirebaseConfig(context: Context, config: FirebaseAppConfig) {
        try {
            val json = moshi.adapter(FirebaseAppConfig::class.java).toJson(config)
            val encrypted = encrypt(json)
            getPrefs(context).edit().putString(KEY_FIREBASE_CONFIG, encrypted).apply()
            Log.d(TAG, "Firebase config encrypted and saved successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save encrypted Firebase config", e)
        }
    }

    fun getFirebaseConfig(context: Context): FirebaseAppConfig? {
        return try {
            val encrypted = getPrefs(context).getString(KEY_FIREBASE_CONFIG, "") ?: ""
            if (encrypted.isBlank()) return null
            val decrypted = decrypt(encrypted)
            moshi.adapter(FirebaseAppConfig::class.java).fromJson(decrypted)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read encrypted Firebase config", e)
            null
        }
    }

    fun saveCallingConfig(context: Context, config: CallingProviderConfig) {
        try {
            val json = moshi.adapter(CallingProviderConfig::class.java).toJson(config)
            val encrypted = encrypt(json)
            getPrefs(context).edit().putString(KEY_CALLING_CONFIG, encrypted).apply()
            Log.d(TAG, "Calling config encrypted and saved successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save encrypted Calling config", e)
        }
    }

    fun getCallingConfig(context: Context): CallingProviderConfig? {
        return try {
            val encrypted = getPrefs(context).getString(KEY_CALLING_CONFIG, "") ?: ""
            if (encrypted.isBlank()) return null
            val decrypted = decrypt(encrypted)
            moshi.adapter(CallingProviderConfig::class.java).fromJson(decrypted)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read encrypted Calling config", e)
            null
        }
    }

    fun saveStorageConfig(context: Context, config: StorageProviderConfig) {
        try {
            val json = moshi.adapter(StorageProviderConfig::class.java).toJson(config)
            val encrypted = encrypt(json)
            getPrefs(context).edit().putString(KEY_STORAGE_CONFIG, encrypted).apply()
            Log.d(TAG, "Storage config encrypted and saved successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save encrypted Storage config", e)
        }
    }

    fun getStorageConfig(context: Context): StorageProviderConfig? {
        return try {
            val encrypted = getPrefs(context).getString(KEY_STORAGE_CONFIG, "") ?: ""
            if (encrypted.isBlank()) return null
            val decrypted = decrypt(encrypted)
            moshi.adapter(StorageProviderConfig::class.java).fromJson(decrypted)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read encrypted Storage config", e)
            null
        }
    }
}
