/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useCallback } from 'react';
import { Navbar } from './components/Navbar';
import { PortfolioOverview } from './components/PortfolioOverview';
import { AssetBalancesTable } from './components/AssetBalancesTable';
import { AssetDetailModal } from './components/AssetDetailModal';
import { MarketWatchlist } from './components/MarketWatchlist';
import { TradeHistoryList } from './components/TradeHistoryList';
import { ConnectionModal } from './components/ConnectionModal';
import { SharePortfolioModal } from './components/SharePortfolioModal';
import { SharedSnapshotView } from './components/SharedSnapshotView';
import vaultImage from './assets/images/binance_connection_vault_1790677921057.jpg';
import avatarImage from './assets/images/avatar_trader_profile_1790677932387.jpg';

import type {
  BinanceAccountInfo,
  BinanceCredentials,
  BinanceTrade,
  PortfolioAsset,
  RawBalance,
  SharedSnapshotData,
} from './types/binance';
import {
  calculatePortfolioAssets,
  connectBinanceAccount,
  createDemoBinanceAccount,
  fetchLiveTickers,
  fetchRecentTrades,
} from './services/binanceApi';
import { Key, ShieldCheck, Zap, ArrowRight, Share2, AlertCircle } from 'lucide-react';

export default function App() {
  const [accountInfo, setAccountInfo] = useState<BinanceAccountInfo | null>(null);
  const [credentials, setCredentials] = useState<BinanceCredentials | null>(null);
  const [rawBalances, setRawBalances] = useState<RawBalance[]>([]);
  const [trades, setTrades] = useState<BinanceTrade[]>([]);
  const [priceMap, setPriceMap] = useState<Record<string, { price: number; change24h: number; high: number; low: number; volume: number }>>({});
  const [activeTab, setActiveTab] = useState<'portfolio' | 'assets' | 'markets' | 'trades' | 'share'>('portfolio');
  const [selectedAsset, setSelectedAsset] = useState<PortfolioAsset | null>(null);

  const [isConnectModalOpen, setIsConnectModalOpen] = useState(false);
  const [isShareModalOpen, setIsShareModalOpen] = useState(false);
  const [hideAmounts, setHideAmounts] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [sharedSnapshot, setSharedSnapshot] = useState<SharedSnapshotData | null>(null);

  // Check for shared snapshot in URL
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const snapshotId = urlParams.get('snapshot');
    const hash = window.location.hash;

    if (snapshotId) {
      fetch(`/api/share/${snapshotId}`)
        .then((res) => res.json())
        .then((data) => {
          if (data.success && data.snapshot) {
            setSharedSnapshot(data.snapshot);
          }
        })
        .catch((err) => console.warn('Could not load shared snapshot from server:', err));
    } else if (hash.startsWith('#snapshot=')) {
      try {
        const encoded = hash.replace('#snapshot=', '');
        const decoded = JSON.parse(decodeURIComponent(escape(atob(decodeURIComponent(encoded)))));
        if (decoded) {
          setSharedSnapshot(decoded);
        }
      } catch (err) {
        console.warn('Could not decode snapshot hash:', err);
      }
    }
  }, []);

  // Fetch initial live prices and load demo account if not connected
  useEffect(() => {
    let isSubscribed = true;

    fetchLiveTickers().then((prices) => {
      if (!isSubscribed) return;
      setPriceMap(prices);

      // Pre-populate with realistic demo account so the user immediately experiences live Binance data
      const demo = createDemoBinanceAccount(prices);
      setAccountInfo(demo.accountInfo);
      setRawBalances(demo.rawBalances);
      setTrades(demo.trades);
    });

    // Refresh tickers every 15 seconds
    const interval = setInterval(() => {
      fetchLiveTickers().then((prices) => {
        if (isSubscribed) setPriceMap(prices);
      });
    }, 15000);

    return () => {
      isSubscribed = false;
      clearInterval(interval);
    };
  }, []);

  // Refresh balances & prices
  const handleRefresh = useCallback(async () => {
    setIsRefreshing(true);
    try {
      const prices = await fetchLiveTickers();
      setPriceMap(prices);

      if (credentials && credentials.apiKey) {
        const result = await connectBinanceAccount(credentials);
        if (result.success && result.accountInfo && result.rawBalances) {
          setAccountInfo(result.accountInfo);
          setRawBalances(result.rawBalances);

          // Fetch recent trades for top holdings
          if (result.rawBalances.length > 0) {
            const topPair = `${result.rawBalances[0].asset}USDT`;
            const fetchedTrades = await fetchRecentTrades(credentials, topPair);
            if (fetchedTrades.length > 0) setTrades(fetchedTrades);
          }
        }
      } else if (accountInfo?.network === 'demo') {
        const demo = createDemoBinanceAccount(prices);
        setAccountInfo(demo.accountInfo);
        setRawBalances(demo.rawBalances);
      }
    } catch (err) {
      console.error('Refresh error:', err);
    } finally {
      setIsRefreshing(false);
    }
  }, [credentials, accountInfo?.network]);

  // Connect via API key
  const handleConnectApi = async (newCreds: BinanceCredentials): Promise<boolean> => {
    const result = await connectBinanceAccount(newCreds);
    if (result.success && result.accountInfo && result.rawBalances) {
      setCredentials(newCreds);
      setAccountInfo(result.accountInfo);
      setRawBalances(result.rawBalances);

      // Also try fetching trades
      const topSymbol = result.rawBalances[0]?.asset ? `${result.rawBalances[0].asset}USDT` : 'BTCUSDT';
      const fetchedTrades = await fetchRecentTrades(newCreds, topSymbol);
      if (fetchedTrades.length > 0) {
        setTrades(fetchedTrades);
      }
      return true;
    }
    return false;
  };

  // Load demo account
  const handleLoadDemo = () => {
    const demo = createDemoBinanceAccount(priceMap);
    setCredentials(null);
    setAccountInfo(demo.accountInfo);
    setRawBalances(demo.rawBalances);
    setTrades(demo.trades);
  };

  // Import CSV statement
  const handleImportCsv = (csvText: string) => {
    try {
      const lines = csvText.split('\n');
      const parsedBalances: RawBalance[] = [];

      for (let i = 1; i < lines.length; i++) {
        const parts = lines[i].split(',').map((p) => p.replace(/"/g, '').trim());
        if (parts.length >= 2) {
          const asset = parts[0]?.toUpperCase();
          const amount = parseFloat(parts[1]) || 0;
          if (asset && amount > 0) {
            parsedBalances.push({
              asset,
              free: amount.toString(),
              locked: '0',
            });
          }
        }
      }

      if (parsedBalances.length > 0) {
        setRawBalances(parsedBalances);
        setAccountInfo({
          isConnected: true,
          network: 'demo',
          accountType: 'SPOT (Imported CSV)',
          updateTime: Date.now(),
          canTrade: false,
          canWithdraw: false,
          canDeposit: false,
          apiKeyMasked: 'CSV_STATEMENT_DATA',
          latencyMs: 5,
          lastSyncedAt: Date.now(),
        });
      }
    } catch (err) {
      console.error('Failed to parse statement CSV:', err);
    }
  };

  // Disconnect
  const handleDisconnect = () => {
    setAccountInfo(null);
    setCredentials(null);
    setRawBalances([]);
    setTrades([]);
  };

  // Calculate portfolio stats
  const { assets, totalNetWorthUSD, change24hUSD, change24hPercent } = calculatePortfolioAssets(
    rawBalances,
    priceMap
  );

  const btcPrice = priceMap['BTCUSDT']?.price || 95000;

  // If viewing a shared snapshot
  if (sharedSnapshot) {
    return (
      <SharedSnapshotView
        snapshot={sharedSnapshot}
        onGoToApp={() => {
          setSharedSnapshot(null);
          window.history.replaceState({}, document.title, window.location.pathname);
        }}
        onConnectOwnAccount={() => {
          setSharedSnapshot(null);
          window.history.replaceState({}, document.title, window.location.pathname);
          setIsConnectModalOpen(true);
        }}
      />
    );
  }

  return (
    <div className="min-h-screen bg-[#0b0e11] text-[#eaecef] font-sans flex flex-col">
      {/* Top Bar Contract */}
      <Navbar
        accountInfo={accountInfo}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onOpenConnectModal={() => setIsConnectModalOpen(true)}
        onRefresh={handleRefresh}
        isRefreshing={isRefreshing}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
        {/* Connection Hero Callout if disconnected */}
        {!accountInfo?.isConnected && (
          <div className="rounded-2xl border border-[#2b313a] bg-gradient-to-r from-[#181a20] via-[#1f232b] to-[#181a20] p-6 lg:p-8 flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="flex items-center gap-5">
              <img
                src={vaultImage}
                alt="Binance Connection Vault"
                referrerPolicy="no-referrer"
                className="w-16 h-16 sm:w-20 sm:h-20 rounded-2xl object-cover border border-[#FCD535]/30 shadow-lg shrink-0"
              />
              <div>
                <div className="flex items-center gap-2 text-xs font-semibold text-[#FCD535]">
                  <ShieldCheck className="w-4 h-4" />
                  <span>Secure Read-Only Integration</span>
                </div>
                <h2 className="text-xl sm:text-2xl font-bold text-white mt-1">
                  Connect & Share Your Binance Spot Account
                </h2>
                <p className="text-xs sm:text-sm text-[#848e9c] max-w-xl mt-1">
                  Sync spot balances, view live order book valuations, and generate shareable performance cards with customizable privacy.
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3 shrink-0 w-full md:w-auto">
              <button
                onClick={() => setIsConnectModalOpen(true)}
                className="w-full md:w-auto flex items-center justify-center gap-2 px-5 py-3 text-xs sm:text-sm font-semibold text-black bg-[#FCD535] hover:bg-[#fcd535]/90 rounded-lg shadow-sm transition-all cursor-pointer"
              >
                <Key className="w-4 h-4" />
                <span>Connect Account</span>
              </button>
              <button
                onClick={handleLoadDemo}
                className="w-full md:w-auto flex items-center justify-center gap-2 px-4 py-3 text-xs sm:text-sm font-medium text-white bg-[#0b0e11] hover:bg-[#2b313a] border border-[#2b313a] rounded-lg transition-colors cursor-pointer"
              >
                <Zap className="w-4 h-4 text-[#FCD535]" />
                <span>Try Demo</span>
              </button>
            </div>
          </div>
        )}

        {/* Tab 1: Overview */}
        {activeTab === 'portfolio' && (
          <div className="space-y-6">
            <PortfolioOverview
              totalNetWorthUSD={totalNetWorthUSD}
              change24hUSD={change24hUSD}
              change24hPercent={change24hPercent}
              assets={assets}
              accountInfo={accountInfo}
              onOpenShareModal={() => setIsShareModalOpen(true)}
              onRefresh={handleRefresh}
              isRefreshing={isRefreshing}
              hideAmounts={hideAmounts}
              setHideAmounts={setHideAmounts}
              btcPrice={btcPrice}
            />

            <AssetBalancesTable
              assets={assets}
              hideAmounts={hideAmounts}
              onSelectAsset={(asset) => setSelectedAsset(asset)}
            />
          </div>
        )}

        {/* Tab 2: Spot Assets */}
        {activeTab === 'assets' && (
          <AssetBalancesTable
            assets={assets}
            hideAmounts={hideAmounts}
            onSelectAsset={(asset) => setSelectedAsset(asset)}
          />
        )}

        {/* Tab 3: Live Markets */}
        {activeTab === 'markets' && <MarketWatchlist />}

        {/* Tab 4: Trade History */}
        {activeTab === 'trades' && (
          <TradeHistoryList
            trades={trades}
            onRefreshTrades={handleRefresh}
          />
        )}

        {/* Tab 5: Share & Export */}
        {activeTab === 'share' && (
          <div className="space-y-6">
            <div className="rounded-2xl border border-[#2b313a] bg-[#181a20] p-6 lg:p-8">
              <div className="flex flex-col md:flex-row items-center justify-between gap-6">
                <div>
                  <div className="flex items-center gap-2 text-xs font-semibold text-[#FCD535]">
                    <Share2 className="w-4 h-4" />
                    <span>Portfolio Sharing & Export Suite</span>
                  </div>
                  <h2 className="text-xl sm:text-2xl font-bold text-white mt-1">
                    Share Your Binance Portfolio with the World
                  </h2>
                  <p className="text-xs sm:text-sm text-[#848e9c] max-w-xl mt-1">
                    Generate cryptographic verified snapshot links, download high-res image cards, copy summary posts for social channels, or export accounting CSVs.
                  </p>
                </div>

                <button
                  onClick={() => setIsShareModalOpen(true)}
                  className="flex items-center gap-2 px-6 py-3 text-xs sm:text-sm font-semibold text-black bg-[#FCD535] hover:bg-[#fcd535]/90 rounded-lg shadow-sm transition-all cursor-pointer whitespace-nowrap"
                >
                  <Share2 className="w-4 h-4" />
                  <span>Open Snapshot Creator</span>
                </button>
              </div>
            </div>

            {/* Quick action grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div
                onClick={() => setIsShareModalOpen(true)}
                className="rounded-xl border border-[#2b313a] bg-[#181a20] p-5 hover:border-[#FCD535]/50 transition-all cursor-pointer group"
              >
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-[#FCD535]/10 text-[#FCD535] mb-3">
                  <Share2 className="w-5 h-5" />
                </div>
                <h4 className="text-sm font-bold text-white group-hover:text-[#FCD535] transition-colors">
                  Shareable Snapshot Link
                </h4>
                <p className="text-xs text-[#848e9c] mt-1">
                  Create a public link for followers, investors, or friends to view your portfolio allocation without revealing private keys.
                </p>
              </div>

              <div
                onClick={() => setIsShareModalOpen(true)}
                className="rounded-xl border border-[#2b313a] bg-[#181a20] p-5 hover:border-[#FCD535]/50 transition-all cursor-pointer group"
              >
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-emerald-500/10 text-emerald-400 mb-3">
                  <ShieldCheck className="w-5 h-5" />
                </div>
                <h4 className="text-sm font-bold text-white group-hover:text-emerald-400 transition-colors">
                  Privacy Protection Mode
                </h4>
                <p className="text-xs text-[#848e9c] mt-1">
                  Mask dollar numbers ($) with asterisks to protect your bankroll while showcasing exact percentage returns and asset weights.
                </p>
              </div>

              <div
                onClick={() => setIsShareModalOpen(true)}
                className="rounded-xl border border-[#2b313a] bg-[#181a20] p-5 hover:border-[#FCD535]/50 transition-all cursor-pointer group"
              >
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-500/10 text-blue-400 mb-3">
                  <ArrowRight className="w-5 h-5" />
                </div>
                <h4 className="text-sm font-bold text-white group-hover:text-blue-400 transition-colors">
                  PNG & CSV Export
                </h4>
                <p className="text-xs text-[#848e9c] mt-1">
                  Download high-resolution image cards suitable for Twitter / Discord, or export raw CSV ledgers for tax calculation.
                </p>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Modals */}
      <ConnectionModal
        isOpen={isConnectModalOpen}
        onClose={() => setIsConnectModalOpen(false)}
        onConnectApi={handleConnectApi}
        onLoadDemo={handleLoadDemo}
        onImportCsv={handleImportCsv}
        accountInfo={accountInfo}
        onDisconnect={handleDisconnect}
      />

      <SharePortfolioModal
        isOpen={isShareModalOpen}
        onClose={() => setIsShareModalOpen(false)}
        totalNetWorthUSD={totalNetWorthUSD}
        change24hUSD={change24hUSD}
        change24hPercent={change24hPercent}
        assets={assets}
        accountInfo={accountInfo}
      />

      <AssetDetailModal
        asset={selectedAsset}
        onClose={() => setSelectedAsset(null)}
        hideAmounts={hideAmounts}
      />

      {/* Footer */}
      <footer className="mt-auto border-t border-[#2b313a] bg-[#0b0e11] py-6 text-xs text-[#848e9c]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <span className="text-white font-semibold">Binance Connect & Share</span>
            <span>·</span>
            <span>Read-Only API Gateway</span>
          </div>

          <div className="flex items-center gap-4 text-[11px]">
            <span>Binance Global · Binance US · Spot Testnet</span>
            <span>·</span>
            <span className="text-emerald-400">HMAC-SHA256 Encrypted</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
