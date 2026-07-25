package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.MatchItem
import com.example.data.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MatchesViewModel @JvmOverloads constructor(
    private val matchRepo: MatchRepository = MatchRepository()
) : ViewModel() {

    val matches: StateFlow<List<MatchItem>> = matchRepo.matches

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredMatches(currentUserId: String): List<MatchItem> {
        val query = _searchQuery.value.trim()
        val all = matches.value
        if (query.isEmpty()) return all
        return all.filter { match ->
            val other = match.getOtherUser(currentUserId)
            other.name.contains(query, ignoreCase = true) ||
            match.lastMessage.contains(query, ignoreCase = true)
        }
    }
}
