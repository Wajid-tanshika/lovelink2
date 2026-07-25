package com.example.data.source

import android.util.Log
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreAdminService {
    private const val TAG = "FirestoreAdminService"
    private const val COLLECTION_ADMINS = "admins"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_REPORTS = "reports"
    private const val COLLECTION_NOTIFICATIONS = "notifications"
    private const val COLLECTION_PACKAGES = "diamond_packages"
    private const val COLLECTION_PLANS = "premium_plans"
    private const val COLLECTION_SETTINGS = "app_settings"
    private const val COLLECTION_TRANSACTIONS = "payment_transactions"

    // Dynamic active admin cache for offline / runtime session verification
    private val activeAdminSessions = mutableMapOf<String, AdminUser>()

    /**
     * Checks whether the user email exists in the Firestore collection "admins"
     * with document ID = User Email and status == "active".
     *
     * Requirement:
     * Collection: admins
     * Document ID: User Email
     * Fields: email, role, status, createdAt
     */
    suspend fun verifyAdminStatus(email: String): Pair<Boolean, AdminUser?> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) return Pair(false, null)

        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection(COLLECTION_ADMINS).document(cleanEmail)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                val status = snapshot.getString("status") ?: ""
                val role = snapshot.getString("role") ?: "admin"
                val adminEmail = snapshot.getString("email") ?: cleanEmail
                val createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()

                val adminUser = AdminUser(email = adminEmail, role = role, status = status, createdAt = createdAt)
                val isActive = status.equals("active", ignoreCase = true)
                if (isActive) {
                    activeAdminSessions[cleanEmail] = adminUser
                }
                Log.d(TAG, "Firestore admin check for $cleanEmail: isActive=$isActive, role=$role")
                Pair(isActive, adminUser)
            } else {
                Log.d(TAG, "No Firestore admin document found for $cleanEmail")
                val cached = activeAdminSessions[cleanEmail]
                if (cached != null && cached.status.equals("active", ignoreCase = true)) {
                    Pair(true, cached)
                } else {
                    Pair(false, null)
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firestore admin lookup failed, checking local dynamic sessions: ${e.message}")
            val cached = activeAdminSessions[cleanEmail]
            if (cached != null && cached.status.equals("active", ignoreCase = true)) {
                Pair(true, cached)
            } else {
                Pair(false, null)
            }
        }
    }

    /**
     * Seed or register an admin document in Firestore
     */
    suspend fun createOrUpdateAdmin(admin: AdminUser): Boolean {
        val cleanEmail = admin.email.trim().lowercase()
        if (cleanEmail.isEmpty()) return false

        activeAdminSessions[cleanEmail] = admin

        return try {
            val firestore = FirebaseFirestore.getInstance()
            val data = mapOf(
                "email" to admin.email,
                "role" to admin.role,
                "status" to admin.status,
                "createdAt" to admin.createdAt
            )
            firestore.collection(COLLECTION_ADMINS).document(cleanEmail).set(data).await()
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Error saving admin to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Save app settings to Firestore
     */
    suspend fun saveAppSettings(settings: AppSettings): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val data = mapOf(
                "freeDailyLikesLimit" to settings.freeDailyLikesLimit,
                "superlikeCostDiamonds" to settings.superlikeCostDiamonds,
                "profileBoostCostDiamonds" to settings.profileBoostCostDiamonds,
                "isMaintenanceMode" to settings.isMaintenanceMode,
                "maintenanceMessage" to settings.maintenanceMessage,
                "enforceGoogleSignInOnly" to settings.enforceGoogleSignInOnly,
                "adminAlertsEnabled" to settings.adminAlertsEnabled
            )
            firestore.collection(COLLECTION_SETTINGS).document("global").set(data).await()
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Error saving settings to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Send broadcast or targeted FCM notification record in Firestore
     */
    suspend fun recordNotification(notification: AdminNotification): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = if (notification.id.isNotEmpty()) {
                firestore.collection(COLLECTION_NOTIFICATIONS).document(notification.id)
            } else {
                firestore.collection(COLLECTION_NOTIFICATIONS).document()
            }
            docRef.set(notification).await()
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Error recording notification: ${e.message}")
            false
        }
    }
}
