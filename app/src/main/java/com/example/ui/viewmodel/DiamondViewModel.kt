package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.DiamondRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiamondViewModel @JvmOverloads constructor(
    private val diamondRepo: DiamondRepository = DiamondRepository()
) : ViewModel() {

    val balance = diamondRepo.balance
    val coinBalance = diamondRepo.coinBalance
    val isPremium = diamondRepo.isPremium
    val premiumExpiresTimestamp = diamondRepo.premiumExpiresTimestamp
    val dailyStreakDay = diamondRepo.dailyStreakDay
    val lastDailyClaimTimestamp = diamondRepo.lastDailyClaimTimestamp
    val myReferralCode = diamondRepo.myReferralCode
    val referralsCount = diamondRepo.referralsCount
    val earnTasks = diamondRepo.earnTasks
    val userWithdrawals = diamondRepo.userWithdrawals
    val transactions = diamondRepo.transactions
    val refundRequests = diamondRepo.refundRequests

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    fun claimDailyReward(rewardAmount: Int) {
        diamondRepo.claimDailyReward(rewardAmount)
        _snackMessage.value = "Claimed +$rewardAmount Daily Streak Reward Diamonds! 🎁"
    }

    fun buyPackageWithPlayBilling(
        userId: String,
        userName: String,
        userEmail: String,
        pkg: DiamondPackage
    ) {
        viewModelScope.launch {
            val (success, message) = diamondRepo.buyDiamondPackageWithPlayBilling(userId, userName, userEmail, pkg)
            _snackMessage.value = message
        }
    }

    fun subscribePremiumWithPlayBilling(
        userId: String,
        userName: String,
        userEmail: String,
        plan: PremiumPlan
    ) {
        viewModelScope.launch {
            val (success, message) = diamondRepo.subscribePremiumWithPlayBilling(userId, userName, userEmail, plan)
            _snackMessage.value = message
        }
    }

    fun restorePurchases(userId: String, userEmail: String) {
        viewModelScope.launch {
            val (success, message) = diamondRepo.restorePurchases(userId, userEmail)
            _snackMessage.value = message
        }
    }

    fun redeemCoupon(code: String) {
        val (success, msg) = diamondRepo.redeemCoupon(code)
        _snackMessage.value = msg
    }

    fun redeemReferralCode(code: String, bonusAmount: Int = 25) {
        val (success, msg) = diamondRepo.redeemReferralCode(code, bonusAmount)
        _snackMessage.value = msg
    }

    fun sendVirtualGift(targetUserId: String, targetUserName: String, gift: VirtualGift): Boolean {
        val success = diamondRepo.sendVirtualGift(targetUserId, targetUserName, gift)
        if (success) {
            _snackMessage.value = "Sent ${gift.emoji} ${gift.name} to $targetUserName! 💖"
        } else {
            _snackMessage.value = "Insufficient Diamonds to send ${gift.name} (${gift.diamondCost} 💎 required)"
        }
        return success
    }

    fun activateProfileBoost(costDiamonds: Int = 50, durationMinutes: Int = 30): Boolean {
        val success = diamondRepo.activateProfileBoost(costDiamonds, durationMinutes)
        if (success) {
            _snackMessage.value = "Profile Boost Activated for $durationMinutes minutes! ⚡"
        } else {
            _snackMessage.value = "Need $costDiamonds Diamonds to activate Profile Boost!"
        }
        return success
    }

    fun sendSuperLike(targetUserName: String, costDiamonds: Int = 10): Boolean {
        val success = diamondRepo.sendSuperLike(targetUserName, costDiamonds)
        if (success) {
            _snackMessage.value = "Super Like ⭐ sent to $targetUserName!"
        } else {
            _snackMessage.value = "Need $costDiamonds Diamonds to send Super Like!"
        }
        return success
    }

    fun submitRefundRequest(
        txId: String,
        itemName: String,
        amountPaid: String,
        reason: String,
        userId: String,
        userName: String,
        userEmail: String
    ) {
        diamondRepo.submitRefundRequest(txId, itemName, amountPaid, reason, userId, userName, userEmail)
        _snackMessage.value = "Refund request submitted for review 📑"
    }

    fun watchRewardedAdAndClaimCoins() {
        val (success, msg) = diamondRepo.watchRewardedAdAndClaimCoins()
        _snackMessage.value = msg
    }

    fun claimTaskCoins(taskId: String) {
        val (success, msg) = diamondRepo.claimTaskCoins(taskId)
        _snackMessage.value = msg
    }

    fun submitWithdrawalRequest(
        coins: Int,
        payoutMethod: String,
        payoutDetails: String,
        accountHolderName: String,
        userId: String,
        userName: String,
        userEmail: String,
        userPhone: String
    ) {
        val (success, msg) = diamondRepo.submitWithdrawalRequest(
            coins = coins,
            payoutMethod = payoutMethod,
            payoutDetails = payoutDetails,
            accountHolderName = accountHolderName,
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            userPhone = userPhone
        )
        _snackMessage.value = msg
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }
}
