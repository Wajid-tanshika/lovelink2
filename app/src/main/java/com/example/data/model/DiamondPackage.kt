package com.example.data.model

data class DiamondPackage(
    val id: String = "",
    val amount: Int = 100,
    val priceLabel: String = "$4.99",
    val priceCents: Int = 499,
    val tag: String? = null, // e.g. "Most Popular", "Best Value", "Hot"
    val isActive: Boolean = true
)
