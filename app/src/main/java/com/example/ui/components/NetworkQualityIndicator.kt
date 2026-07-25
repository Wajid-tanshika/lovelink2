package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NetworkQualityLevel
import com.example.data.model.NetworkQualityState

/**
 * Real-time Signal Strength & Network Quality UI component for Agora RTC calls.
 */
@Composable
fun NetworkQualityIndicator(
    networkState: NetworkQualityState,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val barColor = Color(networkState.level.colorHex)

    Surface(
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .testTag("network_quality_indicator")
            .clip(RoundedCornerShape(20.dp))
            .clickable { showDialog = true }
            .border(1.dp, barColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Signal Strength Bars
            SignalStrengthBars(
                bars = networkState.level.bars,
                activeColor = barColor
            )

            // Latency / RTT Text
            Text(
                text = "${networkState.rttMs}ms",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )

            // Quality Label badge
            Box(
                modifier = Modifier
                    .background(barColor.copy(alpha = 0.25f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = networkState.level.label,
                    color = barColor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }

    if (showDialog) {
        NetworkDiagnosticsDialog(
            networkState = networkState,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun SignalStrengthBars(
    bars: Int,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val totalBars = 4
        for (i in 1..totalBars) {
            val heightFraction = i / totalBars.toFloat()
            val isActive = i <= bars
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isActive) activeColor else Color.White.copy(alpha = 0.25f))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDiagnosticsDialog(
    networkState: NetworkQualityState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF81D4FA))
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = null,
                    tint = Color(0xFF81D4FA)
                )
                Text(
                    text = "Agora RTC Network Stats",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Signal Quality Header
                val barColor = Color(networkState.level.colorHex)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(barColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Connection Quality",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = networkState.level.label,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = barColor
                        )
                    }
                    SignalStrengthBars(
                        bars = networkState.level.bars,
                        activeColor = barColor,
                        modifier = Modifier.height(20.dp)
                    )
                }

                // Grid of detailed RTC metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Latency (RTT)",
                        value = "${networkState.rttMs} ms",
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Frame Rate",
                        value = "${networkState.fps} FPS",
                        icon = Icons.Default.Wifi,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Downlink",
                        value = "${(networkState.downlinkKbps / 1000f).let { String.format("%.1f", it) }} Mbps",
                        icon = Icons.Default.SignalCellularAlt,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Uplink",
                        value = "${(networkState.uplinkKbps / 1000f).let { String.format("%.1f", it) }} Mbps",
                        icon = Icons.Default.SignalCellularAlt,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Resolution",
                        value = networkState.resolution,
                        icon = Icons.Default.Info,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Packet Loss",
                        value = String.format("%.2f%%", networkState.packetLossPercent),
                        icon = Icons.Default.Info,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        containerColor = Color(0xFF1E1E2C),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}
