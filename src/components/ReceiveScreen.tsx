import React, { useState } from 'react';
import {
  ArrowLeft,
  Copy,
  Check,
  Zap,
  Shield,
  RefreshCw,
  QrCode,
  Share2,
} from 'lucide-react';
import { useWallet } from '../context/WalletContext';
import { CryptoAsset, NetworkType } from '../types';
import { CRYPTO_ASSETS, CurrencyFormatter, NETWORKS } from '../utils/currency';
import { CryptoAssetSelectorBar } from './CryptoAssetSelectorBar';
import { CryptoQrCode } from './CryptoQrCode';

interface Props {
  onBack: () => void;
}

export const ReceiveScreen: React.FC<Props> = ({ onBack }) => {
  const { walletState, selectAsset, generateLightningInvoice } = useWallet();
  const selectedAsset = walletState.selectedAsset;
  const assetConfig = CRYPTO_ASSETS[selectedAsset];

  const [btcAddressType, setBtcAddressType] = useState<'SegWit' | 'Taproot'>('SegWit');
  const [copied, setCopied] = useState(false);
  const [copiedTag, setCopiedTag] = useState(false);

  // Lightning invoice generator state
  const [lnAmountSats, setLnAmountSats] = useState('50000');
  const [lnMemo, setLnMemo] = useState('Satoshi Wallet Ödemesi');
  const [customInvoice, setCustomInvoice] = useState<string | null>(null);

  const activeAddress = (() => {
    switch (selectedAsset) {
      case 'BTC':
        return btcAddressType === 'SegWit'
          ? walletState.onChainAddress
          : walletState.taprootAddress;
      case 'LIGHTNING':
        return customInvoice || `lnbc500u1pnq9satoshi${walletState.lightningNodeId.slice(0, 32)}`;
      case 'ETH':
        return walletState.ethereumAddress;
      case 'LTC':
        return walletState.litecoinAddress;
      case 'XRP':
        return walletState.rippleAddress;
      default:
        return walletState.onChainAddress;
    }
  })();

  const handleCopy = () => {
    navigator.clipboard.writeText(activeAddress);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleCopyTag = () => {
    navigator.clipboard.writeText(walletState.rippleDestinationTag.toString());
    setCopiedTag(true);
    setTimeout(() => setCopiedTag(false), 2000);
  };

  const handleGenerateNewInvoice = () => {
    const sats = parseInt(lnAmountSats, 10) || 1000;
    const inv = generateLightningInvoice(sats, lnMemo);
    setCustomInvoice(inv);
  };

  const qrUri = (() => {
    if (selectedAsset === 'BTC') {
      return `bitcoin:${activeAddress}`;
    }
    if (selectedAsset === 'LIGHTNING') {
      return `lightning:${activeAddress}`;
    }
    if (selectedAsset === 'ETH') {
      return `ethereum:${activeAddress}`;
    }
    if (selectedAsset === 'LTC') {
      return `litecoin:${activeAddress}`;
    }
    if (selectedAsset === 'XRP') {
      return `ripple:${activeAddress}?dt=${walletState.rippleDestinationTag}`;
    }
    return activeAddress;
  })();

  return (
    <div className="min-h-screen bg-[#090C10] pb-28 pt-4 px-4 max-w-xl mx-auto text-white">
      {/* Top Bar */}
      <div className="flex items-center gap-3 mb-4">
        <button
          type="button"
          onClick={onBack}
          className="p-2 rounded-full bg-[#12161F] text-white border border-[#2B3447] hover:bg-[#1B212D]"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-lg font-bold text-white">Kripto Ödeme Al</h1>
          <p className="text-[11px] text-[#8B949E]">Donanım Korumalı Adresler • Anında QR Kod</p>
        </div>
      </div>

      {/* Asset Selector */}
      <div className="mb-4">
        <label className="text-[11px] font-semibold text-[#8B949E] uppercase tracking-wider block mb-2">
          Almak İstediğiniz Varlığı Seçin
        </label>
        <CryptoAssetSelectorBar
          selectedAsset={selectedAsset}
          onAssetSelected={(asset) => {
            selectAsset(asset);
            setCustomInvoice(null);
          }}
        />
      </div>

      {/* Bitcoin Address Type Toggle */}
      {selectedAsset === 'BTC' && (
        <div className="flex items-center gap-2 mb-4 p-1.5 rounded-xl bg-[#12161F] border border-[#2B3447]">
          <button
            type="button"
            onClick={() => setBtcAddressType('SegWit')}
            className={`flex-1 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              btcAddressType === 'SegWit'
                ? 'bg-[#1B212D] text-[#F7931A] border border-[#F7931A]/40'
                : 'text-[#8B949E] hover:text-white'
            }`}
          >
            Native SegWit (BIP-84)
          </button>
          <button
            type="button"
            onClick={() => setBtcAddressType('Taproot')}
            className={`flex-1 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              btcAddressType === 'Taproot'
                ? 'bg-[#1B212D] text-[#F7931A] border border-[#F7931A]/40'
                : 'text-[#8B949E] hover:text-white'
            }`}
          >
            Taproot (BIP-86)
          </button>
        </div>
      )}

      {/* Lightning Invoice Form */}
      {selectedAsset === 'LIGHTNING' && (
        <div className="p-4 rounded-2xl bg-[#12161F] border border-[#2B3447] mb-4 space-y-3">
          <div className="flex items-center gap-2 text-xs font-bold text-[#00E5FF]">
            <Zap className="w-4 h-4" />
            <span>BOLT-11 Lightning Faturası Oluşturucu</span>
          </div>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="text-[10px] text-[#8B949E] block mb-1">Tutar (Satoshis)</label>
              <input
                type="number"
                value={lnAmountSats}
                onChange={(e) => setLnAmountSats(e.target.value)}
                className="w-full bg-[#090C10] border border-[#2B3447] rounded-xl px-3 py-1.5 text-xs font-mono text-white focus:outline-none focus:border-[#00E5FF]"
              />
            </div>
            <div>
              <label className="text-[10px] text-[#8B949E] block mb-1">Açıklama (Memo)</label>
              <input
                type="text"
                value={lnMemo}
                onChange={(e) => setLnMemo(e.target.value)}
                className="w-full bg-[#090C10] border border-[#2B3447] rounded-xl px-3 py-1.5 text-xs text-white focus:outline-none focus:border-[#00E5FF]"
              />
            </div>
          </div>
          <button
            type="button"
            onClick={handleGenerateNewInvoice}
            className="w-full py-2 rounded-xl bg-[#00E5FF]/20 hover:bg-[#00E5FF]/30 text-[#00E5FF] font-semibold text-xs border border-[#00E5FF]/40 flex items-center justify-center gap-1.5 transition-colors"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            Yeni Lightning Faturası Üret
          </button>
        </div>
      )}

      {/* QR Code Presentation */}
      <div className="flex flex-col items-center justify-center my-4">
        <CryptoQrCode
          value={qrUri}
          size={210}
          badgeSymbol={assetConfig.symbol === 'LIGHTNING' ? '⚡' : assetConfig.symbol.slice(0, 2)}
          badgeColor={assetConfig.iconColorHex}
        />
        <p className="text-[11px] text-[#8B949E] mt-3">
          Ödeme yapmak için cüzdanınızdan bu QR kodu tarayın
        </p>
      </div>

      {/* Address Card */}
      <div className="p-4 rounded-2xl bg-[#12161F] border border-[#2B3447] mb-4">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs text-[#8B949E] font-semibold">
            {selectedAsset === 'LIGHTNING' ? 'Lightning Faturası (Invoice)' : `${assetConfig.displayName} Adresi`}
          </span>
          <button
            type="button"
            onClick={handleCopy}
            className="text-xs text-[#F7931A] hover:underline font-semibold flex items-center gap-1"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-[#00E676]" /> : <Copy className="w-3.5 h-3.5" />}
            {copied ? 'Kopyalandı!' : 'Kopyala'}
          </button>
        </div>

        <p className="font-mono text-xs text-white break-all bg-[#090C10] p-3 rounded-xl border border-[#2B3447]/60">
          {activeAddress}
        </p>

        {/* Ripple Destination Tag */}
        {selectedAsset === 'XRP' && (
          <div className="mt-3 p-3 rounded-xl bg-[#00AAE4]/10 border border-[#00AAE4]/30 flex items-center justify-between">
            <div>
              <p className="text-[10px] text-[#8B949E] uppercase font-semibold">XRPL Hedef Etiketi (Destination Tag)</p>
              <p className="text-sm font-mono font-bold text-white mt-0.5">{walletState.rippleDestinationTag}</p>
            </div>
            <button
              type="button"
              onClick={handleCopyTag}
              className="text-xs text-[#00AAE4] font-semibold flex items-center gap-1"
            >
              {copiedTag ? <Check className="w-3.5 h-3.5 text-[#00E676]" /> : <Copy className="w-3.5 h-3.5" />}
              {copiedTag ? 'Kopyalandı' : 'Etiketi Kopyala'}
            </button>
          </div>
        )}
      </div>

      {/* Security info card */}
      <div className="p-3.5 rounded-2xl bg-[#12161F]/60 border border-[#2B3447]/60 flex items-center gap-3 text-xs text-[#8B949E]">
        <Shield className="w-5 h-5 text-[#00E676] shrink-0" />
        <span>
          Bu adres BIP-44/84/86 hiyerarşik deterministik anahtar türetme ile donanım düzeyinde izole edilmiştir.
        </span>
      </div>
    </div>
  );
};
