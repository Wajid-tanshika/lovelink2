package com.example.data.model

data class PremiumPlan(
    val id: String = "",
    val title: String = "",
    val priceLabel: String = "",
    val planType: String = "MONTHLY", // WEEKLY, MONTHLY, YEARLY
    val durationDays: Int = 30,
    val perks: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val isActive: Boolean = true
)
