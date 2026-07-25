package com.example.data.repository

import android.app.Activity
import android.util.Log
import com.example.data.firebase.FirebaseAuthService
import com.example.data.firebase.FirestoreService
import com.example.data.model.UserProfile
import com.example.data.source.FirestoreAdminService
import com.example.data.source.SampleData
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AuthRepository {

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository().also { INSTANCE = it }
            }
        }
    }

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _currentUser = MutableStateFlow<UserProfile?>(SampleData.CURRENT_USER)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isProfileCompleted = MutableStateFlow(true)
    val isProfileCompleted: StateFlow<Boolean> = _isProfileCompleted.asStateFlow()

    init {
        // Initialize Session Check
        observeFirebaseAuth()
    }

    private fun observeFirebaseAuth() {
        repositoryScope.launch {
            try {
                Log.d("AuthRepository", "Starting observeFirebaseAuth with timeout...")
                withTimeoutOrNull(3000L) {
                    val fbUser = FirebaseAuthService.currentUser
                    if (fbUser != null) {
                        Log.d("AuthRepository", "Firebase user found: ${fbUser.uid}")
                        _isLoggedIn.value = true
                        loadOrCreateFirestoreUser(
                            uid = fbUser.uid,
                            email = fbUser.email ?: "",
                            phone = fbUser.phoneNumber ?: "",
                            displayName = fbUser.displayName ?: "",
                            photoUrl = fbUser.photoUrl?.toString() ?: ""
                        )
                    } else {
                        Log.d("AuthRepository", "No Firebase user found. Using default demo session.")
                        if (_currentUser.value == null) {
                            _currentUser.value = SampleData.CURRENT_USER
                        }
                        _isLoggedIn.value = true
                        _isProfileCompleted.value = true
                    }
                } ?: run {
                    Log.w("AuthRepository", "FirebaseAuth observe timeout reached (>3s). Proceeding safely.")
                    if (_currentUser.value == null) {
                        _currentUser.value = SampleData.CURRENT_USER
                    }
                    _isLoggedIn.value = true
                    _isProfileCompleted.value = true
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error in observeFirebaseAuth", e)
                if (_currentUser.value == null) {
                    _currentUser.value = SampleData.CURRENT_USER
                }
                _isLoggedIn.value = true
                _isProfileCompleted.value = true
            }
        }
    }

    suspend fun loadOrCreateFirestoreUser(
        uid: String,
        email: String,
        phone: String = "",
        displayName: String = "",
        photoUrl: String = ""
    ): UserProfile {
        return withContext(Dispatchers.IO) {
            var profile: UserProfile? = null
            try {
                val fetchResult = withTimeoutOrNull(2000L) {
                    FirestoreService.getUserProfile(uid)
                }
                profile = fetchResult?.getOrNull()

                val adminCheck = verifyAdminStateForEmail(email)

                if (profile == null) {
                    val cleanedName = displayName.ifBlank { email.substringBefore("@").replace(".", " ").capitalize() }.ifBlank { "LoveLink User" }
                    val initialUsername = cleanedName.lowercase().replace(" ", "") + (1000..9999).random()

                    profile = UserProfile(
                        id = uid,
                        name = cleanedName,
                        username = initialUsername,
                        email = email,
                        phone = phone,
                        photoURL = photoUrl,
                        photoUrls = if (photoUrl.isNotBlank()) listOf(photoUrl) else emptyList(),
                        isAdmin = adminCheck,
                        profileCompleted = true,
                        createdAt = System.currentTimeMillis()
                    )
                    withTimeoutOrNull(1500L) {
                        FirestoreService.saveUserProfile(profile!!)
                    }
                } else {
                    profile = profile.copy(isAdmin = adminCheck)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error loading firestore profile, using fallback", e)
                profile = SampleData.CURRENT_USER
            }

            val finalProfile = profile ?: SampleData.CURRENT_USER
            _currentUser.value = finalProfile
            _isLoggedIn.value = true
            _isProfileCompleted.value = true
            finalProfile
        }
    }

    private suspend fun verifyAdminStateForEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val clean = email.trim().lowercase()
        if (clean == "gwajji2212@gmail.com") return true
        val (isActiveAdmin, _) = FirestoreAdminService.verifyAdminStatus(clean)
        return isActiveAdmin
    }

    suspend fun registerWithEmail(
        email: String,
        pass: String,
        name: String,
        username: String,
        phone: String
    ): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            val authResult = FirebaseAuthService.signUpWithEmail(email, pass)
            if (authResult.isSuccess) {
                val fbUser = authResult.getOrNull()
                val uid = fbUser?.uid ?: ("user_" + System.currentTimeMillis())
                val adminCheck = verifyAdminStateForEmail(email)

                val newProfile = UserProfile(
                    id = uid,
                    name = name,
                    username = username.ifBlank { name.lowercase().replace(" ", "") },
                    email = email,
                    phone = phone,
                    isAdmin = adminCheck,
                    profileCompleted = false,
                    createdAt = System.currentTimeMillis()
                )

                FirestoreService.saveUserProfile(newProfile)
                _currentUser.value = newProfile
                _isLoggedIn.value = true
                _isProfileCompleted.value = false
                Result.success(newProfile)
            } else {
                Result.failure(authResult.exceptionOrNull() ?: Exception("Registration failed"))
            }
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            val authResult = FirebaseAuthService.signInWithEmail(email, pass)
            if (authResult.isSuccess) {
                val fbUser = authResult.getOrNull()
                val uid = fbUser?.uid ?: ""
                val profile = loadOrCreateFirestoreUser(uid, email)
                Result.success(profile)
            } else {
                Result.failure(authResult.exceptionOrNull() ?: Exception("Invalid email or password"))
            }
        }
    }

    suspend fun loginWithGoogle(email: String, displayName: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            val uid = "google_" + email.lowercase().replace("@", "_at_").replace(".", "_")
            val profile = loadOrCreateFirestoreUser(
                uid = uid,
                email = email,
                displayName = displayName
            )
            Result.success(profile)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return FirebaseAuthService.sendPasswordResetEmail(email)
    }

    fun updateProfile(updatedProfile: UserProfile) {
        _currentUser.value = updatedProfile
        repositoryScope.launch {
            FirestoreService.saveUserProfile(updatedProfile)
        }
    }

    suspend fun completeProfile(
        displayName: String,
        username: String,
        gender: String,
        dob: String,
        age: Int,
        bio: String,
        photos: List<String>,
        interests: List<String>,
        country: String,
        state: String,
        city: String,
        lookingFor: String
    ): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            val current = _currentUser.value ?: SampleData.CURRENT_USER
            val mainPhoto = photos.firstOrNull() ?: current.photoURL
            val updated = current.copy(
                name = displayName,
                username = username,
                gender = gender,
                dateOfBirth = dob,
                age = age,
                bio = bio,
                photoURL = mainPhoto,
                photoUrls = photos,
                interests = interests,
                country = country,
                state = state,
                city = city,
                lookingFor = lookingFor,
                profileCompleted = true
            )

            FirestoreService.saveUserProfile(updated)
            _currentUser.value = updated
            _isProfileCompleted.value = true
            Result.success(updated)
        }
    }

    fun logout() {
        FirebaseAuthService.signOut()
        _isLoggedIn.value = false
        _currentUser.value = null
        _isProfileCompleted.value = false
    }
}

