package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Zap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BinanceAccountInfo
import com.example.ui.theme.BinanceBackground
import com.example.ui.theme.BinanceBorder
import com.example.ui.theme.BinanceCard
import com.example.ui.theme.BinanceCardElevated
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceTextMuted
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow

@Composable
fun ConnectionDialog(
    isOpen: Boolean,
    accountInfo: BinanceAccountInfo?,
    isRefreshing: Boolean,
    lastPingLatency: Long,
    isPingTesting: Boolean,
    onTestPing: (String) -> Unit,
    onConnectApi: (apiKey: String, apiSecret: String, network: String, onDone: (Boolean) -> Unit) -> Unit,
    onLoadDemo: () -> Unit,
    onImportCsv: (String) -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var activeTab by remember { mutableStateOf("api") }
    var selectedNetwork by remember { mutableStateOf("global") }
    var apiKey by remember { mutableStateOf("") }
    var apiSecret by remember { mutableStateOf("") }
    var showSecret by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var csvInputText by remember { mutableStateOf("") }

    val networkBaseUrl = when (selectedNetwork) {
        "us" -> "https://api.binance.us"
        "testnet" -> "https://testnet.binance.vision"
        else -> "https://api.binance.com"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(BinanceCard)
                .border(1.dp, BinanceBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BinanceYellow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = BinanceYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Connect Binance Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BinanceTextPrimary
                            )
                            Text(
                                text = "Sync spot balances & portfolio metrics",
                                style = MaterialTheme.typography.bodySmall,
                                color = BinanceTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BinanceCardElevated)
                            .testTag("close_connection_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BinanceTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // If already connected, show status and disconnect option
                if (accountInfo?.isConnected == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BinanceGreen.copy(alpha = 0.1f))
                            .border(1.dp, BinanceGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BinanceGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Connected (${accountInfo.network.uppercase()})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BinanceGreen
                                    )
                                    Text(
                                        text = accountInfo.apiKeyMasked ?: "API Connected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BinanceTextSecondary
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    onDisconnect()
                                    onDismiss()
                                },
                                modifier = Modifier.testTag("disconnect_account_button")
                            ) {
                                Text(
                                    text = "Disconnect",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BinanceRed
                                )
                            }
                        }
                    }
                }

                // Connection Mode Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BinanceBackground)
                        .border(1.dp, BinanceBorder, RoundedCornerShape(8.dp))
                        .padding(3.dp)
                ) {
                    ConnectionTabButton(
                        label = "API Key",
                        active = activeTab == "api",
                        modifier = Modifier.weight(1f),
                        onClick = { activeTab = "api" }
                    )
                    ConnectionTabButton(
                        label = "Demo Mode",
                        active = activeTab == "demo",
                        modifier = Modifier.weight(1f),
                        onClick = { activeTab = "demo" }
                    )
                    ConnectionTabButton(
                        label = "CSV Import",
                        active = activeTab == "csv",
                        modifier = Modifier.weight(1f),
                        onClick = { activeTab = "csv" }
                    )
                }

                // Tab 1: Binance API Key
                if (activeTab == "api") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Network Choice
                        Text(
                            text = "Binance Network",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = BinanceTextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NetworkChip(
                                label = "Binance.com",
                                sub = "Global",
                                active = selectedNetwork == "global",
                                modifier = Modifier.weight(1f),
                                onClick = { selectedNetwork = "global" }
                            )
                            NetworkChip(
                                label = "Binance.US",
                                sub = "United States",
                                active = selectedNetwork == "us",
                                modifier = Modifier.weight(1f),
                                onClick = { selectedNetwork = "us" }
                            )
                            NetworkChip(
                                label = "Testnet",
                                sub = "Sandbox",
                                active = selectedNetwork == "testnet",
                                modifier = Modifier.weight(1f),
                                onClick = { selectedNetwork = "testnet" }
                            )
                        }

                        // API Key Field with Ping Test
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "API Key",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = BinanceTextSecondary
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onTestPing(networkBaseUrl) }
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPingTesting) "Pinging..." else if (lastPingLatency > 0) "${lastPingLatency}ms" else "Test Ping",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastPingLatency > 0) BinanceGreen else BinanceYellow
                                )
                            }
                        }

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            placeholder = { Text("Enter Binance API Key", style = MaterialTheme.typography.bodySmall, color = BinanceTextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("api_key_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BinanceYellow,
                                unfocusedBorderColor = BinanceBorder,
                                focusedContainerColor = BinanceBackground,
                                unfocusedContainerColor = BinanceBackground,
                                focusedTextColor = BinanceTextPrimary,
                                unfocusedTextColor = BinanceTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // API Secret Field
                        Text(
                            text = "API Secret",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = BinanceTextSecondary
                        )
                        OutlinedTextField(
                            value = apiSecret,
                            onValueChange = { apiSecret = it },
                            placeholder = { Text("HMAC SHA-256 Secret", style = MaterialTheme.typography.bodySmall, color = BinanceTextMuted) },
                            singleLine = true,
                            visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showSecret = !showSecret }) {
                                    Icon(
                                        imageVector = if (showSecret) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle secret",
                                        tint = BinanceTextSecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("api_secret_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BinanceYellow,
                                unfocusedBorderColor = BinanceBorder,
                                focusedContainerColor = BinanceBackground,
                                unfocusedContainerColor = BinanceBackground,
                                focusedTextColor = BinanceTextPrimary,
                                unfocusedTextColor = BinanceTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Security recommendations box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BinanceBackground)
                                .border(1.dp, BinanceBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = BinanceGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Read-Only Security Guidelines",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BinanceTextPrimary
                                    )
                                }
                                Text(
                                    text = "• Only enable 'Reading' on Binance API Management.\n• Never enable 'Withdrawals' or 'Margin'.\n• Keys remain on-device and requests are signed locally with HMAC SHA-256.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BinanceTextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = BinanceRed
                            )
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                if (apiKey.isBlank() || apiSecret.isBlank()) {
                                    errorMessage = "Please enter both API Key and Secret."
                                    return@Button
                                }
                                errorMessage = null
                                isSubmitting = true
                                onConnectApi(apiKey, apiSecret, selectedNetwork) { success ->
                                    isSubmitting = false
                                    if (success) {
                                        onDismiss()
                                    } else {
                                        errorMessage = "Failed to connect. Check key/secret and permissions."
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("connect_api_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BinanceYellow,
                                contentColor = BinanceBackground
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = BinanceBackground,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connect Read-Only API",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Tab 2: Demo Mode
                if (activeTab == "demo") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BinanceYellow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Zap,
                                contentDescription = null,
                                tint = BinanceYellow,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Text(
                            text = "Instant Demo Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BinanceTextPrimary
                        )

                        Text(
                            text = "Explore spot balances, live Binance market feeds, allocation charts, and portfolio sharing without entering any API credentials.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BinanceTextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BinanceBackground)
                                .border(1.dp, BinanceBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Includes Realistic Spot Holdings:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BinanceTextPrimary
                                )
                                Text(
                                    text = "• 0.895 BTC · 6.42 ETH · 53.50 SOL · 26.18 BNB\n• 19,920 USDT · 820 NEAR · 95.4 AVAX · 1,450 SUI\n✓ 100% Real-time market prices from Binance exchange",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BinanceTextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onLoadDemo()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("launch_demo_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BinanceYellow,
                                contentColor = BinanceBackground
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Zap, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Launch Demo Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Tab 3: CSV Import
                if (activeTab == "csv") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Import Binance Spot Statement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BinanceTextPrimary
                        )
                        Text(
                            text = "Paste CSV text exported from Binance (Asset, Amount):",
                            style = MaterialTheme.typography.bodySmall,
                            color = BinanceTextSecondary
                        )

                        OutlinedTextField(
                            value = csvInputText,
                            onValueChange = { csvInputText = it },
                            placeholder = {
                                Text(
                                    text = "Asset,Total\nBTC,0.85\nETH,4.2\nSOL,45.0\nUSDT,12500.0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BinanceTextMuted
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("csv_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BinanceYellow,
                                unfocusedBorderColor = BinanceBorder,
                                focusedContainerColor = BinanceBackground,
                                unfocusedContainerColor = BinanceBackground,
                                focusedTextColor = BinanceTextPrimary,
                                unfocusedTextColor = BinanceTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                if (csvInputText.isNotBlank()) {
                                    onImportCsv(csvInputText)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("import_csv_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BinanceYellow,
                                contentColor = BinanceBackground
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Import Statement Balances",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionTabButton(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) BinanceCard else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color = if (active) BinanceYellow else BinanceTextSecondary
        )
    }
}

@Composable
fun NetworkChip(
    label: String,
    sub: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) BinanceYellow.copy(alpha = 0.15f) else BinanceBackground)
            .border(
                1.dp,
                if (active) BinanceYellow else BinanceBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = if (active) BinanceYellow else BinanceTextPrimary
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = BinanceTextSecondary
            )
        }
    }
}
