import React, { useState, useMemo } from 'react';
import { Search, ArrowUpDown, ChevronRight, ExternalLink, SlidersHorizontal } from 'lucide-react';
import type { PortfolioAsset } from '../types/binance';

interface AssetBalancesTableProps {
  assets: PortfolioAsset[];
  hideAmounts: boolean;
  onSelectAsset: (asset: PortfolioAsset) => void;
}

type SortField = 'value' | 'change' | 'balance' | 'name';
type SortOrder = 'asc' | 'desc';

export const AssetBalancesTable: React.FC<AssetBalancesTableProps> = ({
  assets,
  hideAmounts,
  onSelectAsset,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [hideSmallBalances, setHideSmallBalances] = useState(true);
  const [sortField, setSortField] = useState<SortField>('value');
  const [sortOrder, setSortOrder] = useState<SortOrder>('desc');

  const filteredAssets = useMemo(() => {
    return assets.filter((item) => {
      if (hideSmallBalances && item.valueUSD < 1.0) {
        return false;
      }
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase().trim();
        return item.asset.toLowerCase().includes(q) || item.name.toLowerCase().includes(q);
      }
      return true;
    });
  }, [assets, hideSmallBalances, searchQuery]);

  const sortedAssets = useMemo(() => {
    const list = [...filteredAssets];
    list.sort((a, b) => {
      let comparison = 0;
      if (sortField === 'value') {
        comparison = a.valueUSD - b.valueUSD;
      } else if (sortField === 'change') {
        comparison = a.change24h - b.change24h;
      } else if (sortField === 'balance') {
        comparison = a.total - b.total;
      } else if (sortField === 'name') {
        comparison = a.asset.localeCompare(b.asset);
      }
      return sortOrder === 'asc' ? comparison : -comparison;
    });
    return list;
  }, [filteredAssets, sortField, sortOrder]);

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortOrder('desc');
    }
  };

  return (
    <div className="rounded-2xl border border-[#2b313a] bg-[#181a20] overflow-hidden">
      {/* Table Toolbar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 border-b border-[#2b313a]">
        <div>
          <h3 className="text-base font-semibold text-white">Binance Spot Balances</h3>
          <p className="text-xs text-[#848e9c]">Real-time valuations based on Binance spot order books</p>
        </div>

        <div className="flex items-center gap-3 flex-wrap">
          {/* Search bar */}
          <div className="relative">
            <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-[#848e9c]" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search coin (e.g. BTC)..."
              className="pl-8 pr-3 py-1.5 text-xs rounded-lg border border-[#2b313a] bg-[#0b0e11] text-white placeholder-[#848e9c] focus:border-[#FCD535] focus:outline-none w-44 sm:w-52 transition-colors"
            />
          </div>

          {/* Hide Small Balances Toggle */}
          <label className="flex items-center gap-2 text-xs text-[#848e9c] hover:text-white cursor-pointer select-none">
            <input
              type="checkbox"
              checked={hideSmallBalances}
              onChange={(e) => setHideSmallBalances(e.target.checked)}
              className="rounded border-[#2b313a] bg-[#0b0e11] text-[#FCD535] focus:ring-0 cursor-pointer"
            />
            <span>Hide &lt; $1.00</span>
          </label>
        </div>
      </div>

      {/* Table Element */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[#2b313a] bg-[#0b0e11]/60 text-[#848e9c] select-none">
              <th className="py-3 px-4 font-medium">
                <button
                  onClick={() => handleSort('name')}
                  className="flex items-center gap-1 hover:text-white transition-colors cursor-pointer"
                >
                  <span>Asset</span>
                  <ArrowUpDown className="w-3 h-3" />
                </button>
              </th>
              <th className="py-3 px-4 font-medium text-right">
                <button
                  onClick={() => handleSort('balance')}
                  className="flex items-center gap-1 ml-auto hover:text-white transition-colors cursor-pointer"
                >
                  <span>Holdings</span>
                  <ArrowUpDown className="w-3 h-3" />
                </button>
              </th>
              <th className="py-3 px-4 font-medium text-right">Price (USD)</th>
              <th className="py-3 px-4 font-medium text-right">
                <button
                  onClick={() => handleSort('change')}
                  className="flex items-center gap-1 ml-auto hover:text-white transition-colors cursor-pointer"
                >
                  <span>24h Change</span>
                  <ArrowUpDown className="w-3 h-3" />
                </button>
              </th>
              <th className="py-3 px-4 font-medium text-right">
                <button
                  onClick={() => handleSort('value')}
                  className="flex items-center gap-1 ml-auto hover:text-white transition-colors cursor-pointer"
                >
                  <span>Total Value</span>
                  <ArrowUpDown className="w-3 h-3" />
                </button>
              </th>
              <th className="py-3 px-4 font-medium text-right">Allocation</th>
              <th className="py-3 px-4 text-right"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#2b313a]/50">
            {sortedAssets.length === 0 ? (
              <tr>
                <td colSpan={7} className="text-center py-12 text-[#848e9c]">
                  No assets found matching your filter criteria.
                </td>
              </tr>
            ) : (
              sortedAssets.map((asset) => {
                const isPositive = asset.change24h >= 0;
                return (
                  <tr
                    key={asset.asset}
                    onClick={() => onSelectAsset(asset)}
                    className="hover:bg-[#1f232b] transition-colors cursor-pointer group"
                  >
                    {/* Coin Icon & Name */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-2.5">
                        <div className="flex h-7 w-7 items-center justify-center rounded-full bg-[#2b313a] text-white font-bold text-[10px] uppercase">
                          {asset.asset.slice(0, 3)}
                        </div>
                        <div>
                          <div className="font-semibold text-white group-hover:text-[#FCD535] transition-colors">
                            {asset.asset}
                          </div>
                          <div className="text-[11px] text-[#848e9c]">{asset.name}</div>
                        </div>
                      </div>
                    </td>

                    {/* Holdings (Total & Locked) */}
                    <td className="py-3.5 px-4 text-right font-mono tabular-nums">
                      <div className="text-white font-medium">
                        {hideAmounts ? '••••' : asset.total.toLocaleString('en-US', { maximumFractionDigits: 6 })}
                      </div>
                      {asset.locked > 0 && !hideAmounts && (
                        <div className="text-[10px] text-amber-400">
                          {asset.locked.toLocaleString('en-US', { maximumFractionDigits: 4 })} locked in orders
                        </div>
                      )}
                    </td>

                    {/* Price USD */}
                    <td className="py-3.5 px-4 text-right font-mono tabular-nums text-white">
                      ${asset.priceUSD >= 1
                        ? asset.priceUSD.toLocaleString('en-US', {
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2,
                          })
                        : asset.priceUSD.toFixed(5)}
                    </td>

                    {/* 24h Change */}
                    <td className="py-3.5 px-4 text-right font-mono tabular-nums">
                      <span
                        className={`font-semibold ${
                          isPositive ? 'text-[#0ECB81]' : 'text-[#F6465D]'
                        }`}
                      >
                        {isPositive ? '+' : ''}
                        {asset.change24h.toFixed(2)}%
                      </span>
                    </td>

                    {/* Value in USD */}
                    <td className="py-3.5 px-4 text-right font-mono tabular-nums font-semibold text-white">
                      {hideAmounts
                        ? '$ ••••••'
                        : `$${asset.valueUSD.toLocaleString('en-US', {
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2,
                          })}`}
                    </td>

                    {/* Allocation % with bar */}
                    <td className="py-3.5 px-4 text-right font-mono tabular-nums">
                      <div className="flex items-center justify-end gap-2">
                        <span className="text-[#848e9c] text-xs font-medium">
                          {asset.percentOfPortfolio.toFixed(1)}%
                        </span>
                        <div className="w-12 h-1.5 rounded-full bg-[#0b0e11] overflow-hidden">
                          <div
                            style={{ width: `${Math.min(100, asset.percentOfPortfolio)}%` }}
                            className="h-full bg-[#FCD535] rounded-full"
                          />
                        </div>
                      </div>
                    </td>

                    {/* View arrow */}
                    <td className="py-3.5 px-4 text-right">
                      <ChevronRight className="w-4 h-4 text-[#848e9c] group-hover:text-white transition-colors inline-block" />
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
