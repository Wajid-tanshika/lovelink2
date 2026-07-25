package com.example.data.model

enum class RefundStatus { PENDING, APPROVED, REJECTED }

data class RefundRequest(
    val id: String = "",
    val transactionId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val itemName: String = "",
    val amountPaid: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: RefundStatus = RefundStatus.PENDING,
    val adminNotes: String? = null
)
