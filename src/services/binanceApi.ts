import type {
  BinanceAccountInfo,
  BinanceCredentials,
  BinanceNetwork,
  BinanceTrade,
  MarketTicker,
  PortfolioAsset,
  RawBalance,
} from '../types/binance';

// Common asset names mapping
export const ASSET_NAMES: Record<string, string> = {
  BTC: 'Bitcoin',
  ETH: 'Ethereum',
  BNB: 'BNB',
  SOL: 'Solana',
  USDT: 'Tether USD',
  USDC: 'USD Coin',
  FDUSD: 'First Digital USD',
  XRP: 'XRP',
  ADA: 'Cardano',
  AVAX: 'Avalanche',
  DOGE: 'Dogecoin',
  DOT: 'Polkadot',
  LINK: 'Chainlink',
  NEAR: 'NEAR Protocol',
  SUI: 'Sui',
  RENDER: 'Render',
  TAO: 'Bittensor',
  PEPE: 'Pepe',
  SHIB: 'Shiba Inu',
  LTC: 'Litecoin',
  UNI: 'Uniswap',
  MATIC: 'Polygon',
  POL: 'Polygon',
  APT: 'Aptos',
  ICP: 'Internet Computer',
  FET: 'Artificial Superintelligence Alliance',
  INJ: 'Injective',
  TRX: 'TRON',
};

// Ping Binance API
export async function pingBinance(network: BinanceNetwork = 'global'): Promise<{ success: boolean; latency: number }> {
  try {
    let endpoint = 'https://api.binance.com';
    if (network === 'us') endpoint = 'https://api.binance.us';
    if (network === 'testnet') endpoint = 'https://testnet.binance.vision';

    const start = performance.now();
    const res = await fetch(`/api/binance/ping?endpoint=${encodeURIComponent(endpoint)}`);
    const latency = Math.round(performance.now() - start);

    if (res.ok) {
      const data = await res.json();
      return { success: true, latency: data.latency || latency };
    }
    return { success: false, latency: 0 };
  } catch {
    return { success: false, latency: 0 };
  }
}

// Fetch live 24hr tickers from Binance public API
export async function fetchLiveTickers(): Promise<Record<string, { price: number; change24h: number; high: number; low: number; volume: number }>> {
  try {
    // Try direct public Binance API first
    let res = await fetch('https://api.binance.com/api/v3/ticker/24hr', {
      headers: { Accept: 'application/json' },
    });

    if (!res.ok) {
      // Fallback to server proxy
      res = await fetch('/api/binance/tickers');
    }

    if (!res.ok) throw new Error('Failed to fetch tickers');

    const data: Array<{
      symbol: string;
      lastPrice: string;
      priceChangePercent: string;
      highPrice: string;
      lowPrice: string;
      quoteVolume: string;
    }> = await res.json();

    const priceMap: Record<string, { price: number; change24h: number; high: number; low: number; volume: number }> = {};
    for (const item of data) {
      priceMap[item.symbol] = {
        price: parseFloat(item.lastPrice) || 0,
        change24h: parseFloat(item.priceChangePercent) || 0,
        high: parseFloat(item.highPrice) || 0,
        low: parseFloat(item.lowPrice) || 0,
        volume: parseFloat(item.quoteVolume) || 0,
      };
    }
    return priceMap;
  } catch (err) {
    console.warn('Could not fetch direct tickers, using fallback price cache:', err);
    return FALLBACK_PRICES;
  }
}

// Fetch historical candlesticks / klines for mini sparkline & detail chart
export async function fetchKlines(symbol: string, interval = '1h', limit = 24): Promise<number[]> {
  try {
    const res = await fetch(`https://api.binance.com/api/v3/klines?symbol=${symbol}&interval=${interval}&limit=${limit}`);
    if (!res.ok) return [];
    const data: Array<[number, string, string, string, string]> = await res.json();
    return data.map((d) => parseFloat(d[4])); // Close prices
  } catch {
    return [];
  }
}

// Connect Binance account using API key & secret via backend HMAC proxy
export async function connectBinanceAccount(credentials: BinanceCredentials): Promise<{
  success: boolean;
  accountInfo?: BinanceAccountInfo;
  rawBalances?: RawBalance[];
  error?: string;
}> {
  try {
    const start = performance.now();
    const res = await fetch('/api/binance/account', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        apiKey: credentials.apiKey,
        apiSecret: credentials.apiSecret,
        network: credentials.network,
      }),
    });

    const data = await res.json();
    const latency = Math.round(performance.now() - start);

    if (!res.ok || !data.success) {
      return {
        success: false,
        error: data.error || 'Failed to authenticate with Binance. Please verify your API Key and Secret.',
      };
    }

    const maskedKey = `${credentials.apiKey.slice(0, 6)}...${credentials.apiKey.slice(-4)}`;

    const accountInfo: BinanceAccountInfo = {
      isConnected: true,
      network: credentials.network,
      accountType: data.accountType || 'SPOT',
      updateTime: data.updateTime || Date.now(),
      canTrade: Boolean(data.canTrade),
      canWithdraw: Boolean(data.canWithdraw),
      canDeposit: Boolean(data.canDeposit),
      apiKeyMasked: maskedKey,
      latencyMs: latency,
      lastSyncedAt: Date.now(),
    };

    return {
      success: true,
      accountInfo,
      rawBalances: data.balances || [],
    };
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Connection failed';
    return {
      success: false,
      error: `Network error connecting to Binance: ${msg}`,
    };
  }
}

// Fetch trades for symbol
export async function fetchRecentTrades(credentials: BinanceCredentials, symbol: string): Promise<BinanceTrade[]> {
  try {
    const res = await fetch('/api/binance/trades', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        apiKey: credentials.apiKey,
        apiSecret: credentials.apiSecret,
        network: credentials.network,
        symbol,
        limit: 15,
      }),
    });

    if (!res.ok) return [];
    const data = await res.json();
    if (!data.success || !Array.isArray(data.trades)) return [];

    return data.trades.map((t: {
      id: number;
      orderId: number;
      price: string;
      qty: string;
      quoteQty: string;
      commission: string;
      commissionAsset: string;
      time: number;
      isBuyer: boolean;
    }) => ({
      id: t.id,
      symbol,
      orderId: t.orderId,
      price: parseFloat(t.price),
      qty: parseFloat(t.qty),
      quoteQty: parseFloat(t.quoteQty),
      commission: parseFloat(t.commission),
      commissionAsset: t.commissionAsset,
      time: t.time,
      isBuyer: t.isBuyer,
    }));
  } catch {
    return [];
  }
}

// Convert raw Binance balances into calculated portfolio assets with live prices
export function calculatePortfolioAssets(
  rawBalances: RawBalance[],
  priceMap: Record<string, { price: number; change24h: number; high: number; low: number; volume: number }>
): {
  assets: PortfolioAsset[];
  totalNetWorthUSD: number;
  change24hUSD: number;
  change24hPercent: number;
} {
  const assets: PortfolioAsset[] = [];
  let totalNetWorthUSD = 0;

  for (const b of rawBalances) {
    const free = parseFloat(b.free) || 0;
    const locked = parseFloat(b.locked) || 0;
    const total = free + locked;

    if (total <= 0.00000001) continue;

    let priceUSD = 0;
    let change24h = 0;
    let high24h = 0;
    let low24h = 0;
    let volume24h = 0;

    const asset = b.asset.toUpperCase();

    // Stablecoin checks
    if (['USDT', 'USDC', 'FDUSD', 'BUSD', 'DAI', 'TUSD'].includes(asset)) {
      priceUSD = 1.0;
      change24h = 0.01;
    } else {
      // Look up symbol in USDT / USDC pairs
      const usdtPair = `${asset}USDT`;
      const usdcPair = `${asset}USDC`;
      const btcPair = `${asset}BTC`;

      if (priceMap[usdtPair]) {
        priceUSD = priceMap[usdtPair].price;
        change24h = priceMap[usdtPair].change24h;
        high24h = priceMap[usdtPair].high;
        low24h = priceMap[usdtPair].low;
        volume24h = priceMap[usdtPair].volume;
      } else if (priceMap[usdcPair]) {
        priceUSD = priceMap[usdcPair].price;
        change24h = priceMap[usdcPair].change24h;
        high24h = priceMap[usdcPair].high;
        low24h = priceMap[usdcPair].low;
        volume24h = priceMap[usdcPair].volume;
      } else if (priceMap[btcPair] && priceMap['BTCUSDT']) {
        priceUSD = priceMap[btcPair].price * priceMap['BTCUSDT'].price;
        change24h = priceMap[btcPair].change24h;
      }
    }

    const valueUSD = total * priceUSD;
    totalNetWorthUSD += valueUSD;

    assets.push({
      asset,
      name: ASSET_NAMES[asset] || asset,
      free,
      locked,
      total,
      priceUSD,
      valueUSD,
      percentOfPortfolio: 0, // calculate after total
      change24h,
      high24h,
      low24h,
      volume24h,
    });
  }

  // Calculate percentages and overall 24h PnL change
  let totalWeighted24hGainUSD = 0;

  for (const asset of assets) {
    asset.percentOfPortfolio = totalNetWorthUSD > 0 ? (asset.valueUSD / totalNetWorthUSD) * 100 : 0;
    const assetPreviousValue = asset.change24h !== 0 ? asset.valueUSD / (1 + asset.change24h / 100) : asset.valueUSD;
    totalWeighted24hGainUSD += asset.valueUSD - assetPreviousValue;
  }

  // Sort by highest value USD
  assets.sort((a, b) => b.valueUSD - a.valueUSD);

  const previousTotal = totalNetWorthUSD - totalWeighted24hGainUSD;
  const change24hPercent = previousTotal > 0 ? (totalWeighted24hGainUSD / previousTotal) * 100 : 0;

  return {
    assets,
    totalNetWorthUSD,
    change24hUSD: totalWeighted24hGainUSD,
    change24hPercent,
  };
}

// Generate realistic sample Binance spot account with live prices
export function createDemoBinanceAccount(
  priceMap: Record<string, { price: number; change24h: number; high: number; low: number; volume: number }>
): {
  accountInfo: BinanceAccountInfo;
  rawBalances: RawBalance[];
  trades: BinanceTrade[];
} {
  const rawBalances: RawBalance[] = [
    { asset: 'BTC', free: '0.84500000', locked: '0.05000000' },
    { asset: 'ETH', free: '6.42000000', locked: '0.00000000' },
    { asset: 'SOL', free: '48.50000000', locked: '5.00000000' },
    { asset: 'BNB', free: '24.18000000', locked: '2.00000000' },
    { asset: 'USDT', free: '18420.50000000', locked: '1500.00000000' },
    { asset: 'NEAR', free: '820.00000000', locked: '0.00000000' },
    { asset: 'AVAX', free: '95.40000000', locked: '0.00000000' },
    { asset: 'LINK', free: '240.00000000', locked: '0.00000000' },
    { asset: 'SUI', free: '1450.00000000', locked: '0.00000000' },
  ];

  const now = Date.now();
  const btcPrice = priceMap['BTCUSDT']?.price || 94500;
  const ethPrice = priceMap['ETHUSDT']?.price || 3350;
  const solPrice = priceMap['SOLUSDT']?.price || 192;
  const bnbPrice = priceMap['BNBUSDT']?.price || 640;

  const trades: BinanceTrade[] = [
    {
      id: 91402831,
      symbol: 'BTCUSDT',
      orderId: 1049281,
      price: btcPrice * 0.985,
      qty: 0.25,
      quoteQty: 0.25 * btcPrice * 0.985,
      commission: 0.0001875,
      commissionAsset: 'BNB',
      time: now - 3600000 * 4,
      isBuyer: true,
    },
    {
      id: 91398241,
      symbol: 'SOLUSDT',
      orderId: 1049102,
      price: solPrice * 0.97,
      qty: 15.0,
      quoteQty: 15.0 * solPrice * 0.97,
      commission: 0.0014,
      commissionAsset: 'BNB',
      time: now - 3600000 * 18,
      isBuyer: true,
    },
    {
      id: 91374921,
      symbol: 'ETHUSDT',
      orderId: 1048894,
      price: ethPrice * 1.02,
      qty: 1.5,
      quoteQty: 1.5 * ethPrice * 1.02,
      commission: 0.0031,
      commissionAsset: 'BNB',
      time: now - 3600000 * 36,
      isBuyer: false,
    },
    {
      id: 91350124,
      symbol: 'BNBUSDT',
      orderId: 1048201,
      price: bnbPrice * 0.96,
      qty: 5.0,
      quoteQty: 5.0 * bnbPrice * 0.96,
      commission: 0.00075,
      commissionAsset: 'BNB',
      time: now - 3600000 * 62,
      isBuyer: true,
    },
  ];

  const accountInfo: BinanceAccountInfo = {
    isConnected: true,
    network: 'demo',
    accountType: 'SPOT (Demo Account)',
    updateTime: now,
    canTrade: true,
    canWithdraw: false,
    canDeposit: false,
    apiKeyMasked: 'DEMO_BNB_LIVE_FEED_READY',
    latencyMs: 14,
    lastSyncedAt: now,
  };

  return { accountInfo, rawBalances, trades };
}

// Fallback prices in case of network offline
const FALLBACK_PRICES: Record<string, { price: number; change24h: number; high: number; low: number; volume: number }> = {
  BTCUSDT: { price: 95400, change24h: 3.42, high: 96200, low: 92800, volume: 1845000000 },
  ETHUSDT: { price: 3410, change24h: 2.18, high: 3490, low: 3320, volume: 920000000 },
  SOLUSDT: { price: 194.5, change24h: 6.84, high: 198.2, low: 181.5, volume: 640000000 },
  BNBUSDT: { price: 652.0, change24h: 1.85, high: 660.0, low: 638.0, volume: 290000000 },
  NEARUSDT: { price: 6.82, change24h: 8.42, high: 7.15, low: 6.22, volume: 140000000 },
  AVAXUSDT: { price: 34.2, change24h: -1.15, high: 35.8, low: 33.6, volume: 180000000 },
  LINKUSDT: { price: 19.8, change24h: 4.12, high: 20.4, low: 18.9, volume: 110000000 },
  SUIUSDT: { price: 3.45, change24h: 11.2, high: 3.65, low: 3.08, volume: 220000000 },
  DOGEUSDT: { price: 0.245, change24h: -2.4, high: 0.265, low: 0.238, volume: 380000000 },
};
