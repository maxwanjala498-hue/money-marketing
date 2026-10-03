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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import java.util.Locale

@Composable
fun AssetBalancesSection(
    assets: List<PortfolioAsset>,
    hideAmounts: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    hideSmallBalances: Boolean,
    onToggleHideSmallBalances: (Boolean) -> Unit,
    sortField: String,
    sortAsc: Boolean,
    onSortChange: (String) -> Unit,
    onSelectAsset: (PortfolioAsset) -> Unit
) {
    // Filter
    val filtered = assets.filter { item ->
        if (hideSmallBalances && item.valueUSD < 1.0) return@filter false
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            item.asset.lowercase().contains(q) || item.name.lowercase().contains(q)
        } else true
    }

    // Sort
    val sorted = filtered.sortedWith { a, b ->
        val cmp = when (sortField) {
            "balance" -> a.total.compareTo(b.total)
            "change" -> a.change24h.compareTo(b.change24h)
            "name" -> a.asset.compareTo(b.asset)
            else -> a.valueUSD.compareTo(b.valueUSD)
        }
        if (sortAsc) cmp else -cmp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Title & Info
        Column {
            Text(
                text = "Binance Spot Balances",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary
            )
            Text(
                text = "Real-time valuations based on Binance spot order books",
                style = MaterialTheme.typography.bodySmall,
                color = BinanceTextSecondary
            )
        }

        // Search Bar & Filter Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text("Search coin (e.g. BTC)...", style = MaterialTheme.typography.bodySmall, color = BinanceTextMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BinanceTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("asset_search_input"),
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

            // Hide < $1.00 Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleHideSmallBalances(!hideSmallBalances) }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hideSmallBalances,
                    onCheckedChange = onToggleHideSmallBalances,
                    colors = CheckboxDefaults.colors(
                        checkedColor = BinanceYellow,
                        checkmarkColor = BinanceBackground,
                        uncheckedColor = BinanceBorder
                    ),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "> $1",
                    style = MaterialTheme.typography.labelSmall,
                    color = BinanceTextSecondary
                )
            }
        }

        // Sorting Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sort:",
                style = MaterialTheme.typography.labelSmall,
                color = BinanceTextSecondary
            )
            SortChip(label = "Value", field = "value", activeField = sortField, asc = sortAsc, onSelect = onSortChange)
            SortChip(label = "Holdings", field = "balance", activeField = sortField, asc = sortAsc, onSelect = onSortChange)
            SortChip(label = "24h PnL", field = "change", activeField = sortField, asc = sortAsc, onSelect = onSortChange)
            SortChip(label = "Name", field = "name", activeField = sortField, asc = sortAsc, onSelect = onSortChange)
        }

        // Assets List
        if (sorted.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BinanceCard)
                    .border(1.dp, BinanceBorder, RoundedCornerShape(12.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No assets found matching filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BinanceTextSecondary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BinanceCard)
                    .border(1.dp, BinanceBorder, RoundedCornerShape(16.dp))
            ) {
                sorted.forEachIndexed { index, asset ->
                    AssetRowItem(
                        asset = asset,
                        hideAmounts = hideAmounts,
                        onClick = { onSelectAsset(asset) }
                    )
                    if (index < sorted.lastIndex) {
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
fun SortChip(
    label: String,
    field: String,
    activeField: String,
    asc: Boolean,
    onSelect: (String) -> Unit
) {
    val isSelected = activeField == field
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) BinanceYellow.copy(alpha = 0.15f) else BinanceCard)
            .border(
                1.dp,
                if (isSelected) BinanceYellow else BinanceBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable { onSelect(field) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) BinanceYellow else BinanceTextSecondary
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = if (asc) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = BinanceYellow,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun AssetRowItem(
    asset: PortfolioAsset,
    hideAmounts: Boolean,
    onClick: () -> Unit
) {
    val isPos = asset.change24h >= 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("asset_row_${asset.asset.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Coin Icon & Symbol / Name
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
                    text = asset.asset.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = asset.asset,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextPrimary
                )
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = BinanceTextSecondary,
                    maxLines = 1
                )
            }
        }

        // Holdings & USD Price
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (hideAmounts) "••••" else String.format(Locale.US, "%.4f", asset.total),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = BinanceTextPrimary
            )
            Text(
                text = "$${if (asset.priceUSD >= 1.0) String.format(Locale.US, "%,.2f", asset.priceUSD) else String.format(Locale.US, "%.4f", asset.priceUSD)}",
                style = MaterialTheme.typography.labelSmall,
                color = BinanceTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Total Value & 24h PnL
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1.1f)
        ) {
            Text(
                text = if (hideAmounts) "$ ••••" else "$${String.format(Locale.US, "%,.2f", asset.valueUSD)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary
            )
            Text(
                text = "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", asset.change24h)}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isPos) BinanceGreen else BinanceRed
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Details",
            tint = BinanceTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
