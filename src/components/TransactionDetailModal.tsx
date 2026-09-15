import React, { useState } from 'react';
import {
  X,
  Copy,
  Check,
  ExternalLink,
  ArrowUpRight,
  ArrowDownLeft,
  ShieldCheck,
  Clock,
  FileText,
} from 'lucide-react';
import { TransactionRecord } from '../types';
import { CRYPTO_ASSETS, CurrencyFormatter, NETWORKS } from '../utils/currency';
import { useWallet } from '../context/WalletContext';

interface Props {
  tx: TransactionRecord | null;
  onClose: () => void;
}

export const TransactionDetailModal: React.FC<Props> = ({ tx, onClose }) => {
  const { walletState } = useWallet();
  const [copiedHash, setCopiedHash] = useState(false);
  const [copiedAddress, setCopiedAddress] = useState(false);

  if (!tx) return null;

  const isSend = tx.type === 'SEND';
  const network = NETWORKS[tx.network];
  const assetConfig = CRYPTO_ASSETS[tx.asset];

  const handleCopyHash = () => {
    navigator.clipboard.writeText(tx.txHash);
    setCopiedHash(true);
    setTimeout(() => setCopiedHash(false), 2000);
  };

  const handleCopyAddress = () => {
    navigator.clipboard.writeText(tx.recipientOrSender);
    setCopiedAddress(true);
    setTimeout(() => setCopiedAddress(false), 2000);
  };

  const amountDisplay = tx.customAmountText
    ? tx.customAmountText
    : tx.asset === 'BTC' || tx.asset === 'LIGHTNING'
    ? CurrencyFormatter.formatSats(tx.amountSatoshis)
    : `${(tx.amountSatoshis / 100_000_000).toFixed(4)} ${tx.asset}`;

  const fiatEquivalent = CurrencyFormatter.formatFiat(
    tx.amountSatoshis,
    walletState.activeFiatCurrency
  );

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-end sm:items-center justify-center p-0 sm:p-4">
      <div className="bg-[#12161F] w-full max-w-lg rounded-t-3xl sm:rounded-3xl border border-[#2B3447] p-6 text-white shadow-2xl max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[#2B3447]/60 pb-4 mb-4">
          <div className="flex items-center gap-3">
            <div
              className={`w-10 h-10 rounded-xl flex items-center justify-center ${
                isSend ? 'bg-[#FF5252]/15 text-[#FF5252]' : 'bg-[#00E676]/15 text-[#00E676]'
              }`}
            >
              {isSend ? <ArrowUpRight className="w-5 h-5" /> : <ArrowDownLeft className="w-5 h-5" />}
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">
                {isSend ? 'Gönderilen Transfer' : 'Gelen Transfer'}
              </h3>
              <p className="text-xs text-[#8B949E]">{network.title}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-2 rounded-lg bg-[#1B212D] text-[#8B949E] hover:text-white"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Amount Hero */}
        <div className="text-center py-4 bg-[#090C10] rounded-2xl border border-[#2B3447]/60 mb-5">
          <p className="text-xs text-[#8B949E] font-medium uppercase tracking-wider">İşlem Tutarı</p>
          <p className={`text-2xl font-black mt-1 ${isSend ? 'text-[#FF5252]' : 'text-[#00E676]'}`}>
            {isSend ? '-' : '+'}
            {amountDisplay}
          </p>
          <p className="text-xs text-[#8B949E] mt-0.5">{fiatEquivalent}</p>
        </div>

        {/* Details Grid */}
        <div className="space-y-3 text-xs">
          {/* Status */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#1B212D]/60 border border-[#2B3447]/40">
            <span className="text-[#8B949E]">Durum</span>
            <span className="flex items-center gap-1 text-[#00E676] font-semibold">
              <ShieldCheck className="w-3.5 h-3.5" />
              {tx.status} (Blokzincir Onaylandı)
            </span>
          </div>

          {/* Timestamp */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#1B212D]/60 border border-[#2B3447]/40">
            <span className="text-[#8B949E]">Zaman</span>
            <span className="text-white font-medium flex items-center gap-1">
              <Clock className="w-3.5 h-3.5 text-[#8B949E]" />
              {new Date(tx.timestamp).toLocaleString('tr-TR')}
            </span>
          </div>

          {/* Fee */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#1B212D]/60 border border-[#2B3447]/40">
            <span className="text-[#8B949E]">Ağ Ücreti</span>
            <span className="text-white font-medium">
              {tx.customFeeText || `${tx.feeSatoshis.toLocaleString()} sats`}
            </span>
          </div>

          {/* Memo */}
          {tx.memo && (
            <div className="p-3 rounded-xl bg-[#1B212D]/60 border border-[#2B3447]/40">
              <span className="text-[#8B949E] block mb-1">Açıklama (Memo)</span>
              <span className="text-white font-medium flex items-start gap-1.5">
                <FileText className="w-3.5 h-3.5 text-[#F7931A] shrink-0 mt-0.5" />
                {tx.memo}
              </span>
            </div>
          )}

          {/* Address */}
          <div className="p-3 rounded-xl bg-[#1B212D]/60 border border-[#2B3447]/40">
            <div className="flex items-center justify-between mb-1">
              <span className="text-[#8B949E]">{isSend ? 'Alıcı Adres' : 'Gönderen / Kaynak'}</span>
              <button
                type="button"
                onClick={handleCopyAddress}
                className="text-[11px] text-[#00E5FF] hover:underline flex items-center gap-1"
              >
                {copiedAddress ? <Check className="w-3 h-3 text-[#00E676]" /> : <Copy className="w-3 h-3" />}
                {copiedAddress ? 'Kopyalandı' : 'Kopyala'}
              </button>
            </div>
            <p className="font-mono text-[11px] text-white break-all">{tx.recipientOrSender}</p>
          </div>

          {/* TxHash */}
          <div className="p-3 rounded-xl bg-[#1B212D]/60 border border-[#2B3447]/40">
            <div className="flex items-center justify-between mb-1">
              <span className="text-[#8B949E]">İşlem Özeti (TXID / Hash)</span>
              <button
                type="button"
                onClick={handleCopyHash}
                className="text-[11px] text-[#F7931A] hover:underline flex items-center gap-1"
              >
                {copiedHash ? <Check className="w-3 h-3 text-[#00E676]" /> : <Copy className="w-3 h-3" />}
                {copiedHash ? 'Kopyalandı' : 'Kopyala'}
              </button>
            </div>
            <p className="font-mono text-[11px] text-white break-all">{tx.txHash}</p>
          </div>
        </div>

        <div className="mt-6 flex gap-3">
          <button
            type="button"
            onClick={onClose}
            className="flex-1 py-3 rounded-xl bg-[#1B212D] text-white font-semibold hover:bg-[#2B3447] transition-colors"
          >
            Kapat
          </button>
        </div>
      </div>
    </div>
  );
};
