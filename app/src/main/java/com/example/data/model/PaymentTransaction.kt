package com.example.data.model

data class PaymentTransaction(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val itemName: String = "",
    val amountPaid: String = "",
    val paymentMethod: String = "Google Play Billing",
    val status: String = "SUCCESS", // SUCCESS, PENDING, REFUNDED
    val timestamp: Long = System.currentTimeMillis()
)
