package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExploreViewModel @JvmOverloads constructor(
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private val _selectedTab = MutableStateFlow("Recommended") // "Recommended", "Nearby", "Verified"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _minAge = MutableStateFlow(18)
    val minAge: StateFlow<Int> = _minAge.asStateFlow()

    private val _maxAge = MutableStateFlow(40)
    val maxAge: StateFlow<Int> = _maxAge.asStateFlow()

    private val _maxDistance = MutableStateFlow(30.0)
    val maxDistance: StateFlow<Double> = _maxDistance.asStateFlow()

    private val _genderFilter = MutableStateFlow("All")
    val genderFilter: StateFlow<String> = _genderFilter.asStateFlow()

    private val _verifiedOnly = MutableStateFlow(false)
    val verifiedOnly: StateFlow<Boolean> = _verifiedOnly.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredUsers: StateFlow<List<UserProfile>> = userRepo.userList

    fun setTab(tab: String) {
        _selectedTab.value = tab
    }

    fun updateFilters(minAge: Int, maxAge: Int, distance: Double, gender: String, verified: Boolean) {
        _minAge.value = minAge
        _maxAge.value = maxAge
        _maxDistance.value = distance
        _genderFilter.value = gender
        _verifiedOnly.value = verified
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredList(): List<UserProfile> {
        return userRepo.getRecommendedFeed(
            minAge = _minAge.value,
            maxAge = _maxAge.value,
            gender = _genderFilter.value,
            maxDistanceKm = _maxDistance.value,
            verifiedOnly = _verifiedOnly.value || _selectedTab.value == "Verified"
        ).filter { user ->
            _searchQuery.value.isEmpty() ||
            user.name.contains(_searchQuery.value, ignoreCase = true) ||
            user.city.contains(_searchQuery.value, ignoreCase = true) ||
            user.interests.any { it.contains(_searchQuery.value, ignoreCase = true) }
        }
    }
}
