package com.example.data.repository

import com.example.data.api.BinanceApiClient
import com.example.data.model.BinanceAccountInfo
import com.example.data.model.BinanceCredentials
import com.example.data.model.BinanceTrade
import com.example.data.model.MarketTicker
import com.example.data.model.PortfolioAsset
import com.example.data.model.PriceDirection
import com.example.data.model.RawBalance
import com.example.data.model.SharedAssetItem
import com.example.data.model.SharedSnapshotData
import com.example.data.model.Ticker24hrRaw
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class BinanceRepository(
    private val apiClient: BinanceApiClient = BinanceApiClient()
) {

    val assetNames: Map<String, String> = mapOf(
        "BTC" to "Bitcoin",
        "ETH" to "Ethereum",
        "BNB" to "BNB",
        "SOL" to "Solana",
        "USDT" to "Tether USD",
        "USDC" to "USD Coin",
        "FDUSD" to "First Digital USD",
        "XRP" to "XRP",
        "ADA" to "Cardano",
        "AVAX" to "Avalanche",
        "DOGE" to "Dogecoin",
        "DOT" to "Polkadot",
        "LINK" to "Chainlink",
        "NEAR" to "NEAR Protocol",
        "SUI" to "Sui",
        "RENDER" to "Render",
        "TAO" to "Bittensor",
        "PEPE" to "Pepe",
        "SHIB" to "Shiba Inu",
        "LTC" to "Litecoin",
        "UNI" to "Uniswap",
        "MATIC" to "Polygon",
        "POL" to "Polygon",
        "APT" to "Aptos",
        "ICP" to "Internet Computer",
        "FET" to "Artificial Superintelligence",
        "INJ" to "Injective",
        "TRX" to "TRON"
    )

    private val _tickers = MutableStateFlow<Map<String, Ticker24hrRaw>>(emptyMap())
    val tickers: StateFlow<Map<String, Ticker24hrRaw>> = _tickers.asStateFlow()

    suspend fun refreshTickers(): Map<String, Ticker24hrRaw> {
        val fetched = apiClient.fetchTickers24hr()
        _tickers.value = fetched
        return fetched
    }

    suspend fun ping(baseUrl: String): Pair<Boolean, Long> {
        return apiClient.ping(baseUrl)
    }

    suspend fun fetchKlines(symbol: String, interval: String = "1h", limit: Int = 30): List<Double> {
        return apiClient.fetchKlines(symbol, interval, limit)
    }

    suspend fun connectAccount(credentials: BinanceCredentials): Result<Pair<BinanceAccountInfo, List<RawBalance>>> {
        return apiClient.fetchAccountInfo(credentials)
    }

    suspend fun fetchRecentTrades(credentials: BinanceCredentials, symbol: String): List<BinanceTrade> {
        return apiClient.fetchRecentTrades(credentials, symbol)
    }

    fun calculatePortfolio(
        rawBalances: List<RawBalance>,
        priceMap: Map<String, Ticker24hrRaw>
    ): PortfolioCalculationResult {
        val assets = mutableListOf<PortfolioAsset>()
        var totalNetWorthUSD = 0.0

        val btcPrice = priceMap["BTCUSDT"]?.price ?: 95400.0

        for (b in rawBalances) {
            val total = b.totalDouble
            if (total <= 0.00000001) continue

            val asset = b.asset.uppercase()
            var priceUSD = 0.0
            var change24h = 0.0
            var high24h = 0.0
            var low24h = 0.0
            var volume24h = 0.0

            if (listOf("USDT", "USDC", "FDUSD", "BUSD", "DAI", "TUSD").contains(asset)) {
                priceUSD = 1.0
                change24h = 0.01
                high24h = 1.0
                low24h = 1.0
                volume24h = 500000000.0
            } else {
                val usdtPair = "${asset}USDT"
                val usdcPair = "${asset}USDC"
                val btcPair = "${asset}BTC"

                val usdtTick = priceMap[usdtPair]
                val usdcTick = priceMap[usdcPair]
                val btcTick = priceMap[btcPair]

                if (usdtTick != null) {
                    priceUSD = usdtTick.price
                    change24h = usdtTick.change24h
                    high24h = usdtTick.high
                    low24h = usdtTick.low
                    volume24h = usdtTick.volume
                } else if (usdcTick != null) {
                    priceUSD = usdcTick.price
                    change24h = usdcTick.change24h
                    high24h = usdcTick.high
                    low24h = usdcTick.low
                    volume24h = usdcTick.volume
                } else if (btcTick != null) {
                    priceUSD = btcTick.price * btcPrice
                    change24h = btcTick.change24h
                    high24h = btcTick.high * btcPrice
                    low24h = btcTick.low * btcPrice
                }
            }

            val valueUSD = total * priceUSD
            totalNetWorthUSD += valueUSD

            assets.add(
                PortfolioAsset(
                    asset = asset,
                    name = assetNames[asset] ?: asset,
                    free = b.freeDouble,
                    locked = b.lockedDouble,
                    total = total,
                    priceUSD = priceUSD,
                    valueUSD = valueUSD,
                    percentOfPortfolio = 0.0,
                    change24h = change24h,
                    high24h = high24h,
                    low24h = low24h,
                    volume24h = volume24h
                )
            )
        }

        var totalWeighted24hGainUSD = 0.0
        val calculatedAssets = assets.map { a ->
            val percent = if (totalNetWorthUSD > 0) (a.valueUSD / totalNetWorthUSD) * 100.0 else 0.0
            val previousValue = if (a.change24h != 0.0) a.valueUSD / (1.0 + a.change24h / 100.0) else a.valueUSD
            totalWeighted24hGainUSD += (a.valueUSD - previousValue)
            a.copy(percentOfPortfolio = percent)
        }.sortedByDescending { it.valueUSD }

        val previousTotal = totalNetWorthUSD - totalWeighted24hGainUSD
        val change24hPercent = if (previousTotal > 0) (totalWeighted24hGainUSD / previousTotal) * 100.0 else 0.0

        return PortfolioCalculationResult(
            assets = calculatedAssets,
            totalNetWorthUSD = totalNetWorthUSD,
            change24hUSD = totalWeighted24hGainUSD,
            change24hPercent = change24hPercent
        )
    }

    fun createDemoAccount(priceMap: Map<String, Ticker24hrRaw>): DemoAccountData {
        val now = System.currentTimeMillis()
        val rawBalances = listOf(
            RawBalance("BTC", "0.84500000", "0.05000000"),
            RawBalance("ETH", "6.42000000", "0.00000000"),
            RawBalance("SOL", "48.50000000", "5.00000000"),
            RawBalance("BNB", "24.18000000", "2.00000000"),
            RawBalance("USDT", "18420.50000000", "1500.00000000"),
            RawBalance("NEAR", "820.00000000", "0.00000000"),
            RawBalance("AVAX", "95.40000000", "0.00000000"),
            RawBalance("LINK", "240.00000000", "0.00000000"),
            RawBalance("SUI", "1450.00000000", "0.00000000")
        )

        val btcPrice = priceMap["BTCUSDT"]?.price ?: 95400.0
        val ethPrice = priceMap["ETHUSDT"]?.price ?: 3410.0
        val solPrice = priceMap["SOLUSDT"]?.price ?: 194.5
        val bnbPrice = priceMap["BNBUSDT"]?.price ?: 652.0

        val trades = listOf(
            BinanceTrade(
                id = 91402831L,
                symbol = "BTCUSDT",
                orderId = 1049281L,
                price = btcPrice * 0.985,
                qty = 0.25,
                quoteQty = 0.25 * btcPrice * 0.985,
                commission = 0.0001875,
                commissionAsset = "BNB",
                time = now - 3600000L * 4,
                isBuyer = true
            ),
            BinanceTrade(
                id = 91398241L,
                symbol = "SOLUSDT",
                orderId = 1049102L,
                price = solPrice * 0.97,
                qty = 15.0,
                quoteQty = 15.0 * solPrice * 0.97,
                commission = 0.0014,
                commissionAsset = "BNB",
                time = now - 3600000L * 18,
                isBuyer = true
            ),
            BinanceTrade(
                id = 91374921L,
                symbol = "ETHUSDT",
                orderId = 1048894L,
                price = ethPrice * 1.02,
                qty = 1.5,
                quoteQty = 1.5 * ethPrice * 1.02,
                commission = 0.0031,
                commissionAsset = "BNB",
                time = now - 3600000L * 36,
                isBuyer = false
            ),
            BinanceTrade(
                id = 91350124L,
                symbol = "BNBUSDT",
                orderId = 1048201L,
                price = bnbPrice * 0.96,
                qty = 5.0,
                quoteQty = 5.0 * bnbPrice * 0.96,
                commission = 0.00075,
                commissionAsset = "BNB",
                time = now - 3600000L * 62,
                isBuyer = true
            )
        )

        val accountInfo = BinanceAccountInfo(
            isConnected = true,
            network = "demo",
            accountType = "SPOT (Demo Account)",
            updateTime = now,
            canTrade = true,
            canWithdraw = false,
            canDeposit = false,
            apiKeyMasked = "DEMO_BNB_LIVE_FEED_READY",
            latencyMs = 14,
            lastSyncedAt = now
        )

        return DemoAccountData(accountInfo, rawBalances, trades)
    }

    fun parseCsvStatement(csvText: String): Pair<BinanceAccountInfo, List<RawBalance>>? {
        val lines = csvText.lines()
        val parsedBalances = mutableListOf<RawBalance>()

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val parts = line.split(",").map { it.replace("\"", "").trim() }
            if (parts.size >= 2) {
                val asset = parts[0].uppercase()
                val amount = parts[1].toDoubleOrNull() ?: 0.0
                if (asset.isNotEmpty() && amount > 0) {
                    parsedBalances.add(
                        RawBalance(
                            asset = asset,
                            free = amount.toString(),
                            locked = "0"
                        )
                    )
                }
            }
        }

        if (parsedBalances.isEmpty()) return null

        val now = System.currentTimeMillis()
        val accountInfo = BinanceAccountInfo(
            isConnected = true,
            network = "demo",
            accountType = "SPOT (Imported CSV)",
            updateTime = now,
            canTrade = false,
            canWithdraw = false,
            canDeposit = false,
            apiKeyMasked = "CSV_STATEMENT_DATA",
            latencyMs = 5,
            lastSyncedAt = now
        )

        return Pair(accountInfo, parsedBalances)
    }

    fun createSnapshot(
        title: String,
        creatorName: String,
        network: String,
        hideBalances: Boolean,
        totalNetWorthUSD: Double,
        change24hUSD: Double,
        change24hPercent: Double,
        assets: List<PortfolioAsset>
    ): SharedSnapshotData {
        return SharedSnapshotData(
            id = UUID.randomUUID().toString().take(12),
            createdAt = System.currentTimeMillis(),
            title = title,
            creatorName = creatorName,
            network = network,
            hideBalances = hideBalances,
            totalNetWorthUSD = totalNetWorthUSD,
            change24hUSD = change24hUSD,
            change24hPercent = change24hPercent,
            assets = assets.map {
                SharedAssetItem(
                    asset = it.asset,
                    name = it.name,
                    total = it.total,
                    priceUSD = it.priceUSD,
                    valueUSD = it.valueUSD,
                    percentOfPortfolio = it.percentOfPortfolio,
                    change24h = it.change24h
                )
            }
        )
    }
}

data class PortfolioCalculationResult(
    val assets: List<PortfolioAsset>,
    val totalNetWorthUSD: Double,
    val change24hUSD: Double,
    val change24hPercent: Double
)

data class DemoAccountData(
    val accountInfo: BinanceAccountInfo,
    val rawBalances: List<RawBalance>,
    val trades: List<BinanceTrade>
)
