package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.MatchItem
import com.example.data.model.SwipeType
import com.example.data.model.UserProfile
import com.example.data.repository.DiamondRepository
import com.example.data.repository.MatchRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel @JvmOverloads constructor(
    private val userRepo: UserRepository = UserRepository(),
    private val matchRepo: MatchRepository = MatchRepository(),
    private val diamondRepo: DiamondRepository = DiamondRepository()
) : ViewModel() {

    val feed = userRepo.userList

    private val _newMatchEvent = MutableStateFlow<MatchItem?>(null)
    val newMatchEvent: StateFlow<MatchItem?> = _newMatchEvent.asStateFlow()

    private val _showMatchDialog = MutableStateFlow(false)
    val showMatchDialog: StateFlow<Boolean> = _showMatchDialog.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    fun swipe(targetUser: UserProfile, type: SwipeType, currentUser: UserProfile) {
        if (type == SwipeType.SUPER_LIKE) {
            val success = diamondRepo.spendDiamonds(20, com.example.data.model.TransactionType.SUPER_LIKE_USED, "Super Liked ${targetUser.name}")
            if (!success) {
                _snackMessage.value = "Need 20 Diamonds for Super Like! 💎"
                return
            }
        }

        val isMatch = userRepo.recordSwipe(targetUser.id, type)
        if (isMatch) {
            val match = matchRepo.createMatch(currentUser, targetUser, isSuperLike = (type == SwipeType.SUPER_LIKE))
            _newMatchEvent.value = match
            _showMatchDialog.value = true
        }
    }

    fun undo(currentUser: UserProfile) {
        if (!currentUser.isPremium) {
            val success = diamondRepo.spendDiamonds(15, com.example.data.model.TransactionType.UNDO_USED, "Undo Swipe")
            if (!success) {
                _snackMessage.value = "Upgrade to Premium or spend 15 Diamonds to Undo! 💎"
                return
            }
        }
        val restored = userRepo.undoLastSwipe()
        if (restored != null) {
            _snackMessage.value = "Restored ${restored.name}'s profile ⏪"
        }
    }

    fun dismissMatchDialog() {
        _showMatchDialog.value = false
        _newMatchEvent.value = null
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }
}
