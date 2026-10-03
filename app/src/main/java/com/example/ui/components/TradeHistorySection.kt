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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BinanceTrade
import com.example.ui.theme.BinanceBackground
import com.example.ui.theme.BinanceBorder
import com.example.ui.theme.BinanceCard
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TradeHistorySection(
    trades: List<BinanceTrade>,
    filterSide: String,
    onFilterChange: (String) -> Unit
) {
    val filtered = trades.filter {
        when (filterSide) {
            "buy" -> it.isBuyer
            "sell" -> !it.isBuyer
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header & Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Binance Trade & Order History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextPrimary
                )
                Text(
                    text = "Recent spot fills and execution prices",
                    style = MaterialTheme.typography.bodySmall,
                    color = BinanceTextSecondary
                )
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BinanceBackground)
                    .border(1.dp, BinanceBorder, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                TradeFilterTab(
                    label = "All (${trades.size})",
                    active = filterSide == "all",
                    onClick = { onFilterChange("all") }
                )
                TradeFilterTab(
                    label = "Buys",
                    active = filterSide == "buy",
                    activeColor = BinanceGreen,
                    onClick = { onFilterChange("buy") }
                )
                TradeFilterTab(
                    label = "Sells",
                    active = filterSide == "sell",
                    activeColor = BinanceRed,
                    onClick = { onFilterChange("sell") }
                )
            }
        }

        // Trades List
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BinanceCard)
                    .border(1.dp, BinanceBorder, RoundedCornerShape(16.dp))
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = BinanceTextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No recent spot orders found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BinanceTextSecondary
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BinanceCard)
                    .border(1.dp, BinanceBorder, RoundedCornerShape(16.dp))
            ) {
                filtered.forEachIndexed { index, trade ->
                    val isBuy = trade.isBuyer
                    val dateFormatted = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(trade.time))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("trade_item_${trade.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Side & Pair & Date
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isBuy) BinanceGreen.copy(alpha = 0.15f)
                                        else BinanceRed.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isBuy) Icons.Default.CallReceived else Icons.Default.CallMade,
                                    contentDescription = if (isBuy) "Buy" else "Sell",
                                    tint = if (isBuy) BinanceGreen else BinanceRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = trade.symbol,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BinanceTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBuy) "BUY" else "SELL",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBuy) BinanceGreen else BinanceRed
                                    )
                                }
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BinanceTextSecondary
                                )
                            }
                        }

                        // Amount & Total Value
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", trade.quoteQty)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BinanceTextPrimary
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.4f", trade.qty)} @ $${String.format(Locale.US, "%,.2f", trade.price)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BinanceTextSecondary
                            )
                        }
                    }

                    if (index < filtered.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BinanceBorder.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TradeFilterTab(
    label: String,
    active: Boolean,
    activeColor: Color = BinanceTextPrimary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) BinanceCard else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color = if (active) activeColor else BinanceTextSecondary
        )
    }
}
