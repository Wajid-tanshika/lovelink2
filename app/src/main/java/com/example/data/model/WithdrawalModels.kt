package com.example.data.model

enum class WithdrawalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PAID
}

data class WithdrawalRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val coinsAmount: Int = 500, // 500 Coins = ₹50
    val inrAmount: Double = 50.0, // 10 Coins = ₹1
    val payoutMethod: String = "UPI", // "UPI", "Paytm", "Bank Transfer"
    val payoutDetails: String = "", // UPI ID or Paytm No or A/C No + IFSC
    val accountHolderName: String = "",
    val status: WithdrawalStatus = WithdrawalStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val processedTimestamp: Long? = null,
    val rejectionReason: String? = null,
    val transactionRef: String? = null
)

data class EarnTask(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val coinsReward: Int = 10,
    val iconName: String = "Star",
    val isCompleted: Boolean = false,
    val category: String = "DAILY" // ONBOARDING, DAILY, SOCIAL, PROMO
)
