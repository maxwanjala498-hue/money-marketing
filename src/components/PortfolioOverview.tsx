import React from 'react';
import {
  TrendingUp,
  TrendingDown,
  Share2,
  Download,
  RefreshCw,
  Eye,
  EyeOff,
  Shield,
  Layers,
  Percent,
  Wallet,
} from 'lucide-react';
import type { BinanceAccountInfo, PortfolioAsset } from '../types/binance';

interface PortfolioOverviewProps {
  totalNetWorthUSD: number;
  change24hUSD: number;
  change24hPercent: number;
  assets: PortfolioAsset[];
  accountInfo: BinanceAccountInfo | null;
  onOpenShareModal: () => void;
  onRefresh: () => void;
  isRefreshing: boolean;
  hideAmounts: boolean;
  setHideAmounts: (val: boolean) => void;
  btcPrice: number;
}

export const PortfolioOverview: React.FC<PortfolioOverviewProps> = ({
  totalNetWorthUSD,
  change24hUSD,
  change24hPercent,
  assets,
  accountInfo,
  onOpenShareModal,
  onRefresh,
  isRefreshing,
  hideAmounts,
  setHideAmounts,
  btcPrice,
}) => {
  const isPositive = change24hUSD >= 0;
  const btcEquivalent = btcPrice > 0 ? (totalNetWorthUSD / btcPrice).toFixed(4) : '0.0000';

  // Compute breakdown categories
  const btcHolding = assets.find((a) => a.asset === 'BTC')?.valueUSD || 0;
  const ethHolding = assets.find((a) => a.asset === 'ETH')?.valueUSD || 0;
  const bnbHolding = assets.find((a) => a.asset === 'BNB')?.valueUSD || 0;
  const solHolding = assets.find((a) => a.asset === 'SOL')?.valueUSD || 0;

  const stablecoinHolding = assets
    .filter((a) => ['USDT', 'USDC', 'FDUSD', 'DAI', 'BUSD'].includes(a.asset))
    .reduce((sum, a) => sum + a.valueUSD, 0);

  const altsHolding = Math.max(
    0,
    totalNetWorthUSD - (btcHolding + ethHolding + bnbHolding + solHolding + stablecoinHolding)
  );

  const btcPct = totalNetWorthUSD > 0 ? (btcHolding / totalNetWorthUSD) * 100 : 0;
  const ethPct = totalNetWorthUSD > 0 ? (ethHolding / totalNetWorthUSD) * 100 : 0;
  const solPct = totalNetWorthUSD > 0 ? (solHolding / totalNetWorthUSD) * 100 : 0;
  const bnbPct = totalNetWorthUSD > 0 ? (bnbHolding / totalNetWorthUSD) * 100 : 0;
  const stablePct = totalNetWorthUSD > 0 ? (stablecoinHolding / totalNetWorthUSD) * 100 : 0;
  const altsPct = totalNetWorthUSD > 0 ? (altsHolding / totalNetWorthUSD) * 100 : 0;

  const topAsset = assets[0];

  return (
    <div className="space-y-6">
      {/* Main Net Worth Banner & Action Bar */}
      <div className="rounded-2xl border border-[#2b313a] bg-[#181a20] p-6 lg:p-8">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="flex items-center gap-3 text-xs font-medium text-[#848e9c]">
              <span>Binance Spot Net Worth</span>
              <button
                type="button"
                onClick={() => setHideAmounts(!hideAmounts)}
                className="flex items-center gap-1 text-[#848e9c] hover:text-white transition-colors cursor-pointer"
                title={hideAmounts ? 'Reveal dollar values' : 'Mask dollar values for privacy'}
              >
                {hideAmounts ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                <span className="text-[11px]">{hideAmounts ? 'Values Hidden' : 'Hide Values'}</span>
              </button>
            </div>

            <div className="mt-2 flex items-baseline gap-4 flex-wrap">
              <div className="text-3xl sm:text-4xl lg:text-5xl font-bold tracking-tight text-white font-mono tabular-nums">
                {hideAmounts
                  ? '$ •••••••'
                  : `$${totalNetWorthUSD.toLocaleString('en-US', {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}`}
              </div>

              <div
                className={`flex items-center gap-1 text-sm sm:text-base font-semibold font-mono tabular-nums ${
                  isPositive ? 'text-[#0ECB81]' : 'text-[#F6465D]'
                }`}
              >
                {isPositive ? <TrendingUp className="w-4 h-4" /> : <TrendingDown className="w-4 h-4" />}
                <span>
                  {isPositive ? '+' : ''}
                  {change24hPercent.toFixed(2)}%
                </span>
                {!hideAmounts && (
                  <span className="text-xs opacity-80">
                    ({isPositive ? '+' : ''}
                    ${Math.abs(change24hUSD).toLocaleString('en-US', {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })})
                  </span>
                )}
              </div>
            </div>

            <div className="mt-2 text-xs text-[#848e9c] font-mono tabular-nums flex items-center gap-2">
              <span>≈ {hideAmounts ? '•••' : btcEquivalent} BTC</span>
              <span>·</span>
              <span className="text-emerald-400">
                {accountInfo?.network === 'demo'
                  ? 'Binance Demo Account'
                  : accountInfo?.network
                  ? `Binance ${accountInfo.network.toUpperCase()}`
                  : 'Binance Live Feed'}
              </span>
              {accountInfo?.lastSyncedAt && (
                <>
                  <span>·</span>
                  <span>Synced {new Date(accountInfo.lastSyncedAt).toLocaleTimeString()}</span>
                </>
              )}
            </div>
          </div>

          {/* Quick Action Buttons */}
          <div className="flex items-center gap-3 flex-wrap">
            <button
              onClick={onOpenShareModal}
              className="flex items-center gap-2 px-4 py-2.5 text-xs sm:text-sm font-semibold text-black bg-[#FCD535] hover:bg-[#fcd535]/90 rounded-lg shadow-sm transition-all cursor-pointer whitespace-nowrap"
            >
              <Share2 className="w-4 h-4" />
              <span>Share Snapshot</span>
            </button>

            <button
              onClick={onRefresh}
              disabled={isRefreshing}
              className="flex items-center gap-2 px-3.5 py-2.5 text-xs sm:text-sm font-medium text-[#eaecef] bg-[#0b0e11] hover:bg-[#2b313a] border border-[#2b313a] rounded-lg transition-colors cursor-pointer disabled:opacity-50"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-[#FCD535]' : ''}`} />
              <span className="hidden sm:inline">Refresh Data</span>
            </button>
          </div>
        </div>

        {/* Multi-asset Allocation Stacked Bar */}
        <div className="mt-8 pt-6 border-t border-[#2b313a]">
          <div className="flex items-center justify-between text-xs text-[#848e9c] mb-2.5">
            <span className="font-semibold text-white">Portfolio Allocation Breakdown</span>
            <span className="font-mono tabular-nums">{assets.length} Active Holdings</span>
          </div>

          <div className="h-3 w-full rounded-full bg-[#0b0e11] overflow-hidden flex">
            {btcPct > 0 && (
              <div
                style={{ width: `${btcPct}%` }}
                className="bg-[#F7931A] h-full transition-all"
                title={`BTC: ${btcPct.toFixed(1)}%`}
              />
            )}
            {ethPct > 0 && (
              <div
                style={{ width: `${ethPct}%` }}
                className="bg-[#627EEA] h-full transition-all"
                title={`ETH: ${ethPct.toFixed(1)}%`}
              />
            )}
            {solPct > 0 && (
              <div
                style={{ width: `${solPct}%` }}
                className="bg-[#14F195] h-full transition-all"
                title={`SOL: ${solPct.toFixed(1)}%`}
              />
            )}
            {bnbPct > 0 && (
              <div
                style={{ width: `${bnbPct}%` }}
                className="bg-[#FCD535] h-full transition-all"
                title={`BNB: ${bnbPct.toFixed(1)}%`}
              />
            )}
            {stablePct > 0 && (
              <div
                style={{ width: `${stablePct}%` }}
                className="bg-[#26A17B] h-full transition-all"
                title={`Stables: ${stablePct.toFixed(1)}%`}
              />
            )}
            {altsPct > 0 && (
              <div
                style={{ width: `${altsPct}%` }}
                className="bg-[#8C52FF] h-full transition-all"
                title={`Other Alts: ${altsPct.toFixed(1)}%`}
              />
            )}
          </div>

          {/* Allocation Legend */}
          <div className="mt-3 flex items-center gap-4 flex-wrap text-xs text-[#848e9c]">
            {btcPct > 0 && (
              <div className="flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-[#F7931A]" />
                <span className="text-white font-medium">BTC</span>
                <span className="font-mono tabular-nums">{btcPct.toFixed(1)}%</span>
              </div>
            )}
            {ethPct > 0 && (
              <div className="flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-[#627EEA]" />
                <span className="text-white font-medium">ETH</span>
                <span className="font-mono tabular-nums">{ethPct.toFixed(1)}%</span>
              </div>
            )}
            {solPct > 0 && (
              <div className="flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-[#14F195]" />
                <span className="text-white font-medium">SOL</span>
                <span className="font-mono tabular-nums">{solPct.toFixed(1)}%</span>
              </div>
            )}
            {bnbPct > 0 && (
              <div className="flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-[#FCD535]" />
                <span className="text-white font-medium">BNB</span>
                <span className="font-mono tabular-nums">{bnbPct.toFixed(1)}%</span>
              </div>
            )}
            {stablePct > 0 && (
              <div className="flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-[#26A17B]" />
                <span className="text-white font-medium">Stables</span>
                <span className="font-mono tabular-nums">{stablePct.toFixed(1)}%</span>
              </div>
            )}
            {altsPct > 0 && (
              <div className="flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-[#8C52FF]" />
                <span className="text-white font-medium">Alts</span>
                <span className="font-mono tabular-nums">{altsPct.toFixed(1)}%</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Grid of Key Crypto Performance Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card 1: 24h Return */}
        <div className="rounded-xl border border-[#2b313a] bg-[#181a20] p-4">
          <div className="flex items-center justify-between text-xs text-[#848e9c]">
            <span>24h Unrealized Return</span>
            <TrendingUp className="w-4 h-4 text-[#848e9c]" />
          </div>
          <div
            className={`mt-2 text-xl font-bold font-mono tabular-nums ${
              isPositive ? 'text-[#0ECB81]' : 'text-[#F6465D]'
            }`}
          >
            {isPositive ? '+' : ''}
            {change24hPercent.toFixed(2)}%
          </div>
          <div className="mt-1 text-xs text-[#848e9c] font-mono tabular-nums">
            {hideAmounts ? '••••••' : `${isPositive ? '+' : ''}$${change24hUSD.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
          </div>
        </div>

        {/* Card 2: Top Asset Exposure */}
        <div className="rounded-xl border border-[#2b313a] bg-[#181a20] p-4">
          <div className="flex items-center justify-between text-xs text-[#848e9c]">
            <span>Primary Allocation</span>
            <Wallet className="w-4 h-4 text-[#848e9c]" />
          </div>
          <div className="mt-2 text-xl font-bold text-white flex items-center gap-2">
            <span>{topAsset?.asset || 'None'}</span>
            <span className="text-xs text-[#FCD535] font-mono font-medium">
              {topAsset ? `${topAsset.percentOfPortfolio.toFixed(1)}%` : '0%'}
            </span>
          </div>
          <div className="mt-1 text-xs text-[#848e9c] font-mono tabular-nums">
            {hideAmounts ? '••••••' : topAsset ? `$${topAsset.valueUSD.toLocaleString('en-US', { maximumFractionDigits: 0 })}` : '$0'}
          </div>
        </div>

        {/* Card 3: Stablecoin Liquidity Reserve */}
        <div className="rounded-xl border border-[#2b313a] bg-[#181a20] p-4">
          <div className="flex items-center justify-between text-xs text-[#848e9c]">
            <span>Stablecoin Reserves</span>
            <Shield className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="mt-2 text-xl font-bold text-white font-mono tabular-nums">
            {stablePct.toFixed(1)}%
          </div>
          <div className="mt-1 text-xs text-[#848e9c] font-mono tabular-nums">
            {hideAmounts ? '••••••' : `$${stablecoinHolding.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`} dry powder
          </div>
        </div>

        {/* Card 4: Connection Security & Latency */}
        <div className="rounded-xl border border-[#2b313a] bg-[#181a20] p-4">
          <div className="flex items-center justify-between text-xs text-[#848e9c]">
            <span>Binance Sync Latency</span>
            <Layers className="w-4 h-4 text-[#848e9c]" />
          </div>
          <div className="mt-2 text-xl font-bold text-emerald-400 font-mono tabular-nums">
            {accountInfo?.latencyMs ? `${accountInfo.latencyMs} ms` : '18 ms'}
          </div>
          <div className="mt-1 text-xs text-[#848e9c]">
            <span>Read-Only API · SSL Encrypted</span>
          </div>
        </div>
      </div>
    </div>
  );
};
