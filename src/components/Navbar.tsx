import React from 'react';
import { ShieldCheck, RefreshCw, Zap, ExternalLink, Key } from 'lucide-react';
import type { BinanceAccountInfo } from '../types/binance';

interface NavbarProps {
  accountInfo: BinanceAccountInfo | null;
  activeTab: 'portfolio' | 'assets' | 'markets' | 'trades' | 'share';
  setActiveTab: (tab: 'portfolio' | 'assets' | 'markets' | 'trades' | 'share') => void;
  onOpenConnectModal: () => void;
  onRefresh: () => void;
  isRefreshing: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  accountInfo,
  activeTab,
  setActiveTab,
  onOpenConnectModal,
  onRefresh,
  isRefreshing,
}) => {
  return (
    <header className="sticky top-0 z-40 w-full border-b border-[#2b313a] bg-[#0b0e11]/95 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Zone 1: Single text element wordmark with brand badge */}
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-[#FCD535] text-black shadow-sm font-bold text-lg select-none">
            <svg viewBox="0 0 24 24" className="w-5 h-5 fill-current">
              <path d="M12 2L6 8l2.12 2.12L12 6.24l3.88 3.88L18 8 12 2zm-6 6L2 12l4 4 2.12-2.12L6.24 12l1.88-1.88L6 8zm12 0l-2.12 2.12L17.76 12l-1.88 1.88L18 16l4-4-4-4zm-6 6l-3.88 3.88L6 16l6 6 6-6-2.12-2.12L12 17.76l-1.88-1.88L12 14z" />
            </svg>
          </div>
          <button
            onClick={() => setActiveTab('portfolio')}
            className="text-left group cursor-pointer focus:outline-none"
          >
            <span className="text-base sm:text-lg font-bold tracking-tight text-white group-hover:text-[#FCD535] transition-colors">
              Binance Connect
            </span>
          </button>
        </div>

        {/* Zone 2: 4 clean text navigation links */}
        <nav className="hidden md:flex items-center gap-1 sm:gap-2">
          <button
            onClick={() => setActiveTab('portfolio')}
            className={`px-3 py-1.5 text-xs sm:text-sm font-medium rounded-md transition-colors cursor-pointer ${
              activeTab === 'portfolio'
                ? 'text-[#FCD535] bg-[#FCD535]/10'
                : 'text-[#848e9c] hover:text-white hover:bg-[#181a20]'
            }`}
          >
            Overview
          </button>
          <button
            onClick={() => setActiveTab('assets')}
            className={`px-3 py-1.5 text-xs sm:text-sm font-medium rounded-md transition-colors cursor-pointer ${
              activeTab === 'assets'
                ? 'text-[#FCD535] bg-[#FCD535]/10'
                : 'text-[#848e9c] hover:text-white hover:bg-[#181a20]'
            }`}
          >
            Spot Assets
          </button>
          <button
            onClick={() => setActiveTab('markets')}
            className={`px-3 py-1.5 text-xs sm:text-sm font-medium rounded-md transition-colors cursor-pointer ${
              activeTab === 'markets'
                ? 'text-[#FCD535] bg-[#FCD535]/10'
                : 'text-[#848e9c] hover:text-white hover:bg-[#181a20]'
            }`}
          >
            Live Markets
          </button>
          <button
            onClick={() => setActiveTab('trades')}
            className={`px-3 py-1.5 text-xs sm:text-sm font-medium rounded-md transition-colors cursor-pointer ${
              activeTab === 'trades'
                ? 'text-[#FCD535] bg-[#FCD535]/10'
                : 'text-[#848e9c] hover:text-white hover:bg-[#181a20]'
            }`}
          >
            Trade History
          </button>
          <button
            onClick={() => setActiveTab('share')}
            className={`px-3 py-1.5 text-xs sm:text-sm font-medium rounded-md transition-colors cursor-pointer ${
              activeTab === 'share'
                ? 'text-[#FCD535] bg-[#FCD535]/10'
                : 'text-[#848e9c] hover:text-white hover:bg-[#181a20]'
            }`}
          >
            Share & Export
          </button>
        </nav>

        {/* Zone 3: Primary action & connection status */}
        <div className="flex items-center gap-2 sm:gap-3">
          {accountInfo?.isConnected ? (
            <>
              <button
                onClick={onRefresh}
                disabled={isRefreshing}
                title="Refresh balances & market prices"
                className="flex items-center gap-1.5 px-2.5 py-1.5 text-xs font-mono text-[#848e9c] hover:text-white bg-[#181a20] hover:bg-[#2b313a] border border-[#2b313a] rounded-lg transition-colors cursor-pointer disabled:opacity-50"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-[#FCD535]' : ''}`} />
                <span className="hidden sm:inline">Sync</span>
              </button>

              <button
                onClick={onOpenConnectModal}
                className="flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-white bg-[#181a20] hover:bg-[#2b313a] border border-[#2b313a] rounded-lg transition-colors cursor-pointer"
              >
                <span className="relative flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                </span>
                <span className="hidden sm:inline uppercase text-[11px] tracking-wider text-emerald-400 font-semibold">
                  {accountInfo.network === 'demo' ? 'Demo Mode' : accountInfo.network}
                </span>
                <span className="text-[#848e9c] text-xs">
                  {accountInfo.apiKeyMasked || 'Connected'}
                </span>
              </button>
            </>
          ) : (
            <button
              onClick={onOpenConnectModal}
              className="flex items-center gap-2 px-3.5 py-2 text-xs sm:text-sm font-semibold text-black bg-[#FCD535] hover:bg-[#fcd535]/90 rounded-lg shadow-sm transition-all cursor-pointer whitespace-nowrap"
            >
              <Key className="w-3.5 h-3.5" />
              <span>Connect Binance</span>
            </button>
          )}
        </div>
      </div>
    </header>
  );
};
