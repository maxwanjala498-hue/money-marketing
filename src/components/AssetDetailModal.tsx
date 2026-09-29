import React, { useState, useEffect } from 'react';
import { X, ExternalLink, TrendingUp, TrendingDown, Clock, Activity, BarChart2 } from 'lucide-react';
import type { PortfolioAsset } from '../types/binance';
import { fetchKlines } from '../services/binanceApi';

interface AssetDetailModalProps {
  asset: PortfolioAsset | null;
  onClose: () => void;
  hideAmounts: boolean;
}

export const AssetDetailModal: React.FC<AssetDetailModalProps> = ({
  asset,
  onClose,
  hideAmounts,
}) => {
  const [klines, setKlines] = useState<number[]>([]);
  const [interval, setInterval] = useState<'1h' | '1d'>('1h');
  const [loadingChart, setLoadingChart] = useState(false);

  useEffect(() => {
    if (!asset) return;
    let isMounted = true;
    setLoadingChart(true);

    const pair = asset.asset === 'USDT' ? 'BTCUSDT' : `${asset.asset}USDT`;
    fetchKlines(pair, interval, 30).then((points) => {
      if (isMounted) {
        setKlines(points);
        setLoadingChart(false);
      }
    });

    return () => {
      isMounted = false;
    };
  }, [asset, interval]);

  if (!asset) return null;

  const isPositive = asset.change24h >= 0;
  const pairSymbol = asset.asset === 'USDT' ? 'BTCUSDT' : `${asset.asset}USDT`;
  const binanceTradeUrl = `https://www.binance.com/en/trade/${asset.asset}_USDT?type=spot`;

  // SVG Chart path calculation
  const minPrice = klines.length > 0 ? Math.min(...klines) : asset.low24h || asset.priceUSD * 0.95;
  const maxPrice = klines.length > 0 ? Math.max(...klines) : asset.high24h || asset.priceUSD * 1.05;
  const range = maxPrice - minPrice || 1;

  const width = 500;
  const height = 180;
  const padding = 15;

  let pathString = '';
  if (klines.length > 1) {
    const points = klines.map((price, i) => {
      const x = padding + (i / (klines.length - 1)) * (width - padding * 2);
      const y = height - padding - ((price - minPrice) / range) * (height - padding * 2);
      return `${x},${y}`;
    });
    pathString = `M ${points.join(' L ')}`;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="relative w-full max-w-xl rounded-2xl border border-[#2b313a] bg-[#181a20] text-[#eaecef] shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[#2b313a] p-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#2b313a] text-white font-bold text-sm">
              {asset.asset.slice(0, 3)}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-lg font-bold text-white">{asset.name}</h3>
                <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded bg-[#2b313a] text-[#848e9c]">
                  {asset.asset}
                </span>
              </div>
              <p className="text-xs text-[#848e9c]">Binance Spot Market Asset</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <a
              href={binanceTradeUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-[#FCD535] bg-[#FCD535]/10 hover:bg-[#FCD535]/20 rounded-lg transition-colors"
            >
              <span>Trade on Binance</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
            <button
              onClick={onClose}
              className="rounded-lg p-1.5 text-[#848e9c] hover:bg-[#2b313a] hover:text-white transition-colors cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Price & Holdings Summary */}
        <div className="p-6 space-y-6">
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
            <div>
              <div className="text-xs text-[#848e9c]">Current Price</div>
              <div className="text-xl font-bold font-mono tabular-nums text-white mt-0.5">
                ${asset.priceUSD >= 1 ? asset.priceUSD.toLocaleString('en-US', { minimumFractionDigits: 2 }) : asset.priceUSD.toFixed(5)}
              </div>
              <div className={`flex items-center gap-1 text-xs font-semibold mt-0.5 ${isPositive ? 'text-[#0ECB81]' : 'text-[#F6465D]'}`}>
                {isPositive ? <TrendingUp className="w-3.5 h-3.5" /> : <TrendingDown className="w-3.5 h-3.5" />}
                <span>{isPositive ? '+' : ''}{asset.change24h.toFixed(2)}% (24h)</span>
              </div>
            </div>

            <div>
              <div className="text-xs text-[#848e9c]">Your Holdings</div>
              <div className="text-xl font-bold font-mono tabular-nums text-white mt-0.5">
                {hideAmounts ? '••••' : asset.total.toLocaleString('en-US', { maximumFractionDigits: 6 })}
              </div>
              <div className="text-xs text-[#848e9c] mt-0.5">
                ≈ {hideAmounts ? '••••••' : `$${asset.valueUSD.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
              </div>
            </div>

            <div>
              <div className="text-xs text-[#848e9c]">Portfolio Share</div>
              <div className="text-xl font-bold font-mono tabular-nums text-[#FCD535] mt-0.5">
                {asset.percentOfPortfolio.toFixed(1)}%
              </div>
              <div className="text-xs text-[#848e9c] mt-0.5">
                {asset.locked > 0 ? `${asset.locked} locked in open orders` : 'All Available'}
              </div>
            </div>
          </div>

          {/* Price Chart Section */}
          <div className="rounded-xl border border-[#2b313a] bg-[#0b0e11] p-4">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2 text-xs font-semibold text-white">
                <BarChart2 className="w-4 h-4 text-[#FCD535]" />
                <span>Price Trend ({pairSymbol})</span>
              </div>
              <div className="flex items-center gap-1 bg-[#181a20] p-1 rounded-lg border border-[#2b313a]">
                <button
                  onClick={() => setInterval('1h')}
                  className={`px-2.5 py-0.5 text-[11px] font-mono rounded cursor-pointer transition-colors ${
                    interval === '1h' ? 'bg-[#FCD535] text-black font-semibold' : 'text-[#848e9c] hover:text-white'
                  }`}
                >
                  1H (24h)
                </button>
                <button
                  onClick={() => setInterval('1d')}
                  className={`px-2.5 py-0.5 text-[11px] font-mono rounded cursor-pointer transition-colors ${
                    interval === '1d' ? 'bg-[#FCD535] text-black font-semibold' : 'text-[#848e9c] hover:text-white'
                  }`}
                >
                  1D (30d)
                </button>
              </div>
            </div>

            {/* SVG Mini Chart */}
            <div className="relative h-44 w-full flex items-center justify-center">
              {loadingChart ? (
                <div className="text-xs text-[#848e9c] flex items-center gap-2">
                  <div className="w-3.5 h-3.5 border-2 border-[#FCD535] border-t-transparent rounded-full animate-spin" />
                  <span>Loading Binance klines...</span>
                </div>
              ) : pathString ? (
                <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-full overflow-visible">
                  <defs>
                    <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor={isPositive ? '#0ECB81' : '#F6465D'} stopOpacity="0.25" />
                      <stop offset="100%" stopColor={isPositive ? '#0ECB81' : '#F6465D'} stopOpacity="0.0" />
                    </linearGradient>
                  </defs>

                  {/* Gradient Area fill */}
                  <path
                    d={`${pathString} L ${width - padding},${height} L ${padding},${height} Z`}
                    fill="url(#chartGradient)"
                  />

                  {/* Line stroke */}
                  <path
                    d={pathString}
                    fill="none"
                    stroke={isPositive ? '#0ECB81' : '#F6465D'}
                    strokeWidth="2.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              ) : (
                <div className="text-xs text-[#848e9c]">Price history available on Binance exchange</div>
              )}
            </div>

            <div className="flex items-center justify-between text-[11px] font-mono text-[#848e9c] pt-2 border-t border-[#2b313a]/60">
              <span>Low: ${minPrice.toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
              <span>High: ${maxPrice.toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
