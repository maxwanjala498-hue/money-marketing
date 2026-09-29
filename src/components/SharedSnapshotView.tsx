import React from 'react';
import {
  ShieldCheck,
  TrendingUp,
  TrendingDown,
  ArrowLeft,
  Key,
  Calendar,
  Lock,
} from 'lucide-react';
import type { SharedSnapshotData } from '../types/binance';

interface SharedSnapshotViewProps {
  snapshot: SharedSnapshotData;
  onGoToApp: () => void;
  onConnectOwnAccount: () => void;
}

export const SharedSnapshotView: React.FC<SharedSnapshotViewProps> = ({
  snapshot,
  onGoToApp,
  onConnectOwnAccount,
}) => {
  const isPositive = snapshot.change24hPercent >= 0;
  const createdDate = new Date(snapshot.createdAt);

  return (
    <div className="min-h-screen bg-[#0b0e11] text-[#eaecef] p-4 sm:p-6 lg:p-8">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Navigation Bar */}
        <div className="flex items-center justify-between border-b border-[#2b313a] pb-4">
          <button
            onClick={onGoToApp}
            className="flex items-center gap-2 text-xs sm:text-sm text-[#848e9c] hover:text-white transition-colors cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Open Binance Connect App</span>
          </button>

          <button
            onClick={onConnectOwnAccount}
            className="flex items-center gap-2 px-3.5 py-1.5 text-xs font-semibold text-black bg-[#FCD535] hover:bg-[#fcd535]/90 rounded-lg shadow-sm transition-all cursor-pointer"
          >
            <Key className="w-3.5 h-3.5" />
            <span>Connect Your Binance</span>
          </button>
        </div>

        {/* Snapshot Header Card */}
        <div className="rounded-2xl border border-[#2b313a] bg-[#181a20] p-6 lg:p-8 relative overflow-hidden">
          <div className="absolute top-0 left-0 right-0 h-1 bg-[#FCD535]" />

          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 text-xs font-semibold text-[#848e9c]">
                <span className="uppercase tracking-wider">Shared Portfolio Snapshot</span>
                <span>·</span>
                <span className="flex items-center gap-1 text-emerald-400">
                  <ShieldCheck className="w-3.5 h-3.5" />
                  <span>Verified Binance Spot Data</span>
                </span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-bold text-white mt-1">
                {snapshot.title}
              </h1>
              <div className="flex items-center gap-3 text-xs text-[#848e9c] mt-2">
                <span>By {snapshot.creatorName}</span>
                <span>·</span>
                <span className="flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5" />
                  <span>{createdDate.toLocaleDateString()} at {createdDate.toLocaleTimeString()}</span>
                </span>
              </div>
            </div>

            <div className="text-left sm:text-right">
              <div className="text-xs text-[#848e9c]">Spot Net Worth</div>
              <div className="text-3xl sm:text-4xl font-bold font-mono tabular-nums text-white">
                {snapshot.hideBalances
                  ? '$ •••••••••'
                  : `$${snapshot.totalNetWorthUSD.toLocaleString('en-US', {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}`}
              </div>
              <div
                className={`flex items-center gap-1 text-sm font-semibold font-mono tabular-nums sm:justify-end mt-1 ${
                  isPositive ? 'text-[#0ECB81]' : 'text-[#F6465D]'
                }`}
              >
                {isPositive ? <TrendingUp className="w-4 h-4" /> : <TrendingDown className="w-4 h-4" />}
                <span>{isPositive ? '+' : ''}{snapshot.change24hPercent.toFixed(2)}% (24h)</span>
              </div>
            </div>
          </div>

          {/* Allocation Bar */}
          <div className="mt-8 pt-6 border-t border-[#2b313a]">
            <div className="text-xs font-semibold text-white mb-2">Asset Allocations</div>
            <div className="h-3 w-full rounded-full bg-[#0b0e11] overflow-hidden flex">
              {snapshot.assets.map((asset, i) => (
                <div
                  key={asset.asset}
                  style={{
                    width: `${asset.percentOfPortfolio}%`,
                    backgroundColor: ['#F7931A', '#627EEA', '#14F195', '#FCD535', '#26A17B', '#8C52FF'][i % 6],
                  }}
                  className="h-full transition-all"
                  title={`${asset.asset}: ${asset.percentOfPortfolio.toFixed(1)}%`}
                />
              ))}
            </div>
          </div>
        </div>

        {/* Assets Table */}
        <div className="rounded-2xl border border-[#2b313a] bg-[#181a20] overflow-hidden">
          <div className="p-5 border-b border-[#2b313a]">
            <h3 className="text-base font-semibold text-white">Verified Asset Breakdown</h3>
            <p className="text-xs text-[#848e9c]">Holdings and market prices at the time of export</p>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-[#2b313a] bg-[#0b0e11]/60 text-[#848e9c]">
                  <th className="py-3 px-4 font-medium">Asset</th>
                  <th className="py-3 px-4 font-medium text-right">Holdings</th>
                  <th className="py-3 px-4 font-medium text-right">Price (USD)</th>
                  <th className="py-3 px-4 font-medium text-right">24h Change</th>
                  <th className="py-3 px-4 font-medium text-right">Value (USD)</th>
                  <th className="py-3 px-4 font-medium text-right">Allocation</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#2b313a]/50">
                {snapshot.assets.map((asset) => {
                  const assetPos = asset.change24h >= 0;
                  return (
                    <tr key={asset.asset} className="hover:bg-[#1f232b]">
                      <td className="py-3.5 px-4">
                        <div className="font-semibold text-white">{asset.asset}</div>
                        <div className="text-[11px] text-[#848e9c]">{asset.name}</div>
                      </td>

                      <td className="py-3.5 px-4 text-right font-mono tabular-nums text-white">
                        {snapshot.hideBalances ? '••••' : asset.total.toLocaleString('en-US', { maximumFractionDigits: 4 })}
                      </td>

                      <td className="py-3.5 px-4 text-right font-mono tabular-nums text-white">
                        ${asset.priceUSD >= 1 ? asset.priceUSD.toLocaleString('en-US', { minimumFractionDigits: 2 }) : asset.priceUSD.toFixed(5)}
                      </td>

                      <td className="py-3.5 px-4 text-right font-mono tabular-nums">
                        <span className={`font-semibold ${assetPos ? 'text-[#0ECB81]' : 'text-[#F6465D]'}`}>
                          {assetPos ? '+' : ''}{asset.change24h.toFixed(2)}%
                        </span>
                      </td>

                      <td className="py-3.5 px-4 text-right font-mono tabular-nums font-semibold text-white">
                        {snapshot.hideBalances
                          ? '$ ••••••'
                          : `$${asset.valueUSD.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
                      </td>

                      <td className="py-3.5 px-4 text-right font-mono tabular-nums text-[#FCD535] font-medium">
                        {asset.percentOfPortfolio.toFixed(1)}%
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        {/* Security & Verification Footer */}
        <div className="rounded-xl border border-[#2b313a] bg-[#0b0e11] p-5 text-center space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs text-emerald-400 font-medium">
            <ShieldCheck className="w-4 h-4" />
            <span>Cryptographically Verified Binance Spot Connection</span>
          </div>
          <p className="text-xs text-[#848e9c] max-w-lg mx-auto">
            This portfolio snapshot was exported directly from a connected Binance account using Read-Only API permissions. Want to track and share your own Binance holdings?
          </p>
          <div className="pt-2">
            <button
              onClick={onConnectOwnAccount}
              className="inline-flex items-center gap-2 px-5 py-2.5 text-xs font-semibold text-black bg-[#FCD535] hover:bg-[#fcd535]/90 rounded-lg shadow-sm transition-all cursor-pointer"
            >
              <Key className="w-3.5 h-3.5" />
              <span>Connect Your Binance Account</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
