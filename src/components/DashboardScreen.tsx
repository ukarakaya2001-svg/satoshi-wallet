import React, { useState } from 'react';
import {
  Eye,
  EyeOff,
  Lock,
  ArrowUpRight,
  ArrowDownLeft,
  Zap,
  ShieldCheck,
  Cloud,
  Search,
  ChevronDown,
  Globe,
  TrendingUp,
  ExternalLink,
  SlidersHorizontal,
} from 'lucide-react';
import { useWallet } from '../context/WalletContext';
import {
  CryptoAsset,
  FiatCurrency,
  TransactionRecord,
  TransactionType,
  WalletNavTab,
} from '../types';
import { CRYPTO_ASSETS, CurrencyFormatter, FIAT_CURRENCIES } from '../utils/currency';
import { TransactionDetailModal } from './TransactionDetailModal';

interface Props {
  onNavigateTab: (tab: WalletNavTab) => void;
}

export const DashboardScreen: React.FC<Props> = ({ onNavigateTab }) => {
  const {
    walletState,
    toggleBalancePrivacy,
    selectFiat,
    lockWallet,
    selectAsset,
  } = useWallet();

  const [showFiatModal, setShowFiatModal] = useState(false);
  const [selectedTx, setSelectedTx] = useState<TransactionRecord | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterAsset, setFilterAsset] = useState<CryptoAsset | 'ALL'>('ALL');
  const [filterType, setFilterType] = useState<TransactionType | 'ALL'>('ALL');

  // Calculate total portfolio fiat value
  const fiatConfig = FIAT_CURRENCIES[walletState.activeFiatCurrency];

  const btcFiatVal = (walletState.onChainBalanceSats / 100_000_000) * fiatConfig.btcPriceInFiat;
  const lnFiatVal = (walletState.lightningBalanceSats / 100_000_000) * fiatConfig.btcPriceInFiat;
  const ethFiatVal = walletState.ethereumBalanceEth * CRYPTO_ASSETS.ETH.basePriceUsd * fiatConfig.usdToFiatRate;
  const ltcFiatVal = walletState.litecoinBalanceLtc * CRYPTO_ASSETS.LTC.basePriceUsd * fiatConfig.usdToFiatRate;
  const xrpFiatVal = walletState.rippleBalanceXrp * CRYPTO_ASSETS.XRP.basePriceUsd * fiatConfig.usdToFiatRate;

  const totalFiatVal = btcFiatVal + lnFiatVal + ethFiatVal + ltcFiatVal + xrpFiatVal;
  const totalBtcEquivalent = totalFiatVal / fiatConfig.btcPriceInFiat;
  const totalSatsEquivalent = Math.round(totalBtcEquivalent * 100_000_000);

  // Filter transactions
  const filteredTransactions = walletState.transactions.filter((tx) => {
    const matchesAsset = filterAsset === 'ALL' || tx.asset === filterAsset;
    const matchesType = filterType === 'ALL' || tx.type === filterType;
    const query = searchQuery.trim().toLowerCase();
    const matchesQuery =
      !query ||
      tx.memo.toLowerCase().includes(query) ||
      tx.txHash.toLowerCase().includes(query) ||
      tx.recipientOrSender.toLowerCase().includes(query);
    return matchesAsset && matchesType && matchesQuery;
  });

  const auditGrade = walletState.auditSummary?.securityGrade.split(' ')[0] || 'A+';
  const hardeningPct = Math.round(walletState.auditSummary?.hardeningPercentage || 100);

  return (
    <div className="min-h-screen bg-[#090C10] pb-28 pt-4 px-4 max-w-xl mx-auto text-white">
      {/* 1. TOP HEADER: Brand Identity, Live Security Badge, Fiat & Lock */}
      <header className="flex items-center justify-between gap-2 mb-4">
        <div className="flex items-center gap-2.5">
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-[#F7931A] to-[#1E1B2D] border border-[#F7931A]/60 flex items-center justify-center shadow-lg shadow-[#F7931A]/20">
            <span className="text-white font-black text-lg">₿</span>
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <h1 className="text-base font-bold text-white tracking-tight">Satoshi Wallet</h1>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-[#F7931A]/20 text-[#FFAB00] font-semibold">
                v2.4 HSM
              </span>
            </div>
            <p className="text-[11px] text-[#8B949E]">Askeri Düzey Kripto & Lightning</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Live Security Badge */}
          <button
            type="button"
            onClick={() => onNavigateTab('SECURITY')}
            className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-[#12161F] border border-[#00E676]/40 text-[#00E676] text-xs font-semibold hover:bg-[#00E676]/10 transition-colors"
            title="Güvenlik Denetimi"
          >
            <ShieldCheck className="w-3.5 h-3.5 text-[#00E676]" />
            <span>{auditGrade} ({hardeningPct}%)</span>
          </button>

          {/* Fiat Switcher */}
          <button
            type="button"
            onClick={() => setShowFiatModal(true)}
            className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-[#12161F] border border-[#2B3447] text-[#8B949E] hover:text-white text-xs font-medium"
          >
            <Globe className="w-3.5 h-3.5 text-[#00E5FF]" />
            <span>{walletState.activeFiatCurrency}</span>
            <ChevronDown className="w-3 h-3" />
          </button>

          {/* Privacy Toggle */}
          <button
            type="button"
            onClick={toggleBalancePrivacy}
            className="p-1.5 rounded-lg bg-[#12161F] border border-[#2B3447] text-[#8B949E] hover:text-white"
            title={walletState.isBalanceHidden ? 'Bakiyeleri Göster' : 'Bakiyeleri Gizle'}
          >
            {walletState.isBalanceHidden ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
          </button>

          {/* Lock Wallet */}
          <button
            type="button"
            onClick={lockWallet}
            className="p-1.5 rounded-lg bg-[#12161F] border border-[#2B3447] text-[#8B949E] hover:text-[#FF5252]"
            title="Cüzdanı Kilitle"
          >
            <Lock className="w-4 h-4" />
          </button>
        </div>
      </header>

      {/* 2. MULTI-ASSET PORTFOLIO CARD */}
      <div className="p-5 rounded-2xl bg-gradient-to-b from-[#12161F] to-[#0D1117] border border-[#2B3447] shadow-xl shadow-black/50 mb-5 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-44 h-44 bg-[#F7931A]/10 rounded-full blur-3xl pointer-events-none" />
        <div className="flex items-center justify-between text-xs text-[#8B949E] mb-1">
          <span className="font-semibold uppercase tracking-wider">Toplam Portföy Değeri</span>
          <span className="flex items-center gap-1 text-[#00E676] font-medium">
            <TrendingUp className="w-3.5 h-3.5" />
            +4.28% (24s)
          </span>
        </div>

        <div className="flex items-baseline gap-2 mb-2">
          <span className="text-3xl font-extrabold text-white tracking-tight">
            {walletState.isBalanceHidden
              ? '••••••••'
              : `${fiatConfig.symbol}${totalFiatVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
          </span>
          <span className="text-xs text-[#8B949E] font-medium">{walletState.activeFiatCurrency}</span>
        </div>

        <p className="text-xs text-[#8B949E] font-mono mb-4">
          {walletState.isBalanceHidden ? '•••• sats' : `≈ ${totalSatsEquivalent.toLocaleString('en-US')} sats (${totalBtcEquivalent.toFixed(6)} BTC)`}
        </p>

        {/* Allocation Bar */}
        <div className="w-full h-2 rounded-full bg-[#1B212D] overflow-hidden flex mb-2">
          <div style={{ width: `${(btcFiatVal / totalFiatVal) * 100}%` }} className="bg-[#F7931A] h-full" title="Bitcoin" />
          <div style={{ width: `${(lnFiatVal / totalFiatVal) * 100}%` }} className="bg-[#00E5FF] h-full" title="Lightning" />
          <div style={{ width: `${(ethFiatVal / totalFiatVal) * 100}%` }} className="bg-[#627EEA] h-full" title="Ethereum" />
          <div style={{ width: `${(ltcFiatVal / totalFiatVal) * 100}%` }} className="bg-[#345D9D] h-full" title="Litecoin" />
          <div style={{ width: `${(xrpFiatVal / totalFiatVal) * 100}%` }} className="bg-[#00AAE4] h-full" title="Ripple XRP" />
        </div>

        <div className="flex items-center justify-between text-[10px] text-[#8B949E]">
          <span className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-[#F7931A]" /> BTC {Math.round((btcFiatVal / totalFiatVal) * 100)}%
          </span>
          <span className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-[#00E5FF]" /> LN {Math.round((lnFiatVal / totalFiatVal) * 100)}%
          </span>
          <span className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-[#627EEA]" /> ETH {Math.round((ethFiatVal / totalFiatVal) * 100)}%
          </span>
          <span className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-[#345D9D]" /> LTC {Math.round((ltcFiatVal / totalFiatVal) * 100)}%
          </span>
          <span className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-[#00AAE4]" /> XRP {Math.round((xrpFiatVal / totalFiatVal) * 100)}%
          </span>
        </div>
      </div>

      {/* 3. QUICK ACTION BUTTONS */}
      <div className="grid grid-cols-4 gap-2.5 mb-5">
        <button
          type="button"
          onClick={() => onNavigateTab('SEND')}
          className="flex flex-col items-center justify-center p-3 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447] transition-all group active:scale-95"
        >
          <div className="w-10 h-10 rounded-xl bg-[#F7931A]/15 text-[#F7931A] flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
            <ArrowUpRight className="w-5 h-5" />
          </div>
          <span className="text-xs font-semibold text-white">Gönder</span>
        </button>

        <button
          type="button"
          onClick={() => onNavigateTab('RECEIVE')}
          className="flex flex-col items-center justify-center p-3 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447] transition-all group active:scale-95"
        >
          <div className="w-10 h-10 rounded-xl bg-[#00E676]/15 text-[#00E676] flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
            <ArrowDownLeft className="w-5 h-5" />
          </div>
          <span className="text-xs font-semibold text-white">Al</span>
        </button>

        <button
          type="button"
          onClick={() => onNavigateTab('LIGHTNING')}
          className="flex flex-col items-center justify-center p-3 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447] transition-all group active:scale-95"
        >
          <div className="w-10 h-10 rounded-xl bg-[#00E5FF]/15 text-[#00E5FF] flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
            <Zap className="w-5 h-5" />
          </div>
          <span className="text-xs font-semibold text-white">Lightning</span>
        </button>

        <button
          type="button"
          onClick={() => onNavigateTab('SECURITY')}
          className="flex flex-col items-center justify-center p-3 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447] transition-all group active:scale-95"
        >
          <div className="w-10 h-10 rounded-xl bg-purple-500/15 text-purple-400 flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <span className="text-xs font-semibold text-white">Güvenlik</span>
        </button>
      </div>

      {/* 4. MULTI-ASSET WATCHLIST */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xs font-bold text-[#8B949E] uppercase tracking-wider">Kripto Varlıklar</h2>
          <span className="text-[11px] text-[#8B949E]">5 Aktif Varlık</span>
        </div>

        <div className="space-y-2.5">
          {/* Bitcoin */}
          <div
            onClick={() => {
              selectAsset('BTC');
              onNavigateTab('SEND');
            }}
            className="p-3.5 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447]/70 cursor-pointer flex items-center justify-between transition-all"
          >
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-[#F7931A]/15 border border-[#F7931A]/30 flex items-center justify-center text-[#F7931A] font-black text-sm">
                ₿
              </div>
              <div>
                <p className="text-sm font-bold text-white">Bitcoin</p>
                <p className="text-xs text-[#8B949E]">
                  SegWit & Taproot • {fiatConfig.symbol}{fiatConfig.btcPriceInFiat.toLocaleString()}
                </p>
              </div>
            </div>
            <div className="text-right">
              <p className="text-sm font-mono font-bold text-white">
                {walletState.isBalanceHidden ? '••••' : CurrencyFormatter.formatSatsToBtc(walletState.onChainBalanceSats)}
              </p>
              <p className="text-xs text-[#8B949E]">
                {walletState.isBalanceHidden ? '••••' : `${fiatConfig.symbol}${btcFiatVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
              </p>
            </div>
          </div>

          {/* Lightning */}
          <div
            onClick={() => {
              selectAsset('LIGHTNING');
              onNavigateTab('LIGHTNING');
            }}
            className="p-3.5 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447]/70 cursor-pointer flex items-center justify-between transition-all"
          >
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-[#00E5FF]/15 border border-[#00E5FF]/30 flex items-center justify-center text-[#00E5FF]">
                <Zap className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-1.5">
                  <p className="text-sm font-bold text-white">Lightning Network</p>
                  <span className="text-[10px] px-1 rounded bg-[#00E5FF]/20 text-[#00E5FF] font-bold">Layer 2</span>
                </div>
                <p className="text-xs text-[#8B949E]">2 Kanal Açık • Sıfır Gecikme</p>
              </div>
            </div>
            <div className="text-right">
              <p className="text-sm font-mono font-bold text-[#00E5FF]">
                {walletState.isBalanceHidden ? '••••' : CurrencyFormatter.formatSats(walletState.lightningBalanceSats)}
              </p>
              <p className="text-xs text-[#8B949E]">
                {walletState.isBalanceHidden ? '••••' : `${fiatConfig.symbol}${lnFiatVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
              </p>
            </div>
          </div>

          {/* Ethereum */}
          <div
            onClick={() => {
              selectAsset('ETH');
              onNavigateTab('SEND');
            }}
            className="p-3.5 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447]/70 cursor-pointer flex items-center justify-between transition-all"
          >
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-[#627EEA]/15 border border-[#627EEA]/30 flex items-center justify-center text-[#627EEA] font-bold text-sm">
                Ξ
              </div>
              <div>
                <p className="text-sm font-bold text-white">Ethereum</p>
                <p className="text-xs text-[#8B949E]">
                  ERC-20 • {fiatConfig.symbol}{(CRYPTO_ASSETS.ETH.basePriceUsd * fiatConfig.usdToFiatRate).toLocaleString()}
                </p>
              </div>
            </div>
            <div className="text-right">
              <p className="text-sm font-mono font-bold text-white">
                {walletState.isBalanceHidden ? '••••' : `${walletState.ethereumBalanceEth.toFixed(4)} ETH`}
              </p>
              <p className="text-xs text-[#8B949E]">
                {walletState.isBalanceHidden ? '••••' : `${fiatConfig.symbol}${ethFiatVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
              </p>
            </div>
          </div>

          {/* Litecoin */}
          <div
            onClick={() => {
              selectAsset('LTC');
              onNavigateTab('SEND');
            }}
            className="p-3.5 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447]/70 cursor-pointer flex items-center justify-between transition-all"
          >
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-[#345D9D]/15 border border-[#345D9D]/30 flex items-center justify-center text-[#345D9D] font-bold text-sm">
                Ł
              </div>
              <div>
                <p className="text-sm font-bold text-white">Litecoin</p>
                <p className="text-xs text-[#8B949E]">
                  Scrypt • {fiatConfig.symbol}{(CRYPTO_ASSETS.LTC.basePriceUsd * fiatConfig.usdToFiatRate).toFixed(2)}
                </p>
              </div>
            </div>
            <div className="text-right">
              <p className="text-sm font-mono font-bold text-white">
                {walletState.isBalanceHidden ? '••••' : `${walletState.litecoinBalanceLtc.toFixed(2)} LTC`}
              </p>
              <p className="text-xs text-[#8B949E]">
                {walletState.isBalanceHidden ? '••••' : `${fiatConfig.symbol}${ltcFiatVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
              </p>
            </div>
          </div>

          {/* Ripple XRP */}
          <div
            onClick={() => {
              selectAsset('XRP');
              onNavigateTab('SEND');
            }}
            className="p-3.5 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447]/70 cursor-pointer flex items-center justify-between transition-all"
          >
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-[#00AAE4]/15 border border-[#00AAE4]/30 flex items-center justify-center text-[#00AAE4] font-bold text-sm">
                ✕
              </div>
              <div>
                <p className="text-sm font-bold text-white">Ripple XRP</p>
                <p className="text-xs text-[#8B949E]">
                  XRPL Tag: {walletState.rippleDestinationTag} • {fiatConfig.symbol}{(CRYPTO_ASSETS.XRP.basePriceUsd * fiatConfig.usdToFiatRate).toFixed(2)}
                </p>
              </div>
            </div>
            <div className="text-right">
              <p className="text-sm font-mono font-bold text-white">
                {walletState.isBalanceHidden ? '••••' : `${walletState.rippleBalanceXrp.toFixed(2)} XRP`}
              </p>
              <p className="text-xs text-[#8B949E]">
                {walletState.isBalanceHidden ? '••••' : `${fiatConfig.symbol}${xrpFiatVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* 5. FILTERABLE & SEARCHABLE TRANSACTIONS LIST */}
      <div className="mb-4">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xs font-bold text-[#8B949E] uppercase tracking-wider">İşlem Geçmişi</h2>
          <span className="text-[11px] text-[#8B949E]">{filteredTransactions.length} Kayıt</span>
        </div>

        {/* Search Bar */}
        <div className="relative mb-3">
          <Search className="w-4 h-4 text-[#8B949E] absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Açıklama, TXID veya adres ara..."
            className="w-full bg-[#12161F] border border-[#2B3447] rounded-xl pl-10 pr-4 py-2 text-xs text-white placeholder-[#8B949E] focus:outline-none focus:border-[#F7931A]"
          />
        </div>

        {/* Filter Chips */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-2 mb-2 no-scrollbar">
          {(['ALL', 'BTC', 'LIGHTNING', 'ETH', 'LTC', 'XRP'] as const).map((asset) => (
            <button
              key={asset}
              type="button"
              onClick={() => setFilterAsset(asset)}
              className={`px-2.5 py-1 rounded-lg text-xs font-semibold whitespace-nowrap transition-colors ${
                filterAsset === asset
                  ? 'bg-[#F7931A] text-black font-bold'
                  : 'bg-[#12161F] text-[#8B949E] hover:text-white border border-[#2B3447]'
              }`}
            >
              {asset === 'ALL' ? 'Tüm Varlıklar' : asset === 'LIGHTNING' ? 'Lightning' : asset}
            </button>
          ))}
        </div>

        {/* Type Filter */}
        <div className="flex items-center gap-1.5 mb-3">
          {(['ALL', 'RECEIVE', 'SEND'] as const).map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => setFilterType(t)}
              className={`px-2.5 py-1 rounded-lg text-[11px] font-semibold transition-colors ${
                filterType === t
                  ? 'bg-[#1B212D] text-white border border-[#F7931A]/60'
                  : 'text-[#8B949E] hover:text-white'
              }`}
            >
              {t === 'ALL' ? 'Tüm Yönler' : t === 'RECEIVE' ? 'Gelenler (+)' : 'Gidenler (-)'}
            </button>
          ))}
        </div>

        {/* Transaction Rows */}
        <div className="space-y-2">
          {filteredTransactions.length === 0 ? (
            <div className="p-8 text-center bg-[#12161F]/40 rounded-2xl border border-dashed border-[#2B3447]">
              <p className="text-xs text-[#8B949E]">Kayıtlı işlem bulunamadı.</p>
            </div>
          ) : (
            filteredTransactions.map((tx) => {
              const isSend = tx.type === 'SEND';
              return (
                <div
                  key={tx.id}
                  onClick={() => setSelectedTx(tx)}
                  className="p-3 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] border border-[#2B3447]/60 flex items-center justify-between cursor-pointer transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-9 h-9 rounded-xl flex items-center justify-center ${
                        isSend ? 'bg-[#FF5252]/15 text-[#FF5252]' : 'bg-[#00E676]/15 text-[#00E676]'
                      }`}
                    >
                      {isSend ? <ArrowUpRight className="w-5 h-5" /> : <ArrowDownLeft className="w-5 h-5" />}
                    </div>
                    <div>
                      <div className="flex items-center gap-1.5">
                        <span className="text-xs font-bold text-white">
                          {isSend ? 'Gönderildi' : 'Alındı'}
                        </span>
                        <span className="text-[10px] px-1 py-0.2 rounded bg-black/40 text-[#8B949E] font-mono">
                          {tx.asset}
                        </span>
                      </div>
                      <p className="text-[11px] text-[#8B949E] truncate max-w-[180px]">{tx.memo}</p>
                    </div>
                  </div>

                  <div className="text-right">
                    <p
                      className={`text-xs font-mono font-bold ${
                        isSend ? 'text-[#FF5252]' : 'text-[#00E676]'
                      }`}
                    >
                      {isSend ? '-' : '+'}
                      {tx.customAmountText || CurrencyFormatter.formatSats(tx.amountSatoshis)}
                    </p>
                    <p className="text-[10px] text-[#8B949E]">
                      {new Date(tx.timestamp).toLocaleDateString('tr-TR', {
                        month: 'short',
                        day: 'numeric',
                      })}
                    </p>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* Fiat Switcher Modal */}
      {showFiatModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#12161F] border border-[#2B3447] rounded-3xl p-5 w-full max-w-sm">
            <h3 className="text-base font-bold text-white mb-3">Para Birimi Seçin</h3>
            <div className="grid grid-cols-2 gap-2 mb-4">
              {(Object.keys(FIAT_CURRENCIES) as FiatCurrency[]).map((fiat) => {
                const conf = FIAT_CURRENCIES[fiat];
                const isActive = walletState.activeFiatCurrency === fiat;
                return (
                  <button
                    key={fiat}
                    type="button"
                    onClick={() => {
                      selectFiat(fiat);
                      setShowFiatModal(false);
                    }}
                    className={`p-3 rounded-xl border text-left flex flex-col justify-between transition-colors ${
                      isActive
                        ? 'bg-[#1B212D] border-[#F7931A] text-white'
                        : 'bg-[#090C10] border-[#2B3447]/60 text-[#8B949E] hover:text-white'
                    }`}
                  >
                    <div className="flex items-center justify-between w-full">
                      <span className="font-bold text-sm">{fiat}</span>
                      <span className="text-xs text-[#00E5FF]">{conf.symbol}</span>
                    </div>
                    <span className="text-[10px] mt-1 text-[#8B949E]">{conf.displayName}</span>
                  </button>
                );
              })}
            </div>
            <button
              type="button"
              onClick={() => setShowFiatModal(false)}
              className="w-full py-2.5 rounded-xl bg-[#1B212D] text-xs font-semibold text-white hover:bg-[#2B3447]"
            >
              Kapat
            </button>
          </div>
        </div>
      )}

      {/* Transaction Detail Modal */}
      <TransactionDetailModal tx={selectedTx} onClose={() => setSelectedTx(null)} />
    </div>
  );
};
