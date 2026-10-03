package com.example.data.model

import kotlinx.serialization.Serializable

enum class BinanceNetwork(val id: String, val displayName: String, val baseUrl: String) {
    GLOBAL("global", "Binance.com (Global)", "https://api.binance.com"),
    US("us", "Binance.US", "https://api.binance.us"),
    TESTNET("testnet", "Spot Testnet", "https://testnet.binance.vision"),
    DEMO("demo", "Binance Demo Mode", "https://api.binance.com")
}

@Serializable
data class BinanceCredentials(
    val apiKey: String,
    val apiSecret: String,
    val network: String = "global",
    val label: String? = null
)

@Serializable
data class RawBalance(
    val asset: String,
    val free: String,
    val locked: String
) {
    val freeDouble: Double get() = free.toDoubleOrNull() ?: 0.0
    val lockedDouble: Double get() = locked.toDoubleOrNull() ?: 0.0
    val totalDouble: Double get() = freeDouble + lockedDouble
}

@Serializable
data class PortfolioAsset(
    val asset: String,
    val name: String,
    val free: Double,
    val locked: Double,
    val total: Double,
    val priceUSD: Double,
    val valueUSD: Double,
    val percentOfPortfolio: Double,
    val change24h: Double,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume24h: Double = 0.0,
    val sparkline: List<Double> = emptyList()
)

@Serializable
data class BinanceTrade(
    val id: Long,
    val symbol: String,
    val orderId: Long,
    val price: Double,
    val qty: Double,
    val quoteQty: Double,
    val commission: Double,
    val commissionAsset: String,
    val time: Long,
    val isBuyer: Boolean,
    val isMaker: Boolean = false
)

@Serializable
data class BinanceAccountInfo(
    val isConnected: Boolean,
    val network: String,
    val accountType: String,
    val updateTime: Long,
    val canTrade: Boolean,
    val canWithdraw: Boolean,
    val canDeposit: Boolean,
    val apiKeyMasked: String? = null,
    val latencyMs: Long = 0,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Serializable
data class MarketTicker(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String = "USDT",
    val name: String,
    val price: Double,
    val change24h: Double,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume24h: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val direction: PriceDirection = PriceDirection.NEUTRAL
)

enum class PriceDirection { UP, DOWN, NEUTRAL }

@Serializable
data class SharedSnapshotData(
    val id: String,
    val createdAt: Long,
    val title: String,
    val creatorName: String,
    val network: String,
    val hideBalances: Boolean,
    val totalNetWorthUSD: Double,
    val change24hUSD: Double,
    val change24hPercent: Double,
    val assets: List<SharedAssetItem>
)

@Serializable
data class SharedAssetItem(
    val asset: String,
    val name: String,
    val total: Double,
    val priceUSD: Double,
    val valueUSD: Double,
    val percentOfPortfolio: Double,
    val change24h: Double
)

data class Ticker24hrRaw(
    val symbol: String,
    val price: Double,
    val change24h: Double,
    val high: Double,
    val low: Double,
    val volume: Double
)
