package com.example.data.model

enum class TransactionType {
    PURCHASE, REWARD, DAILY_BONUS, GIFT_SENT, GIFT_RECEIVED, BOOST_USED, SUPER_LIKE_USED, UNDO_USED
}

data class DiamondTransaction(
    val id: String = "",
    val userId: String = "",
    val type: TransactionType = TransactionType.DAILY_BONUS,
    val amount: Int = 0,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
