package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class OtpSent(val phoneNumber: String) : AuthUiState()
    object LoggedIn : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel @JvmOverloads constructor(
    private val authRepo: AuthRepository = AuthRepository.getInstance()
) : ViewModel() {

    val currentUser = authRepo.currentUser
    val isLoggedIn = authRepo.isLoggedIn
    val isProfileCompleted = authRepo.isProfileCompleted

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val res = authRepo.signInWithEmail(email, pass)
            if (res.isSuccess) {
                _uiState.value = AuthUiState.LoggedIn
            } else {
                _uiState.value = AuthUiState.Error(res.exceptionOrNull()?.message ?: "Authentication failed")
            }
        }
    }

    fun registerWithEmail(
        email: String,
        pass: String,
        confirmPass: String,
        name: String,
        username: String,
        phone: String
    ) {
        if (name.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your display name")
            return
        }
        if (username.length < 3) {
            _uiState.value = AuthUiState.Error("Username must be at least 3 characters")
            return
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters long")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val res = authRepo.registerWithEmail(
                email = email.trim(),
                pass = pass,
                name = name.trim(),
                username = username.trim().lowercase().replace(" ", ""),
                phone = phone.trim()
            )
            if (res.isSuccess) {
                _uiState.value = AuthUiState.LoggedIn
            } else {
                _uiState.value = AuthUiState.Error(res.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun loginWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepo.loginWithGoogle(email, name)
            _uiState.value = AuthUiState.LoggedIn
        }
    }

    fun sendOtp(phoneNumber: String) {
        if (phoneNumber.length < 8) {
            _uiState.value = AuthUiState.Error("Please enter a valid phone number")
            return
        }
        _uiState.value = AuthUiState.Loading
        _uiState.value = AuthUiState.OtpSent(phoneNumber)
    }

    fun verifyOtp(phoneNumber: String, otpCode: String) {
        if (otpCode.length < 4) {
            _uiState.value = AuthUiState.Error("Enter 6-digit OTP code")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            // Log in or load user profile for phone
            val cleanEmail = "user_" + phoneNumber.replace("+", "").replace("-", "").replace(" ", "") + "@lovelink.app"
            authRepo.loginWithGoogle(cleanEmail, "LoveLink Member")
            _uiState.value = AuthUiState.LoggedIn
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val res = authRepo.sendPasswordResetEmail(email)
            if (res.isSuccess) {
                _uiState.value = AuthUiState.Success("Password reset email sent to $email")
            } else {
                _uiState.value = AuthUiState.Error(res.exceptionOrNull()?.message ?: "Failed to send password reset email")
            }
        }
    }

    fun completeProfile(
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
    ) {
        if (displayName.isBlank()) {
            _uiState.value = AuthUiState.Error("Display Name is required")
            return
        }
        if (username.isBlank()) {
            _uiState.value = AuthUiState.Error("Username is required")
            return
        }
        if (age < 18) {
            _uiState.value = AuthUiState.Error("You must be 18+ years old to join LoveLink")
            return
        }
        if (photos.size < 2) {
            _uiState.value = AuthUiState.Error("Please upload or select at least 2 profile photos")
            return
        }
        if (interests.size < 2) {
            _uiState.value = AuthUiState.Error("Please pick at least 2 interests")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val res = authRepo.completeProfile(
                displayName = displayName.trim(),
                username = username.trim().lowercase().replace(" ", ""),
                gender = gender,
                dob = dob,
                age = age,
                bio = bio.trim(),
                photos = photos,
                interests = interests,
                country = country.trim(),
                state = state.trim(),
                city = city.trim(),
                lookingFor = lookingFor
            )
            if (res.isSuccess) {
                _uiState.value = AuthUiState.Success("Profile updated successfully")
            } else {
                _uiState.value = AuthUiState.Error(res.exceptionOrNull()?.message ?: "Profile setup failed")
            }
        }
    }

    fun logout() {
        authRepo.logout()
        _uiState.value = AuthUiState.Idle
    }
}

