package com.example.ui.screens.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PremiumPlan
import com.example.data.model.UserProfile
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.VioletSecondary
import com.example.ui.viewmodel.DiamondViewModel
import com.example.util.PreventScreenshotProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    diamondViewModel: DiamondViewModel,
    currentUser: UserProfile,
    onBackClick: () -> Unit
) {
    // 🔒 Enforce Privacy Screenshot Protection
    PreventScreenshotProtection(enabled = true)

    val isPremium by diamondViewModel.isPremium.collectAsState()
    val snackMessage by diamondViewModel.snackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val plans = listOf(
        PremiumPlan("plan_weekly", "1 Week VIP", "$4.99/wk", "WEEKLY", 7, listOf("Unlimited Swipes", "See Who Liked You"), false, true),
        PremiumPlan("plan_monthly", "1 Month VIP", "$14.99/mo", "MONTHLY", 30, listOf("Unlimited Swipes", "See Who Liked You", "5 Free Daily Superlikes"), true, true),
        PremiumPlan("plan_yearly", "1 Year VIP", "$59.99/yr", "YEARLY", 365, listOf("Unlimited Swipes", "See Who Liked You", "5 Daily Superlikes", "1 Free Monthly Boost", "VIP Badge"), true, true)
    )

    var selectedPlan by remember { mutableStateOf(plans[1]) }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            diamondViewModel.clearSnackMessage()
        }
    }

    val features = listOf(
        "✔ Unlimited Chats",
        "✔ Unlimited Likes",
        "✔ Unlimited Matches",
        "✔ Unlimited Super Likes",
        "✔ Unlimited Profile Boost",
        "✔ Ad-Free Experience",
        "✔ Premium Badge",
        "✔ Read Receipts",
        "✔ See Who Liked You",
        "✔ Priority Profile Ranking",
        "✔ Unlimited Image Sharing",
        "✔ Unlimited Voice Messages"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LoveLink VIP Premium", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Banner
            Card(shape = RoundedCornerShape(24.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(RosePrimary, VioletSecondary)))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldPremium, modifier = Modifier.size(48.dp))
                        Text("Unlock LoveLink VIP", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("Maximize your dating matches by 500%", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))

                        if (isPremium) {
                            Surface(color = GoldPremium, shape = RoundedCornerShape(12.dp)) {
                                Text("✨ VIP MEMBERSHIP IS ACTIVE", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Features List
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    features.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(RosePrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(16.dp))
                            }
                            Text(text = feat, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Pricing Plans Selector
            Text("Select Subscription Plan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                plans.forEach { plan ->
                    val isSelected = selectedPlan.id == plan.id

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPlan = plan },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) RosePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, RosePrimary) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (plan.planType == "YEARLY") {
                                Surface(color = GoldPremium, shape = RoundedCornerShape(6.dp)) {
                                    Text("SAVE 65%", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }
                            }
                            Text(plan.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Text(plan.priceLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = RosePrimary)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    diamondViewModel.subscribePremiumWithPlayBilling(
                        currentUser.id,
                        currentUser.name,
                        currentUser.email,
                        selectedPlan
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text(
                    text = if (isPremium) "Extend Subscription (${selectedPlan.priceLabel})" else "Subscribe via Google Play Billing (${selectedPlan.priceLabel}) ✨",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = {
                    diamondViewModel.restorePurchases(currentUser.id, currentUser.email)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Restore Purchases 🔄",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
