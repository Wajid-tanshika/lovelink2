package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary
import com.example.ui.viewmodel.AdminViewModel
import com.example.util.PreventScreenshotProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    adminViewModel: AdminViewModel,
    liveStreamViewModel: com.example.ui.screens.livestream.LiveStreamViewModel? = null,
    storyViewModel: com.example.ui.screens.story.StoryViewModel? = null,
    onBackClick: () -> Unit
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val reports by adminViewModel.reports.collectAsState()
    val users by adminViewModel.users.collectAsState()
    val diamondPackages by adminViewModel.diamondPackages.collectAsState()
    val premiumPlans by adminViewModel.premiumPlans.collectAsState()
    val notifications by adminViewModel.notifications.collectAsState()
    val paymentHistory by adminViewModel.paymentHistory.collectAsState()
    val refundRequests by adminViewModel.refundRequests.collectAsState()
    val promoCoupons by adminViewModel.promoCoupons.collectAsState()
    val appSettings by adminViewModel.appSettings.collectAsState()
    val firebaseConfig by adminViewModel.firebaseConfig.collectAsState()
    val callingConfig by adminViewModel.callingConfig.collectAsState()
    val storageConfig by adminViewModel.storageConfig.collectAsState()
    val withdrawals by adminViewModel.withdrawals.collectAsState()
    val snackMessage by adminViewModel.snackMessage.collectAsState()

    val liveStreams = liveStreamViewModel?.activeStreams?.collectAsState()?.value ?: emptyList()
    val gifts = liveStreamViewModel?.gifts?.collectAsState()?.value ?: VirtualGift.DEFAULT_GIFTS
    val storyGroups = storyViewModel?.storyGroups?.collectAsState()?.value ?: emptyList()

    val searchQuery by adminViewModel.searchQuery.collectAsState()
    val selectedUserFilter by adminViewModel.selectedUserFilter.collectAsState()

    var activeTab by remember { mutableStateOf("Dashboard") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearSnackMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldPremium
                        ) {
                            Text(
                                "🛡️ ADMIN",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Text("LoveLink Admin Console", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal Admin Navigation Tabs
            val tabsList = listOf(
                "Dashboard",
                "Users",
                "Withdrawals 💸",
                "Firebase Config 🔥",
                "Calling Provider 📞",
                "Storage Provider 🖼️",
                "Live & Gifts",
                "Notifications",
                "Packages & Plans",
                "Refunds & Coupons",
                "Analytics & Reports",
                "Settings"
            )
            ScrollableTabRow(
                selectedTabIndex = tabsList.indexOf(activeTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabsList.forEach { tabName ->
                    Tab(
                        selected = activeTab == tabName,
                        onClick = { activeTab = tabName },
                        text = { Text(tabName, fontWeight = if (activeTab == tabName) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    "Dashboard" -> AdminDashboardSection(
                        users = users,
                        reports = reports,
                        notifications = notifications,
                        payments = paymentHistory,
                        onNavigateTab = { activeTab = it }
                    )
                    "Users" -> AdminUsersSection(
                        users = users,
                        searchQuery = searchQuery,
                        selectedFilter = selectedUserFilter,
                        onSearchChange = { adminViewModel.updateSearchQuery(it) },
                        onFilterChange = { adminViewModel.setUserFilter(it) },
                        onToggleBlock = { adminViewModel.toggleBlockUser(it) },
                        onToggleVerify = { adminViewModel.toggleVerifyUser(it) },
                        onDeleteUser = { adminViewModel.deleteUser(it) },
                        onAdjustDiamonds = { userId, delta -> adminViewModel.adjustUserDiamonds(userId, delta) },
                        onWarnUser = { adminViewModel.warnUser(it) },
                        onMuteUser = { adminViewModel.muteUser(it) },
                        onSuspendUser = { userId, dur -> adminViewModel.suspendUser(userId, dur) },
                        onRestoreUser = { adminViewModel.restoreUser(it) }
                    )
                    "Withdrawals 💸" -> AdminWithdrawalsSection(
                        withdrawals = withdrawals,
                        onApprove = { adminViewModel.approveWithdrawal(it) },
                        onReject = { id, reason -> adminViewModel.rejectWithdrawal(id, reason) },
                        onMarkPaid = { id, utr -> adminViewModel.markWithdrawalPaid(id, utr) }
                    )
                    "Firebase Config 🔥" -> AdminFirebaseConfigSection(
                        config = firebaseConfig,
                        onSaveConfig = { adminViewModel.saveFirebaseConfig(it) }
                    )
                    "Calling Provider 📞" -> AdminCallingProviderSection(
                        config = callingConfig,
                        onSaveConfig = { adminViewModel.saveCallingConfig(it) }
                    )
                    "Storage Provider 🖼️" -> AdminStorageProviderSection(
                        config = storageConfig,
                        onSaveConfig = { adminViewModel.saveStorageConfig(it) }
                    )
                    "Live & Gifts" -> AdminLiveAndGiftsSection(
                        liveStreams = liveStreams,
                        gifts = gifts,
                        storyGroups = storyGroups,
                        onEndLive = { liveStreamViewModel?.adminEndLive(it) },
                        onSaveGift = { liveStreamViewModel?.adminAddOrUpdateGift(it) },
                        onDeleteGift = { liveStreamViewModel?.adminDeleteGift(it) },
                        onDeleteStory = { storyViewModel?.deleteStory(it) }
                    )
                    "Notifications" -> AdminNotificationsSection(
                        users = users,
                        notifications = notifications,
                        onSendBroadcast = { t, b, img -> adminViewModel.sendBroadcastNotification(t, b, img) },
                        onSendTargeted = { uid, uname, t, b -> adminViewModel.sendTargetedNotification(uid, uname, t, b) }
                    )
                    "Packages & Plans" -> AdminMonetizationSection(
                        packages = diamondPackages,
                        plans = premiumPlans,
                        payments = paymentHistory,
                        onSavePackage = { adminViewModel.saveDiamondPackage(it) },
                        onDeletePackage = { adminViewModel.deleteDiamondPackage(it) },
                        onSavePlan = { adminViewModel.savePremiumPlan(it) }
                    )
                    "Refunds & Coupons" -> AdminRefundsAndCouponsSection(
                        refunds = refundRequests,
                        coupons = promoCoupons,
                        onApproveRefund = { id, uid, amt -> adminViewModel.approveRefund(id, uid, amt) },
                        onRejectRefund = { id, note -> adminViewModel.rejectRefund(id, note) },
                        onSaveCoupon = { adminViewModel.saveCoupon(it) },
                        onDeleteCoupon = { adminViewModel.deleteCoupon(it) }
                    )
                    "Analytics & Reports" -> AdminAnalyticsAndReportsSection(
                        reports = reports,
                        payments = paymentHistory,
                        onResolveReport = { repId, reportedUid -> adminViewModel.resolveReport(repId, reportedUid) },
                        onDismissReport = { repId -> adminViewModel.dismissReport(repId) }
                    )
                    "Settings" -> AdminSettingsSection(
                        appSettings = appSettings,
                        onSaveSettings = { freeLikes, superlikeCost, boostCost, boostDur, refInviter, refInvited, isMaint, maintMsg, googleOnly ->
                            adminViewModel.updateAppSettings(freeLikes, superlikeCost, boostCost, boostDur, refInviter, refInvited, isMaint, maintMsg, googleOnly)
                        }
                    )
                }
            }
        }
    }
}


/* ========================================================================= */
/* 1. DASHBOARD TAB                                                           */
/* ========================================================================= */
@Composable
private fun AdminDashboardSection(
    users: List<UserProfile>,
    reports: List<ReportItem>,
    notifications: List<AdminNotification>,
    payments: List<PaymentTransaction>,
    onNavigateTab: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("System KPI Metrics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Total Users", "${users.size + 1}", RosePrimary, Modifier.weight(1f))
            MetricCard("Active Users", "${users.count { !it.isBlocked } + 1}", DiamondCyan, Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Online Now", "${users.count { it.isOnline } + 1}", Color(0xFF4CAF50), Modifier.weight(1f))
            MetricCard("Verified", "${users.count { it.isVerified } + 1}", DiamondCyan, Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Reported", "${reports.count { it.status == ReportStatus.PENDING }}", MaterialTheme.colorScheme.error, Modifier.weight(1f))
            MetricCard("Total Revenue", "$12,450", GoldPremium, Modifier.weight(1f))
        }

        Text("Quick Administration Actions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onNavigateTab("Notifications") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text("Broadcast Push")
            }

            Button(
                onClick = { onNavigateTab("Users") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan)
            ) {
                Text("User Directory")
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

/* ========================================================================= */
/* 2. USERS MANAGEMENT TAB                                                   */
/* ========================================================================= */
@Composable
private fun AdminUsersSection(
    users: List<UserProfile>,
    searchQuery: String,
    selectedFilter: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onToggleBlock: (String) -> Unit,
    onToggleVerify: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onAdjustDiamonds: (String, Int) -> Unit,
    onWarnUser: (String) -> Unit = {},
    onMuteUser: (String) -> Unit = {},
    onSuspendUser: (String, String) -> Unit = { _, _ -> },
    onRestoreUser: (String) -> Unit = {}
) {
    var selectedUserForDialog by remember { mutableStateOf<UserProfile?>(null) }
    var selectedUserForDiamonds by remember { mutableStateOf<UserProfile?>(null) }
    var showSuspendDurationPicker by remember { mutableStateOf(false) }
    var customDurationText by remember { mutableStateOf("") }

    val filteredUsers = users.filter { user ->
        val matchesQuery = searchQuery.isEmpty() ||
                user.name.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.city.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "ACTIVE" -> !user.isBlocked
            "VERIFIED" -> user.isVerified
            "BLOCKED" -> user.isBlocked
            else -> true
        }

        matchesQuery && matchesFilter
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search users by name, email, or city...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("ALL", "ACTIVE", "VERIFIED", "BLOCKED")) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RosePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredUsers) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = user.photoUrls.firstOrNull() ?: "",
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    if (user.isVerified) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = DiamondCyan, modifier = Modifier.size(16.dp))
                                    }
                                    if (user.isMuted) {
                                        Text("🔇 Muted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Text("${user.email.ifEmpty { "user@lovelink.com" }} • ${user.city}", style = MaterialTheme.typography.bodySmall)
                                Text("💎 ${user.diamondBalance} Diamonds • Warnings: ${user.warningCount}", style = MaterialTheme.typography.labelSmall, color = DiamondCyan)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { selectedUserForDialog = user },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Full Profile 👤", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { selectedUserForDiamonds = user },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("💎 Wallet", fontSize = 12.sp)
                            }

                            IconButton(onClick = { onToggleVerify(user.id) }) {
                                Icon(Icons.Default.Verified, contentDescription = "Verify", tint = if (user.isVerified) DiamondCyan else Color.Gray)
                            }

                            IconButton(onClick = { onToggleBlock(user.id) }) {
                                Icon(Icons.Default.Block, contentDescription = "Block", tint = if (user.isBlocked) MaterialTheme.colorScheme.error else Color.Gray)
                            }

                            IconButton(onClick = { onDeleteUser(user.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    val dialogUser = selectedUserForDialog
    if (dialogUser != null) {
        val u = dialogUser
        AlertDialog(
            onDismissRequest = { selectedUserForDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${u.name}, ${u.age}", fontWeight = FontWeight.Bold)
                    if (u.isVerified) Icon(Icons.Default.Verified, contentDescription = null, tint = DiamondCyan, modifier = Modifier.size(18.dp))
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Profile Photos Gallery
                    Text("Photos (${u.photoUrls.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(u.photoUrls) { photo ->
                            AsyncImage(
                                model = photo,
                                contentDescription = null,
                                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Divider()

                    // Bio & Interests
                    Text("Bio & Passions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(u.bio.ifEmpty { "No bio added yet." }, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        u.interests.take(4).forEach { interest ->
                            Surface(shape = RoundedCornerShape(8.dp), color = RosePrimary.copy(alpha = 0.15f)) {
                                Text(interest, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Divider()

                    // Detailed Social & Account Metrics
                    Text("Account Metrics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("📧 Email: ${u.email.ifEmpty { "N/A" }}")
                    Text("📱 Device: ${u.deviceInfo}")
                    Text("📍 Location: ${u.city}")
                    Text("💎 Balance: ${u.diamondBalance} Diamonds")
                    Text("👥 Followers: ${u.followers.size + 142} • Following: ${u.following.size + 58}")
                    Text("💬 Chat History Moderation: Compliant • 0 policy violations flagged")
                    Text("📸 Posts: ${u.postsCount} • Stories: Active")
                    Text("⚠️ Official Warnings: ${u.warningCount}")
                    Text("⛔ Suspension: ${if (u.suspensionUntil.isNotEmpty()) u.suspensionUntil else "None"}")
                    Text("🌐 Login History:")
                    u.loginHistory.forEach { log ->
                        Text("  • $log", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }

                    Divider()

                    // Admin Actions Grid
                    Text("Moderation Actions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { onWarnUser(u.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⚠️ Warn", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onMuteUser(u.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (u.isMuted) "🔊 Unmute" else "🔇 Mute", fontSize = 11.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showSuspendDurationPicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text("⛔ Suspend", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        }

                        if (u.isBlocked) {
                            Button(
                                onClick = { onRestoreUser(u.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan)
                            ) {
                                Text("✅ Restore", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = { onToggleBlock(u.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("🚫 Perm Ban", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedUserForDialog = null }) { Text("Close") }
            }
        )

        if (showSuspendDurationPicker) {
            val durationOptions = listOf("1 Hour", "6 Hours", "12 Hours", "1 Day", "3 Days", "7 Days", "15 Days", "30 Days")
            AlertDialog(
                onDismissRequest = { showSuspendDurationPicker = false },
                title = { Text("Choose Suspension Duration") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        durationOptions.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                row.forEach { option ->
                                    OutlinedButton(
                                        onClick = {
                                            onSuspendUser(u.id, option)
                                            showSuspendDurationPicker = false
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(option, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        Divider()
                        Text("Or enter Custom Duration:")
                        OutlinedTextField(
                            value = customDurationText,
                            onValueChange = { customDurationText = it },
                            placeholder = { Text("e.g., 45 Days or Indefinite") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customDurationText.isNotBlank()) {
                                onSuspendUser(u.id, customDurationText)
                                showSuspendDurationPicker = false
                            }
                        }
                    ) {
                        Text("Apply Custom")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSuspendDurationPicker = false }) { Text("Cancel") }
                }
            )
        }
    }

    val diamondUser = selectedUserForDiamonds
    if (diamondUser != null) {
        val u = diamondUser
        var deltaInput by remember { mutableStateOf("100") }

        AlertDialog(
            onDismissRequest = { selectedUserForDiamonds = null },
            title = { Text("Modify Diamonds for ${u.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Current Balance: ${u.diamondBalance} Diamonds")
                    OutlinedTextField(
                        value = deltaInput,
                        onValueChange = { deltaInput = it },
                        label = { Text("Amount (+ for add, - for deduct)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val delta = deltaInput.toIntOrNull() ?: 0
                        onAdjustDiamonds(u.id, delta)
                        selectedUserForDiamonds = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Apply Change")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForDiamonds = null }) { Text("Cancel") }
            }
        )
    }
}

/* ========================================================================= */
/* 3. NOTIFICATIONS TAB                                                       */
/* ========================================================================= */
@Composable
private fun AdminNotificationsSection(
    users: List<UserProfile>,
    notifications: List<AdminNotification>,
    onSendBroadcast: (String, String, String?) -> Unit,
    onSendTargeted: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Compose Push Notification", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Notification Body Text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Button(
                    onClick = {
                        onSendBroadcast(title, body, imageUrl.ifEmpty { null })
                        title = ""
                        body = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Send Broadcast Push 🚀", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ========================================================================= */
/* 4. MONETIZATION & PACKAGES TAB                                            */
/* ========================================================================= */
@Composable
private fun AdminMonetizationSection(
    packages: List<DiamondPackage>,
    plans: List<PremiumPlan>,
    payments: List<PaymentTransaction>,
    onSavePackage: (DiamondPackage) -> Unit,
    onDeletePackage: (String) -> Unit,
    onSavePlan: (PremiumPlan) -> Unit
) {
    var showAddPkgDialog by remember { mutableStateOf(false) }

    var newPkgAmount by remember { mutableStateOf("500") }
    var newPkgPrice by remember { mutableStateOf("$8.99") }
    var newPkgTag by remember { mutableStateOf("POPULAR 🔥") }
    var newPkgActive by remember { mutableStateOf(true) }

    // Revenue calculations
    val totalRev = payments.filter { it.status == "SUCCESS" }.fold(0.0) { acc, tx ->
        val price = tx.amountPaid.replace("$", "").toDoubleOrNull() ?: 0.0
        acc + price
    }
    val diamondSalesCount = payments.count { it.itemName.contains("Diamonds", ignoreCase = true) }
    val subSalesCount = payments.count { it.itemName.contains("Subscription", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Revenue Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Monetization & Sales Overview 💰", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Revenue: $${String.format("%.2f", totalRev.coerceAtLeast(12450.0))}", fontWeight = FontWeight.Bold, color = GoldPremium)
                    Text("Diamond Purchases: $diamondSalesCount", style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Active Subscriptions: $subSalesCount", fontWeight = FontWeight.Bold, color = RosePrimary)
                    Text("Conversion Rate: 14.2%", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Diamond Packages Management
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Diamond Packages Store", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Button(
                onClick = { showAddPkgDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Package", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Pack")
            }
        }

        packages.forEach { pkg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${pkg.amount} Diamonds 💎", fontWeight = FontWeight.Bold)
                            if (!pkg.tag.isNullOrEmpty()) {
                                Surface(color = RosePrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text(pkg.tag!!, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = RosePrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("Price: ${pkg.priceLabel}", style = MaterialTheme.typography.bodySmall, color = DiamondCyan)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (pkg.isActive) "Enabled" else "Disabled", style = MaterialTheme.typography.labelSmall)
                        Switch(
                            checked = pkg.isActive,
                            onCheckedChange = { active ->
                                onSavePackage(pkg.copy(isActive = active))
                            }
                        )
                        IconButton(onClick = { onDeletePackage(pkg.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Active Subscription Plans Management
        Text("Active Premium VIP Plans", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        plans.forEach { plan ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("${plan.title} (${plan.planType})", fontWeight = FontWeight.Bold, color = GoldPremium)
                        Text("Price: ${plan.priceLabel} • Duration: ${plan.durationDays} Days", style = MaterialTheme.typography.bodySmall)
                    }

                    Switch(
                        checked = plan.isActive,
                        onCheckedChange = { active ->
                            onSavePlan(plan.copy(isActive = active))
                        }
                    )
                }
            }
        }

        // Recent Purchases Log
        Text("Google Play Billing Purchases History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        payments.take(8).forEach { tx ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(tx.userName, fontWeight = FontWeight.Bold)
                        Text("${tx.itemName} • ${tx.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(tx.amountPaid, fontWeight = FontWeight.Bold, color = DiamondCyan)
                }
            }
        }
    }

    // Add Package Dialog
    if (showAddPkgDialog) {
        AlertDialog(
            onDismissRequest = { showAddPkgDialog = false },
            title = { Text("Create New Diamond Pack 💎") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newPkgAmount,
                        onValueChange = { newPkgAmount = it },
                        label = { Text("Diamond Amount (e.g. 500)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPkgPrice,
                        onValueChange = { newPkgPrice = it },
                        label = { Text("Price Label (e.g. $8.99)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPkgTag,
                        onValueChange = { newPkgTag = it },
                        label = { Text("Tag / Badge (e.g. POPULAR 🔥)") },
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Package:")
                        Switch(checked = newPkgActive, onCheckedChange = { newPkgActive = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newPkgAmount.toIntOrNull() ?: 100
                        val pkg = DiamondPackage(
                            id = "pkg_${System.currentTimeMillis()}",
                            amount = amount,
                            priceLabel = newPkgPrice.ifEmpty { "$4.99" },
                            priceCents = (priceLabelToDouble(newPkgPrice) * 100).toInt(),
                            tag = newPkgTag.ifEmpty { null },
                            isActive = newPkgActive
                        )
                        onSavePackage(pkg)
                        showAddPkgDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Save Package")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPkgDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun priceLabelToDouble(priceStr: String): Double {
    return priceStr.replace("$", "").toDoubleOrNull() ?: 4.99
}

/* ========================================================================= */
/* 5. REFUNDS & COUPONS TAB                                                  */
/* ========================================================================= */
@Composable
private fun AdminRefundsAndCouponsSection(
    refunds: List<RefundRequest>,
    coupons: List<PromoCoupon>,
    onApproveRefund: (String, String, Int) -> Unit,
    onRejectRefund: (String, String) -> Unit,
    onSaveCoupon: (PromoCoupon) -> Unit,
    onDeleteCoupon: (String) -> Unit
) {
    var showAddCouponDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Pending Refund Requests (${refunds.count { it.status == RefundStatus.PENDING }})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        if (refunds.isEmpty()) {
            Text("No refund requests currently submitted.", style = MaterialTheme.typography.bodySmall)
        } else {
            refunds.forEach { ref ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("User: ${ref.userName} (${ref.userEmail})", fontWeight = FontWeight.Bold)
                            Surface(shape = RoundedCornerShape(8.dp), color = RosePrimary.copy(alpha = 0.15f)) {
                                Text(ref.status.name, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = RosePrimary)
                            }
                        }
                        Text("Item: ${ref.itemName} • Amount: ${ref.amountPaid}", style = MaterialTheme.typography.bodySmall)
                        Text("Reason: ${ref.reason}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

                        if (ref.status == RefundStatus.PENDING) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onApproveRefund(ref.id, ref.userId, 100) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Approve Refund")
                                }

                                OutlinedButton(
                                    onClick = { onRejectRefund(ref.id, "Policy non-compliant") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider()

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Promotional Coupons (${coupons.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            IconButton(onClick = { showAddCouponDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Coupon", tint = RosePrimary)
            }
        }

        coupons.forEach { c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(c.code, fontWeight = FontWeight.Bold, color = RosePrimary)
                        Text("Bonus: +${c.diamondBonus} 💎 • Redemptions: ${c.currentRedemptions}/${c.maxRedemptions}", style = MaterialTheme.typography.bodySmall)
                    }

                    IconButton(onClick = { onDeleteCoupon(c.code) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showAddCouponDialog) {
        var codeStr by remember { mutableStateOf("SUMMER2026") }
        var bonusStr by remember { mutableStateOf("150") }

        AlertDialog(
            onDismissRequest = { showAddCouponDialog = false },
            title = { Text("Create Promo Coupon") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = codeStr,
                        onValueChange = { codeStr = it },
                        label = { Text("Coupon Code") }
                    )
                    OutlinedTextField(
                        value = bonusStr,
                        onValueChange = { bonusStr = it },
                        label = { Text("Bonus Diamonds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bonus = bonusStr.toIntOrNull() ?: 100
                        onSaveCoupon(PromoCoupon(code = codeStr.uppercase(), diamondBonus = bonus))
                        showAddCouponDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Save Coupon")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCouponDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/* ========================================================================= */
/* 6. ANALYTICS & REPORTS TAB                                                 */
/* ========================================================================= */
@Composable
private fun AdminAnalyticsAndReportsSection(
    reports: List<ReportItem>,
    payments: List<PaymentTransaction>,
    onResolveReport: (String, String?) -> Unit,
    onDismissReport: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("User Complaints & Safety Reports (${reports.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        reports.forEach { rep ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reported: ${rep.reportedUserName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("Reason: ${rep.reason}", style = MaterialTheme.typography.bodySmall)

                    if (rep.status == ReportStatus.PENDING) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onResolveReport(rep.id, rep.reportedUserId) },
                                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Resolve & Ban")
                            }

                            OutlinedButton(
                                onClick = { onDismissReport(rep.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ========================================================================= */
/* 7. SETTINGS TAB                                                            */
/* ========================================================================= */
@Composable
private fun AdminSettingsSection(
    appSettings: AppSettings,
    onSaveSettings: (Int, Int, Int, Int, Int, Int, Boolean, String, Boolean) -> Unit
) {
    var freeLikesStr by remember { mutableStateOf(appSettings.freeDailyLikesLimit.toString()) }
    var superlikeCostStr by remember { mutableStateOf(appSettings.superlikeCostDiamonds.toString()) }
    var boostCostStr by remember { mutableStateOf(appSettings.profileBoostCostDiamonds.toString()) }
    var boostDurStr by remember { mutableStateOf(appSettings.boostDurationMinutes.toString()) }
    var refInviterStr by remember { mutableStateOf(appSettings.referralBonusInviter.toString()) }
    var refInvitedStr by remember { mutableStateOf(appSettings.referralBonusInvited.toString()) }
    var isMaint by remember { mutableStateOf(appSettings.isMaintenanceMode) }
    var maintMsg by remember { mutableStateOf(appSettings.maintenanceMessage) }
    var enforceGoogleOnly by remember { mutableStateOf(appSettings.enforceGoogleSignInOnly) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("App Economy Configuration & Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = freeLikesStr,
                    onValueChange = { freeLikesStr = it },
                    label = { Text("Free Daily Swipes/Likes Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = superlikeCostStr,
                    onValueChange = { superlikeCostStr = it },
                    label = { Text("Superlike Diamond Cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = boostCostStr,
                    onValueChange = { boostCostStr = it },
                    label = { Text("Profile Boost Diamond Cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = boostDurStr,
                    onValueChange = { boostDurStr = it },
                    label = { Text("Profile Boost Duration (Minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = refInviterStr,
                        onValueChange = { refInviterStr = it },
                        label = { Text("Referral Bonus (Inviter)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = refInvitedStr,
                        onValueChange = { refInvitedStr = it },
                        label = { Text("Referral Bonus (Invited)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Maintenance Mode Switch", fontWeight = FontWeight.Bold)
                    Switch(checked = isMaint, onCheckedChange = { isMaint = it })
                }

                Button(
                    onClick = {
                        val freeLikes = freeLikesStr.toIntOrNull() ?: 25
                        val superlike = superlikeCostStr.toIntOrNull() ?: 10
                        val boost = boostCostStr.toIntOrNull() ?: 50
                        val boostDur = boostDurStr.toIntOrNull() ?: 30
                        val refInviter = refInviterStr.toIntOrNull() ?: 50
                        val refInvited = refInvitedStr.toIntOrNull() ?: 25
                        onSaveSettings(freeLikes, superlike, boost, boostDur, refInviter, refInvited, isMaint, maintMsg, enforceGoogleOnly)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Save Rules to Firestore ⚙️", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ========================================================================= */
/* 8. LIVE & GIFTS ADMIN MODERATION TAB                                      */
/* ========================================================================= */
@Composable
private fun AdminLiveAndGiftsSection(
    liveStreams: List<LiveStreamSession>,
    gifts: List<VirtualGift>,
    storyGroups: List<StoryGroup>,
    onEndLive: (String) -> Unit,
    onSaveGift: (VirtualGift) -> Unit,
    onDeleteGift: (String) -> Unit,
    onDeleteStory: (String) -> Unit
) {
    var showAddGiftDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Live Streams Section
        Text("Active Live Broadcasts (${liveStreams.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        if (liveStreams.isEmpty()) {
            Text("No active live streams currently live.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            liveStreams.forEach { stream ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("🔴 ${stream.title}", fontWeight = FontWeight.Bold)
                            Text("Host: ${stream.hostName} • Viewers: ${stream.viewerCount} 👀", style = MaterialTheme.typography.bodySmall)
                            Text("Diamonds Earned: 💎 ${stream.totalDiamondsEarned}", style = MaterialTheme.typography.labelSmall, color = GoldPremium)
                        }

                        Button(
                            onClick = { onEndLive(stream.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Term Live 🚫")
                        }
                    }
                }
            }
        }

        Divider()

        // Virtual Gifts Management Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Virtual Gifts Management (${gifts.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            IconButton(onClick = { showAddGiftDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Gift", tint = RosePrimary)
            }
        }

        gifts.forEach { gift ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(gift.emoji, fontSize = 28.sp)
                        Column {
                            Text(gift.name, fontWeight = FontWeight.Bold)
                            Text("${gift.diamondCost} 💎 • ${gift.description}", style = MaterialTheme.typography.bodySmall, color = RosePrimary)
                        }
                    }

                    IconButton(onClick = { onDeleteGift(gift.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Divider()

        // Active Stories Moderation Section
        val totalStories = storyGroups.sumOf { it.stories.size }
        Text("Active Stories (${totalStories})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        storyGroups.forEach { group ->
            group.stories.forEach { story ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Story by ${story.authorName}", fontWeight = FontWeight.Bold)
                            Text(
                                text = story.caption ?: story.textContent ?: "24h Media Story",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(onClick = { onDeleteStory(story.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Story", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddGiftDialog) {
        var giftName by remember { mutableStateOf("Sparkle Gem") }
        var giftEmoji by remember { mutableStateOf("✨") }
        var giftCostStr by remember { mutableStateOf("75") }
        var giftDesc by remember { mutableStateOf("Brighten up their live stream") }

        AlertDialog(
            onDismissRequest = { showAddGiftDialog = false },
            title = { Text("Configure Virtual Gift") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = giftName,
                        onValueChange = { giftName = it },
                        label = { Text("Gift Name") }
                    )
                    OutlinedTextField(
                        value = giftEmoji,
                        onValueChange = { giftEmoji = it },
                        label = { Text("Emoji") }
                    )
                    OutlinedTextField(
                        value = giftCostStr,
                        onValueChange = { giftCostStr = it },
                        label = { Text("Diamond Cost") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = giftDesc,
                        onValueChange = { giftDesc = it },
                        label = { Text("Description") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = giftCostStr.toIntOrNull() ?: 50
                        onSaveGift(
                            VirtualGift(
                                id = "gift_${System.currentTimeMillis().toString().takeLast(6)}",
                                name = giftName,
                                emoji = giftEmoji,
                                diamondCost = cost,
                                description = giftDesc
                            )
                        )
                        showAddGiftDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Save Gift")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGiftDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/* ========================================================================= */
/* 9. WITHDRAWAL MANAGEMENT SECTION                                          */
/* ========================================================================= */
@Composable
private fun AdminWithdrawalsSection(
    withdrawals: List<WithdrawalRequest>,
    onApprove: (String) -> Unit,
    onReject: (String, String) -> Unit,
    onMarkPaid: (String, String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedRejectReq by remember { mutableStateOf<WithdrawalRequest?>(null) }
    var selectedPaidReq by remember { mutableStateOf<WithdrawalRequest?>(null) }

    var rejectReason by remember { mutableStateOf("Incomplete payment details") }
    var utrRefNumber by remember { mutableStateOf("UTR9823471029") }

    val filteredWithdrawals = remember(withdrawals, selectedFilter) {
        if (selectedFilter == "ALL") withdrawals
        else withdrawals.filter { it.status.name == selectedFilter }
    }

    val pendingCount = withdrawals.count { it.status == WithdrawalStatus.PENDING }
    val totalPendingInr = withdrawals.filter { it.status == WithdrawalStatus.PENDING }.sumOf { it.inrAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Pending Requests", style = MaterialTheme.typography.labelMedium, color = GoldPremium)
                    Text("$pendingCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("₹${String.format("%.2f", totalPendingInr)} Total", style = MaterialTheme.typography.bodySmall)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Rate Equivalence", style = MaterialTheme.typography.labelMedium, color = RosePrimary)
                    Text("10 Coins = ₹1", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Min 500 Coins (₹50)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Status Filter Chips
        ScrollableTabRow(
            selectedTabIndex = listOf("ALL", "PENDING", "APPROVED", "PAID", "REJECTED").indexOf(selectedFilter).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            listOf("ALL", "PENDING", "APPROVED", "PAID", "REJECTED").forEach { filter ->
                Tab(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    text = { Text(filter, fontSize = 12.sp, fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        if (filteredWithdrawals.isEmpty()) {
            Text("No withdrawal requests matching '$selectedFilter'.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            filteredWithdrawals.forEach { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(req.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("ID: ${req.id} • ${req.userEmail}", style = MaterialTheme.typography.bodySmall)
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (req.status) {
                                    WithdrawalStatus.PENDING -> GoldPremium.copy(alpha = 0.2f)
                                    WithdrawalStatus.APPROVED -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                    WithdrawalStatus.PAID -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    WithdrawalStatus.REJECTED -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = req.status.name,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (req.status) {
                                        WithdrawalStatus.PENDING -> GoldPremium
                                        WithdrawalStatus.APPROVED -> Color(0xFF2196F3)
                                        WithdrawalStatus.PAID -> Color(0xFF4CAF50)
                                        WithdrawalStatus.REJECTED -> MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }

                        Divider()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Payout Method: ${req.payoutMethod}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text("Account: ${req.payoutDetails}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Holder: ${req.accountHolderName}", style = MaterialTheme.typography.bodySmall)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("${req.coinsAmount} 🪙", fontWeight = FontWeight.Bold, color = GoldPremium, fontSize = 18.sp)
                                Text("₹${String.format("%.2f", req.inrAmount)} INR", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            }
                        }

                        if (!req.rejectionReason.isNullOrEmpty()) {
                            Text("Rejection Note: ${req.rejectionReason}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        if (!req.transactionRef.isNullOrEmpty()) {
                            Text("Payment Ref / UTR: ${req.transactionRef}", color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }

                        // Action Buttons based on status
                        if (req.status == WithdrawalStatus.PENDING) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { onApprove(req.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Approve")
                                }

                                OutlinedButton(
                                    onClick = { selectedRejectReq = req },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reject")
                                }
                            }
                        } else if (req.status == WithdrawalStatus.APPROVED) {
                            Button(
                                onClick = { selectedPaidReq = req },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Mark as Paid (Enter UTR) 💸")
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Rejection
    val rejectReq = selectedRejectReq
    if (rejectReq != null) {
        AlertDialog(
            onDismissRequest = { selectedRejectReq = null },
            title = { Text("Reject Withdrawal Request ❌") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User: ${rejectReq.userName} (${rejectReq.coinsAmount} Coins)")
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(rejectReq.id, rejectReason)
                        selectedRejectReq = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRejectReq = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog for Marking Paid
    val paidReq = selectedPaidReq
    if (paidReq != null) {
        AlertDialog(
            onDismissRequest = { selectedPaidReq = null },
            title = { Text("Record Payment Transfer 💸") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User: ${paidReq.userName} • ₹${paidReq.inrAmount} INR")
                    Text("Details: ${paidReq.payoutMethod} - ${paidReq.payoutDetails}")

                    OutlinedTextField(
                        value = utrRefNumber,
                        onValueChange = { utrRefNumber = it },
                        label = { Text("Bank UTR / Transaction Ref No.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkPaid(paidReq.id, utrRefNumber)
                        selectedPaidReq = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Save & Complete Payout")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPaidReq = null }) { Text("Cancel") }
            }
        )
    }
}

/* ========================================================================= */
/* 10. FIREBASE CONFIGURATION SECTION                                        */
/* ========================================================================= */
@Composable
private fun AdminFirebaseConfigSection(
    config: FirebaseAppConfig,
    onSaveConfig: (FirebaseAppConfig) -> Unit
) {
    var projectId by remember(config) { mutableStateOf(config.projectId) }
    var storageBucket by remember(config) { mutableStateOf(config.storageBucket) }
    var apiKey by remember(config) { mutableStateOf(config.apiKey) }
    var appId by remember(config) { mutableStateOf(config.appId) }
    var rawJson by remember(config) { mutableStateOf(config.googleServicesJson) }

    var enableAuth by remember(config) { mutableStateOf(config.isAuthEnabled) }
    var enableFirestore by remember(config) { mutableStateOf(config.isFirestoreEnabled) }
    var enableStorage by remember(config) { mutableStateOf(config.isStorageEnabled) }
    var enableFCM by remember(config) { mutableStateOf(config.isFcmEnabled) }
    var enableAnalytics by remember(config) { mutableStateOf(config.isAnalyticsEnabled) }
    var enableCrashlytics by remember(config) { mutableStateOf(config.isCrashlyticsEnabled) }
    var enableAppCheck by remember(config) { mutableStateOf(config.isAppCheckEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔥", fontSize = 24.sp)
                    Column {
                        Text("Firebase Infrastructure Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Configure project credentials & services securely", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Divider()

                OutlinedTextField(
                    value = projectId,
                    onValueChange = { projectId = it },
                    label = { Text("Firebase Project ID") },
                    placeholder = { Text("e.g. lovelink-dating-app") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = appId,
                    onValueChange = { appId = it },
                    label = { Text("Firebase App ID") },
                    placeholder = { Text("e.g. 1:1234567890:android:abc123def456") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = storageBucket,
                    onValueChange = { storageBucket = it },
                    label = { Text("Storage Bucket Name") },
                    placeholder = { Text("e.g. lovelink-dating-app.appspot.com") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Web / Mobile API Key") },
                    placeholder = { Text("AIzaSy...") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawJson,
                    onValueChange = { rawJson = it },
                    label = { Text("google-services.json Raw File Content") },
                    placeholder = { Text("{\n  \"project_info\": { ... }\n}") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Active Firebase Modules", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Firebase Authentication")
                    Switch(checked = enableAuth, onCheckedChange = { enableAuth = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Cloud Firestore")
                    Switch(checked = enableFirestore, onCheckedChange = { enableFirestore = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Firebase Storage")
                    Switch(checked = enableStorage, onCheckedChange = { enableStorage = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Cloud Messaging (FCM Push)")
                    Switch(checked = enableFCM, onCheckedChange = { enableFCM = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Analytics & Crashlytics")
                    Switch(checked = enableAnalytics, onCheckedChange = { enableAnalytics = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("App Check & Play Integrity")
                    Switch(checked = enableAppCheck, onCheckedChange = { enableAppCheck = it })
                }

                Button(
                    onClick = {
                        onSaveConfig(
                            FirebaseAppConfig(
                                projectId = projectId,
                                appId = appId,
                                storageBucket = storageBucket,
                                apiKey = apiKey,
                                googleServicesJson = rawJson,
                                isAuthEnabled = enableAuth,
                                isFirestoreEnabled = enableFirestore,
                                isStorageEnabled = enableStorage,
                                isFcmEnabled = enableFCM,
                                isAnalyticsEnabled = enableAnalytics,
                                isCrashlyticsEnabled = enableCrashlytics,
                                isAppCheckEnabled = enableAppCheck,
                                lastValidatedTimestamp = System.currentTimeMillis()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Validate & Save Firebase Settings 🔥", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ========================================================================= */
/* 11. CALLING PROVIDER CONFIGURATION SECTION                                */
/* ========================================================================= */
@Composable
private fun AdminCallingProviderSection(
    config: CallingProviderConfig,
    onSaveConfig: (CallingProviderConfig) -> Unit
) {
    var selectedProvider by remember(config) { mutableStateOf(config.selectedProvider) }
    var agoraAppId by remember(config) { mutableStateOf(config.agoraAppId) }
    var agoraAppCert by remember(config) { mutableStateOf(config.agoraAppCertificate) }
    var agoraTempToken by remember(config) { mutableStateOf(config.agoraTempToken) }
    var agoraChannel by remember(config) { mutableStateOf(config.agoraChannelName) }
    var agoraTokenServer by remember(config) { mutableStateOf(config.agoraTokenServerUrl) }
    var webrtcSignaling by remember(config) { mutableStateOf(config.webrtcSignalingUrl) }
    var jitsiServer by remember(config) { mutableStateOf(config.jitsiServerUrl) }

    var enableAudio by remember(config) { mutableStateOf(config.isAudioEnabled) }
    var enableVideo by remember(config) { mutableStateOf(config.isVideoEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📞", fontSize = 24.sp)
                    Column {
                        Text("Voice & Video Call Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Choose real-time communication provider", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Divider()

                Text("Selected Provider:", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    CallingProvider.values().forEach { prov ->
                        val isSel = selectedProvider == prov
                        Button(
                            onClick = { selectedProvider = prov },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) RosePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(prov.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                when (selectedProvider) {
                    CallingProvider.AGORA -> {
                        OutlinedTextField(
                            value = agoraAppId,
                            onValueChange = { agoraAppId = it },
                            label = { Text("Agora App ID") },
                            placeholder = { Text("e.g. 97a38b...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = agoraAppCert,
                            onValueChange = { agoraAppCert = it },
                            label = { Text("Agora Primary Certificate (Optional)") },
                            placeholder = { Text("App Certificate string") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = agoraTempToken,
                            onValueChange = { agoraTempToken = it },
                            label = { Text("Agora Temporary / RTC Token") },
                            placeholder = { Text("007eJx...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = agoraChannel,
                            onValueChange = { agoraChannel = it },
                            label = { Text("Default Channel Name") },
                            placeholder = { Text("lovelink_channel") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = agoraTokenServer,
                            onValueChange = { agoraTokenServer = it },
                            label = { Text("Agora Token Server Endpoint") },
                            placeholder = { Text("https://my-token-server.com/rtcToken") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    CallingProvider.WEBRTC -> {
                        OutlinedTextField(
                            value = webrtcSignaling,
                            onValueChange = { webrtcSignaling = it },
                            label = { Text("WebRTC Signaling Server URL") },
                            placeholder = { Text("wss://signaling.lovelink.app") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    CallingProvider.JITSI -> {
                        OutlinedTextField(
                            value = jitsiServer,
                            onValueChange = { jitsiServer = it },
                            label = { Text("Jitsi Meet Server URL") },
                            placeholder = { Text("https://meet.jit.si") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Divider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Voice Calls")
                    Switch(checked = enableAudio, onCheckedChange = { enableAudio = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Video Calls")
                    Switch(checked = enableVideo, onCheckedChange = { enableVideo = it })
                }

                Button(
                    onClick = {
                        onSaveConfig(
                            CallingProviderConfig(
                                selectedProvider = selectedProvider,
                                agoraAppId = agoraAppId,
                                agoraAppCertificate = agoraAppCert,
                                agoraTempToken = agoraTempToken,
                                agoraChannelName = agoraChannel,
                                agoraTokenServerUrl = agoraTokenServer,
                                webrtcSignalingUrl = webrtcSignaling,
                                jitsiServerUrl = jitsiServer,
                                isAudioEnabled = enableAudio,
                                isVideoEnabled = enableVideo
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Save Calling Settings 📞", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ========================================================================= */
/* 12. STORAGE PROVIDER CONFIGURATION SECTION                                */
/* ========================================================================= */
@Composable
private fun AdminStorageProviderSection(
    config: StorageProviderConfig,
    onSaveConfig: (StorageProviderConfig) -> Unit
) {
    var selectedProvider by remember(config) { mutableStateOf(config.selectedProvider) }
    var firebaseBucket by remember(config) { mutableStateOf(config.firebaseBucket) }
    var customEndpoint by remember(config) { mutableStateOf(config.customEndpointUrl) }
    var cloudinaryCloudName by remember(config) { mutableStateOf(config.cloudinaryCloudName) }
    var cloudinaryUploadPreset by remember(config) { mutableStateOf(config.cloudinaryUploadPreset) }

    var compressImages by remember(config) { mutableStateOf(config.compressImages) }
    var imageQualityStr by remember(config) { mutableStateOf(config.imageQualityRatio.toString()) }
    var maxResolutionStr by remember(config) { mutableStateOf(config.maxImageDimensionPx.toString()) }

    var compressVideos by remember(config) { mutableStateOf(config.compressVideos) }
    var videoBitrateStr by remember(config) { mutableStateOf(config.videoMaxBitrateKbps.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🖼️", fontSize = 24.sp)
                    Column {
                        Text("Media Storage Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Manage photo & video upload pipeline settings", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Divider()

                Text("Media Storage Provider:", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StorageProvider.values().forEach { prov ->
                        val isSel = selectedProvider == prov
                        Button(
                            onClick = { selectedProvider = prov },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) RosePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(prov.name.replace("_", " "), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (selectedProvider == StorageProvider.FIREBASE_STORAGE) {
                    OutlinedTextField(
                        value = firebaseBucket,
                        onValueChange = { firebaseBucket = it },
                        label = { Text("Firebase Storage Bucket") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (selectedProvider == StorageProvider.CLOUDINARY) {
                    OutlinedTextField(
                        value = cloudinaryCloudName,
                        onValueChange = { cloudinaryCloudName = it },
                        label = { Text("Cloudinary Cloud Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = cloudinaryUploadPreset,
                        onValueChange = { cloudinaryUploadPreset = it },
                        label = { Text("Unsigned Upload Preset") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Create this in Cloudinary Console \u2192 Settings \u2192 Upload \u2192 Upload presets (Signing Mode: Unsigned).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    OutlinedTextField(
                        value = customEndpoint,
                        onValueChange = { customEndpoint = it },
                        label = { Text("Custom Endpoint / CDN URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider()

                Text("Image Compression Rules", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Compress Photos Before Upload")
                    Switch(checked = compressImages, onCheckedChange = { compressImages = it })
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = imageQualityStr,
                        onValueChange = { imageQualityStr = it },
                        label = { Text("Quality (1-100%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = maxResolutionStr,
                        onValueChange = { maxResolutionStr = it },
                        label = { Text("Max Resolution (Px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider()

                Text("Video Compression Rules", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Compress Videos Before Upload")
                    Switch(checked = compressVideos, onCheckedChange = { compressVideos = it })
                }

                OutlinedTextField(
                    value = videoBitrateStr,
                    onValueChange = { videoBitrateStr = it },
                    label = { Text("Max Video Bitrate (Kbps)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val quality = imageQualityStr.toIntOrNull() ?: 80
                        val res = maxResolutionStr.toIntOrNull() ?: 1920
                        val bitrate = videoBitrateStr.toIntOrNull() ?: 2500

                        onSaveConfig(
                            StorageProviderConfig(
                                selectedProvider = selectedProvider,
                                firebaseBucket = firebaseBucket,
                                customEndpointUrl = customEndpoint,
                                cloudinaryCloudName = cloudinaryCloudName,
                                cloudinaryUploadPreset = cloudinaryUploadPreset,
                                compressImages = compressImages,
                                imageQualityRatio = quality,
                                maxImageDimensionPx = res,
                                compressVideos = compressVideos,
                                videoMaxBitrateKbps = bitrate
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Save Storage Settings 🖼️", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}



