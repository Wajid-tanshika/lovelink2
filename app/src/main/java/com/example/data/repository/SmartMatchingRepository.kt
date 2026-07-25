package com.example.data.repository

import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.min

data class FakeProfileAnalysis(
    val userId: String,
    val userName: String,
    val riskLevel: RiskLevel,
    val riskScore: Int, // 0 to 100
    val reasons: List<String>
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

data class AnalyticsData(
    val dailyActiveUsers: Int = 1420,
    val newUsersToday: Int = 89,
    val totalMatches: Int = 3450,
    val totalChatsSent: Int = 18200,
    val totalProfileViews: Int = 45200,
    val totalCallMinutes: Int = 1280,
    val totalStoryViews: Int = 8900,
    val totalVideoViews: Int = 24500,
    val premiumSalesRevenueUSD: Double = 3450.00,
    val diamondSalesRevenueUSD: Double = 1890.50
)

class SmartMatchingRepository {

    private val _analytics = MutableStateFlow(AnalyticsData())
    val analytics: StateFlow<AnalyticsData> = _analytics.asStateFlow()

    /**
     * Calculates compatibility score (0 - 100%) between two user profiles.
     */
    fun calculateCompatibilityScore(userA: UserProfile, userB: UserProfile): Int {
        var score = 30 // Base baseline score

        // 1. Shared Interests (+25 Max)
        val commonInterests = userA.interests.intersect(userB.interests.toSet())
        score += min(commonInterests.size * 8, 25)

        // 2. Distance Proximity (+20 Max)
        val dist = userB.distanceKm
        when {
            dist <= 5.0 -> score += 20
            dist <= 15.0 -> score += 15
            dist <= 30.0 -> score += 10
            dist <= 50.0 -> score += 5
        }

        // 3. Age Difference (+15 Max)
        val ageDiff = abs(userA.age - userB.age)
        when {
            ageDiff <= 3 -> score += 15
            ageDiff <= 6 -> score += 10
            ageDiff <= 10 -> score += 5
        }

        // 4. Looking For Goal Match (+10 Max)
        if (userA.lookingFor.isNotBlank() && userA.lookingFor.equals(userB.lookingFor, ignoreCase = true)) {
            score += 10
        }

        // 5. Language Match (+10 Max)
        if (userA.language.equals(userB.language, ignoreCase = true)) {
            score += 10
        }

        // 6. Quality Boost: Verified & Premium (+10 Max)
        if (userB.isVerified) score += 5
        if (userB.isPremium) score += 5

        return score.coerceIn(15, 99)
    }

    /**
     * Ranks candidates using AI Smart Recommendation Algorithm.
     */
    fun getSmartRecommendations(currentUser: UserProfile, candidates: List<UserProfile>): List<UserProfile> {
        return candidates
            .filter { candidate ->
                candidate.id != currentUser.id &&
                !candidate.isBlocked &&
                !currentUser.blockedUserIds.contains(candidate.id) &&
                !candidate.isHiddenFromSearch
            }
            .sortedByDescending { candidate ->
                var rank = calculateCompatibilityScore(currentUser, candidate)
                if (candidate.isPremium) rank += 15 // Priority ranking for premium
                if (candidate.isVerified) rank += 10
                if (candidate.isOnline && !candidate.hideOnlineStatus) rank += 5
                rank
            }
    }

    /**
     * Detects suspicious or fake profile patterns.
     */
    fun analyzeFakeProfileRisk(user: UserProfile): FakeProfileAnalysis {
        val reasons = mutableListOf<String>()
        var score = 0

        // Checks
        if (user.photoUrls.isEmpty() && user.photoURL.isBlank()) {
            score += 35
            reasons.add("No profile photos uploaded")
        }

        if (user.bio.length < 5) {
            score += 15
            reasons.add("Incomplete or empty bio")
        }

        val spamKeywords = listOf("whatsapp", "telegram", "crypto", "cash", "pay", "http", "www.", "+1", "+91")
        if (spamKeywords.any { user.bio.contains(it, ignoreCase = true) }) {
            score += 40
            reasons.add("Contains external contact/spam keywords in bio")
        }

        if (!user.isVerified && user.warningCount > 1) {
            score += 20
            reasons.add("Multiple community warnings reported")
        }

        val riskLevel = when {
            score >= 60 -> RiskLevel.HIGH
            score >= 30 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return FakeProfileAnalysis(
            userId = user.id,
            userName = user.name,
            riskLevel = riskLevel,
            riskScore = score,
            reasons = if (reasons.isEmpty()) listOf("No suspicious signals detected") else reasons
        )
    }

    /**
     * Analytics event logging
     */
    fun trackEvent(eventType: String) {
        val curr = _analytics.value
        _analytics.value = when (eventType) {
            "LIKE" -> curr.copy(totalMatches = curr.totalMatches + 1)
            "CHAT_SENT" -> curr.copy(totalChatsSent = curr.totalChatsSent + 1)
            "PROFILE_VIEW" -> curr.copy(totalProfileViews = curr.totalProfileViews + 1)
            "STORY_VIEW" -> curr.copy(totalStoryViews = curr.totalStoryViews + 1)
            "VIDEO_VIEW" -> curr.copy(totalVideoViews = curr.totalVideoViews + 1)
            else -> curr
        }
    }
}
