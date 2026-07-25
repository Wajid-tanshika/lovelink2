package com.example.data.firebase

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Production-ready Firebase Authentication Service.
 * Supports Email/Password, Google Sign-In Credential, Phone Number OTP, and Auth State Streams.
 * Uses standard Firebase SDK without hardcoded configuration strings.
 */
object FirebaseAuthService {
    private const val TAG = "FirebaseAuthService"

    /**
     * Safely retrieves the FirebaseAuth instance if Firebase is initialized.
     */
    val authInstance: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseAuth instance not available: ${e.message}")
            null
        }

    /**
     * Current logged-in Firebase User or null.
     */
    val currentUser: FirebaseUser?
        get() = authInstance?.currentUser

    /**
     * Reactive StateFlow/Flow stream for Firebase Auth state changes.
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val auth = authInstance
        if (auth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Sign in with Email and Password
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        val auth = authInstance ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        return try {
            val result: AuthResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            Log.d(TAG, "signInWithEmail successful: ${result.user?.uid}")
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Create User account with Email and Password
     */
    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        val auth = authInstance ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        return try {
            val result: AuthResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            Log.d(TAG, "signUpWithEmail successful: ${result.user?.uid}")
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithEmail error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sign in with Google IdToken
     */
    suspend fun signInWithGoogleToken(idToken: String): Result<FirebaseUser?> {
        val auth = authInstance ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Log.d(TAG, "signInWithGoogleToken successful: ${result.user?.uid}")
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithGoogleToken error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Trigger Phone Number OTP verification
     */
    fun sendPhoneOtp(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val auth = authInstance ?: run {
            Log.w(TAG, "Firebase Auth unavailable for sendPhoneOtp")
            return
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Sign in with Phone Auth Credential
     */
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser?> {
        val auth = authInstance ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        return try {
            val result = auth.signInWithCredential(credential).await()
            Log.d(TAG, "signInWithPhoneCredential successful: ${result.user?.uid}")
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithPhoneCredential error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Send Password Reset Email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        val auth = authInstance ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Log.d(TAG, "Password reset email sent to $email")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordResetEmail error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sign Out
     */
    fun signOut() {
        try {
            authInstance?.signOut()
            Log.d(TAG, "User signed out successfully")
        } catch (e: Throwable) {
            Log.e(TAG, "Error signing out: ${e.message}")
        }
    }
}
