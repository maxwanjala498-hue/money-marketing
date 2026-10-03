package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PortfolioAsset
import com.example.ui.theme.BinanceBackground
import com.example.ui.theme.BinanceBorder
import com.example.ui.theme.BinanceCard
import com.example.ui.theme.BinanceCardElevated
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailBottomSheet(
    asset: PortfolioAsset?,
    klines: List<Double>,
    interval: String,
    isLoadingKlines: Boolean,
    hideAmounts: Boolean,
    onIntervalChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (asset == null) return

    val context = LocalContext.current
    val isPos = asset.change24h >= 0
    val pair = if (asset.asset == "USDT") "BTCUSDT" else "${asset.asset}USDT"
    val tradeUrl = "https://www.binance.com/en/trade/${asset.asset}_USDT?type=spot"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BinanceCard,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Coin Icon, Title, Trade button, Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BinanceBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = asset.asset.take(3),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BinanceTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asset.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BinanceTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BinanceBorder)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = asset.asset,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BinanceTextSecondary
                                )
                            }
                        }
                        Text(
                            text = "Binance Spot Market ($pair)",
                            style = MaterialTheme.typography.bodySmall,
                            color = BinanceTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BinanceCardElevated)
                        .testTag("close_asset_detail_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BinanceTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Price & Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Price & 24h Change
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Price",
                        style = MaterialTheme.typography.bodySmall,
                        color = BinanceTextSecondary
                    )
                    Text(
                        text = "$${if (asset.priceUSD >= 1.0) String.format(Locale.US, "%,.2f", asset.priceUSD) else String.format(Locale.US, "%.5f", asset.priceUSD)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPos) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPos) BinanceGreen else BinanceRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", asset.change24h)}% (24h)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPos) BinanceGreen else BinanceRed
                        )
                    }
                }

                // Your Holdings
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Your Balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = BinanceTextSecondary
                    )
                    Text(
                        text = if (hideAmounts) "••••" else String.format(Locale.US, "%.4f", asset.total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextPrimary
                    )
                    Text(
                        text = if (hideAmounts) "••••••" else "≈ $${String.format(Locale.US, "%,.2f", asset.valueUSD)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BinanceTextSecondary
                    )
                }
            }

            // Interactive Kline Chart Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BinanceBackground)
                    .border(1.dp, BinanceBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    // Chart Header with Interval Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = BinanceYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Binance Price Trend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = BinanceTextPrimary
                            )
                        }

                        // Interval switcher
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BinanceCard)
                                .border(1.dp, BinanceBorder, RoundedCornerShape(6.dp))
                                .padding(2.dp)
                        ) {
                            IntervalButton(label = "1H", active = interval == "1h", onClick = { onIntervalChange("1h") })
                            IntervalButton(label = "1D", active = interval == "1d", onClick = { onIntervalChange("1d") })
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Canvas Line Chart
                    if (isLoadingKlines) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = BinanceYellow,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (klines.isNotEmpty()) {
                        KlineCanvasChart(
                            prices = klines,
                            isPositive = isPos,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val minP = klines.minOrNull() ?: 0.0
                        val maxP = klines.maxOrNull() ?: 0.0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Low: $${String.format(Locale.US, "%,.2f", minP)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BinanceTextSecondary
                            )
                            Text(
                                text = "High: $${String.format(Locale.US, "%,.2f", maxP)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BinanceTextSecondary
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Live Binance market price active",
                                style = MaterialTheme.typography.bodySmall,
                                color = BinanceTextSecondary
                            )
                        }
                    }
                }
            }

            // Quick Actions: Trade on Binance
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tradeUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_binance_trade_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BinanceYellow,
                    contentColor = BinanceBackground
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trade ${asset.asset}/USDT on Binance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun IntervalButton(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) BinanceYellow else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color = if (active) BinanceBackground else BinanceTextSecondary
        )
    }
}

@Composable
fun KlineCanvasChart(
    prices: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val chartColor = if (isPositive) BinanceGreen else BinanceRed
    val gradientColor = chartColor.copy(alpha = 0.2f)

    Canvas(modifier = modifier) {
        if (prices.size < 2) return@Canvas

        val minPrice = prices.minOrNull() ?: 0.0
        val maxPrice = prices.maxOrNull() ?: 1.0
        val priceRange = (maxPrice - minPrice).coerceAtLeast(0.00001)

        val w = size.width
        val h = size.height
        val padding = 10f

        val points = prices.mapIndexed { index, price ->
            val x = padding + (index.toFloat() / (prices.size - 1)) * (w - padding * 2)
            val normalizedY = ((price - minPrice) / priceRange).toFloat()
            val y = (h - padding) - normalizedY * (h - padding * 2)
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        val fillPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
            lineTo(points.last().x, h)
            lineTo(points.first().x, h)
            close()
        }

        // Fill area
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(gradientColor, Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Stroke line
        drawPath(
            path = strokePath,
            color = chartColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
