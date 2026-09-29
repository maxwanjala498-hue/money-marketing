import React, { useState } from 'react';
import { ArrowDownLeft, ArrowUpRight, History, Filter } from 'lucide-react';
import type { BinanceTrade } from '../types/binance';

interface TradeHistoryListProps {
  trades: BinanceTrade[];
  onRefreshTrades?: () => void;
}

export const TradeHistoryList: React.FC<TradeHistoryListProps> = ({ trades }) => {
  const [filterSide, setFilterSide] = useState<'all' | 'buy' | 'sell'>('all');

  const filteredTrades = trades.filter((t) => {
    if (filterSide === 'buy') return t.isBuyer;
    if (filterSide === 'sell') return !t.isBuyer;
    return true;
  });

  return (
    <div className="rounded-2xl border border-[#2b313a] bg-[#181a20] overflow-hidden">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 border-b border-[#2b313a]">
        <div>
          <h3 className="text-base font-semibold text-white">Binance Trade & Order History</h3>
          <p className="text-xs text-[#848e9c]">Recent spot fills and execution prices</p>
        </div>

        <div className="flex items-center gap-1.5 p-1 bg-[#0b0e11] rounded-lg border border-[#2b313a]">
          <button
            onClick={() => setFilterSide('all')}
            className={`px-3 py-1 text-xs font-medium rounded-md transition-colors cursor-pointer ${
              filterSide === 'all' ? 'bg-[#2b313a] text-white' : 'text-[#848e9c] hover:text-white'
            }`}
          >
            All Orders ({trades.length})
          </button>
          <button
            onClick={() => setFilterSide('buy')}
            className={`px-3 py-1 text-xs font-medium rounded-md transition-colors cursor-pointer ${
              filterSide === 'buy' ? 'bg-emerald-950 text-emerald-300' : 'text-[#848e9c] hover:text-white'
            }`}
          >
            Buys
          </button>
          <button
            onClick={() => setFilterSide('sell')}
            className={`px-3 py-1 text-xs font-medium rounded-md transition-colors cursor-pointer ${
              filterSide === 'sell' ? 'bg-rose-950 text-rose-300' : 'text-[#848e9c] hover:text-white'
            }`}
          >
            Sells
          </button>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[#2b313a] bg-[#0b0e11]/60 text-[#848e9c] select-none">
              <th className="py-3 px-4 font-medium">Date & Time</th>
              <th className="py-3 px-4 font-medium">Pair</th>
              <th className="py-3 px-4 font-medium">Side</th>
              <th className="py-3 px-4 font-medium text-right">Executed Price</th>
              <th className="py-3 px-4 font-medium text-right">Filled Amount</th>
              <th className="py-3 px-4 font-medium text-right">Total Quote Amount</th>
              <th className="py-3 px-4 font-medium text-right">Fee</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#2b313a]/50">
            {filteredTrades.length === 0 ? (
              <tr>
                <td colSpan={7} className="text-center py-12 text-[#848e9c]">
                  <div className="flex flex-col items-center justify-center gap-2">
                    <History className="w-8 h-8 text-[#474d57]" />
                    <p>No recent filled orders recorded for this account.</p>
                  </div>
                </td>
              </tr>
            ) : (
              filteredTrades.map((trade) => {
                const date = new Date(trade.time);
                const isBuy = trade.isBuyer;

                return (
                  <tr key={trade.id} className="hover:bg-[#1f232b] transition-colors">
                    <td className="py-3.5 px-4 font-mono tabular-nums text-[#848e9c]">
                      <div>{date.toLocaleDateString()}</div>
                      <div className="text-[11px] text-[#474d57]">{date.toLocaleTimeString()}</div>
                    </td>

                    <td className="py-3.5 px-4 font-semibold text-white">
                      {trade.symbol}
                    </td>

                    <td className="py-3.5 px-4">
                      <span
                        className={`inline-flex items-center gap-1 font-semibold ${
                          isBuy ? 'text-[#0ECB81]' : 'text-[#F6465D]'
                        }`}
                      >
                        {isBuy ? <ArrowDownLeft className="w-3.5 h-3.5" /> : <ArrowUpRight className="w-3.5 h-3.5" />}
                        {isBuy ? 'BUY' : 'SELL'}
                      </span>
                    </td>

                    <td className="py-3.5 px-4 text-right font-mono tabular-nums text-white">
                      ${trade.price >= 1 ? trade.price.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : trade.price.toFixed(4)}
                    </td>

                    <td className="py-3.5 px-4 text-right font-mono tabular-nums text-white">
                      {trade.qty.toLocaleString('en-US', { maximumFractionDigits: 6 })}
                    </td>

                    <td className="py-3.5 px-4 text-right font-mono tabular-nums font-medium text-white">
                      ${trade.quoteQty.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>

                    <td className="py-3.5 px-4 text-right font-mono tabular-nums text-[#848e9c]">
                      {trade.commission > 0 ? `${trade.commission} ${trade.commissionAsset}` : '0 BNB'}
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
