package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel @JvmOverloads constructor(
    private val authRepo: AuthRepository = AuthRepository.getInstance(),
    private val notificationRepo: NotificationRepository = NotificationRepository()
) : ViewModel() {

    val currentUser = authRepo.currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    fun updateProfileImage(photoUrl: String) {
        val current = currentUser.value ?: return
        val updatedPhotos = if (current.photoUrls.contains(photoUrl)) {
            listOf(photoUrl) + (current.photoUrls - photoUrl)
        } else {
            listOf(photoUrl) + current.photoUrls
        }
        authRepo.updateProfile(current.copy(photoURL = photoUrl, photoUrls = updatedPhotos))
        _snackMessage.value = "Profile picture updated successfully! 📸"
    }

    fun updateProfile(profile: UserProfile) {
        authRepo.updateProfile(profile)
        _snackMessage.value = "Profile updated successfully ✨"
    }

    fun requestVerification() {
        val current = currentUser.value ?: return
        val updated = current.copy(isVerified = true)
        authRepo.updateProfile(updated)
        notificationRepo.addNotification(
            title = "Profile Verified Badge Granted! Blue Checkmark Active",
            body = "Your profile photos were reviewed and verified.",
            type = com.example.data.model.NotificationType.SYSTEM
        )
        _snackMessage.value = "Profile verified with blue badge! Check your profile."
    }

    fun addPhoto(url: String) {
        val current = currentUser.value ?: return
        val photos = current.photoUrls + url
        authRepo.updateProfile(current.copy(photoUrls = photos))
        _snackMessage.value = "Photo added to profile 📸"
    }

    fun removePhoto(url: String) {
        val current = currentUser.value ?: return
        val photos = current.photoUrls.filter { it != url }
        authRepo.updateProfile(current.copy(photoUrls = photos))
        _snackMessage.value = "Photo removed"
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }
}
