import React, { useState } from 'react';
import {
  X,
  Key,
  ShieldCheck,
  Zap,
  Upload,
  ExternalLink,
  Eye,
  EyeOff,
  AlertTriangle,
  CheckCircle2,
  Lock,
} from 'lucide-react';
import type { BinanceAccountInfo, BinanceCredentials, BinanceNetwork } from '../types/binance';
import { pingBinance } from '../services/binanceApi';

interface ConnectionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConnectApi: (credentials: BinanceCredentials) => Promise<boolean>;
  onLoadDemo: () => void;
  onImportCsv: (csvContent: string) => void;
  accountInfo: BinanceAccountInfo | null;
  onDisconnect: () => void;
}

export const ConnectionModal: React.FC<ConnectionModalProps> = ({
  isOpen,
  onClose,
  onConnectApi,
  onLoadDemo,
  onImportCsv,
  accountInfo,
  onDisconnect,
}) => {
  const [activeTab, setActiveTab] = useState<'api' | 'demo' | 'csv'>('api');
  const [network, setNetwork] = useState<BinanceNetwork>('global');
  const [apiKey, setApiKey] = useState('');
  const [apiSecret, setApiSecret] = useState('');
  const [showSecret, setShowSecret] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [pingResult, setPingResult] = useState<{ tested: boolean; success: boolean; latency: number }>({
    tested: false,
    success: false,
    latency: 0,
  });

  if (!isOpen) return null;

  const handleTestPing = async () => {
    setPingResult({ tested: true, success: false, latency: 0 });
    const res = await pingBinance(network);
    setPingResult({ tested: true, success: res.success, latency: res.latency });
  };

  const handleConnect = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!apiKey.trim() || !apiSecret.trim()) {
      setErrorMsg('Please enter both your Binance API Key and API Secret.');
      return;
    }

    setErrorMsg(null);
    setIsLoading(true);

    try {
      const success = await onConnectApi({
        apiKey: apiKey.trim(),
        apiSecret: apiSecret.trim(),
        network,
      });

      if (success) {
        onClose();
      } else {
        setErrorMsg('Failed to connect. Please check your API credentials and ensure Read permissions are enabled.');
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Unknown error';
      setErrorMsg(`Error: ${msg}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCsvUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const content = event.target?.result as string;
      if (content) {
        onImportCsv(content);
        onClose();
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="relative w-full max-w-xl rounded-2xl border border-[#2b313a] bg-[#181a20] text-[#eaecef] shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[#2b313a] px-6 py-4">
          <div className="flex items-center gap-2.5">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#FCD535]/10 text-[#FCD535]">
              <Key className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-white">Connect Binance Account</h2>
              <p className="text-xs text-[#848e9c]">Sync spot balances, trade history, and share verified portfolio data</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-1.5 text-[#848e9c] hover:bg-[#2b313a] hover:text-white transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Status banner if already connected */}
        {accountInfo?.isConnected && (
          <div className="flex items-center justify-between bg-emerald-950/40 border-b border-emerald-800/40 px-6 py-3 text-xs">
            <div className="flex items-center gap-2 text-emerald-400">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>
                Connected to <strong className="font-semibold">{accountInfo.network.toUpperCase()}</strong> ({accountInfo.apiKeyMasked})
              </span>
            </div>
            <button
              onClick={() => {
                onDisconnect();
                onClose();
              }}
              className="text-rose-400 hover:text-rose-300 font-medium underline underline-offset-2 cursor-pointer"
            >
              Disconnect
            </button>
          </div>
        )}

        {/* Tab switchers */}
        <div className="flex border-b border-[#2b313a] bg-[#0b0e11]/60 px-6 pt-2">
          <button
            onClick={() => setActiveTab('api')}
            className={`pb-3 px-3 text-xs sm:text-sm font-medium border-b-2 transition-colors cursor-pointer ${
              activeTab === 'api'
                ? 'border-[#FCD535] text-[#FCD535]'
                : 'border-transparent text-[#848e9c] hover:text-white'
            }`}
          >
            Binance API Key
          </button>
          <button
            onClick={() => setActiveTab('demo')}
            className={`pb-3 px-3 text-xs sm:text-sm font-medium border-b-2 transition-colors cursor-pointer ${
              activeTab === 'demo'
                ? 'border-[#FCD535] text-[#FCD535]'
                : 'border-transparent text-[#848e9c] hover:text-white'
            }`}
          >
            Instant Demo Account
          </button>
          <button
            onClick={() => setActiveTab('csv')}
            className={`pb-3 px-3 text-xs sm:text-sm font-medium border-b-2 transition-colors cursor-pointer ${
              activeTab === 'csv'
                ? 'border-[#FCD535] text-[#FCD535]'
                : 'border-transparent text-[#848e9c] hover:text-white'
            }`}
          >
            Import CSV Statement
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6">
          {activeTab === 'api' && (
            <form onSubmit={handleConnect} className="space-y-4">
              {/* Network Selection */}
              <div>
                <label className="block text-xs font-medium text-[#848e9c] mb-1.5">
                  Binance Network / Endpoint
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {[
                    { id: 'global', label: 'Binance.com', sub: 'Global' },
                    { id: 'us', label: 'Binance.US', sub: 'United States' },
                    { id: 'testnet', label: 'Spot Testnet', sub: 'Safe Mock Sandbox' },
                  ].map((net) => (
                    <button
                      key={net.id}
                      type="button"
                      onClick={() => {
                        setNetwork(net.id as BinanceNetwork);
                        setPingResult({ tested: false, success: false, latency: 0 });
                      }}
                      className={`flex flex-col items-center justify-center p-2.5 rounded-lg border text-center transition-all cursor-pointer ${
                        network === net.id
                          ? 'border-[#FCD535] bg-[#FCD535]/10 text-white font-medium'
                          : 'border-[#2b313a] bg-[#0b0e11] text-[#848e9c] hover:border-[#474d57]'
                      }`}
                    >
                      <span className="text-xs font-semibold">{net.label}</span>
                      <span className="text-[10px] text-[#848e9c]">{net.sub}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* API Key */}
              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="block text-xs font-medium text-[#848e9c]">API Key</label>
                  <button
                    type="button"
                    onClick={handleTestPing}
                    className="text-[11px] text-[#FCD535] hover:underline flex items-center gap-1 cursor-pointer"
                  >
                    <span>Test Endpoint Ping</span>
                    {pingResult.tested && (
                      <span className={`font-mono text-[10px] ${pingResult.success ? 'text-emerald-400' : 'text-rose-400'}`}>
                        ({pingResult.latency}ms)
                      </span>
                    )}
                  </button>
                </div>
                <input
                  type="text"
                  value={apiKey}
                  onChange={(e) => setApiKey(e.target.value)}
                  placeholder="e.g. vmPUZE6mv9SD5VNHk4HlWFsOr6aKE2zvsw0MuI91823..."
                  className="w-full rounded-lg border border-[#2b313a] bg-[#0b0e11] px-3.5 py-2.5 text-xs sm:text-sm font-mono text-white placeholder-[#474d57] focus:border-[#FCD535] focus:outline-none transition-colors"
                />
              </div>

              {/* API Secret */}
              <div>
                <label className="block text-xs font-medium text-[#848e9c] mb-1.5">API Secret</label>
                <div className="relative">
                  <input
                    type={showSecret ? 'text' : 'password'}
                    value={apiSecret}
                    onChange={(e) => setApiSecret(e.target.value)}
                    placeholder="Enter your HMAC SHA-256 API secret"
                    className="w-full rounded-lg border border-[#2b313a] bg-[#0b0e11] px-3.5 py-2.5 pr-10 text-xs sm:text-sm font-mono text-white placeholder-[#474d57] focus:border-[#FCD535] focus:outline-none transition-colors"
                  />
                  <button
                    type="button"
                    onClick={() => setShowSecret(!showSecret)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-[#848e9c] hover:text-white cursor-pointer"
                  >
                    {showSecret ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              {/* Security instructions box */}
              <div className="rounded-lg border border-[#2b313a] bg-[#0b0e11]/80 p-3.5 text-xs text-[#848e9c] space-y-1.5">
                <div className="flex items-center gap-1.5 text-white font-medium">
                  <ShieldCheck className="w-4 h-4 text-emerald-400" />
                  <span>Security Recommendations</span>
                </div>
                <ul className="list-disc list-inside space-y-1 text-[11px] text-[#848e9c]">
                  <li>
                    Log in to Binance &gt; <strong className="text-white">API Management</strong> &gt; Create API.
                  </li>
                  <li>
                    Grant <strong className="text-emerald-400">"Enable Reading"</strong> only.
                  </li>
                  <li>
                    <strong className="text-rose-400">NEVER enable Withdrawals</strong> or Margin borrowing permissions.
                  </li>
                  <li>Keys are stored only within your local browser session and used to sign spot requests.</li>
                </ul>
              </div>

              {errorMsg && (
                <div className="flex items-center gap-2 rounded-lg border border-rose-800/40 bg-rose-950/40 p-3 text-xs text-rose-300">
                  <AlertTriangle className="w-4 h-4 shrink-0 text-rose-400" />
                  <span>{errorMsg}</span>
                </div>
              )}

              {/* Submit Buttons */}
              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 text-xs font-medium text-[#848e9c] hover:text-white rounded-lg transition-colors cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="flex items-center gap-2 px-5 py-2.5 text-xs font-semibold text-black bg-[#FCD535] hover:bg-[#fcd535]/90 rounded-lg shadow-sm transition-all cursor-pointer disabled:opacity-50"
                >
                  {isLoading ? (
                    <>
                      <div className="w-3.5 h-3.5 border-2 border-black border-t-transparent rounded-full animate-spin" />
                      <span>Verifying with Binance...</span>
                    </>
                  ) : (
                    <>
                      <Lock className="w-3.5 h-3.5" />
                      <span>Connect Read-Only API</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          )}

          {activeTab === 'demo' && (
            <div className="space-y-4 text-center py-2">
              <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-[#FCD535]/10 text-[#FCD535]">
                <Zap className="w-7 h-7" />
              </div>
              <div>
                <h3 className="text-base font-semibold text-white">Instant Demo Account</h3>
                <p className="text-xs text-[#848e9c] max-w-md mx-auto mt-1">
                  Explore the full Binance portfolio tracker, asset allocation charts, live WebSocket tickers, and data sharing tools without entering any API keys.
                </p>
              </div>

              <div className="rounded-lg border border-[#2b313a] bg-[#0b0e11] p-4 text-left text-xs space-y-2">
                <div className="font-semibold text-white text-xs">Demo Portfolio Includes:</div>
                <div className="grid grid-cols-2 gap-2 text-[11px] font-mono text-[#848e9c]">
                  <div>• 0.895 BTC (Bitcoin)</div>
                  <div>• 6.420 ETH (Ethereum)</div>
                  <div>• 53.50 SOL (Solana)</div>
                  <div>• 26.18 BNB (Binance Coin)</div>
                  <div>• 19,920.50 USDT (Tether)</div>
                  <div>• 820.0 NEAR, 95.4 AVAX, 1,450 SUI</div>
                </div>
                <div className="text-[10px] text-emerald-400 pt-1">
                  ✓ Synced with 100% real-time live prices directly from Binance exchange API
                </div>
              </div>

              <button
                type="button"
                onClick={() => {
                  onLoadDemo();
                  onClose();
                }}
                className="w-full flex items-center justify-center gap-2 px-4 py-2.5 text-xs font-semibold text-black bg-[#FCD535] hover:bg-[#fcd535]/90 rounded-lg shadow-sm transition-all cursor-pointer"
              >
                <Zap className="w-4 h-4" />
                <span>Launch Demo Account with Live Feeds</span>
              </button>
            </div>
          )}

          {activeTab === 'csv' && (
            <div className="space-y-4 py-2">
              <div>
                <h3 className="text-base font-semibold text-white">Import Binance Statement</h3>
                <p className="text-xs text-[#848e9c] mt-1">
                  Upload an official statement exported from Binance (Spot Order History, Trade History, or Balance Snapshot).
                </p>
              </div>

              <label className="flex flex-col items-center justify-center border-2 border-dashed border-[#2b313a] hover:border-[#FCD535]/60 rounded-xl p-6 bg-[#0b0e11] transition-colors cursor-pointer group">
                <Upload className="w-8 h-8 text-[#848e9c] group-hover:text-[#FCD535] transition-colors mb-2" />
                <span className="text-xs font-medium text-white">Click to select or drag and drop statement file</span>
                <span className="text-[11px] text-[#848e9c] mt-1">Supports .csv, .json exported from Binance</span>
                <input
                  type="file"
                  accept=".csv,.json"
                  onChange={handleCsvUpload}
                  className="hidden"
                />
              </label>

              <div className="text-[11px] text-[#848e9c] space-y-1">
                <p className="font-semibold text-white">How to export from Binance:</p>
                <p>1. Go to Binance.com &gt; Orders &gt; Spot Order &gt; Trade History</p>
                <p>2. Click "Export" in the top right &gt; Select time range &gt; Download CSV</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
