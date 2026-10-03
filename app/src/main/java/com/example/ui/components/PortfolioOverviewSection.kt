package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BinanceAccountInfo
import com.example.data.model.PortfolioAsset
import com.example.ui.theme.AltsPurple
import com.example.ui.theme.BinanceBackground
import com.example.ui.theme.BinanceBorder
import com.example.ui.theme.BinanceCard
import com.example.ui.theme.BinanceCardElevated
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import com.example.ui.theme.BnbGold
import com.example.ui.theme.BtcOrange
import com.example.ui.theme.EthBlue
import com.example.ui.theme.SolGreen
import com.example.ui.theme.UsdtTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortfolioOverviewSection(
    totalNetWorthUSD: Double,
    change24hUSD: Double,
    change24hPercent: Double,
    assets: List<PortfolioAsset>,
    accountInfo: BinanceAccountInfo?,
    btcPrice: Double,
    hideAmounts: Boolean,
    onToggleHideAmounts: () -> Unit,
    onOpenShare: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    val isPositive = change24hUSD >= 0
    val btcEquivalent = if (btcPrice > 0) String.format(Locale.US, "%.4f", totalNetWorthUSD / btcPrice) else "0.0000"

    // Breakdown computations
    val btcVal = assets.firstOrNull { it.asset == "BTC" }?.valueUSD ?: 0.0
    val ethVal = assets.firstOrNull { it.asset == "ETH" }?.valueUSD ?: 0.0
    val solVal = assets.firstOrNull { it.asset == "SOL" }?.valueUSD ?: 0.0
    val bnbVal = assets.firstOrNull { it.asset == "BNB" }?.valueUSD ?: 0.0
    val stablesVal = assets.filter { listOf("USDT", "USDC", "FDUSD", "DAI", "BUSD").contains(it.asset) }.sumOf { it.valueUSD }
    val altsVal = (totalNetWorthUSD - (btcVal + ethVal + solVal + bnbVal + stablesVal)).coerceAtLeast(0.0)

    val btcPct = if (totalNetWorthUSD > 0) (btcVal / totalNetWorthUSD * 100).toFloat() else 0f
    val ethPct = if (totalNetWorthUSD > 0) (ethVal / totalNetWorthUSD * 100).toFloat() else 0f
    val solPct = if (totalNetWorthUSD > 0) (solVal / totalNetWorthUSD * 100).toFloat() else 0f
    val bnbPct = if (totalNetWorthUSD > 0) (bnbVal / totalNetWorthUSD * 100).toFloat() else 0f
    val stablesPct = if (totalNetWorthUSD > 0) (stablesVal / totalNetWorthUSD * 100).toFloat() else 0f
    val altsPct = if (totalNetWorthUSD > 0) (altsVal / totalNetWorthUSD * 100).toFloat() else 0f

    val topAsset = assets.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Spot Net Worth Banner Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BinanceCard)
                .border(1.dp, BinanceBorder, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("portfolio_networth_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with Privacy Eye Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Binance Spot Net Worth",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = BinanceTextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onToggleHideAmounts() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("privacy_toggle_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hideAmounts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (hideAmounts) "Show amounts" else "Hide amounts",
                            tint = BinanceTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hideAmounts) "Hidden" else "Hide",
                            style = MaterialTheme.typography.labelSmall,
                            color = BinanceTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Net Worth Large Typography & 24h PnL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (hideAmounts) "$ ••••••••" else "$${String.format(Locale.US, "%,.2f", totalNetWorthUSD)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = BinanceTextPrimary,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subtitle: BTC equivalent & sync time
                        val timeStr = accountInfo?.lastSyncedAt?.let {
                            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it))
                        } ?: "Just now"

                        Text(
                            text = "≈ ${if (hideAmounts) "•••" else btcEquivalent} BTC · Synced $timeStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = BinanceTextSecondary
                        )
                    }

                    // 24h Return Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isPositive) BinanceGreen.copy(alpha = 0.15f)
                                else BinanceRed.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isPositive) BinanceGreen else BinanceRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", change24hPercent)}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) BinanceGreen else BinanceRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons: Share Snapshot & Refresh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenShare,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("share_snapshot_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BinanceYellow,
                            contentColor = BinanceBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Snapshot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("refresh_overview_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BinanceTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = BinanceYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRefreshing) "Syncing..." else "Sync Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Multi-Asset Allocation Stacked Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Portfolio Allocation Breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BinanceTextPrimary
                    )
                    Text(
                        text = "${assets.size} Spot Assets",
                        style = MaterialTheme.typography.labelSmall,
                        color = BinanceTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stacked Bar Container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(BinanceBackground)
                ) {
                    if (btcPct > 0) Box(modifier = Modifier.weight(btcPct.coerceAtLeast(0.1f)).height(10.dp).background(BtcOrange))
                    if (ethPct > 0) Box(modifier = Modifier.weight(ethPct.coerceAtLeast(0.1f)).height(10.dp).background(EthBlue))
                    if (solPct > 0) Box(modifier = Modifier.weight(solPct.coerceAtLeast(0.1f)).height(10.dp).background(SolGreen))
                    if (bnbPct > 0) Box(modifier = Modifier.weight(bnbPct.coerceAtLeast(0.1f)).height(10.dp).background(BnbGold))
                    if (stablesPct > 0) Box(modifier = Modifier.weight(stablesPct.coerceAtLeast(0.1f)).height(10.dp).background(UsdtTeal))
                    if (altsPct > 0) Box(modifier = Modifier.weight(altsPct.coerceAtLeast(0.1f)).height(10.dp).background(AltsPurple))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Legend Items FlowRow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (btcPct > 0) AllocationLegendItem("BTC", btcPct, BtcOrange)
                    if (ethPct > 0) AllocationLegendItem("ETH", ethPct, EthBlue)
                    if (solPct > 0) AllocationLegendItem("SOL", solPct, SolGreen)
                    if (bnbPct > 0) AllocationLegendItem("BNB", bnbPct, BnbGold)
                    if (stablesPct > 0) AllocationLegendItem("Stables", stablesPct, UsdtTeal)
                    if (altsPct > 0) AllocationLegendItem("Other Alts", altsPct, AltsPurple)
                }
            }
        }

        // 4 Key Performance Metric Cards Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: 24h Return
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "24h Return",
                    value = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", change24hPercent)}%",
                    subValue = if (hideAmounts) "••••••" else "${if (isPositive) "+" else ""}$${String.format(Locale.US, "%,.2f", change24hUSD)}",
                    valueColor = if (isPositive) BinanceGreen else BinanceRed,
                    icon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown
                )

                // Card 2: Primary Exposure
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Primary Allocation",
                    value = topAsset?.asset ?: "None",
                    subValue = topAsset?.let { "${String.format(Locale.US, "%.1f", it.percentOfPortfolio)}% of portfolio" } ?: "0%",
                    valueColor = BinanceYellow,
                    icon = Icons.Default.AccountBalanceWallet
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 3: Stablecoin Reserves
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Stablecoin Reserves",
                    value = "${String.format(Locale.US, "%.1f", stablesPct)}%",
                    subValue = if (hideAmounts) "••••••" else "$${String.format(Locale.US, "%,.0f", stablesVal)} dry powder",
                    valueColor = BinanceGreen,
                    icon = Icons.Default.Shield
                )

                // Card 4: Connection Latency & Security
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Binance Latency",
                    value = "${accountInfo?.latencyMs ?: 16} ms",
                    subValue = "SSL · Read-Only API",
                    valueColor = BinanceGreen,
                    icon = Icons.Default.ElectricBolt
                )
            }
        }
    }
}

@Composable
fun AllocationLegendItem(label: String, pct: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label ${String.format(Locale.US, "%.1f", pct)}%",
            style = MaterialTheme.typography.bodySmall,
            color = BinanceTextSecondary
        )
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subValue: String,
    valueColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BinanceCard)
            .border(1.dp, BinanceBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = BinanceTextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BinanceTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                color = BinanceTextSecondary
            )
        }
    }
}
