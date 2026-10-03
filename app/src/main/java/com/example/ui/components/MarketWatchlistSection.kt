package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MarketTicker
import com.example.data.model.PriceDirection
import com.example.ui.theme.BinanceBackground
import com.example.ui.theme.BinanceBorder
import com.example.ui.theme.BinanceCard
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceTextMuted
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import java.util.Locale

@Composable
fun MarketWatchlistSection(
    tickers: List<MarketTicker>,
    onSelectPair: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Binance Live Market Feed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextPrimary
                )
                Text(
                    text = "Real-time spot order book prices from Binance exchange",
                    style = MaterialTheme.typography.bodySmall,
                    color = BinanceTextSecondary
                )
            }

            // Live stream indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(BinanceGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BinanceGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BinanceGreen
                )
            }
        }

        // Ticker Cards List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BinanceCard)
                .border(1.dp, BinanceBorder, RoundedCornerShape(16.dp))
        ) {
            tickers.forEachIndexed { index, ticker ->
                val isPos = ticker.change24h >= 0
                val priceColor = when (ticker.direction) {
                    PriceDirection.UP -> BinanceGreen
                    PriceDirection.DOWN -> BinanceRed
                    PriceDirection.NEUTRAL -> BinanceTextPrimary
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPair(ticker.baseAsset) }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("ticker_row_${ticker.symbol.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Coin Icon & Pair
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BinanceBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ticker.baseAsset.take(3),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BinanceTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ticker.baseAsset,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BinanceTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "/USDT",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BinanceTextSecondary
                                )
                            }
                            Text(
                                text = ticker.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = BinanceTextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    // Last Price & 24h PnL
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(
                            text = "$${if (ticker.price >= 1.0) String.format(Locale.US, "%,.2f", ticker.price) else String.format(Locale.US, "%.4f", ticker.price)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = priceColor
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPos) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isPos) BinanceGreen else BinanceRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", ticker.change24h)}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPos) BinanceGreen else BinanceRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Trade on Binance Button
                    Button(
                        onClick = {
                            val url = "https://www.binance.com/en/trade/${ticker.baseAsset}_USDT?type=spot"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("trade_btn_${ticker.symbol.lowercase()}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BinanceYellow.copy(alpha = 0.15f),
                            contentColor = BinanceYellow
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Trade",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (index < tickers.lastIndex) {
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
