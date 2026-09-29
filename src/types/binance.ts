export type BinanceNetwork = 'global' | 'us' | 'testnet' | 'demo';

export interface BinanceCredentials {
  apiKey: string;
  apiSecret: string;
  network: BinanceNetwork;
  label?: string;
}

export interface RawBalance {
  asset: string;
  free: string;
  locked: string;
}

export interface PortfolioAsset {
  asset: string;
  name: string;
  free: number;
  locked: number;
  total: number;
  priceUSD: number;
  valueUSD: number;
  percentOfPortfolio: number;
  change24h: number;
  high24h?: number;
  low24h?: number;
  volume24h?: number;
  sparkline?: number[];
}

export interface BinanceTrade {
  id: number | string;
  symbol: string;
  orderId: number | string;
  price: number;
  qty: number;
  quoteQty: number;
  commission: number;
  commissionAsset: string;
  time: number;
  isBuyer: boolean;
  isMaker?: boolean;
}

export interface BinanceAccountInfo {
  isConnected: boolean;
  network: BinanceNetwork;
  accountType: string;
  updateTime: number;
  canTrade: boolean;
  canWithdraw: boolean;
  canDeposit: boolean;
  apiKeyMasked?: string;
  latencyMs?: number;
  lastSyncedAt: number;
}

export interface MarketTicker {
  symbol: string;
  baseAsset: string;
  quoteAsset: string;
  price: number;
  change24h: number;
  changePercent24h: number;
  high24h: number;
  low24h: number;
  volume24h: number;
}

export interface SharedSnapshotData {
  id: string;
  createdAt: number;
  title: string;
  creatorName: string;
  network: BinanceNetwork;
  hideBalances: boolean;
  totalNetWorthUSD: number;
  change24hUSD: number;
  change24hPercent: number;
  assets: Array<{
    asset: string;
    name: string;
    total: number;
    priceUSD: number;
    valueUSD: number;
    percentOfPortfolio: number;
    change24h: number;
  }>;
}
