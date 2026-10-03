package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BinanceAccountInfo
import com.example.data.model.BinanceCredentials
import com.example.data.model.BinanceTrade
import com.example.data.model.MarketTicker
import com.example.data.model.PortfolioAsset
import com.example.data.model.PriceDirection
import com.example.data.model.RawBalance
import com.example.data.model.SharedSnapshotData
import com.example.data.model.Ticker24hrRaw
import com.example.data.repository.BinanceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    OVERVIEW("Overview"),
    ASSETS("Spot Assets"),
    MARKETS("Live Markets"),
    TRADES("Trade History"),
    SHARE("Share & Export")
}

data class BinanceUiState(
    val activeTab: AppTab = AppTab.OVERVIEW,
    val accountInfo: BinanceAccountInfo? = null,
    val rawBalances: List<RawBalance> = emptyList(),
    val portfolioAssets: List<PortfolioAsset> = emptyList(),
    val totalNetWorthUSD: Double = 0.0,
    val change24hUSD: Double = 0.0,
    val change24hPercent: Double = 0.0,
    val btcPrice: Double = 95400.0,
    val trades: List<BinanceTrade> = emptyList(),
    val marketTickers: List<MarketTicker> = emptyList(),
    val selectedAsset: PortfolioAsset? = null,
    val selectedAssetKlines: List<Double> = emptyList(),
    val klineInterval: String = "1h",
    val isLoadingKlines: Boolean = false,
    val hideAmounts: Boolean = false,
    val isRefreshing: Boolean = false,
    val showConnectDialog: Boolean = false,
    val showShareDialog: Boolean = false,
    val assetSearchQuery: String = "",
    val hideSmallBalances: Boolean = true,
    val assetSortField: String = "value",
    val assetSortAsc: Boolean = false,
    val tradeFilterSide: String = "all",
    val userNotification: String? = null,
    val lastPingLatency: Long = 0,
    val isPingTesting: Boolean = false
)

class BinanceViewModel(
    private val repository: BinanceRepository = BinanceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BinanceUiState())
    val uiState: StateFlow<BinanceUiState> = _uiState.asStateFlow()

    private var activeCredentials: BinanceCredentials? = null
    private var tickerPollingJob: Job? = null

    init {
        // Start live ticker polling & pre-populate demo account
        loadInitialData()
        startLivePolling()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val prices = repository.refreshTickers()
            val demo = repository.createDemoAccount(prices)
            val result = repository.calculatePortfolio(demo.rawBalances, prices)

            _uiState.update { current ->
                current.copy(
                    accountInfo = demo.accountInfo,
                    rawBalances = demo.rawBalances,
                    portfolioAssets = result.assets,
                    totalNetWorthUSD = result.totalNetWorthUSD,
                    change24hUSD = result.change24hUSD,
                    change24hPercent = result.change24hPercent,
                    btcPrice = prices["BTCUSDT"]?.price ?: 95400.0,
                    trades = demo.trades,
                    marketTickers = buildMarketTickers(prices, current.marketTickers),
                    isRefreshing = false
                )
            }
        }
    }

    private fun startLivePolling() {
        tickerPollingJob?.cancel()
        tickerPollingJob = viewModelScope.launch {
            while (isActive) {
                delay(12000L) // Binance live poll every 12 seconds
                val prices = repository.refreshTickers()
                recalculateWithNewPrices(prices)
            }
        }
    }

    private fun recalculateWithNewPrices(prices: Map<String, Ticker24hrRaw>) {
        val currentBalances = _uiState.value.rawBalances
        val result = repository.calculatePortfolio(currentBalances, prices)
        val oldTickers = _uiState.value.marketTickers

        _uiState.update { current ->
            current.copy(
                portfolioAssets = result.assets,
                totalNetWorthUSD = result.totalNetWorthUSD,
                change24hUSD = result.change24hUSD,
                change24hPercent = result.change24hPercent,
                btcPrice = prices["BTCUSDT"]?.price ?: current.btcPrice,
                marketTickers = buildMarketTickers(prices, oldTickers)
            )
        }
    }

    private fun buildMarketTickers(
        prices: Map<String, Ticker24hrRaw>,
        previousTickers: List<MarketTicker>
    ): List<MarketTicker> {
        val pairs = listOf(
            Pair("BTCUSDT", "BTC" to "Bitcoin"),
            Pair("ETHUSDT", "ETH" to "Ethereum"),
            Pair("SOLUSDT", "SOL" to "Solana"),
            Pair("BNBUSDT", "BNB" to "BNB"),
            Pair("XRPUSDT", "XRP" to "XRP"),
            Pair("DOGEUSDT", "DOGE" to "Dogecoin"),
            Pair("SUIUSDT", "SUI" to "Sui"),
            Pair("NEARUSDT", "NEAR" to "NEAR Protocol"),
            Pair("AVAXUSDT", "AVAX" to "Avalanche"),
            Pair("LINKUSDT", "LINK" to "Chainlink"),
            Pair("PEPEUSDT", "PEPE" to "Pepe"),
            Pair("SHIBUSDT", "SHIB" to "Shiba Inu"),
            Pair("ADAUSDT", "ADA" to "Cardano")
        )

        val prevMap = previousTickers.associateBy { it.symbol }

        return pairs.map { (symbol, info) ->
            val raw = prices[symbol]
            val prevPrice = prevMap[symbol]?.price ?: raw?.price ?: 0.0
            val currentPrice = raw?.price ?: prevPrice
            val direction = if (currentPrice > prevPrice) PriceDirection.UP
            else if (currentPrice < prevPrice) PriceDirection.DOWN
            else PriceDirection.NEUTRAL

            MarketTicker(
                symbol = symbol,
                baseAsset = info.first,
                name = info.second,
                price = currentPrice,
                change24h = raw?.change24h ?: 0.0,
                high24h = raw?.high ?: 0.0,
                low24h = raw?.low ?: 0.0,
                volume24h = raw?.volume ?: 0.0,
                direction = direction
            )
        }
    }

    fun setActiveTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val prices = repository.refreshTickers()

            val creds = activeCredentials
            if (creds != null) {
                val accResult = repository.connectAccount(creds)
                accResult.onSuccess { (info, balances) ->
                    val calc = repository.calculatePortfolio(balances, prices)
                    val trades = if (balances.isNotEmpty()) {
                        repository.fetchRecentTrades(creds, "${balances.first().asset}USDT")
                    } else emptyList()

                    _uiState.update {
                        it.copy(
                            accountInfo = info,
                            rawBalances = balances,
                            portfolioAssets = calc.assets,
                            totalNetWorthUSD = calc.totalNetWorthUSD,
                            change24hUSD = calc.change24hUSD,
                            change24hPercent = calc.change24hPercent,
                            trades = if (trades.isNotEmpty()) trades else it.trades,
                            isRefreshing = false,
                            userNotification = "Synced with Binance Spot successfully"
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            userNotification = "Sync error: ${err.message}"
                        )
                    }
                }
            } else {
                recalculateWithNewPrices(prices)
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        userNotification = "Live Binance market prices updated"
                    )
                }
            }
        }
    }

    fun connectApi(apiKey: String, apiSecret: String, network: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val creds = BinanceCredentials(apiKey.trim(), apiSecret.trim(), network)
            val result = repository.connectAccount(creds)

            result.onSuccess { (info, balances) ->
                activeCredentials = creds
                val prices = repository.refreshTickers()
                val calc = repository.calculatePortfolio(balances, prices)
                val trades = if (balances.isNotEmpty()) {
                    repository.fetchRecentTrades(creds, "${balances.first().asset}USDT")
                } else emptyList()

                _uiState.update {
                    it.copy(
                        accountInfo = info,
                        rawBalances = balances,
                        portfolioAssets = calc.assets,
                        totalNetWorthUSD = calc.totalNetWorthUSD,
                        change24hUSD = calc.change24hUSD,
                        change24hPercent = calc.change24hPercent,
                        trades = if (trades.isNotEmpty()) trades else it.trades,
                        isRefreshing = false,
                        showConnectDialog = false,
                        userNotification = "Connected to Binance (${info.apiKeyMasked})"
                    )
                }
                onComplete(true)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        userNotification = "Authentication failed: ${err.message}"
                    )
                }
                onComplete(false)
            }
        }
    }

    fun loadDemoAccount() {
        viewModelScope.launch {
            activeCredentials = null
            val prices = repository.refreshTickers()
            val demo = repository.createDemoAccount(prices)
            val calc = repository.calculatePortfolio(demo.rawBalances, prices)

            _uiState.update {
                it.copy(
                    accountInfo = demo.accountInfo,
                    rawBalances = demo.rawBalances,
                    portfolioAssets = calc.assets,
                    totalNetWorthUSD = calc.totalNetWorthUSD,
                    change24hUSD = calc.change24hUSD,
                    change24hPercent = calc.change24hPercent,
                    trades = demo.trades,
                    showConnectDialog = false,
                    userNotification = "Demo Binance Spot portfolio loaded"
                )
            }
        }
    }

    fun importCsv(csvContent: String) {
        viewModelScope.launch {
            val parsed = repository.parseCsvStatement(csvContent)
            if (parsed != null) {
                activeCredentials = null
                val (info, balances) = parsed
                val prices = repository.refreshTickers()
                val calc = repository.calculatePortfolio(balances, prices)

                _uiState.update {
                    it.copy(
                        accountInfo = info,
                        rawBalances = balances,
                        portfolioAssets = calc.assets,
                        totalNetWorthUSD = calc.totalNetWorthUSD,
                        change24hUSD = calc.change24hUSD,
                        change24hPercent = calc.change24hPercent,
                        showConnectDialog = false,
                        userNotification = "Imported ${balances.size} assets from CSV"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(userNotification = "Could not parse CSV format. Please export from Binance Spot.")
                }
            }
        }
    }

    fun disconnect() {
        activeCredentials = null
        loadDemoAccount()
        _uiState.update {
            it.copy(userNotification = "Disconnected API key. Switched to demo mode.")
        }
    }

    fun testPing(baseUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPingTesting = true) }
            val (success, latency) = repository.ping(baseUrl)
            _uiState.update {
                it.copy(
                    isPingTesting = false,
                    lastPingLatency = latency,
                    userNotification = if (success) "Binance API reachable (${latency}ms)" else "Ping failed"
                )
            }
        }
    }

    fun selectAsset(asset: PortfolioAsset?) {
        _uiState.update { it.copy(selectedAsset = asset) }
        if (asset != null) {
            loadKlines(asset.asset, _uiState.value.klineInterval)
        }
    }

    fun setKlineInterval(interval: String) {
        _uiState.update { it.copy(klineInterval = interval) }
        val current = _uiState.value.selectedAsset
        if (current != null) {
            loadKlines(current.asset, interval)
        }
    }

    private fun loadKlines(asset: String, interval: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingKlines = true) }
            val points = repository.fetchKlines(asset, interval, 30)
            _uiState.update {
                it.copy(
                    selectedAssetKlines = points,
                    isLoadingKlines = false
                )
            }
        }
    }

    fun toggleHideAmounts() {
        _uiState.update { it.copy(hideAmounts = !it.hideAmounts) }
    }

    fun setShowConnectDialog(show: Boolean) {
        _uiState.update { it.copy(showConnectDialog = show) }
    }

    fun setShowShareDialog(show: Boolean) {
        _uiState.update { it.copy(showShareDialog = show) }
    }

    fun setAssetSearchQuery(q: String) {
        _uiState.update { it.copy(assetSearchQuery = q) }
    }

    fun setHideSmallBalances(hide: Boolean) {
        _uiState.update { it.copy(hideSmallBalances = hide) }
    }

    fun setAssetSort(field: String) {
        _uiState.update { current ->
            if (current.assetSortField == field) {
                current.copy(assetSortAsc = !current.assetSortAsc)
            } else {
                current.copy(assetSortField = field, assetSortAsc = false)
            }
        }
    }

    fun setTradeFilterSide(side: String) {
        _uiState.update { it.copy(tradeFilterSide = side) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }

    fun createShareSnapshot(
        title: String,
        creatorName: String,
        hideBalances: Boolean
    ): SharedSnapshotData {
        val s = _uiState.value
        return repository.createSnapshot(
            title = title,
            creatorName = creatorName,
            network = s.accountInfo?.network ?: "demo",
            hideBalances = hideBalances,
            totalNetWorthUSD = s.totalNetWorthUSD,
            change24hUSD = s.change24hUSD,
            change24hPercent = s.change24hPercent,
            assets = s.portfolioAssets
        )
    }

    fun getExportCsvText(): String {
        val s = _uiState.value
        val sb = StringBuilder()
        sb.append("Asset,Name,Total Holdings,Price USD,Value USD,Portfolio %,24h Change %\n")
        s.portfolioAssets.forEach { a ->
            sb.append("${a.asset},\"${a.name}\",${a.total},${String.format("%.4f", a.priceUSD)},${String.format("%.2f", a.valueUSD)},${String.format("%.2f", a.percentOfPortfolio)},${String.format("%.2f", a.change24h)}\n")
        }
        return sb.toString()
    }

    fun getSocialShareText(creatorName: String, hideBalances: Boolean): String {
        val s = _uiState.value
        val top = s.portfolioAssets.take(5).joinToString("\n") { a ->
            "• ${a.asset}: ${String.format("%.1f", a.percentOfPortfolio)}% (${if (a.change24h >= 0) "+" else ""}${String.format("%.1f", a.change24h)}%)"
        }
        val isPos = s.change24hPercent >= 0
        val worthText = if (hideBalances) "[Hidden by Trader]" else "$${String.format("%,.2f", s.totalNetWorthUSD)}"

        return """
            📊 $creatorName's Binance Spot Portfolio
            
            📈 24h Return: ${if (isPos) "+" else ""}${String.format("%.2f", s.change24hPercent)}%
            💰 Net Worth: $worthText
            
            🏆 Top Allocations:
            $top
            
            🔒 Verified via Binance Spot API
        """.trimIndent()
    }
}
