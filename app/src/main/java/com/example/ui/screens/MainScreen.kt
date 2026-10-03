package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AssetBalancesSection
import com.example.ui.components.AssetDetailBottomSheet
import com.example.ui.components.ConnectionDialog
import com.example.ui.components.MarketWatchlistSection
import com.example.ui.components.NavbarHeader
import com.example.ui.components.PortfolioOverviewSection
import com.example.ui.components.SharePortfolioDialog
import com.example.ui.components.TradeHistorySection
import com.example.ui.theme.BinanceBackground
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.BinanceViewModel

@Composable
fun MainScreen(
    viewModel: BinanceViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back press: if not on Overview tab, return to Overview
    BackHandler(enabled = uiState.activeTab != AppTab.OVERVIEW) {
        viewModel.setActiveTab(AppTab.OVERVIEW)
    }

    // Show notifications in snackbar
    LaunchedEffect(uiState.userNotification) {
        val msg = uiState.userNotification
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = BinanceBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            NavbarHeader(
                accountInfo = uiState.accountInfo,
                activeTab = uiState.activeTab,
                onTabSelected = { tab ->
                    if (tab == AppTab.SHARE) {
                        viewModel.setShowShareDialog(true)
                    } else {
                        viewModel.setActiveTab(tab)
                    }
                },
                onOpenConnect = { viewModel.setShowConnectDialog(true) },
                onRefresh = { viewModel.refresh() },
                isRefreshing = uiState.isRefreshing
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BinanceBackground)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (uiState.activeTab) {
                    AppTab.OVERVIEW -> {
                        PortfolioOverviewSection(
                            totalNetWorthUSD = uiState.totalNetWorthUSD,
                            change24hUSD = uiState.change24hUSD,
                            change24hPercent = uiState.change24hPercent,
                            assets = uiState.portfolioAssets,
                            accountInfo = uiState.accountInfo,
                            btcPrice = uiState.btcPrice,
                            hideAmounts = uiState.hideAmounts,
                            onToggleHideAmounts = { viewModel.toggleHideAmounts() },
                            onOpenShare = { viewModel.setShowShareDialog(true) },
                            onRefresh = { viewModel.refresh() },
                            isRefreshing = uiState.isRefreshing
                        )
                        // Also show top spot balances summary in Overview
                        AssetBalancesSection(
                            assets = uiState.portfolioAssets,
                            hideAmounts = uiState.hideAmounts,
                            searchQuery = uiState.assetSearchQuery,
                            onSearchQueryChange = { viewModel.setAssetSearchQuery(it) },
                            hideSmallBalances = uiState.hideSmallBalances,
                            onToggleHideSmallBalances = { viewModel.setHideSmallBalances(it) },
                            sortField = uiState.assetSortField,
                            sortAsc = uiState.assetSortAsc,
                            onSortChange = { viewModel.setAssetSort(it) },
                            onSelectAsset = { viewModel.selectAsset(it) }
                        )
                    }

                    AppTab.ASSETS -> {
                        AssetBalancesSection(
                            assets = uiState.portfolioAssets,
                            hideAmounts = uiState.hideAmounts,
                            searchQuery = uiState.assetSearchQuery,
                            onSearchQueryChange = { viewModel.setAssetSearchQuery(it) },
                            hideSmallBalances = uiState.hideSmallBalances,
                            onToggleHideSmallBalances = { viewModel.setHideSmallBalances(it) },
                            sortField = uiState.assetSortField,
                            sortAsc = uiState.assetSortAsc,
                            onSortChange = { viewModel.setAssetSort(it) },
                            onSelectAsset = { viewModel.selectAsset(it) }
                        )
                    }

                    AppTab.MARKETS -> {
                        MarketWatchlistSection(
                            tickers = uiState.marketTickers,
                            onSelectPair = { base ->
                                val matched = uiState.portfolioAssets.firstOrNull { it.asset == base }
                                if (matched != null) {
                                    viewModel.selectAsset(matched)
                                }
                            }
                        )
                    }

                    AppTab.TRADES -> {
                        TradeHistorySection(
                            trades = uiState.trades,
                            filterSide = uiState.tradeFilterSide,
                            onFilterChange = { viewModel.setTradeFilterSide(it) }
                        )
                    }

                    AppTab.SHARE -> {
                        // Handled by dialog
                    }
                }
            }
        }

        // Bottom Sheet for selected asset detail
        AssetDetailBottomSheet(
            asset = uiState.selectedAsset,
            klines = uiState.selectedAssetKlines,
            interval = uiState.klineInterval,
            isLoadingKlines = uiState.isLoadingKlines,
            hideAmounts = uiState.hideAmounts,
            onIntervalChange = { viewModel.setKlineInterval(it) },
            onDismiss = { viewModel.selectAsset(null) }
        )

        // Connection Dialog
        ConnectionDialog(
            isOpen = uiState.showConnectDialog,
            accountInfo = uiState.accountInfo,
            isRefreshing = uiState.isRefreshing,
            lastPingLatency = uiState.lastPingLatency,
            isPingTesting = uiState.isPingTesting,
            onTestPing = { viewModel.testPing(it) },
            onConnectApi = { key, secret, net, onDone ->
                viewModel.connectApi(key, secret, net, onDone)
            },
            onLoadDemo = { viewModel.loadDemoAccount() },
            onImportCsv = { viewModel.importCsv(it) },
            onDisconnect = { viewModel.disconnect() },
            onDismiss = { viewModel.setShowConnectDialog(false) }
        )

        // Share Dialog
        SharePortfolioDialog(
            isOpen = uiState.showShareDialog,
            totalNetWorthUSD = uiState.totalNetWorthUSD,
            change24hUSD = uiState.change24hUSD,
            change24hPercent = uiState.change24hPercent,
            assets = uiState.portfolioAssets,
            onGetSocialText = { name, hide -> viewModel.getSocialShareText(name, hide) },
            onGetCsvText = { viewModel.getExportCsvText() },
            onDismiss = { viewModel.setShowShareDialog(false) }
        )
    }
}
