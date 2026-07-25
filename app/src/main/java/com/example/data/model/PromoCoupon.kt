package com.example.data.model

data class PromoCoupon(
    val code: String = "",
    val diamondBonus: Int = 100,
    val maxRedemptions: Int = 500,
    val currentRedemptions: Int = 0,
    val expiresAt: Long = System.currentTimeMillis() + 86400000L * 30, // 30 days
    val isActive: Boolean = true
)
