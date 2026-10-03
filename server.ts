import express from 'express';
import type { Request, Response } from 'express';
import crypto from 'node:crypto';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import dotenv from 'dotenv';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = Number(process.env.PORT) || 3000;

app.use(express.json({ limit: '2mb' }));

// In-memory store for shared portfolio cards
interface SharedSnapshot {
  id: string;
  createdAt: number;
  title: string;
  creatorName: string;
  hideBalances: boolean;
  totalNetWorthUSD: number;
  change24hUSD: number;
  change24hPercent: number;
  assets: Array<{
    asset: string;
    name: string;
    free: number;
    locked: number;
    priceUSD: number;
    valueUSD: number;
    percentOfPortfolio: number;
    change24h: number;
  }>;
  recentTrades?: Array<{
    symbol: string;
    price: number;
    qty: number;
    time: number;
    isBuyer: boolean;
  }>;
}

const sharedSnapshots = new Map<string, SharedSnapshot>();

// API endpoints
app.get('/api/health', (_req: Request, res: Response) => {
  res.json({ status: 'ok', time: Date.now() });
});

// Ping Binance API
app.get('/api/binance/ping', async (req: Request, res: Response) => {
  const endpoint = (req.query.endpoint as string) || 'https://api.binance.com';
  try {
    const start = Date.now();
    const response = await fetch(`${endpoint}/api/v3/time`);
    const latency = Date.now() - start;
    if (!response.ok) {
      return res.status(response.status).json({
        success: false,
        error: `Binance ping failed with status ${response.status}`,
      });
    }
    const data = await response.json();
    return res.json({ success: true, serverTime: data.serverTime, latency });
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Unknown error';
    return res.status(500).json({ success: false, error: msg });
  }
});

// Fetch Binance 24hr tickers (public, no key needed)
app.get('/api/binance/tickers', async (req: Request, res: Response) => {
  const endpoint = (req.query.endpoint as string) || 'https://api.binance.com';
  try {
    const response = await fetch(`${endpoint}/api/v3/ticker/24hr`);
    if (!response.ok) {
      return res.status(response.status).json({ error: 'Failed to fetch Binance tickers' });
    }
    const data = await response.json();
    return res.json(data);
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Unknown error';
    return res.status(500).json({ error: msg });
  }
});

// Connect and verify Binance Account (Private endpoint, signed with HMAC-SHA256)
app.post('/api/binance/account', async (req: Request, res: Response) => {
  const { apiKey, apiSecret, network = 'global' } = req.body;

  if (!apiKey || !apiSecret) {
    return res.status(400).json({
      success: false,
      error: 'Both API Key and API Secret are required to connect Binance account.',
    });
  }

  let baseUrl = 'https://api.binance.com';
  if (network === 'us') {
    baseUrl = 'https://api.binance.us';
  } else if (network === 'testnet') {
    baseUrl = 'https://testnet.binance.vision';
  }

  try {
    const timestamp = Date.now();
    const recvWindow = 10000;
    const queryString = `timestamp=${timestamp}&recvWindow=${recvWindow}`;

    const signature = crypto
      .createHmac('sha256', apiSecret.trim())
      .update(queryString)
      .digest('hex');

    const signedUrl = `${baseUrl}/api/v3/account?${queryString}&signature=${signature}`;

    const response = await fetch(signedUrl, {
      method: 'GET',
      headers: {
        'X-MBX-APIKEY': apiKey.trim(),
        'Content-Type': 'application/json',
      },
    });

    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json({
        success: false,
        error: data.msg || `Binance Error (${data.code || response.status})`,
        code: data.code,
      });
    }

    // Filter non-zero balances
    const nonZeroBalances = (data.balances || []).filter((b: { free: string; locked: string }) => {
      const free = parseFloat(b.free);
      const locked = parseFloat(b.locked);
      return free > 0.00000001 || locked > 0.00000001;
    });

    return res.json({
      success: true,
      accountType: data.accountType || 'SPOT',
      makerCommission: data.makerCommission,
      takerCommission: data.takerCommission,
      canTrade: data.canTrade,
      canWithdraw: data.canWithdraw,
      canDeposit: data.canDeposit,
      updateTime: data.updateTime,
      balances: nonZeroBalances,
      totalAssetsCount: nonZeroBalances.length,
      network,
    });
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Unknown error';
    return res.status(500).json({ success: false, error: msg });
  }
});

// Fetch account trades for a specific symbol
app.post('/api/binance/trades', async (req: Request, res: Response) => {
  const { apiKey, apiSecret, symbol = 'BTCUSDT', network = 'global', limit = 20 } = req.body;

  if (!apiKey || !apiSecret) {
    return res.status(400).json({ success: false, error: 'API Key and Secret required' });
  }

  let baseUrl = 'https://api.binance.com';
  if (network === 'us') {
    baseUrl = 'https://api.binance.us';
  } else if (network === 'testnet') {
    baseUrl = 'https://testnet.binance.vision';
  }

  try {
    const timestamp = Date.now();
    const recvWindow = 10000;
    const queryString = `symbol=${symbol.toUpperCase()}&limit=${limit}&timestamp=${timestamp}&recvWindow=${recvWindow}`;

    const signature = crypto
      .createHmac('sha256', apiSecret.trim())
      .update(queryString)
      .digest('hex');

    const signedUrl = `${baseUrl}/api/v3/myTrades?${queryString}&signature=${signature}`;

    const response = await fetch(signedUrl, {
      method: 'GET',
      headers: {
        'X-MBX-APIKEY': apiKey.trim(),
      },
    });

    const data = await response.json();
    if (!response.ok) {
      return res.status(response.status).json({
        success: false,
        error: data.msg || `Binance Error: ${response.status}`,
      });
    }

    return res.json({ success: true, trades: data });
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Unknown error';
    return res.status(500).json({ success: false, error: msg });
  }
});

// Save a shareable portfolio snapshot
app.post('/api/share/save', (req: Request, res: Response) => {
  try {
    const snapshot: SharedSnapshot = {
      id: crypto.randomBytes(8).toString('hex'),
      createdAt: Date.now(),
      title: req.body.title || 'Binance Portfolio Snapshot',
      creatorName: req.body.creatorName || 'Anonymous Trader',
      hideBalances: Boolean(req.body.hideBalances),
      totalNetWorthUSD: Number(req.body.totalNetWorthUSD) || 0,
      change24hUSD: Number(req.body.change24hUSD) || 0,
      change24hPercent: Number(req.body.change24hPercent) || 0,
      assets: Array.isArray(req.body.assets) ? req.body.assets : [],
      recentTrades: Array.isArray(req.body.recentTrades) ? req.body.recentTrades : [],
    };

    sharedSnapshots.set(snapshot.id, snapshot);

    // Keep max 200 items in memory
    if (sharedSnapshots.size > 200) {
      const oldestKey = sharedSnapshots.keys().next().value;
      if (oldestKey) sharedSnapshots.delete(oldestKey);
    }

    return res.json({ success: true, id: snapshot.id });
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Unknown error';
    return res.status(500).json({ success: false, error: msg });
  }
});

// Retrieve a shared portfolio snapshot
app.get('/api/share/:id', (req: Request, res: Response) => {
  const { id } = req.params;
  const snapshot = sharedSnapshots.get(id);
  if (!snapshot) {
    return res.status(404).json({ success: false, error: 'Shared snapshot not found or expired' });
  }
  return res.json({ success: true, snapshot });
});

// Server entrypoint
async function startServer() {
  app.use(express.static(__dirname));
  app.get('*', (_req: Request, res: Response) => {
    res.sendFile(path.join(__dirname, 'index.html'));
  });

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server listening on http://0.0.0.0:${PORT}`);
  });
}

startServer().catch((err) => {
  console.error('Failed to start server:', err);
  process.exit(1);
});
