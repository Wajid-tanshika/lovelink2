package com.example.ui.screens.diamonds

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary
import com.example.ui.viewmodel.DiamondViewModel
import com.example.util.PreventScreenshotProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiamondStoreScreen(
    diamondViewModel: DiamondViewModel,
    currentUser: UserProfile,
    onBackClick: () -> Unit
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val balance by diamondViewModel.balance.collectAsState()
    val coinBalance by diamondViewModel.coinBalance.collectAsState()
    val earnTasks by diamondViewModel.earnTasks.collectAsState()
    val myWithdrawalRequests by diamondViewModel.userWithdrawals.collectAsState()
    val isPremium by diamondViewModel.isPremium.collectAsState()
    val dailyStreakDay by diamondViewModel.dailyStreakDay.collectAsState()
    val myReferralCode by diamondViewModel.myReferralCode.collectAsState()
    val referralsCount by diamondViewModel.referralsCount.collectAsState()
    val transactions by diamondViewModel.transactions.collectAsState()
    val snackMessage by diamondViewModel.snackMessage.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf("Store") }
    var couponInput by remember { mutableStateOf("") }
    var referralInput by remember { mutableStateOf("") }

    var selectedTxForRefund by remember { mutableStateOf<DiamondTransaction?>(null) }
    var refundReasonInput by remember { mutableStateOf("") }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            diamondViewModel.clearSnackMessage()
        }
    }

    val diamondPackagesList = listOf(
        DiamondPackage("pkg_100", 100, "$1.99", 199, null, true),
        DiamondPackage("pkg_250", 250, "$4.99", 499, "+10% Bonus", true),
        DiamondPackage("pkg_500", 500, "$8.99", 899, "POPULAR 🔥", true),
        DiamondPackage("pkg_1000", 1000, "$15.99", 1599, "+25% Bonus", true),
        DiamondPackage("pkg_2500", 2500, "$34.99", 3499, "BEST VALUE 💎", true),
        DiamondPackage("pkg_5000", 5000, "$59.99", 5999, "VIP CHOICE 👑", true),
        DiamondPackage("pkg_10000", 10000, "$99.99", 9999, "ULTRA VALUE 🚀", true)
    )

    val storeTabs = listOf("Store", "Earn Coins 🪙", "Withdraw 💸", "Daily Rewards", "Virtual Gifts", "Transactions")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet & Monetization", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(end = 12.dp)) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DiamondCyan.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Diamond, contentDescription = null, tint = DiamondCyan, modifier = Modifier.size(16.dp))
                                Text("$balance 💎", fontWeight = FontWeight.Bold, color = DiamondCyan, fontSize = 13.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GoldPremium.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🪙", fontSize = 13.sp)
                                Text("$coinBalance", fontWeight = FontWeight.Bold, color = GoldPremium, fontSize = 13.sp)
                            }
                        }
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
            // Horizontal Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = storeTabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                storeTabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    "Store" -> DiamondStoreMainSection(
                        balance = balance,
                        isPremium = isPremium,
                        packages = diamondPackagesList,
                        couponInput = couponInput,
                        onCouponChange = { couponInput = it },
                        onRedeemCoupon = { diamondViewModel.redeemCoupon(couponInput) },
                        onBuyPackage = { pkg ->
                            diamondViewModel.buyPackageWithPlayBilling(
                                currentUser.id,
                                currentUser.name,
                                currentUser.email,
                                pkg
                            )
                        },
                        onActivateBoost = { diamondViewModel.activateProfileBoost(50, 30) }
                    )

                    "Earn Coins 🪙" -> EarnCoinsSection(
                        coinBalance = coinBalance,
                        earnTasks = earnTasks,
                        onWatchAd = { diamondViewModel.watchRewardedAdAndClaimCoins() },
                        onCompleteTask = { taskId -> diamondViewModel.claimTaskCoins(taskId) }
                    )

                    "Withdraw 💸" -> WithdrawCoinsSection(
                        coinBalance = coinBalance,
                        withdrawalRequests = myWithdrawalRequests,
                        onSubmitRequest = { coins, method, details, holder ->
                            diamondViewModel.submitWithdrawalRequest(
                                coins = coins,
                                payoutMethod = method,
                                payoutDetails = details,
                                accountHolderName = holder,
                                userId = currentUser.id,
                                userName = currentUser.name,
                                userEmail = currentUser.email,
                                userPhone = currentUser.phone
                            )
                        }
                    )

                    "Daily Rewards" -> DailyRewardsSection(
                        dailyStreakDay = dailyStreakDay,
                        myReferralCode = myReferralCode,
                        referralsCount = referralsCount,
                        referralInput = referralInput,
                        onReferralChange = { referralInput = it },
                        onClaimDaily = { amount -> diamondViewModel.claimDailyReward(amount) },
                        onRedeemReferral = { code -> diamondViewModel.redeemReferralCode(code, 25) },
                        onCopyCode = { code ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Referral Code", code)
                            clipboard.setPrimaryClip(clip)
                        }
                    )

                    "Virtual Gifts" -> VirtualGiftsStoreSection(
                        balance = balance,
                        onSendGiftPreview = { gift ->
                            diamondViewModel.sendVirtualGift("user_1", "Sophia Chen", gift)
                        }
                    )

                    "Transactions" -> TransactionsHistorySection(
                        transactions = transactions,
                        onRequestRefund = { tx ->
                            selectedTxForRefund = tx
                        }
                    )
                }
            }
        }
    }

    // Refund Dialog Modal
    val refundTx = selectedTxForRefund
    if (refundTx != null) {
        val tx = refundTx
        AlertDialog(
            onDismissRequest = { selectedTxForRefund = null },
            title = { Text("Request Purchase Refund 📑") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Item: ${tx.description}", fontWeight = FontWeight.Bold)
                    Text("Transaction ID: ${tx.id}", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = refundReasonInput,
                        onValueChange = { refundReasonInput = it },
                        label = { Text("Reason for Refund") },
                        placeholder = { Text("e.g. Accidental purchase, technical issue") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        diamondViewModel.submitRefundRequest(
                            tx.id,
                            tx.description,
                            "$${tx.amount / 50}.99",
                            refundReasonInput,
                            currentUser.id,
                            currentUser.name,
                            currentUser.email
                        )
                        selectedTxForRefund = null
                        refundReasonInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Submit Refund Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTxForRefund = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ========================================================================= */
/* 1. STORE MAIN TAB                                                          */
/* ========================================================================= */
@Composable
private fun DiamondStoreMainSection(
    balance: Int,
    isPremium: Boolean,
    packages: List<DiamondPackage>,
    couponInput: String,
    onCouponChange: (String) -> Unit,
    onRedeemCoupon: () -> Unit,
    onBuyPackage: (DiamondPackage) -> Unit,
    onActivateBoost: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(VioletSecondary, DiamondCyan)))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Wallet Balance", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Diamond, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Text("$balance", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black, fontSize = 38.sp), color = Color.White)
                    }

                    if (isPremium) {
                        Surface(shape = RoundedCornerShape(12.dp), color = GoldPremium) {
                            Text("✨ VIP PREMIUM ACTIVE", modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // Profile Boost Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = GoldPremium.copy(alpha = 0.12f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPremium)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = GoldPremium)
                        Text("30-Minute Profile Boost", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Be the top profile in your area & get 10x more likes!", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = onActivateBoost,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPremium),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("50 💎", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Redeem Coupon Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Redeem Promotional Coupon Code", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = couponInput,
                        onValueChange = onCouponChange,
                        placeholder = { Text("e.g. LOVE2026") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = onRedeemCoupon,
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }

        // Google Play Diamond Packages Grid
        Text("Google Play Diamond Packages", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            packages.forEach { pkg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBuyPackage(pkg) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(DiamondCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Diamond, contentDescription = null, tint = DiamondCyan, modifier = Modifier.size(24.dp))
                            }

                            Column {
                                Text("${pkg.amount} Diamonds", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                pkg.tag?.let { tagText ->
                                    Text(tagText, style = MaterialTheme.typography.labelSmall, color = RosePrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = { onBuyPackage(pkg) },
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(pkg.priceLabel, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ========================================================================= */
/* 2. DAILY REWARDS & REFERRALS TAB                                          */
/* ========================================================================= */
@Composable
private fun DailyRewardsSection(
    dailyStreakDay: Int,
    myReferralCode: String,
    referralsCount: Int,
    referralInput: String,
    onReferralChange: (String) -> Unit,
    onClaimDaily: (Int) -> Unit,
    onRedeemReferral: (String) -> Unit,
    onCopyCode: (String) -> Unit
) {
    val streakRewards = listOf(10, 20, 30, 50, 75, 100, 150)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Streak Rewards Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("7-Day Daily Login Streak 🎁", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Current Streak: Day $dailyStreakDay of 7", style = MaterialTheme.typography.bodySmall, color = RosePrimary)
                    }

                    Button(
                        onClick = { onClaimDaily(streakRewards[dailyStreakDay - 1]) },
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Claim Day $dailyStreakDay")
                    }
                }

                // 7 Days Visual Grid
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    streakRewards.forEachIndexed { index, reward ->
                        val dayNum = index + 1
                        val isCurrent = dayNum == dailyStreakDay
                        val isPassed = dayNum < dailyStreakDay

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) RosePrimary else if (isPassed) DiamondCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background
                            ),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, GoldPremium) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("D$dayNum", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface)
                                Text("+$reward", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isCurrent) GoldPremium else DiamondCyan)
                            }
                        }
                    }
                }
            }
        }

        // Invite & Referral Rewards Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Referral Rewards Program 👥", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Invite single friends to LoveLink! You get +50 💎 and your friend gets +25 💎 bonus.", style = MaterialTheme.typography.bodySmall)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Your Referral Code:", style = MaterialTheme.typography.labelSmall)
                        Text(myReferralCode, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RosePrimary)
                    }

                    OutlinedButton(onClick = { onCopyCode(myReferralCode) }, shape = RoundedCornerShape(10.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy")
                    }
                }

                Text("Total Friends Invited: $referralsCount", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

                Divider()

                Text("Enter a Friend's Referral Code:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = referralInput,
                        onValueChange = onReferralChange,
                        placeholder = { Text("e.g. LOVE-SOFIA") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = { onRedeemReferral(referralInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Claim +25 💎")
                    }
                }
            }
        }
    }
}

/* ========================================================================= */
/* 3. VIRTUAL GIFTS TAB                                                      */
/* ========================================================================= */
@Composable
private fun VirtualGiftsStoreSection(
    balance: Int,
    onSendGiftPreview: (VirtualGift) -> Unit
) {
    val gifts = VirtualGift.DEFAULT_GIFTS

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
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Send Virtual Gifts to Your Matches 🌹", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Gifts capture attention and increase reply rates by up to 300% in chat conversations!", style = MaterialTheme.typography.bodySmall)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            items(gifts) { gift ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(gift.emoji, fontSize = 42.sp)
                        Text(gift.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(gift.description, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 2)

                        Button(
                            onClick = { onSendGiftPreview(gift) },
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("${gift.diamondCost} 💎", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/* ========================================================================= */
/* 4. TRANSACTIONS TAB                                                       */
/* ========================================================================= */
@Composable
private fun TransactionsHistorySection(
    transactions: List<DiamondTransaction>,
    onRequestRefund: (DiamondTransaction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Transaction History & Refunds", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        if (transactions.isEmpty()) {
            Text("No transactions logged yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            transactions.forEach { tx ->
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (tx.amount > 0) Color(0xFF4CAF50).copy(alpha = 0.15f) else RosePrimary.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (tx.amount > 0) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                                    contentDescription = null,
                                    tint = if (tx.amount > 0) Color(0xFF4CAF50) else RosePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(tx.description, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Type: ${tx.type.name}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (tx.amount > 0) "+${tx.amount} 💎" else "${tx.amount} 💎",
                                fontWeight = FontWeight.Bold,
                                color = if (tx.amount > 0) Color(0xFF4CAF50) else RosePrimary
                            )

                            if (tx.type == TransactionType.PURCHASE) {
                                TextTextButton(onClick = { onRequestRefund(tx) }) {
                                    Text("Refund Request", fontSize = 11.sp, color = RosePrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextTextButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
        content()
    }
}

/* ========================================================================= */
/* 5. EARN COINS SECTION                                                     */
/* ========================================================================= */
@Composable
private fun EarnCoinsSection(
    coinBalance: Int,
    earnTasks: List<EarnTask>,
    onWatchAd: () -> Unit,
    onCompleteTask: (String) -> Unit
) {
    val estimatedInr = coinBalance / 10.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Coin Wallet Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFFFA000), GoldPremium)))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Monetization Wallet Balance", color = Color.Black.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🪙", fontSize = 36.sp)
                        Text("$coinBalance", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black, fontSize = 40.sp), color = Color.Black)
                    }

                    Surface(shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.15f)) {
                        Text("Equivalence: ₹${String.format("%.2f", estimatedInr)} INR (10 Coins = ₹1)", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        // Rewarded Video Ads Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPremium)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🎬", fontSize = 18.sp)
                        Text("Watch Rewarded Video Ad", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Watch a short 30s partner video & instantly receive +15 Coins!", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = onWatchAd,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPremium),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("+15 🪙", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // In-App Earn Tasks Checklist
        Text("In-App Activity Tasks", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        earnTasks.forEach { task ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 28.sp)
                        Column {
                            Text(task.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }

                    if (task.isCompleted) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                            Text("Done ✅", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = { onCompleteTask(task.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+${task.coinsReward} 🪙", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Policy Compliance Notice
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🛡️ Google Play Developer Policy Compliant", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text("Coins are utility reward units earned through verified app engagement. Coin withdrawals follow strict verification and user authentication.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }
        }
    }
}

/* ========================================================================= */
/* 6. WITHDRAW COINS SECTION                                                 */
/* ========================================================================= */
@Composable
private fun WithdrawCoinsSection(
    coinBalance: Int,
    withdrawalRequests: List<WithdrawalRequest>,
    onSubmitRequest: (Int, String, String, String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("UPI") }
    var payoutDetails by remember { mutableStateOf("user@upi") }
    var accountHolder by remember { mutableStateOf("Alex Rivers") }
    var coinsToWithdrawStr by remember { mutableStateOf("500") }

    val requestedCoins = coinsToWithdrawStr.toIntOrNull() ?: 500
    val requestedInr = requestedCoins / 10.0
    val canWithdraw = coinBalance >= 500 && requestedCoins >= 500 && requestedCoins <= coinBalance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Available Balance", style = MaterialTheme.typography.labelSmall)
                        Text("$coinBalance 🪙", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = GoldPremium)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Min Threshold", style = MaterialTheme.typography.labelSmall)
                        Text("500 🪙 (₹50)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RosePrimary)
                    }
                }
            }
        }

        // Withdrawal Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Request Money Withdrawal 💸", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                Text("1. Select Payout Method:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("UPI", "Paytm", "Bank Transfer").forEach { method ->
                        val isSel = selectedMethod == method
                        Button(
                            onClick = { selectedMethod = method },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) RosePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(method, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = accountHolder,
                    onValueChange = { accountHolder = it },
                    label = { Text("Account Holder Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = payoutDetails,
                    onValueChange = { payoutDetails = it },
                    label = { Text(if (selectedMethod == "UPI") "UPI ID (e.g. name@upi)" else if (selectedMethod == "Paytm") "Paytm Mobile Number" else "Bank Account No & IFSC") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = coinsToWithdrawStr,
                    onValueChange = { coinsToWithdrawStr = it },
                    label = { Text("Coins to Redeem (Min 500)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(shape = RoundedCornerShape(10.dp), color = GoldPremium.copy(alpha = 0.15f)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated Payout Amount:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("₹${String.format("%.2f", requestedInr)} INR", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4CAF50))
                    }
                }

                Button(
                    onClick = {
                        onSubmitRequest(requestedCoins, selectedMethod, payoutDetails, accountHolder)
                    },
                    enabled = canWithdraw && payoutDetails.isNotBlank() && accountHolder.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(if (canWithdraw) "Submit Withdrawal Request 🚀" else "Minimum 500 Coins Required", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Past Requests History
        Text("Your Withdrawal History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        if (withdrawalRequests.isEmpty()) {
            Text("No withdrawal requests submitted yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            withdrawalRequests.forEach { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Payout: ${req.payoutMethod} - ${req.payoutDetails}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("${req.coinsAmount} 🪙 • ₹${String.format("%.2f", req.inrAmount)} INR", style = MaterialTheme.typography.bodySmall, color = GoldPremium)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when (req.status) {
                                    WithdrawalStatus.PENDING -> GoldPremium.copy(alpha = 0.2f)
                                    WithdrawalStatus.APPROVED -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                    WithdrawalStatus.PAID -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    WithdrawalStatus.REJECTED -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = req.status.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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

                        if (!req.rejectionReason.isNullOrEmpty()) {
                            Text("Rejection Reason: ${req.rejectionReason}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        if (!req.transactionRef.isNullOrEmpty()) {
                            Text("Transaction UTR: ${req.transactionRef}", color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

