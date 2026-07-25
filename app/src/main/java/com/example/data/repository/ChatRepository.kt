package com.example.data.repository

import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository {

    private val sampleMessages = mutableMapOf(
        "match_1" to mutableListOf(
            ChatMessage(
                id = "msg_1",
                matchId = "match_1",
                senderId = "user_1",
                receiverId = "user_me",
                text = "Hey Alex! Loved your photography photos 📸",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 15,
                isRead = true
            )
        ),
        "match_3" to mutableListOf(
            ChatMessage(
                id = "msg_2",
                matchId = "match_3",
                senderId = "user_5",
                receiverId = "user_me",
                text = "Are you going to the gallery opening this Saturday?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                isRead = true
            )
        )
    )

    private val _messagesFlow = MutableStateFlow<Map<String, List<ChatMessage>>>(sampleMessages)
    val messagesFlow: StateFlow<Map<String, List<ChatMessage>>> = _messagesFlow.asStateFlow()

    private val _typingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val typingStatus: StateFlow<Map<String, Boolean>> = _typingStatus.asStateFlow()

    fun getMessagesForMatch(matchId: String): List<ChatMessage> {
        return _messagesFlow.value[matchId] ?: emptyList()
    }

    fun sendMessage(
        matchId: String,
        senderId: String,
        receiverId: String,
        text: String,
        imageUrl: String? = null,
        audioUrl: String? = null,
        replyToId: String? = null,
        replyToText: String? = null
    ): ChatMessage {
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            matchId = matchId,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            imageUrl = imageUrl,
            audioUrl = audioUrl,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isDelivered = true,
            isSeen = false,
            replyToId = replyToId,
            replyToText = replyToText
        )

        val currentMap = _messagesFlow.value.toMutableMap()
        val list = currentMap[matchId]?.toMutableList() ?: mutableListOf()
        list.add(newMsg)
        currentMap[matchId] = list
        _messagesFlow.value = currentMap

        return newMsg
    }

    fun markMessagesAsSeen(matchId: String, currentUserId: String) {
        val currentMap = _messagesFlow.value.toMutableMap()
        val list = currentMap[matchId]?.map { msg ->
            if (msg.receiverId == currentUserId && !msg.isSeen) {
                msg.copy(isRead = true, isSeen = true)
            } else msg
        } ?: return
        currentMap[matchId] = list.toMutableList()
        _messagesFlow.value = currentMap
    }

    fun setTypingStatus(matchId: String, userId: String, isTyping: Boolean) {
        val key = "${matchId}_$userId"
        val current = _typingStatus.value.toMutableMap()
        current[key] = isTyping
        _typingStatus.value = current
    }

    fun deleteMessageForMe(matchId: String, messageId: String, userId: String) {
        val currentMap = _messagesFlow.value.toMutableMap()
        val list = currentMap[matchId]?.map { msg ->
            if (msg.id == messageId) {
                val updatedDeleted = (msg.deletedForUserIds + userId).distinct()
                msg.copy(deletedForUserIds = updatedDeleted)
            } else msg
        } ?: return
        currentMap[matchId] = list.toMutableList()
        _messagesFlow.value = currentMap
    }

    fun deleteMessageForEveryone(matchId: String, messageId: String) {
        val currentMap = _messagesFlow.value.toMutableMap()
        val list = currentMap[matchId]?.map { msg ->
            if (msg.id == messageId) {
                msg.copy(text = "This message was deleted", isDeletedForEveryone = true, imageUrl = null, audioUrl = null)
            } else msg
        } ?: return
        currentMap[matchId] = list.toMutableList()
        _messagesFlow.value = currentMap
    }

    fun searchMessages(matchId: String, query: String): List<ChatMessage> {
        if (query.isBlank()) return emptyList()
        return getMessagesForMatch(matchId).filter {
            !it.isDeletedForEveryone && it.text.contains(query, ignoreCase = true)
        }
    }

    fun deleteChat(matchId: String) {
        val currentMap = _messagesFlow.value.toMutableMap()
        currentMap.remove(matchId)
        _messagesFlow.value = currentMap
    }
}
