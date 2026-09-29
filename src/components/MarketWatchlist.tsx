import React, { useState, useEffect } from 'react';
import { ExternalLink, TrendingUp, TrendingDown, Radio } from 'lucide-react';

interface TickerData {
  symbol: string;
  baseAsset: string;
  price: number;
  change24h: number;
  high24h: number;
  low24h: number;
  volume24h: number;
  lastUpdated: number;
  direction?: 'up' | 'down';
}

const DEFAULT_WATCHLIST_SYMBOLS = [
  { symbol: 'BTCUSDT', base: 'BTC', name: 'Bitcoin' },
  { symbol: 'ETHUSDT', base: 'ETH', name: 'Ethereum' },
  { symbol: 'SOLUSDT', base: 'SOL', name: 'Solana' },
  { symbol: 'BNBUSDT', base: 'BNB', name: 'BNB' },
  { symbol: 'XRPUSDT', base: 'XRP', name: 'XRP' },
  { symbol: 'DOGEUSDT', base: 'DOGE', name: 'Dogecoin' },
  { symbol: 'SUIUSDT', base: 'SUI', name: 'Sui' },
  { symbol: 'NEARUSDT', base: 'NEAR', name: 'NEAR Protocol' },
  { symbol: 'AVAXUSDT', base: 'AVAX', name: 'Avalanche' },
  { symbol: 'LINKUSDT', base: 'LINK', name: 'Chainlink' },
];

export const MarketWatchlist: React.FC = () => {
  const [tickers, setTickers] = useState<Record<string, TickerData>>({});
  const [isLiveConnected, setIsLiveConnected] = useState(false);

  useEffect(() => {
    let ws: WebSocket | null = null;
    let isSubscribed = true;

    // First fetch REST 24h tickers
    fetch('https://api.binance.com/api/v3/ticker/24hr')
      .then((res) => res.json())
      .then((data: any[]) => {
        if (!isSubscribed || !Array.isArray(data)) return;
        const initialMap: Record<string, TickerData> = {};
        for (const item of data) {
          const matched = DEFAULT_WATCHLIST_SYMBOLS.find((s) => s.symbol === item.symbol);
          if (matched) {
            initialMap[item.symbol] = {
              symbol: item.symbol,
              baseAsset: matched.base,
              price: parseFloat(item.lastPrice),
              change24h: parseFloat(item.priceChangePercent),
              high24h: parseFloat(item.highPrice),
              low24h: parseFloat(item.lowPrice),
              volume24h: parseFloat(item.quoteVolume),
              lastUpdated: Date.now(),
            };
          }
        }
        setTickers(initialMap);
      })
      .catch((err) => console.warn('Ticker fetch fallback', err));

    // Open Binance WebSocket stream for live ticker updates
    try {
      ws = new WebSocket('wss://stream.binance.com:9443/ws/!miniTicker@arr');

      ws.onopen = () => {
        if (isSubscribed) setIsLiveConnected(true);
      };

      ws.onmessage = (event) => {
        if (!isSubscribed) return;
        try {
          const streamData = JSON.parse(event.data);
          if (Array.isArray(streamData)) {
            setTickers((prev) => {
              const updated = { ...prev };
              let changed = false;

              for (const tick of streamData) {
                if (DEFAULT_WATCHLIST_SYMBOLS.some((s) => s.symbol === tick.s)) {
                  const currentPrice = parseFloat(tick.c);
                  const prevPrice = prev[tick.s]?.price || currentPrice;
                  const direction = currentPrice > prevPrice ? 'up' : currentPrice < prevPrice ? 'down' : undefined;

                  updated[tick.s] = {
                    symbol: tick.s,
                    baseAsset: DEFAULT_WATCHLIST_SYMBOLS.find((s) => s.symbol === tick.s)?.base || tick.s,
                    price: currentPrice,
                    change24h: prev[tick.s]?.change24h || 0,
                    high24h: parseFloat(tick.h) || prev[tick.s]?.high24h || currentPrice,
                    low24h: parseFloat(tick.l) || prev[tick.s]?.low24h || currentPrice,
                    volume24h: parseFloat(tick.q) || prev[tick.s]?.volume24h || 0,
                    lastUpdated: Date.now(),
                    direction,
                  };
                  changed = true;
                }
              }
              return changed ? updated : prev;
            });
          }
        } catch {
          // ignore parse errors
        }
      };

      ws.onerror = () => {
        if (isSubscribed) setIsLiveConnected(false);
      };

      ws.onclose = () => {
        if (isSubscribed) setIsLiveConnected(false);
      };
    } catch {
      setIsLiveConnected(false);
    }

    return () => {
      isSubscribed = false;
      if (ws) ws.close();
    };
  }, []);

  return (
    <div className="rounded-2xl border border-[#2b313a] bg-[#181a20] overflow-hidden">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 border-b border-[#2b313a]">
        <div>
          <h3 className="text-base font-semibold text-white">Binance Live Market Feed</h3>
          <p className="text-xs text-[#848e9c]">Real-time spot order book prices directly from Binance WebSocket streams</p>
        </div>

        <div className="flex items-center gap-2 text-xs font-mono">
          <Radio className={`w-3.5 h-3.5 ${isLiveConnected ? 'text-emerald-400 animate-pulse' : 'text-amber-400'}`} />
          <span className={isLiveConnected ? 'text-emerald-400' : 'text-amber-400'}>
            {isLiveConnected ? 'WebSocket Live Stream Active' : 'Polling REST Feed'}
          </span>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[#2b313a] bg-[#0b0e11]/60 text-[#848e9c] select-none">
              <th className="py-3 px-4 font-medium">Market Pair</th>
              <th className="py-3 px-4 font-medium text-right">Last Price</th>
              <th className="py-3 px-4 font-medium text-right">24h Change</th>
              <th className="py-3 px-4 font-medium text-right">24h High</th>
              <th className="py-3 px-4 font-medium text-right">24h Low</th>
              <th className="py-3 px-4 font-medium text-right">24h Volume (USDT)</th>
              <th className="py-3 px-4 text-right">Trade</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#2b313a]/50">
            {DEFAULT_WATCHLIST_SYMBOLS.map((pair) => {
              const ticker = tickers[pair.symbol];
              const isPositive = (ticker?.change24h || 0) >= 0;

              return (
                <tr
                  key={pair.symbol}
                  className="hover:bg-[#1f232b] transition-colors"
                >
                  <td className="py-3.5 px-4">
                    <div className="flex items-center gap-2.5">
                      <div className="flex h-7 w-7 items-center justify-center rounded-full bg-[#2b313a] text-white font-bold text-[10px]">
                        {pair.base.slice(0, 3)}
                      </div>
                      <div>
                        <div className="font-semibold text-white">
                          {pair.base} <span className="text-[#848e9c] font-normal">/ USDT</span>
                        </div>
                        <div className="text-[11px] text-[#848e9c]">{pair.name}</div>
                      </div>
                    </div>
                  </td>

                  <td className="py-3.5 px-4 text-right font-mono tabular-nums">
                    <span
                      className={`text-sm font-bold transition-colors duration-300 ${
                        ticker?.direction === 'up'
                          ? 'text-[#0ECB81]'
                          : ticker?.direction === 'down'
                          ? 'text-[#F6465D]'
                          : 'text-white'
                      }`}
                    >
                      {ticker
                        ? `$${ticker.price >= 1 ? ticker.price.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : ticker.price.toFixed(4)}`
                        : '—'}
                    </span>
                  </td>

                  <td className="py-3.5 px-4 text-right font-mono tabular-nums">
                    {ticker ? (
                      <span
                        className={`inline-flex items-center gap-1 font-semibold ${
                          isPositive ? 'text-[#0ECB81]' : 'text-[#F6465D]'
                        }`}
                      >
                        {isPositive ? <TrendingUp className="w-3.5 h-3.5" /> : <TrendingDown className="w-3.5 h-3.5" />}
                        {isPositive ? '+' : ''}
                        {ticker.change24h.toFixed(2)}%
                      </span>
                    ) : (
                      '—'
                    )}
                  </td>

                  <td className="py-3.5 px-4 text-right font-mono tabular-nums text-[#848e9c]">
                    {ticker ? `$${ticker.high24h >= 1 ? ticker.high24h.toLocaleString('en-US', { minimumFractionDigits: 2 }) : ticker.high24h.toFixed(4)}` : '—'}
                  </td>

                  <td className="py-3.5 px-4 text-right font-mono tabular-nums text-[#848e9c]">
                    {ticker ? `$${ticker.low24h >= 1 ? ticker.low24h.toLocaleString('en-US', { minimumFractionDigits: 2 }) : ticker.low24h.toFixed(4)}` : '—'}
                  </td>

                  <td className="py-3.5 px-4 text-right font-mono tabular-nums text-[#848e9c]">
                    {ticker ? `$${(ticker.volume24h / 1_000_000).toFixed(1)}M` : '—'}
                  </td>

                  <td className="py-3.5 px-4 text-right">
                    <a
                      href={`https://www.binance.com/en/trade/${pair.base}_USDT?type=spot`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1 px-2.5 py-1 text-[11px] font-medium text-[#FCD535] bg-[#FCD535]/10 hover:bg-[#FCD535]/20 rounded transition-colors"
                    >
                      <span>Trade</span>
                      <ExternalLink className="w-3 h-3" />
                    </a>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
