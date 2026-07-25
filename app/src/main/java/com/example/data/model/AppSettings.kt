package com.example.data.model

data class AppSettings(
    val freeDailyLikesLimit: Int = 25,
    val superlikeCostDiamonds: Int = 10,
    val profileBoostCostDiamonds: Int = 50,
    val boostDurationMinutes: Int = 30,
    val dailyLoginRewards: List<Int> = listOf(10, 20, 30, 50, 75, 100, 150),
    val referralBonusInviter: Int = 50,
    val referralBonusInvited: Int = 25,
    val undoCostDiamonds: Int = 5,
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "LoveLink is currently undergoing scheduled maintenance. Please check back shortly!",
    val enforceGoogleSignInOnly: Boolean = false,
    val adminAlertsEnabled: Boolean = true,
    val agoraAppId: String = "",
    val agoraToken: String = "",
    val agoraChannelName: String = "lovelink_call_channel",

    // Google Play Billing Configuration (Editable in Admin Panel)
    val premiumMonthlyProductId: String = "lovelink_premium_monthly",
    val premiumYearlyProductId: String = "lovelink_premium_yearly",
    val diamond100ProductId: String = "lovelink_diamonds_100",
    val diamond250ProductId: String = "lovelink_diamonds_250",
    val diamond500ProductId: String = "lovelink_diamonds_500",
    val diamond1000ProductId: String = "lovelink_diamonds_1000",
    val diamond2500ProductId: String = "lovelink_diamonds_2500",

    // Coin Wallet & Withdrawal Configuration
    val coinRateInINR: Double = 0.1, // 10 Coins = ₹1
    val minWithdrawalCoins: Int = 500, // 500 Coins = ₹50
    val minWithdrawalINR: Double = 50.0,

    // Free Limits
    val freeDailyChatsLimit: Int = 5,
    val freeDailyVoiceCallsLimit: Int = 3,
    val freeDailyVideoCallsLimit: Int = 2,

    // Reward Center Amounts
    val rewardDailyCheckInCoins: Int = 50,
    val rewardProfileCompletionCoins: Int = 100,
    val rewardPostingContentCoins: Int = 20,
    val rewardReferralCoins: Int = 50,
    val rewardRewardedAdCoins: Int = 30
)
