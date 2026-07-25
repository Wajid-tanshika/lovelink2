package com.example.data.model

enum class NetworkQualityLevel(val label: String, val bars: Int, val colorHex: Long) {
    EXCELLENT("Excellent", 4, 0xFF4CAF50),
    GOOD("Good", 3, 0xFF8BC34A),
    FAIR("Fair", 2, 0xFFFFB300),
    POOR("Poor", 1, 0xFFFF9800),
    VERY_POOR("Very Poor", 0, 0xFFF44336)
}

data class NetworkQualityState(
    val level: NetworkQualityLevel = NetworkQualityLevel.EXCELLENT,
    val rttMs: Int = 24,
    val uplinkKbps: Int = 2450,
    val downlinkKbps: Int = 3120,
    val fps: Int = 30,
    val resolution: String = "1080p HD",
    val packetLossPercent: Float = 0.05f
)
