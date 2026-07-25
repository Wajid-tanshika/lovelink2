package com.example.data.repository

import com.example.data.model.*
import com.example.data.source.FirestoreAdminService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Initial Reports
    private val initialReports = listOf(
        ReportItem(
            id = "rep_1",
            reporterId = "user_1",
            reporterName = "Sophia Chen",
            reportedUserId = "user_4",
            reportedUserName = "Liam Vance",
            reason = "Inappropriate Messages",
            details = "Sent suspicious external promo links in chat messages.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
            status = ReportStatus.PENDING
        ),
        ReportItem(
            id = "rep_2",
            reporterId = "user_2",
            reporterName = "Elena Rostova",
            reportedUserId = "user_6",
            reportedUserName = "Zoe Rivera",
            reason = "Fake Profile Photos",
            details = "Photos seem copied from public stock photos.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
            status = ReportStatus.PENDING
        )
    )

    private val _reports = MutableStateFlow<List<ReportItem>>(initialReports)
    val reports: StateFlow<List<ReportItem>> = _reports.asStateFlow()

    // Diamond Packages
    private val initialPackages = listOf(
        DiamondPackage("pkg_100", 100, "$1.99", 199, null, true),
        DiamondPackage("pkg_250", 250, "$4.99", 499, "+10% Bonus", true),
        DiamondPackage("pkg_500", 500, "$8.99", 899, "POPULAR 🔥", true),
        DiamondPackage("pkg_1000", 1000, "$15.99", 1599, "+25% Bonus", true),
        DiamondPackage("pkg_2500", 2500, "$34.99", 3499, "BEST VALUE 💎", true),
        DiamondPackage("pkg_5000", 5000, "$59.99", 5999, "VIP CHOICE 👑", true),
        DiamondPackage("pkg_10000", 10000, "$99.99", 9999, "ULTRA VALUE 🚀", true)
    )
    private val _diamondPackages = MutableStateFlow<List<DiamondPackage>>(initialPackages)
    val diamondPackages: StateFlow<List<DiamondPackage>> = _diamondPackages.asStateFlow()

    // Premium VIP Plans
    private val initialPlans = listOf(
        PremiumPlan("plan_weekly", "1 Week VIP", "$4.99/wk", "WEEKLY", 7, listOf("Unlimited Swipes", "See Who Liked You"), false, true),
        PremiumPlan("plan_monthly", "1 Month VIP", "$14.99/mo", "MONTHLY", 30, listOf("Unlimited Swipes", "See Who Liked You", "5 Free Daily Superlikes"), true, true),
        PremiumPlan("plan_yearly", "1 Year VIP", "$59.99/yr", "YEARLY", 365, listOf("Unlimited Swipes", "See Who Liked You", "5 Daily Superlikes", "1 Free Monthly Boost", "VIP Badge"), true, true)
    )
    private val _premiumPlans = MutableStateFlow<List<PremiumPlan>>(initialPlans)
    val premiumPlans: StateFlow<List<PremiumPlan>> = _premiumPlans.asStateFlow()

    // Sent Admin Notifications Log
    private val initialNotifications = listOf(
        AdminNotification(
            id = "notif_1",
            title = "Welcome to LoveLink! 💕",
            body = "Explore thousands of single profiles near you today.",
            timestamp = System.currentTimeMillis() - 86400000,
            sentByEmail = "System Broadcast"
        ),
        AdminNotification(
            id = "notif_2",
            title = "Weekend Boost Special ⚡",
            body = "Enjoy 20% off all diamond packs this weekend only!",
            timestamp = System.currentTimeMillis() - 43200000,
            sentByEmail = "System Broadcast"
        )
    )
    private val _notifications = MutableStateFlow<List<AdminNotification>>(initialNotifications)
    val notifications: StateFlow<List<AdminNotification>> = _notifications.asStateFlow()

    // Payment Transactions History
    private val initialTransactions = listOf(
        PaymentTransaction("tx_101", "user_1", "Sophia Chen", "sophia@lovelink.com", "500 Diamonds Pack", "$8.99", "Google Play Billing", "SUCCESS", System.currentTimeMillis() - 3600000),
        PaymentTransaction("tx_102", "user_2", "Elena Rostova", "elena@lovelink.com", "1 Month VIP Subscription", "$14.99", "Google Play Subscriptions", "SUCCESS", System.currentTimeMillis() - 12000000),
        PaymentTransaction("tx_103", "user_4", "Liam Vance", "liam@lovelink.com", "100 Diamonds Pack", "$1.99", "Google Play Billing", "SUCCESS", System.currentTimeMillis() - 86400000),
        PaymentTransaction("tx_104", "user_5", "Chloe Dubois", "chloe@lovelink.com", "1 Year VIP Subscription", "$59.99", "Google Play Subscriptions", "SUCCESS", System.currentTimeMillis() - 172800000)
    )
    private val _paymentHistory = MutableStateFlow<List<PaymentTransaction>>(initialTransactions)
    val paymentHistory: StateFlow<List<PaymentTransaction>> = _paymentHistory.asStateFlow()

    // Refund Requests Log
    private val initialRefunds = listOf(
        RefundRequest(
            id = "ref_101",
            transactionId = "tx_103",
            userId = "user_4",
            userName = "Liam Vance",
            userEmail = "liam@lovelink.com",
            itemName = "100 Diamonds Pack",
            amountPaid = "$1.99",
            reason = "Accidental duplicate checkout tap",
            timestamp = System.currentTimeMillis() - 18000000,
            status = RefundStatus.PENDING
        )
    )
    private val _refundRequests = MutableStateFlow<List<RefundRequest>>(initialRefunds)
    val refundRequests: StateFlow<List<RefundRequest>> = _refundRequests.asStateFlow()

    // Promo Coupons
    private val initialCoupons = listOf(
        PromoCoupon("LOVE2026", 100, 500, 42, System.currentTimeMillis() + 86400000L * 30, true),
        PromoCoupon("LOVELINK50", 50, 1000, 180, System.currentTimeMillis() + 86400000L * 60, true),
        PromoCoupon("VIPBONUS", 200, 100, 89, System.currentTimeMillis() + 86400000L * 15, true)
    )
    private val _promoCoupons = MutableStateFlow<List<PromoCoupon>>(initialCoupons)
    val promoCoupons: StateFlow<List<PromoCoupon>> = _promoCoupons.asStateFlow()

    // App Settings
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    // Firebase Integration Config
    private val _firebaseConfig = MutableStateFlow(FirebaseAppConfig())
    val firebaseConfig: StateFlow<FirebaseAppConfig> = _firebaseConfig.asStateFlow()

    // Calling Provider Config (Agora, WebRTC, Jitsi)
    private val _callingConfig = MutableStateFlow(CallingProviderConfig())
    val callingConfig: StateFlow<CallingProviderConfig> = _callingConfig.asStateFlow()

    // Storage Provider Config (Firebase Storage vs Custom S3)
    private val _storageConfig = MutableStateFlow(StorageProviderConfig())
    val storageConfig: StateFlow<StorageProviderConfig> = _storageConfig.asStateFlow()

    // Withdrawal Requests Log
    private val initialWithdrawals = listOf(
        WithdrawalRequest(
            id = "wdr_101",
            userId = "user_1",
            userName = "Sophia Chen",
            userEmail = "sophia@lovelink.com",
            userPhone = "+1 555-0192",
            coinsAmount = 500,
            inrAmount = 50.0,
            payoutMethod = "UPI",
            payoutDetails = "sophia@okicici",
            accountHolderName = "Sophia Chen",
            status = WithdrawalStatus.PENDING,
            timestamp = System.currentTimeMillis() - 3600000 * 2
        ),
        WithdrawalRequest(
            id = "wdr_102",
            userId = "user_2",
            userName = "Elena Rostova",
            userEmail = "elena@lovelink.com",
            userPhone = "+1 555-0823",
            coinsAmount = 1000,
            inrAmount = 100.0,
            payoutMethod = "Paytm",
            payoutDetails = "9876543210",
            accountHolderName = "Elena Rostova",
            status = WithdrawalStatus.APPROVED,
            timestamp = System.currentTimeMillis() - 3600000 * 12,
            processedTimestamp = System.currentTimeMillis() - 3600000 * 6
        ),
        WithdrawalRequest(
            id = "wdr_103",
            userId = "user_4",
            userName = "Liam Vance",
            userEmail = "liam@lovelink.com",
            userPhone = "+1 555-0341",
            coinsAmount = 500,
            inrAmount = 50.0,
            payoutMethod = "Bank Transfer",
            payoutDetails = "A/C: 91823719238 • IFSC: HDFC0001234",
            accountHolderName = "Liam Vance",
            status = WithdrawalStatus.PAID,
            timestamp = System.currentTimeMillis() - 86400000,
            processedTimestamp = System.currentTimeMillis() - 43200000,
            transactionRef = "UTR9812739182"
        )
    )
    private val _withdrawals = MutableStateFlow<List<WithdrawalRequest>>(initialWithdrawals)
    val withdrawals: StateFlow<List<WithdrawalRequest>> = _withdrawals.asStateFlow()

    fun reportUser(
        reporterId: String,
        reporterName: String,
        reportedUserId: String,
        reportedUserName: String,
        reason: String,
        details: String
    ) {
        val newRep = ReportItem(
            id = "rep_${System.currentTimeMillis()}",
            reporterId = reporterId,
            reporterName = reporterName,
            reportedUserId = reportedUserId,
            reportedUserName = reportedUserName,
            reason = reason,
            details = details,
            timestamp = System.currentTimeMillis(),
            status = ReportStatus.PENDING
        )
        _reports.value = listOf(newRep) + _reports.value
    }

    fun updateReportStatus(reportId: String, newStatus: ReportStatus) {
        _reports.value = _reports.value.map { rep ->
            if (rep.id == reportId) rep.copy(status = newStatus) else rep
        }
    }

    fun sendAdminNotification(
        title: String,
        body: String,
        targetUserId: String? = null,
        targetUserName: String? = null,
        imageUrl: String? = null,
        senderEmail: String = "System Admin"
    ) {
        val notif = AdminNotification(
            id = "notif_${System.currentTimeMillis()}",
            title = title,
            body = body,
            imageUrl = imageUrl,
            targetUserId = targetUserId,
            targetUserName = targetUserName,
            timestamp = System.currentTimeMillis(),
            sentByEmail = senderEmail
        )
        _notifications.value = listOf(notif) + _notifications.value

        repositoryScope.launch {
            FirestoreAdminService.recordNotification(notif)
        }
    }

    fun addOrUpdateDiamondPackage(pkg: DiamondPackage) {
        val existingIndex = _diamondPackages.value.indexOfFirst { it.id == pkg.id }
        if (existingIndex >= 0) {
            _diamondPackages.value = _diamondPackages.value.toMutableList().apply { set(existingIndex, pkg) }
        } else {
            val newPkg = if (pkg.id.isEmpty()) pkg.copy(id = "pkg_${System.currentTimeMillis()}") else pkg
            _diamondPackages.value = _diamondPackages.value + newPkg
        }
    }

    fun deleteDiamondPackage(pkgId: String) {
        _diamondPackages.value = _diamondPackages.value.filter { it.id != pkgId }
    }

    fun savePremiumPlan(plan: PremiumPlan) {
        val existingIndex = _premiumPlans.value.indexOfFirst { it.id == plan.id }
        if (existingIndex >= 0) {
            _premiumPlans.value = _premiumPlans.value.toMutableList().apply { set(existingIndex, plan) }
        } else {
            val newPlan = if (plan.id.isEmpty()) plan.copy(id = "plan_${System.currentTimeMillis()}") else plan
            _premiumPlans.value = _premiumPlans.value + newPlan
        }
    }

    fun deletePremiumPlan(planId: String) {
        _premiumPlans.value = _premiumPlans.value.filter { it.id != planId }
    }

    fun updateRefundStatus(refundId: String, status: RefundStatus, notes: String?) {
        _refundRequests.value = _refundRequests.value.map { ref ->
            if (ref.id == refundId) ref.copy(status = status, adminNotes = notes) else ref
        }
    }

    fun addOrUpdateCoupon(coupon: PromoCoupon) {
        val existingIndex = _promoCoupons.value.indexOfFirst { it.code.equals(coupon.code, ignoreCase = true) }
        if (existingIndex >= 0) {
            _promoCoupons.value = _promoCoupons.value.toMutableList().apply { set(existingIndex, coupon) }
        } else {
            _promoCoupons.value = _promoCoupons.value + coupon
        }
    }

    fun deleteCoupon(code: String) {
        _promoCoupons.value = _promoCoupons.value.filter { !it.code.equals(code, ignoreCase = true) }
    }

    fun updateAppSettings(settings: AppSettings) {
        _appSettings.value = settings
        repositoryScope.launch {
            FirestoreAdminService.saveAppSettings(settings)
        }
    }

    fun saveFirebaseConfig(config: FirebaseAppConfig): Pair<Boolean, String> {
        if (config.projectId.isBlank()) return Pair(false, "Project ID cannot be empty.")
        if (config.apiKey.isBlank()) return Pair(false, "API Key cannot be empty.")
        val updated = config.copy(lastValidatedTimestamp = System.currentTimeMillis())
        _firebaseConfig.value = updated
        
        com.example.LoveLinkApplication.instance?.let { ctx ->
            com.example.util.EncryptedStorageManager.saveFirebaseConfig(ctx, updated)
        }
        return Pair(true, "Firebase Configuration updated & validated successfully! 🔥")
    }

    fun saveCallingConfig(config: CallingProviderConfig): Pair<Boolean, String> {
        when (config.selectedProvider) {
            CallingProvider.AGORA -> {
                if (config.agoraAppId.isBlank()) return Pair(false, "Agora App ID is required.")
                com.example.data.firebase.AgoraCallService.configure(
                    appId = config.agoraAppId,
                    token = config.agoraTempToken,
                    channel = config.agoraChannelName
                )
            }
            CallingProvider.WEBRTC -> {
                if (config.webrtcSignalingUrl.isBlank()) return Pair(false, "WebRTC Signaling URL is required.")
            }
            CallingProvider.JITSI -> {
                if (config.jitsiServerUrl.isBlank()) return Pair(false, "Jitsi Server URL is required.")
            }
        }
        _callingConfig.value = config

        com.example.LoveLinkApplication.instance?.let { ctx ->
            com.example.util.EncryptedStorageManager.saveCallingConfig(ctx, config)
        }
        return Pair(true, "Calling Provider Configuration (${config.selectedProvider.name}) saved! 📞")
    }

    fun saveStorageConfig(config: StorageProviderConfig): Pair<Boolean, String> {
        if (config.selectedProvider == StorageProvider.FIREBASE_STORAGE && config.firebaseBucket.isBlank()) {
            return Pair(false, "Firebase Storage Bucket is required.")
        }
        if (config.selectedProvider == StorageProvider.CUSTOM_S3 && config.customEndpointUrl.isBlank()) {
            return Pair(false, "Custom S3 Endpoint URL is required.")
        }
        if (config.selectedProvider == StorageProvider.CLOUDINARY) {
            if (config.cloudinaryCloudName.isBlank() || config.cloudinaryUploadPreset.isBlank()) {
                return Pair(false, "Cloudinary Cloud Name and Unsigned Upload Preset are required.")
            }
            com.example.data.firebase.CloudinaryStorageService.configure(
                cloudName = config.cloudinaryCloudName,
                uploadPreset = config.cloudinaryUploadPreset
            )
        }
        _storageConfig.value = config

        com.example.LoveLinkApplication.instance?.let { ctx ->
            com.example.util.EncryptedStorageManager.saveStorageConfig(ctx, config)
        }
        return Pair(true, "Storage Provider Settings (${config.selectedProvider.name}) updated! 🖼️")
    }

    fun updateWithdrawalStatus(
        requestId: String,
        newStatus: WithdrawalStatus,
        rejectionReason: String? = null,
        transactionRef: String? = null
    ) {
        _withdrawals.value = _withdrawals.value.map { wdr ->
            if (wdr.id == requestId) {
                wdr.copy(
                    status = newStatus,
                    processedTimestamp = System.currentTimeMillis(),
                    rejectionReason = rejectionReason ?: wdr.rejectionReason,
                    transactionRef = transactionRef ?: wdr.transactionRef
                )
            } else wdr
        }
    }
}
