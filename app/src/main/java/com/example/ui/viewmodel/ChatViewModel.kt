package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.ChatMessage
import com.example.data.model.MatchItem
import com.example.data.model.UserProfile
import com.example.data.repository.AdminRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.DiamondRepository
import com.example.data.repository.MatchRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel @JvmOverloads constructor(
    private val chatRepo: ChatRepository = ChatRepository(),
    private val matchRepo: MatchRepository = MatchRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val adminRepo: AdminRepository = AdminRepository(),
    private val diamondRepo: DiamondRepository = DiamondRepository()
) : ViewModel() {

    private val _currentMatch = MutableStateFlow<MatchItem?>(null)
    val currentMatch: StateFlow<MatchItem?> = _currentMatch.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    fun loadChat(match: MatchItem) {
        _currentMatch.value = match
        _messages.value = chatRepo.getMessagesForMatch(match.id)
    }

    fun sendMessage(senderId: String, text: String, imageUrl: String? = null, audioUrl: String? = null) {
        val match = _currentMatch.value ?: return
        if (text.isBlank() && imageUrl == null && audioUrl == null) return

        val receiverId = match.users.firstOrNull { it != senderId } ?: ""
        val newMsg = chatRepo.sendMessage(
            matchId = match.id,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            imageUrl = imageUrl,
            audioUrl = audioUrl
        )

        matchRepo.updateLastMessage(match.id, text.ifEmpty { if (imageUrl != null) "📷 Sent a photo" else "🎙️ Sent a voice note" })
        _messages.value = chatRepo.getMessagesForMatch(match.id)
    }

    fun deleteChat(matchId: String) {
        chatRepo.deleteChat(matchId)
        _snackMessage.value = "Chat deleted"
    }

    fun blockUser(otherUser: UserProfile) {
        userRepo.toggleBlockUser(otherUser.id)
        _snackMessage.value = "${otherUser.name} has been blocked"
    }

    fun reportUser(reporter: UserProfile, reported: UserProfile, reason: String, details: String) {
        adminRepo.reportUser(
            reporterId = reporter.id,
            reporterName = reporter.name,
            reportedUserId = reported.id,
            reportedUserName = reported.name,
            reason = reason,
            details = details
        )
        _snackMessage.value = "Report submitted. Our moderation team will review this shortly."
    }

    fun sendVirtualGift(senderId: String, giftName: String, giftEmoji: String, diamondCost: Int) {
        val match = _currentMatch.value ?: return
        val success = diamondRepo.spendDiamonds(diamondCost, com.example.data.model.TransactionType.GIFT_SENT, "Sent $giftName $giftEmoji")
        if (success) {
            sendMessage(senderId, "Sent a virtual gift: $giftName $giftEmoji 🎁")
            _snackMessage.value = "Gift $giftEmoji sent!"
        } else {
            _snackMessage.value = "Insufficient diamonds to send $giftName!"
        }
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }
}
