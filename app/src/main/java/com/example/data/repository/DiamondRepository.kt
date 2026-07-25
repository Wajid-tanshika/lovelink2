package com.example.data.repository

import com.example.data.model.*
import com.example.data.source.BillingManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiamondRepository(
    private val notificationRepo: NotificationRepository = NotificationRepository()
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _balance = MutableStateFlow(350)
    val balance: StateFlow<Int> = _balance.asStateFlow()

    // Monetization & Coins Wallet (10 Coins = ₹1, Min Withdrawal = 500 Coins = ₹50)
    private val _coinBalance = MutableStateFlow(650)
    val coinBalance: StateFlow<Int> = _coinBalance.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _premiumExpiresTimestamp = MutableStateFlow(0L)
    val premiumExpiresTimestamp: StateFlow<Long> = _premiumExpiresTimestamp.asStateFlow()

    private val _dailyStreakDay = MutableStateFlow(1)
    val dailyStreakDay: StateFlow<Int> = _dailyStreakDay.asStateFlow()

    private val _lastDailyClaimTimestamp = MutableStateFlow(0L)
    val lastDailyClaimTimestamp: StateFlow<Long> = _lastDailyClaimTimestamp.asStateFlow()

    private val _lastAdWatchTimestamp = MutableStateFlow(0L)
    val lastAdWatchTimestamp: StateFlow<Long> = _lastAdWatchTimestamp.asStateFlow()

    private val _myReferralCode = MutableStateFlow("LOVE-ALEX24")
    val myReferralCode: StateFlow<String> = _myReferralCode.asStateFlow()

    private val _referralsCount = MutableStateFlow(3)
    val referralsCount: StateFlow<Int> = _referralsCount.asStateFlow()

    // In-App Earn Tasks List
    private val initialEarnTasks = listOf(
        EarnTask("task_1", "Upload 3+ Photos", "Add high quality photos to complete your profile", 20, "Photo", false, "ONBOARDING"),
        EarnTask("task_2", "Send First Message", "Say hello to a match in chat", 15, "Chat", true, "DAILY"),
        EarnTask("task_3", "Join Live Stream", "Watch a live stream for at least 2 minutes", 15, "Live", false, "DAILY"),
        EarnTask("task_4", "Post a Story", "Share a moment in Stories", 20, "Story", false, "SOCIAL"),
        EarnTask("task_5", "Invite 3 Friends", "Share your referral code with friends", 50, "Invite", false, "PROMO")
    )
    private val _earnTasks = MutableStateFlow<List<EarnTask>>(initialEarnTasks)
    val earnTasks: StateFlow<List<EarnTask>> = _earnTasks.asStateFlow()

    // User's Withdrawal Requests History
    private val initialUserWithdrawals = listOf(
        WithdrawalRequest(
            id = "wdr_100",
            userId = "user_me",
            userName = "Alex Rivera",
            userEmail = "alex@example.com",
            userPhone = "+1 555-0199",
            coinsAmount = 500,
            inrAmount = 50.0,
            payoutMethod = "UPI",
            payoutDetails = "alex@okicici",
            accountHolderName = "Alex Rivera",
            status = WithdrawalStatus.PAID,
            timestamp = System.currentTimeMillis() - 86400000 * 3,
            processedTimestamp = System.currentTimeMillis() - 86400000 * 2,
            transactionRef = "UTR7782910394"
        )
    )
    private val _userWithdrawals = MutableStateFlow<List<WithdrawalRequest>>(initialUserWithdrawals)
    val userWithdrawals: StateFlow<List<WithdrawalRequest>> = _userWithdrawals.asStateFlow()

    private val _transactions = MutableStateFlow<List<DiamondTransaction>>(
        listOf(
            DiamondTransaction(
                id = "tx_1",
                userId = "user_me",
                type = TransactionType.DAILY_BONUS,
                amount = 25,
                description = "Daily Streak Day 1 Claim 💎",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 12
            ),
            DiamondTransaction(
                id = "tx_2",
                userId = "user_me",
                type = TransactionType.PURCHASE,
                amount = 300,
                description = "Google Play Purchase (300 Diamonds Pack)",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 48
            ),
            DiamondTransaction(
                id = "tx_3",
                userId = "user_me",
                type = TransactionType.REWARD,
                amount = 50,
                description = "Referral Reward Bonus (Invited Friend) 🎁",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24
            )
        )
    )
    val transactions: StateFlow<List<DiamondTransaction>> = _transactions.asStateFlow()

    private val _refundRequests = MutableStateFlow<List<RefundRequest>>(emptyList())
    val refundRequests: StateFlow<List<RefundRequest>> = _refundRequests.asStateFlow()

    // Available Active Coupons
    private val activeCoupons = mutableMapOf(
        "LOVE2026" to 100,
        "LOVELINK50" to 50,
        "VIPBONUS" to 200
    )
    private val redeemedCoupons = mutableSetOf<String>()

    /**
     * Claim Daily Login Streak Bonus
     */
    fun claimDailyReward(rewardAmount: Int): Boolean {
        val now = System.currentTimeMillis()
        _balance.value += rewardAmount
        val currentStreak = _dailyStreakDay.value
        addTransaction(TransactionType.DAILY_BONUS, rewardAmount, "Daily Streak Day $currentStreak Claim 💎")

        _lastDailyClaimTimestamp.value = now
        _dailyStreakDay.value = if (currentStreak >= 7) 1 else currentStreak + 1

        notificationRepo.addNotification(
            title = "Daily Reward Claimed! 🎁",
            body = "You collected +$rewardAmount Diamonds! Come back tomorrow to keep your streak going.",
            type = NotificationType.SYSTEM
        )
        return true
    }

    /**
     * Process Google Play In-App Purchase for Diamond Package
     */
    suspend fun buyDiamondPackageWithPlayBilling(
        userId: String,
        userName: String,
        userEmail: String,
        pkg: DiamondPackage
    ): Pair<Boolean, String> {
        val (success, message) = BillingManager.processDiamondPackagePurchase(userId, userName, userEmail, pkg)
        if (success) {
            _balance.value += pkg.amount
            addTransaction(TransactionType.PURCHASE, pkg.amount, "Google Play Purchase (${pkg.amount} Diamonds)")

            notificationRepo.addNotification(
                title = "Purchase Success! 💎",
                body = "Successfully purchased +${pkg.amount} Diamonds via Google Play Billing.",
                type = NotificationType.SYSTEM
            )
        }
        return Pair(success, message)
    }

    /**
     * Process Premium VIP Subscription
     */
    suspend fun subscribePremiumWithPlayBilling(
        userId: String,
        userName: String,
        userEmail: String,
        plan: PremiumPlan
    ): Pair<Boolean, String> {
        val (success, message) = BillingManager.processPremiumSubscriptionPurchase(userId, userName, userEmail, plan)
        if (success) {
            _isPremium.value = true
            val durationMs = plan.durationDays * 86400000L
            _premiumExpiresTimestamp.value = System.currentTimeMillis() + durationMs

            notificationRepo.addNotification(
                title = "LoveLink VIP Activated! ✨",
                body = "Welcome to ${plan.title}! Enjoy Unlimited Swipes, See Who Liked You, and VIP Badge.",
                type = NotificationType.SYSTEM
            )
        }
        return Pair(success, message)
    }

    /**
     * Restore Google Play Purchases & Subscriptions
     */
    suspend fun restorePurchases(userId: String, userEmail: String): Pair<Boolean, String> {
        val (success, message) = BillingManager.restorePurchases(userId, userEmail)
        if (success) {
            _isPremium.value = true
            _premiumExpiresTimestamp.value = System.currentTimeMillis() + 30 * 86400000L
            notificationRepo.addNotification(
                title = "Purchases Restored! ✨",
                body = "Your LoveLink VIP Premium membership and purchases have been restored.",
                type = NotificationType.SYSTEM
            )
        }
        return Pair(success, message)
    }

    /**
     * Redeem Promotional Coupon
     */
    fun redeemCoupon(code: String): Pair<Boolean, String> {
        val cleanCode = code.trim().uppercase()
        if (redeemedCoupons.contains(cleanCode)) {
            return Pair(false, "You have already redeemed coupon $cleanCode.")
        }

        val bonus = activeCoupons[cleanCode]
        if (bonus != null) {
            redeemedCoupons.add(cleanCode)
            _balance.value += bonus
            addTransaction(TransactionType.REWARD, bonus, "Redeemed Coupon Code '$cleanCode'")

            notificationRepo.addNotification(
                title = "Coupon Redeemed! 🎉",
                body = "Successfully redeemed coupon '$cleanCode' for +$bonus bonus Diamonds!",
                type = NotificationType.SYSTEM
            )
            return Pair(true, "Successfully redeemed +$bonus Diamonds! 💎")
        }

        return Pair(false, "Invalid or expired coupon code.")
    }

    /**
     * Redeem Referral Code from a Friend
     */
    fun redeemReferralCode(friendCode: String, bonusInvited: Int): Pair<Boolean, String> {
        val cleanCode = friendCode.trim().uppercase()
        if (cleanCode == _myReferralCode.value) {
            return Pair(false, "You cannot use your own referral code.")
        }
        if (cleanCode.length < 4) {
            return Pair(false, "Invalid referral code structure.")
        }

        _balance.value += bonusInvited
        _referralsCount.value += 1
        addTransaction(TransactionType.REWARD, bonusInvited, "Referral Joined with code '$cleanCode'")

        notificationRepo.addNotification(
            title = "Referral Bonus Received! 🎁",
            body = "You earned +$bonusInvited Diamonds for joining via referral code $cleanCode!",
            type = NotificationType.SYSTEM
        )
        return Pair(true, "Referral accepted! +$bonusInvited Diamonds added to your wallet.")
    }

    /**
     * Send Virtual Gift to a Match / User
     */
    fun sendVirtualGift(targetUserId: String, targetUserName: String, gift: VirtualGift): Boolean {
        if (_balance.value >= gift.diamondCost) {
            _balance.value -= gift.diamondCost
            addTransaction(
                type = TransactionType.GIFT_SENT,
                amount = -gift.diamondCost,
                description = "Sent ${gift.emoji} ${gift.name} to $targetUserName"
            )

            notificationRepo.addNotification(
                title = "Virtual Gift Sent! ${gift.emoji}",
                body = "You sent a ${gift.name} (${gift.diamondCost} 💎) to $targetUserName.",
                type = NotificationType.SYSTEM
            )
            return true
        }
        return false
    }

    /**
     * Activate Profile Boost
     */
    fun activateProfileBoost(costDiamonds: Int, durationMinutes: Int): Boolean {
        if (_balance.value >= costDiamonds) {
            _balance.value -= costDiamonds
            addTransaction(
                type = TransactionType.BOOST_USED,
                amount = -costDiamonds,
                description = "Activated $durationMinutes-Min Profile Boost ⚡"
            )

            notificationRepo.addNotification(
                title = "Profile Boost Active! ⚡",
                body = "Your profile is boosted for $durationMinutes minutes! Expect up to 10x more likes.",
                type = NotificationType.SYSTEM
            )
            return true
        }
        return false
    }

    /**
     * Send Super Like
     */
    fun sendSuperLike(targetUserName: String, costDiamonds: Int): Boolean {
        if (_balance.value >= costDiamonds) {
            _balance.value -= costDiamonds
            addTransaction(
                type = TransactionType.SUPER_LIKE_USED,
                amount = -costDiamonds,
                description = "Sent Super Like ⭐ to $targetUserName"
            )

            notificationRepo.addNotification(
                title = "Super Like Sent! ⭐",
                body = "You sent a Super Like to $targetUserName.",
                type = NotificationType.SYSTEM
            )
            return true
        }
        return false
    }

    /**
     * Spend Diamonds General
     */
    fun spendDiamonds(amount: Int, type: TransactionType, description: String): Boolean {
        if (_balance.value >= amount) {
            _balance.value -= amount
            addTransaction(type, -amount, description)
            return true
        }
        return false
    }

    /**
     * Submit Refund Request
     */
    fun submitRefundRequest(txId: String, itemName: String, amountPaid: String, reason: String, userId: String, userName: String, userEmail: String) {
        val req = RefundRequest(
            id = "ref_${System.currentTimeMillis()}",
            transactionId = txId,
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            itemName = itemName,
            amountPaid = amountPaid,
            reason = reason,
            timestamp = System.currentTimeMillis(),
            status = RefundStatus.PENDING
        )
        _refundRequests.value = listOf(req) + _refundRequests.value

        notificationRepo.addNotification(
            title = "Refund Request Received 📑",
            body = "Your refund request for $itemName ($amountPaid) has been submitted for Admin review.",
            type = NotificationType.SYSTEM
        )
    }

    /**
     * Watch Rewarded Ad and Earn Coins
     */
    fun watchRewardedAdAndClaimCoins(rewardCoins: Int = 15): Pair<Boolean, String> {
        val now = System.currentTimeMillis()
        val cooldownMs = 30 * 1000L // 30 second cooldown rate limit to prevent ad fraud / spam
        if (now - _lastAdWatchTimestamp.value < cooldownMs) {
            val remainingSec = ((cooldownMs - (now - _lastAdWatchTimestamp.value)) / 1000).coerceAtLeast(1)
            return Pair(false, "Please wait $remainingSec seconds before watching another rewarded ad ⏳")
        }

        _lastAdWatchTimestamp.value = now
        _coinBalance.value += rewardCoins
        addTransaction(TransactionType.REWARD, rewardCoins, "Watched Rewarded Ad (+ $rewardCoins Coins)")

        notificationRepo.addNotification(
            title = "Rewarded Ad Completed! 📺",
            body = "You earned +$rewardCoins Coins! Keep watching to accumulate more coins.",
            type = NotificationType.SYSTEM
        )
        return Pair(true, "Earned +$rewardCoins Coins! 🪙")
    }

    /**
     * Complete and Claim In-App Task
     */
    fun claimTaskCoins(taskId: String): Pair<Boolean, String> {
        val task = _earnTasks.value.find { it.id == taskId } ?: return Pair(false, "Task not found.")
        if (task.isCompleted) {
            return Pair(false, "You have already completed and claimed rewards for this task.")
        }

        _earnTasks.value = _earnTasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = true) else it
        }

        _coinBalance.value += task.coinsReward
        addTransaction(TransactionType.REWARD, task.coinsReward, "Completed Task '${task.title}' (+${task.coinsReward} Coins)")

        notificationRepo.addNotification(
            title = "Task Completed! 🎯",
            body = "You completed '${task.title}' and earned +${task.coinsReward} Coins!",
            type = NotificationType.SYSTEM
        )
        return Pair(true, "Task completed! Earned +${task.coinsReward} Coins! 🪙")
    }

    /**
     * Submit Withdrawal Request (Conversion: 10 Coins = ₹1, Min Withdrawal = 500 Coins = ₹50)
     */
    fun submitWithdrawalRequest(
        coins: Int,
        payoutMethod: String,
        payoutDetails: String,
        accountHolderName: String,
        userId: String,
        userName: String,
        userEmail: String,
        userPhone: String
    ): Pair<Boolean, String> {
        if (coins < 500) {
            return Pair(false, "Minimum withdrawal limit is 500 Coins (₹50).")
        }
        if (_coinBalance.value < coins) {
            return Pair(false, "Insufficient Coins balance. You have ${_coinBalance.value} Coins.")
        }
        if (payoutDetails.isBlank()) {
            return Pair(false, "Please provide valid payout details (UPI ID, Paytm No, or Bank account).")
        }
        if (accountHolderName.isBlank()) {
            return Pair(false, "Please provide the account holder name.")
        }

        // Check for duplicate pending withdrawal requests
        val hasPending = _userWithdrawals.value.any { it.status == WithdrawalStatus.PENDING }
        if (hasPending) {
            return Pair(false, "You already have a pending withdrawal request under review.")
        }

        val inrValue = coins / 10.0
        _coinBalance.value -= coins

        val newRequest = WithdrawalRequest(
            id = "wdr_${System.currentTimeMillis()}",
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            userPhone = userPhone,
            coinsAmount = coins,
            inrAmount = inrValue,
            payoutMethod = payoutMethod,
            payoutDetails = payoutDetails,
            accountHolderName = accountHolderName,
            status = WithdrawalStatus.PENDING,
            timestamp = System.currentTimeMillis()
        )

        _userWithdrawals.value = listOf(newRequest) + _userWithdrawals.value

        addTransaction(
            type = TransactionType.REWARD,
            amount = -coins,
            description = "Requested Withdrawal of $coins Coins (₹$inrValue) via $payoutMethod"
        )

        notificationRepo.addNotification(
            title = "Withdrawal Requested! 💸",
            body = "Your request to withdraw $coins Coins (₹$inrValue) via $payoutMethod is under Admin review.",
            type = NotificationType.SYSTEM
        )

        return Pair(true, "Withdrawal request submitted! Admin will process your payout shortly. 💸")
    }

    private fun addTransaction(type: TransactionType, amount: Int, description: String) {
        val newTx = DiamondTransaction(
            id = "tx_${System.currentTimeMillis()}",
            userId = "user_me",
            type = type,
            amount = amount,
            description = description,
            timestamp = System.currentTimeMillis()
        )
        _transactions.value = listOf(newTx) + _transactions.value
    }
}
