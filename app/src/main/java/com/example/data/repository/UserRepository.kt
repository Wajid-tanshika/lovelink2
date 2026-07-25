package com.example.data.repository

import com.example.data.model.UserProfile
import com.example.data.model.UserSwipeAction
import com.example.data.model.SwipeType
import com.example.data.source.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepository {

    private val _currentUser = MutableStateFlow<UserProfile>(SampleData.CURRENT_USER)
    val currentUserProfile: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _userList = MutableStateFlow<List<UserProfile>>(SampleData.PROFILES)
    val userList: StateFlow<List<UserProfile>> = _userList.asStateFlow()

    private val swipeHistory = mutableListOf<UserSwipeAction>()

    fun getRecommendedFeed(
        minAge: Int = 18,
        maxAge: Int = 50,
        gender: String = "All",
        maxDistanceKm: Double = 50.0,
        verifiedOnly: Boolean = false,
        country: String = "All",
        city: String = "All",
        language: String = "All",
        onlineOnly: Boolean = false,
        premiumOnly: Boolean = false
    ): List<UserProfile> {
        return _userList.value.filter { user ->
            !user.isBlocked &&
            !user.isHiddenFromSearch &&
            user.age in minAge..maxAge &&
            (gender == "All" || user.gender.equals(gender, ignoreCase = true)) &&
            user.distanceKm <= maxDistanceKm &&
            (!verifiedOnly || user.isVerified) &&
            (country == "All" || user.country.equals(country, ignoreCase = true)) &&
            (city == "All" || user.city.contains(city, ignoreCase = true)) &&
            (language == "All" || user.language.equals(language, ignoreCase = true)) &&
            (!onlineOnly || (user.isOnline && !user.hideOnlineStatus)) &&
            (!premiumOnly || user.isPremium)
        }
    }

    fun requestProfileVerification(userId: String) {
        _currentUser.value = _currentUser.value.copy(verificationStatus = "PENDING")
        _userList.value = _userList.value.map {
            if (it.id == userId) it.copy(verificationStatus = "PENDING") else it
        }
    }

    fun updatePrivacySettings(
        hideOnline: Boolean,
        hideLastSeen: Boolean,
        isPrivate: Boolean,
        hideFromSearch: Boolean
    ) {
        val updated = _currentUser.value.copy(
            hideOnlineStatus = hideOnline,
            hideLastSeen = hideLastSeen,
            isPrivateAccount = isPrivate,
            isHiddenFromSearch = hideFromSearch
        )
        _currentUser.value = updated
    }

    fun deleteAccount(userId: String) {
        _userList.value = _userList.value.filter { it.id != userId }
    }

    fun recordSwipe(targetUserId: String, swipeType: SwipeType): Boolean {
        swipeHistory.add(UserSwipeAction(targetUserId, swipeType))
        _userList.value = _userList.value.filter { it.id != targetUserId }
        return (swipeType == SwipeType.LIKE || swipeType == SwipeType.SUPER_LIKE) && targetUserId in listOf("user_1", "user_3", "user_5")
    }

    fun canUndo(): Boolean = swipeHistory.isNotEmpty()

    fun undoLastSwipe(): UserProfile? {
        if (swipeHistory.isEmpty()) return null
        val lastAction = swipeHistory.removeAt(swipeHistory.size - 1)
        val restoredUser = SampleData.PROFILES.firstOrNull { it.id == lastAction.targetUserId }
        if (restoredUser != null) {
            _userList.value = listOf(restoredUser) + _userList.value
        }
        return restoredUser
    }

    fun toggleBlockUser(userId: String) {
        _userList.value = _userList.value.map {
            if (it.id == userId) it.copy(isBlocked = !it.isBlocked) else it
        }
    }

    fun warnUser(userId: String) {
        _userList.value = _userList.value.map {
            if (it.id == userId) it.copy(warningCount = it.warningCount + 1) else it
        }
    }

    fun muteUser(userId: String) {
        _userList.value = _userList.value.map {
            if (it.id == userId) it.copy(isMuted = !it.isMuted) else it
        }
    }

    fun suspendUser(userId: String, durationLabel: String) {
        _userList.value = _userList.value.map {
            if (it.id == userId) it.copy(isBlocked = true, suspensionUntil = durationLabel) else it
        }
    }

    fun restoreUser(userId: String) {
        _userList.value = _userList.value.map {
            if (it.id == userId) it.copy(isBlocked = false, isMuted = false, suspensionUntil = "") else it
        }
    }

    fun toggleVerifyUser(userId: String) {
        _userList.value = _userList.value.map {
            if (it.id == userId) it.copy(isVerified = !it.isVerified) else it
        }
    }

    fun deleteUser(userId: String) {
        _userList.value = _userList.value.filter { it.id != userId }
    }

    fun toggleFollowUser(targetUserId: String): Boolean {
        val current = _currentUser.value
        val isFollowing = current.following.contains(targetUserId)
        val newFollowing = if (isFollowing) current.following - targetUserId else current.following + targetUserId
        _currentUser.value = current.copy(following = newFollowing)

        _userList.value = _userList.value.map { user ->
            if (user.id == targetUserId) {
                val newFollowers = if (isFollowing) user.followers - current.id else user.followers + current.id
                user.copy(followers = newFollowers)
            } else user
        }
        return !isFollowing
    }

    fun searchUsersAndCreators(query: String): List<UserProfile> {
        if (query.isBlank()) return emptyList()
        return _userList.value.filter { user ->
            !user.isBlocked &&
            (user.name.contains(query, ignoreCase = true) ||
             user.username.contains(query, ignoreCase = true) ||
             user.profession.contains(query, ignoreCase = true) ||
             user.interests.any { it.contains(query, ignoreCase = true) })
        }
    }

    fun adjustUserDiamonds(userId: String, delta: Int) {
        _userList.value = _userList.value.map {
            if (it.id == userId) {
                val newBal = (it.diamondBalance + delta).coerceAtLeast(0)
                it.copy(diamondBalance = newBal)
            } else it
        }
    }
}
