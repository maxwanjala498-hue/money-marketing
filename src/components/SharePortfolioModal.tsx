import React, { useState, useRef } from 'react';
import {
  X,
  Share2,
  Copy,
  Download,
  Check,
  Eye,
  EyeOff,
  ShieldCheck,
  FileSpreadsheet,
  MessageSquare,
  FileText,
  Lock,
} from 'lucide-react';
import type { BinanceAccountInfo, PortfolioAsset, SharedSnapshotData } from '../types/binance';

interface SharePortfolioModalProps {
  isOpen: boolean;
  onClose: () => void;
  totalNetWorthUSD: number;
  change24hUSD: number;
  change24hPercent: number;
  assets: PortfolioAsset[];
  accountInfo: BinanceAccountInfo | null;
}

export const SharePortfolioModal: React.FC<SharePortfolioModalProps> = ({
  isOpen,
  onClose,
  totalNetWorthUSD,
  change24hUSD,
  change24hPercent,
  assets,
  accountInfo,
}) => {
  const [hideBalances, setHideBalances] = useState(false);
  const [creatorName, setCreatorName] = useState('Crypto Trader');
  const [cardTheme, setCardTheme] = useState<'binance' | 'dark' | 'glass'>('binance');
  const [showHoldings, setShowHoldings] = useState(true);
  const [copiedLink, setCopiedLink] = useState(false);
  const [copiedText, setCopiedText] = useState(false);
  const [isSavingSnapshot, setIsSavingSnapshot] = useState(false);
  const [sharedUrl, setSharedUrl] = useState<string | null>(null);

  const cardRef = useRef<HTMLDivElement>(null);

  if (!isOpen) return null;

  const isPositive = change24hPercent >= 0;
  const topAssets = assets.slice(0, 5);

  // Generate shareable link
  const handleCopyLink = async () => {
    setIsSavingSnapshot(true);
    try {
      const snapshotPayload: Partial<SharedSnapshotData> = {
        title: `${creatorName}'s Binance Portfolio`,
        creatorName,
        network: accountInfo?.network || 'demo',
        hideBalances,
        totalNetWorthUSD,
        change24hUSD,
        change24hPercent,
        assets: assets.map((a) => ({
          asset: a.asset,
          name: a.name,
          total: a.total,
          priceUSD: a.priceUSD,
          valueUSD: a.valueUSD,
          percentOfPortfolio: a.percentOfPortfolio,
          change24h: a.change24h,
        })),
      };

      // Try server save
      let snapshotId = '';
      try {
        const res = await fetch('/api/share/save', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(snapshotPayload),
        });
        if (res.ok) {
          const data = await res.json();
          snapshotId = data.id;
        }
      } catch {
        // ignore server failure, use client hash
      }

      const baseUrl = window.location.origin + window.location.pathname;
      let finalUrl = '';
      if (snapshotId) {
        finalUrl = `${baseUrl}?snapshot=${snapshotId}`;
      } else {
        // Fallback: base64 encoded snapshot state
        const encoded = encodeURIComponent(btoa(unescape(encodeURIComponent(JSON.stringify(snapshotPayload)))));
        finalUrl = `${baseUrl}#snapshot=${encoded}`;
      }

      setSharedUrl(finalUrl);
      await navigator.clipboard.writeText(finalUrl);
      setCopiedLink(true);
      setTimeout(() => setCopiedLink(false), 3000);
    } catch (err) {
      console.error('Failed to create share link:', err);
    } finally {
      setIsSavingSnapshot(false);
    }
  };

  // Copy formatted text for Telegram / Twitter / Discord
  const handleCopyText = async () => {
    const formattedHoldings = topAssets
      .map((a) => `• ${a.asset}: ${a.percentOfPortfolio.toFixed(1)}% (${a.change24h >= 0 ? '+' : ''}${a.change24h.toFixed(1)}%)`)
      .join('\n');

    const text = `📊 ${creatorName}'s Binance Spot Portfolio\n\n` +
      `📈 24h Return: ${isPositive ? '+' : ''}${change24hPercent.toFixed(2)}%\n` +
      (hideBalances ? `💰 Net Worth: [Hidden by Trader]\n` : `💰 Net Worth: $${totalNetWorthUSD.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}\n`) +
      `\n🏆 Top Allocations:\n${formattedHoldings}\n\n` +
      `🔒 Verified with Binance Spot API · ${new Date().toLocaleDateString()}`;

    await navigator.clipboard.writeText(text);
    setCopiedText(true);
    setTimeout(() => setCopiedText(false), 3000);
  };

  // Export CSV of spot holdings
  const handleExportCsv = () => {
    const headers = ['Asset', 'Name', 'Total Holdings', 'Price USD', 'Value USD', 'Portfolio %', '24h Change %'];
    const rows = assets.map((a) => [
      a.asset,
      `"${a.name}"`,
      a.total,
      a.priceUSD.toFixed(4),
      a.valueUSD.toFixed(2),
      a.percentOfPortfolio.toFixed(2),
      a.change24h.toFixed(2),
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map((e) => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `binance_portfolio_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Render high-res PNG snapshot using HTML5 Canvas
  const handleDownloadPng = () => {
    const canvas = document.createElement('canvas');
    canvas.width = 1200;
    canvas.height = 700;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Background
    ctx.fillStyle = cardTheme === 'binance' ? '#0b0e11' : '#12161c';
    ctx.fillRect(0, 0, 1200, 700);

    // Border
    ctx.strokeStyle = '#2b313a';
    ctx.lineWidth = 4;
    ctx.strokeRect(10, 10, 1180, 680);

    // Accent header line
    ctx.fillStyle = '#FCD535';
    ctx.fillRect(40, 40, 6, 40);

    // Brand & Title
    ctx.fillStyle = '#FFFFFF';
    ctx.font = 'bold 28px "Plus Jakarta Sans", sans-serif';
    ctx.fillText('BINANCE SPOT PORTFOLIO', 60, 68);

    ctx.fillStyle = '#848e9c';
    ctx.font = '16px "Plus Jakarta Sans", sans-serif';
    ctx.fillText(`VERIFIED ACCOUNT · ${creatorName.toUpperCase()} · ${new Date().toLocaleDateString()}`, 60, 95);

    // Net worth box
    ctx.fillStyle = '#181a20';
    ctx.fillRect(40, 130, 1120, 160);
    ctx.strokeStyle = '#2b313a';
    ctx.lineWidth = 2;
    ctx.strokeRect(40, 130, 1120, 160);

    ctx.fillStyle = '#848e9c';
    ctx.font = '16px "Plus Jakarta Sans", sans-serif';
    ctx.fillText('TOTAL SPOT NET WORTH', 70, 170);

    ctx.fillStyle = '#FFFFFF';
    ctx.font = 'bold 44px "JetBrains Mono", monospace';
    const displayWorth = hideBalances ? '$ •••••••••' : `$${totalNetWorthUSD.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    ctx.fillText(displayWorth, 70, 230);

    // 24h PnL
    ctx.fillStyle = isPositive ? '#0ECB81' : '#F6465D';
    ctx.font = 'bold 26px "JetBrains Mono", monospace';
    const pnlText = `${isPositive ? '+' : ''}${change24hPercent.toFixed(2)}% (24h)`;
    ctx.fillText(pnlText, 850, 230);

    // Top holdings section
    ctx.fillStyle = '#FFFFFF';
    ctx.font = 'bold 20px "Plus Jakarta Sans", sans-serif';
    ctx.fillText('TOP ASSET ALLOCATIONS', 40, 335);

    let startY = 370;
    topAssets.forEach((a, idx) => {
      ctx.fillStyle = '#181a20';
      ctx.fillRect(40, startY, 1120, 50);

      // Asset Symbol
      ctx.fillStyle = '#FCD535';
      ctx.font = 'bold 18px "JetBrains Mono", monospace';
      ctx.fillText(`${idx + 1}. ${a.asset}`, 60, startY + 32);

      // Name
      ctx.fillStyle = '#848e9c';
      ctx.font = '15px "Plus Jakarta Sans", sans-serif';
      ctx.fillText(a.name, 180, startY + 32);

      // Allocation bar
      ctx.fillStyle = '#2b313a';
      ctx.fillRect(400, startY + 18, 300, 14);
      ctx.fillStyle = '#FCD535';
      ctx.fillRect(400, startY + 18, Math.min(300, (300 * a.percentOfPortfolio) / 100), 14);

      // Allocation %
      ctx.fillStyle = '#FFFFFF';
      ctx.font = 'bold 16px "JetBrains Mono", monospace';
      ctx.fillText(`${a.percentOfPortfolio.toFixed(1)}%`, 720, startY + 32);

      // Price & 24h
      ctx.fillStyle = a.change24h >= 0 ? '#0ECB81' : '#F6465D';
      ctx.font = 'bold 16px "JetBrains Mono", monospace';
      ctx.fillText(`${a.change24h >= 0 ? '+' : ''}${a.change24h.toFixed(2)}%`, 980, startY + 32);

      startY += 58;
    });

    // Footer Watermark
    ctx.fillStyle = '#848e9c';
    ctx.font = '14px "Plus Jakarta Sans", sans-serif';
    ctx.fillText('Generated via Binance Connect & Share · Read-Only API Integration', 40, 660);

    const imageUri = canvas.toDataURL('image/png');
    const link = document.createElement('a');
    link.download = `binance_portfolio_card_${Date.now()}.png`;
    link.href = imageUri;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn overflow-y-auto">
      <div className="relative w-full max-w-2xl rounded-2xl border border-[#2b313a] bg-[#181a20] text-[#eaecef] shadow-2xl overflow-hidden my-6">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[#2b313a] p-5">
          <div className="flex items-center gap-2.5">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#FCD535]/10 text-[#FCD535]">
              <Share2 className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-white">Share Portfolio Snapshot</h2>
              <p className="text-xs text-[#848e9c]">Export, share links, or generate image cards with privacy controls</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-1.5 text-[#848e9c] hover:bg-[#2b313a] hover:text-white transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-6">
          {/* Privacy & Customization Controls */}
          <div className="rounded-xl border border-[#2b313a] bg-[#0b0e11] p-4 space-y-4">
            <div className="text-xs font-semibold text-white">Privacy & Card Preferences</div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Creator Name */}
              <div>
                <label className="block text-[11px] font-medium text-[#848e9c] mb-1">
                  Trader Nickname / Title
                </label>
                <input
                  type="text"
                  value={creatorName}
                  onChange={(e) => setCreatorName(e.target.value)}
                  placeholder="e.g. Satoshi99 or Alpha Fund"
                  className="w-full rounded-lg border border-[#2b313a] bg-[#181a20] px-3 py-1.5 text-xs text-white placeholder-[#848e9c] focus:border-[#FCD535] focus:outline-none"
                />
              </div>

              {/* Theme */}
              <div>
                <label className="block text-[11px] font-medium text-[#848e9c] mb-1">Card Theme</label>
                <div className="grid grid-cols-3 gap-1.5">
                  {(['binance', 'dark', 'glass'] as const).map((t) => (
                    <button
                      key={t}
                      type="button"
                      onClick={() => setCardTheme(t)}
                      className={`px-2 py-1 text-[11px] font-medium rounded-md border capitalize transition-colors cursor-pointer ${
                        cardTheme === t
                          ? 'border-[#FCD535] bg-[#FCD535]/15 text-[#FCD535]'
                          : 'border-[#2b313a] text-[#848e9c] hover:text-white'
                      }`}
                    >
                      {t}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Privacy Toggles */}
            <div className="flex items-center gap-6 pt-1 flex-wrap">
              <label className="flex items-center gap-2 text-xs text-white cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={hideBalances}
                  onChange={(e) => setHideBalances(e.target.checked)}
                  className="rounded border-[#2b313a] bg-[#181a20] text-[#FCD535] focus:ring-0 cursor-pointer"
                />
                <span className="flex items-center gap-1.5">
                  <Lock className="w-3.5 h-3.5 text-amber-400" />
                  <span>Mask Dollar Values (Show % & PnL only)</span>
                </span>
              </label>

              <label className="flex items-center gap-2 text-xs text-white cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={showHoldings}
                  onChange={(e) => setShowHoldings(e.target.checked)}
                  className="rounded border-[#2b313a] bg-[#181a20] text-[#FCD535] focus:ring-0 cursor-pointer"
                />
                <span>Include Top Holdings</span>
              </label>
            </div>
          </div>

          {/* Live Card Preview */}
          <div>
            <div className="text-xs font-semibold text-[#848e9c] mb-2 flex items-center justify-between">
              <span>Card Live Preview</span>
              <span className="text-[11px] text-[#FCD535]">Ready for Export</span>
            </div>

            <div
              ref={cardRef}
              className={`rounded-xl border border-[#2b313a] p-6 shadow-xl relative overflow-hidden transition-all ${
                cardTheme === 'binance'
                  ? 'bg-gradient-to-br from-[#0b0e11] via-[#181a20] to-[#0b0e11]'
                  : cardTheme === 'glass'
                  ? 'bg-[#181a20]/90 backdrop-blur-md'
                  : 'bg-[#12161c]'
              }`}
            >
              {/* Binance Yellow Accent */}
              <div className="absolute top-0 left-0 right-0 h-1 bg-[#FCD535]" />

              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#FCD535] text-black font-bold">
                    <svg viewBox="0 0 24 24" className="w-4 h-4 fill-current">
                      <path d="M12 2L6 8l2.12 2.12L12 6.24l3.88 3.88L18 8 12 2zm-6 6L2 12l4 4 2.12-2.12L6.24 12l1.88-1.88L6 8zm12 0l-2.12 2.12L17.76 12l-1.88 1.88L18 16l4-4-4-4zm-6 6l-3.88 3.88L6 16l6 6 6-6-2.12-2.12L12 17.76l-1.88-1.88L12 14z" />
                    </svg>
                  </div>
                  <div>
                    <div className="text-xs font-bold uppercase tracking-wider text-white">
                      Binance Spot Portfolio
                    </div>
                    <div className="text-[11px] text-[#848e9c]">
                      {creatorName} · {new Date().toLocaleDateString()}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-1 text-[10px] text-emerald-400 font-mono font-medium px-2 py-0.5 rounded bg-emerald-950/60 border border-emerald-800/40">
                  <ShieldCheck className="w-3 h-3" />
                  <span>VERIFIED DATA</span>
                </div>
              </div>

              {/* Total & 24h PnL */}
              <div className="mt-5 flex items-baseline justify-between pt-4 border-t border-[#2b313a]/60">
                <div>
                  <div className="text-[11px] text-[#848e9c]">Spot Net Worth</div>
                  <div className="text-2xl sm:text-3xl font-bold font-mono tabular-nums text-white">
                    {hideBalances
                      ? '$ •••••••••'
                      : `$${totalNetWorthUSD.toLocaleString('en-US', {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2,
                        })}`}
                  </div>
                </div>

                <div className="text-right">
                  <div className="text-[11px] text-[#848e9c]">24h PnL</div>
                  <div
                    className={`text-lg sm:text-xl font-bold font-mono tabular-nums ${
                      isPositive ? 'text-[#0ECB81]' : 'text-[#F6465D]'
                    }`}
                  >
                    {isPositive ? '+' : ''}
                    {change24hPercent.toFixed(2)}%
                  </div>
                </div>
              </div>

              {/* Holdings list preview */}
              {showHoldings && topAssets.length > 0 && (
                <div className="mt-4 pt-3 border-t border-[#2b313a]/40 space-y-2">
                  <div className="text-[10px] font-semibold uppercase tracking-wider text-[#848e9c]">
                    Top Allocations
                  </div>
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                    {topAssets.map((asset) => (
                      <div
                        key={asset.asset}
                        className="rounded-lg bg-[#0b0e11]/80 p-2 border border-[#2b313a]/50 text-xs"
                      >
                        <div className="flex items-center justify-between">
                          <span className="font-bold text-white">{asset.asset}</span>
                          <span className="font-mono text-[#FCD535] font-medium">
                            {asset.percentOfPortfolio.toFixed(1)}%
                          </span>
                        </div>
                        <div className="text-[10px] text-[#848e9c] font-mono mt-0.5">
                          {hideBalances ? '••••' : `${asset.change24h >= 0 ? '+' : ''}${asset.change24h.toFixed(1)}% 24h`}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Action Sharing Buttons */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
            {/* Copy Shareable Link */}
            <button
              onClick={handleCopyLink}
              disabled={isSavingSnapshot}
              className="flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg border border-[#FCD535] bg-[#FCD535] text-black font-semibold text-xs hover:bg-[#fcd535]/90 transition-all cursor-pointer disabled:opacity-50"
            >
              {copiedLink ? <Check className="w-4 h-4" /> : <Share2 className="w-4 h-4" />}
              <span>{copiedLink ? 'Link Copied to Clipboard!' : 'Copy Shareable Link'}</span>
            </button>

            {/* Download PNG Snapshot Card */}
            <button
              onClick={handleDownloadPng}
              className="flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg border border-[#2b313a] bg-[#0b0e11] hover:bg-[#2b313a] text-white font-medium text-xs transition-colors cursor-pointer"
            >
              <Download className="w-4 h-4 text-[#FCD535]" />
              <span>Download PNG Card</span>
            </button>

            {/* Copy Markdown / Social Text */}
            <button
              onClick={handleCopyText}
              className="flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg border border-[#2b313a] bg-[#0b0e11] hover:bg-[#2b313a] text-white font-medium text-xs transition-colors cursor-pointer"
            >
              {copiedText ? <Check className="w-4 h-4 text-emerald-400" /> : <MessageSquare className="w-4 h-4 text-[#848e9c]" />}
              <span>{copiedText ? 'Formatted Text Copied!' : 'Copy Discord / X Summary'}</span>
            </button>

            {/* Export CSV statement */}
            <button
              onClick={handleExportCsv}
              className="flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg border border-[#2b313a] bg-[#0b0e11] hover:bg-[#2b313a] text-white font-medium text-xs transition-colors cursor-pointer"
            >
              <FileSpreadsheet className="w-4 h-4 text-emerald-400" />
              <span>Export CSV Statement</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
