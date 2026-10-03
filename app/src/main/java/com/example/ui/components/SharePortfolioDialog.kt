package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PortfolioAsset
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SharePortfolioDialog(
    isOpen: Boolean,
    totalNetWorthUSD: Double,
    change24hUSD: Double,
    change24hPercent: Double,
    assets: List<PortfolioAsset>,
    onGetSocialText: (String, Boolean) -> String,
    onGetCsvText: () -> String,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    var creatorName by remember { mutableStateOf("Crypto Trader") }
    var hideBalances by remember { mutableStateOf(false) }
    var showHoldings by remember { mutableStateOf(true) }
    var cardTheme by remember { mutableStateOf("binance") }

    val isPos = change24hPercent >= 0
    val topAssets = assets.take(4)

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
                // Header
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
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = BinanceYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Share Portfolio Snapshot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BinanceTextPrimary
                            )
                            Text(
                                text = "Export reports with privacy controls",
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
                            .testTag("close_share_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BinanceTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Customization Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BinanceBackground)
                        .border(1.dp, BinanceBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Card Customization",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = BinanceTextPrimary
                        )

                        // Nickname Input
                        OutlinedTextField(
                            value = creatorName,
                            onValueChange = { creatorName = it },
                            label = { Text("Trader Name / Nickname", style = MaterialTheme.typography.labelSmall) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_trader_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BinanceYellow,
                                unfocusedBorderColor = BinanceBorder,
                                focusedContainerColor = BinanceCard,
                                unfocusedContainerColor = BinanceCard,
                                focusedTextColor = BinanceTextPrimary,
                                unfocusedTextColor = BinanceTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Privacy Toggles
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { hideBalances = !hideBalances }
                        ) {
                            Checkbox(
                                checked = hideBalances,
                                onCheckedChange = { hideBalances = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BinanceYellow,
                                    checkmarkColor = BinanceBackground,
                                    uncheckedColor = BinanceBorder
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mask Dollar Values (Show % only)",
                                style = MaterialTheme.typography.bodySmall,
                                color = BinanceTextPrimary
                            )
                        }
                    }
                }

                // Live Card Preview
                Text(
                    text = "Snapshot Preview",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BinanceTextSecondary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BinanceCardElevated)
                        .border(1.dp, BinanceBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Top Brand & Verified Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BinanceYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("◆", color = BinanceBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "BINANCE SPOT PORTFOLIO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BinanceTextPrimary
                                    )
                                    Text(
                                        text = "$creatorName · ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BinanceTextSecondary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BinanceGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = BinanceGreen, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("VERIFIED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BinanceGreen)
                            }
                        }

                        // Net Worth & 24h PnL
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Spot Net Worth", style = MaterialTheme.typography.labelSmall, color = BinanceTextSecondary)
                                Text(
                                    text = if (hideBalances) "$ ••••••••" else "$${String.format(Locale.US, "%,.2f", totalNetWorthUSD)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = BinanceTextPrimary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPos) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isPos) BinanceGreen else BinanceRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", change24hPercent)}% (24h)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPos) BinanceGreen else BinanceRed
                                )
                            }
                        }

                        // Top Holdings Mini Row
                        if (topAssets.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                topAssets.forEach { a ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BinanceBackground)
                                            .padding(6.dp)
                                    ) {
                                        Column {
                                            Text(a.asset, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BinanceTextPrimary)
                                            Text("${String.format(Locale.US, "%.1f", a.percentOfPortfolio)}%", style = MaterialTheme.typography.labelSmall, color = BinanceYellow)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons: Android System Share & Clipboard
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // System Share Intent
                    Button(
                        onClick = {
                            val text = onGetSocialText(creatorName, hideBalances)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Binance Portfolio")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("system_share_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BinanceYellow,
                            contentColor = BinanceBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share via Android Apps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Copy Summary
                    OutlinedButton(
                        onClick = {
                            val text = onGetSocialText(creatorName, hideBalances)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Binance Portfolio", text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Portfolio summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("copy_summary_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BinanceTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Text Summary (X / Telegram)", style = MaterialTheme.typography.bodyMedium)
                    }

                    // Export CSV
                    OutlinedButton(
                        onClick = {
                            val csv = onGetCsvText()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csv)
                                putExtra(Intent.EXTRA_SUBJECT, "Binance_Portfolio_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.csv")
                                type = "text/csv"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export CSV Statement"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("export_csv_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BinanceTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = BinanceGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export CSV Statement", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
