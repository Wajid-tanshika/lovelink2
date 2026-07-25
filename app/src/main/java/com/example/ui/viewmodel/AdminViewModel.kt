package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.*
import com.example.data.repository.AdminRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminViewModel @JvmOverloads constructor(
    private val adminRepo: AdminRepository = AdminRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    val reports = adminRepo.reports
    val users = userRepo.userList
    val diamondPackages = adminRepo.diamondPackages
    val premiumPlans = adminRepo.premiumPlans
    val notifications = adminRepo.notifications
    val paymentHistory = adminRepo.paymentHistory
    val refundRequests = adminRepo.refundRequests
    val promoCoupons = adminRepo.promoCoupons
    val appSettings = adminRepo.appSettings
    val firebaseConfig = adminRepo.firebaseConfig
    val callingConfig = adminRepo.callingConfig
    val storageConfig = adminRepo.storageConfig
    val withdrawals = adminRepo.withdrawals

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedUserFilter = MutableStateFlow("ALL") // ALL, ACTIVE, VERIFIED, BLOCKED
    val selectedUserFilter: StateFlow<String> = _selectedUserFilter.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setUserFilter(filter: String) {
        _selectedUserFilter.value = filter
    }

    fun toggleBlockUser(userId: String) {
        userRepo.toggleBlockUser(userId)
        _snackMessage.value = "User block status toggled 🚫"
    }

    fun warnUser(userId: String) {
        userRepo.warnUser(userId)
        _snackMessage.value = "Official warning issued to user ⚠️"
    }

    fun muteUser(userId: String) {
        userRepo.muteUser(userId)
        _snackMessage.value = "User mute state toggled 🔇"
    }

    fun suspendUser(userId: String, durationLabel: String) {
        userRepo.suspendUser(userId, durationLabel)
        _snackMessage.value = "User suspended ($durationLabel) ⛔"
    }

    fun restoreUser(userId: String) {
        userRepo.restoreUser(userId)
        _snackMessage.value = "User account fully restored ✅"
    }

    fun toggleVerifyUser(userId: String) {
        userRepo.toggleVerifyUser(userId)
        _snackMessage.value = "User verification badge toggled 💙"
    }

    fun deleteUser(userId: String) {
        userRepo.deleteUser(userId)
        _snackMessage.value = "User account permanently deleted 🗑️"
    }

    fun adjustUserDiamonds(userId: String, deltaAmount: Int) {
        userRepo.adjustUserDiamonds(userId, deltaAmount)
        val actionText = if (deltaAmount >= 0) "Added +$deltaAmount" else "Deducted $deltaAmount"
        _snackMessage.value = "$actionText Diamonds for selected user 💎"
    }

    fun sendBroadcastNotification(title: String, body: String, imageUrl: String?) {
        if (title.isBlank() || body.isBlank()) {
            _snackMessage.value = "Title and Body cannot be empty"
            return
        }
        adminRepo.sendAdminNotification(title, body, targetUserId = null, targetUserName = null, imageUrl = imageUrl)
        _snackMessage.value = "Broadcast Notification sent to ALL users! 🚀"
    }

    fun sendTargetedNotification(userId: String, userName: String, title: String, body: String) {
        if (title.isBlank() || body.isBlank()) {
            _snackMessage.value = "Title and Body cannot be empty"
            return
        }
        adminRepo.sendAdminNotification(title, body, targetUserId = userId, targetUserName = userName)
        _snackMessage.value = "Direct Notification sent to $userName! 📨"
    }

    fun saveDiamondPackage(pkg: DiamondPackage) {
        adminRepo.addOrUpdateDiamondPackage(pkg)
        _snackMessage.value = "Diamond package saved successfully 💎"
    }

    fun deleteDiamondPackage(pkgId: String) {
        adminRepo.deleteDiamondPackage(pkgId)
        _snackMessage.value = "Diamond package removed"
    }

    fun savePremiumPlan(plan: PremiumPlan) {
        adminRepo.savePremiumPlan(plan)
        _snackMessage.value = "Premium Plan updated ✨"
    }

    fun deletePremiumPlan(planId: String) {
        adminRepo.deletePremiumPlan(planId)
        _snackMessage.value = "Premium Plan removed"
    }

    fun approveRefund(refundId: String, userId: String, deductDiamonds: Int) {
        adminRepo.updateRefundStatus(refundId, RefundStatus.APPROVED, "Refund approved & processed by Admin")
        if (deductDiamonds > 0) {
            userRepo.adjustUserDiamonds(userId, -deductDiamonds)
        }
        _snackMessage.value = "Refund approved! User notified & wallet updated 💵"
    }

    fun rejectRefund(refundId: String, note: String) {
        adminRepo.updateRefundStatus(refundId, RefundStatus.REJECTED, note)
        _snackMessage.value = "Refund request rejected"
    }

    fun saveCoupon(coupon: PromoCoupon) {
        adminRepo.addOrUpdateCoupon(coupon)
        _snackMessage.value = "Promo Coupon '${coupon.code}' saved 🎉"
    }

    fun deleteCoupon(code: String) {
        adminRepo.deleteCoupon(code)
        _snackMessage.value = "Promo Coupon '$code' removed"
    }

    fun updateAppSettings(
        freeDailyLikes: Int,
        superlikeCost: Int,
        boostCost: Int,
        boostDuration: Int,
        referralInviter: Int,
        referralInvited: Int,
        isMaintenance: Boolean,
        maintenanceMsg: String,
        enforceGoogleSignIn: Boolean
    ) {
        val updated = appSettings.value.copy(
            freeDailyLikesLimit = freeDailyLikes,
            superlikeCostDiamonds = superlikeCost,
            profileBoostCostDiamonds = boostCost,
            boostDurationMinutes = boostDuration,
            referralBonusInviter = referralInviter,
            referralBonusInvited = referralInvited,
            isMaintenanceMode = isMaintenance,
            maintenanceMessage = maintenanceMsg,
            enforceGoogleSignInOnly = enforceGoogleSignIn
        )
        adminRepo.updateAppSettings(updated)
        _snackMessage.value = "App Settings & Economy Rules saved to Firestore ⚙️"
    }

    fun saveFirebaseConfig(config: FirebaseAppConfig) {
        val (success, message) = adminRepo.saveFirebaseConfig(config)
        _snackMessage.value = message
    }

    fun saveCallingConfig(config: CallingProviderConfig) {
        val (success, message) = adminRepo.saveCallingConfig(config)
        _snackMessage.value = message
    }

    fun saveStorageConfig(config: StorageProviderConfig) {
        val (success, message) = adminRepo.saveStorageConfig(config)
        _snackMessage.value = message
    }

    fun approveWithdrawal(requestId: String) {
        adminRepo.updateWithdrawalStatus(requestId, WithdrawalStatus.APPROVED)
        _snackMessage.value = "Withdrawal request APPROVED! Ready for payout 💵"
    }

    fun rejectWithdrawal(requestId: String, reason: String) {
        adminRepo.updateWithdrawalStatus(requestId, WithdrawalStatus.REJECTED, rejectionReason = reason)
        _snackMessage.value = "Withdrawal request REJECTED & coins refunded to user ❌"
    }

    fun markWithdrawalPaid(requestId: String, utrRef: String) {
        adminRepo.updateWithdrawalStatus(requestId, WithdrawalStatus.PAID, transactionRef = utrRef)
        _snackMessage.value = "Withdrawal marked as PAID (Ref: $utrRef) ✅"
    }

    fun resolveReport(reportId: String, reportedUserId: String?) {
        adminRepo.updateReportStatus(reportId, ReportStatus.RESOLVED)
        reportedUserId?.let {
            userRepo.toggleBlockUser(it)
        }
        _snackMessage.value = "Report resolved and offender banned 🛡️"
    }

    fun dismissReport(reportId: String) {
        adminRepo.updateReportStatus(reportId, ReportStatus.DISMISSED)
        _snackMessage.value = "Report dismissed"
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }
}
