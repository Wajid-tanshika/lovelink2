package com.example.data.model

enum class ReportStatus {
    PENDING, RESOLVED, DISMISSED
}

data class ReportItem(
    val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reportedUserId: String = "",
    val reportedUserName: String = "",
    val reason: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.PENDING
)
