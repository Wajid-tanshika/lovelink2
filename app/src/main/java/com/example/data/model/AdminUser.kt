package com.example.data.model

data class AdminUser(
    val email: String = "",
    val role: String = "admin", // superadmin, admin, moderator
    val status: String = "active", // active, inactive, suspended
    val createdAt: Long = System.currentTimeMillis()
)
