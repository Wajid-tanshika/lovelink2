package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LoveLinkFullLogo
import com.example.ui.theme.RosePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    var activeDialogTitle by remember { mutableStateOf<String?>(null) }
    var activeDialogText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About LoveLink", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Official Logo & Version Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LoveLinkFullLogo(
                        iconSize = 88.dp,
                        showTagline = true
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RosePrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Version 2.5.0 (Build 2026.07)",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = RosePrimary
                        )
                    }

                    Text(
                        text = "LoveLink connects singles worldwide through genuine matching, real-time messaging, interactive diamond gifts, and verified dating profiles.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Legal & Terms Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Legal & Safety",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // Privacy Policy Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeDialogTitle = "Privacy Policy"
                                    activeDialogText = "At LoveLink, we take your privacy seriously. Your location, messages, photos, and personal information are strictly encrypted and stored securely in Firebase Firestore. We never sell your personal data to third parties. You have complete control over ghost mode, visibility settings, and account deletion."
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = RosePrimary)
                                Text("Privacy Policy", fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Terms & Conditions Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeDialogTitle = "Terms & Conditions"
                                    activeDialogText = "By creating an account on LoveLink, you agree to treat all members with respect. All users must be 18 years of age or older. Hate speech, harassment, fake profiles, and fraudulent commercial activity will result in immediate permanent account termination without refund."
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = RosePrimary)
                                Text("Terms & Conditions", fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Community Guidelines Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeDialogTitle = "Community Guidelines"
                                    activeDialogText = "1. Be authentic & real.\n2. Respect boundaries.\n3. Keep conversations safe & positive.\n4. Report suspicious accounts using the in-app user report button.\n5. Protect your personal financial details."
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = RosePrimary)
                                Text("Community Guidelines", fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Data Safety & Play Policies Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeDialogTitle = "Data Safety & Security Compliance"
                                    activeDialogText = "LoveLink strictly adheres to Google Play Developer Policies:\n\n• Encryption: All data transmitted over HTTPS/TLS.\n• Storage: User profiles, media, and chat records secured via Firebase Security Rules & RBAC.\n• Payments: Integrated via Google Play Billing with audit trail.\n• User Control: In-app account deletion, profile privacy controls, and screenshot protection support."
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = RosePrimary)
                                Text("Data Safety & Policy Compliance", fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            // Copyright Footer
            Column(
                modifier = Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "© 2026 LoveLink Inc. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Made with ❤️ for meaningful connections",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    val dialogTitle = activeDialogTitle
    val dialogText = activeDialogText
    if (dialogTitle != null && dialogText != null) {
        AlertDialog(
            onDismissRequest = {
                activeDialogTitle = null
                activeDialogText = null
            },
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
            text = { Text(dialogText, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        activeDialogTitle = null
                        activeDialogText = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
