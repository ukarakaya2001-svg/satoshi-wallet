import React, { useState } from 'react';
import {
  ArrowLeft,
  Shield,
  Fingerprint,
  Lock,
  CheckCircle2,
  AlertCircle,
  QrCode,
  Copy,
  Check,
  Zap,
} from 'lucide-react';
import { useWallet } from '../context/WalletContext';
import { CryptoAsset, NetworkType, TransactionRecord } from '../types';
import { CRYPTO_ASSETS, CurrencyFormatter, FIAT_CURRENCIES, NETWORKS } from '../utils/currency';
import { CryptoAssetSelectorBar } from './CryptoAssetSelectorBar';
import { validateAddressForAsset } from '../utils/crypto';

interface Props {
  onBack: () => void;
}

export const SendScreen: React.FC<Props> = ({ onBack }) => {
  const {
    walletState,
    executeSend,
    selectAsset,
    verifyMfa,
    testBiometrics,
  } = useWallet();

  const selectedAsset = walletState.selectedAsset;
  const assetConfig = CRYPTO_ASSETS[selectedAsset];
  const fiatConfig = FIAT_CURRENCIES[walletState.activeFiatCurrency];

  const [recipient, setRecipient] = useState('');
  const [amountStr, setAmountStr] = useState('');
  const [memo, setMemo] = useState('');
  const [feeRate, setFeeRate] = useState(20); // sats/vB or Gwei

  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isAuthorizing, setIsAuthorizing] = useState(false);
  const [showMfaModal, setShowMfaModal] = useState(false);
  const [mfaCode, setMfaCode] = useState('');
  const [mfaError, setMfaError] = useState<string | null>(null);
  const [sentTx, setSentTx] = useState<TransactionRecord | null>(null);
  const [copiedTxHash, setCopiedTxHash] = useState(false);

  // Calculate balance for current asset
  let balanceDecimal = 0;
  let balanceUnit = assetConfig.symbol;
  if (selectedAsset === 'BTC') {
    balanceDecimal = walletState.onChainBalanceSats / 100_000_000;
  } else if (selectedAsset === 'LIGHTNING') {
    balanceDecimal = walletState.lightningBalanceSats;
    balanceUnit = 'sats';
  } else if (selectedAsset === 'ETH') {
    balanceDecimal = walletState.ethereumBalanceEth;
  } else if (selectedAsset === 'LTC') {
    balanceDecimal = walletState.litecoinBalanceLtc;
  } else if (selectedAsset === 'XRP') {
    balanceDecimal = walletState.rippleBalanceXrp;
  }

  const balanceFiat = selectedAsset === 'BTC' || selectedAsset === 'LIGHTNING'
    ? CurrencyFormatter.formatFiat(
        selectedAsset === 'BTC' ? walletState.onChainBalanceSats : walletState.lightningBalanceSats,
        walletState.activeFiatCurrency
      )
    : CurrencyFormatter.formatFiatValue(
        balanceDecimal * assetConfig.basePriceUsd,
        walletState.activeFiatCurrency
      );

  // Live fiat value of entered amount
  const parsedAmount = parseFloat(amountStr) || 0;
  const enteredFiat = selectedAsset === 'BTC'
    ? (parsedAmount * fiatConfig.btcPriceInFiat)
    : selectedAsset === 'LIGHTNING'
    ? (parsedAmount / 100_000_000 * fiatConfig.btcPriceInFiat)
    : (parsedAmount * assetConfig.basePriceUsd * fiatConfig.usdToFiatRate);

  const handlePaste = async () => {
    try {
      const text = await navigator.clipboard.readText();
      const clean = text.trim();
      setRecipient(clean);

      // Auto-detect asset
      if (clean.toLowerCase().startsWith('lnbc')) {
        selectAsset('LIGHTNING');
      } else if (clean.startsWith('0x')) {
        selectAsset('ETH');
      } else if (clean.startsWith('ltc1') || clean.startsWith('L')) {
        selectAsset('LTC');
      } else if (clean.startsWith('r') && clean.length > 24) {
        selectAsset('XRP');
      } else if (clean.startsWith('bc1') || clean.startsWith('1') || clean.startsWith('3')) {
        selectAsset('BTC');
      }
    } catch (err) {
      // ignore
    }
  };

  const handleMax = () => {
    if (selectedAsset === 'LIGHTNING') {
      setAmountStr((balanceDecimal - 1).toString());
    } else if (selectedAsset === 'BTC') {
      const btcMax = Math.max(0, balanceDecimal - 0.00005);
      setAmountStr(btcMax.toFixed(6));
    } else {
      setAmountStr(balanceDecimal.toString());
    }
  };

  const validateAndProceed = () => {
    setErrorMsg(null);
    if (!recipient.trim()) {
      setErrorMsg('Lütfen geçerli bir alıcı adresi veya Lightning invoice girin.');
      return;
    }

    if (!validateAddressForAsset(selectedAsset, recipient)) {
      setErrorMsg(`${selectedAsset} için adres biçimi geçersiz. Lütfen adresi kontrol edin.`);
      return;
    }

    if (parsedAmount <= 0) {
      setErrorMsg('Lütfen 0’dan büyük bir gönderim tutarı girin.');
      return;
    }

    if (parsedAmount > balanceDecimal) {
      setErrorMsg('Yetersiz bakiye. Girilen tutar mevcut bakiyenizi aşıyor.');
      return;
    }

    if (selectedAsset === 'BTC' && parsedAmount * 100_000_000 < 546) {
      setErrorMsg('Bitcoin tozu (dust) limiti altında gönderim yapılamaz (>546 sats gereklidir).');
      return;
    }

    // Require MFA or Biometric
    if (walletState.securitySettings.requireMfaForSend && walletState.securitySettings.isMfaEnabled) {
      setShowMfaModal(true);
    } else {
      executeSendTransaction();
    }
  };

  const executeSendTransaction = async () => {
    setIsAuthorizing(true);
    try {
      const record = await executeSend({
        asset: selectedAsset,
        recipient: recipient.trim(),
        amount: parsedAmount,
        memo: memo.trim(),
        feeRate,
      });
      setSentTx(record);
    } catch (err: any) {
      setErrorMsg(err.message || 'Gönderim sırasında donanım imzalama hatası oluştu.');
    } finally {
      setIsAuthorizing(false);
    }
  };

  const handleMfaConfirm = async () => {
    setMfaError(null);
    const valid = await verifyMfa(mfaCode);
    if (valid) {
      setShowMfaModal(false);
      executeSendTransaction();
    } else {
      setMfaError('MFA TOTP kodu hatalı veya süresi dolmuş. Lütfen kontrol edin.');
    }
  };

  const handleBiometricConfirm = async () => {
    const res = await testBiometrics();
    if (res.success) {
      setShowMfaModal(false);
      executeSendTransaction();
    }
  };

  if (sentTx) {
    return (
      <div className="min-h-screen bg-[#090C10] pb-28 pt-4 px-4 max-w-xl mx-auto text-white flex flex-col justify-between">
        <div>
          <div className="text-center py-10">
            <div className="w-16 h-16 rounded-full bg-[#00E676]/20 text-[#00E676] flex items-center justify-center mx-auto mb-4 border border-[#00E676]/40">
              <CheckCircle2 className="w-9 h-9" />
            </div>
            <h2 className="text-2xl font-bold text-white">İşlem Başarıyla İmzalandı!</h2>
            <p className="text-xs text-[#8B949E] mt-1">
              StrongBox Donanım Güvenlik Modülü (HSM) tarafından ECDSA ile imzalandı ve ağa yayınlandı.
            </p>
          </div>

          <div className="p-4 rounded-2xl bg-[#12161F] border border-[#2B3447] space-y-3 text-xs mb-6">
            <div className="flex justify-between py-1 border-b border-[#2B3447]/60">
              <span className="text-[#8B949E]">Varlık</span>
              <span className="font-bold text-white">{sentTx.asset}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-[#2B3447]/60">
              <span className="text-[#8B949E]">Tutar</span>
              <span className="font-mono font-bold text-[#00E676]">
                {sentTx.customAmountText || CurrencyFormatter.formatSats(sentTx.amountSatoshis)}
              </span>
            </div>
            <div className="flex justify-between py-1 border-b border-[#2B3447]/60">
              <span className="text-[#8B949E]">Ağ Ücreti</span>
              <span className="text-white">{sentTx.customFeeText || `${sentTx.feeSatoshis} sats`}</span>
            </div>
            <div className="py-1">
              <div className="flex justify-between mb-1">
                <span className="text-[#8B949E]">İşlem Özeti (TXID)</span>
                <button
                  type="button"
                  onClick={() => {
                    navigator.clipboard.writeText(sentTx.txHash);
                    setCopiedTxHash(true);
                    setTimeout(() => setCopiedTxHash(false), 2000);
                  }}
                  className="text-[#F7931A] hover:underline flex items-center gap-1"
                >
                  {copiedTxHash ? <Check className="w-3 h-3 text-[#00E676]" /> : <Copy className="w-3 h-3" />}
                  {copiedTxHash ? 'Kopyalandı' : 'Kopyala'}
                </button>
              </div>
              <p className="font-mono text-[11px] text-white break-all bg-[#090C10] p-2 rounded-lg">
                {sentTx.txHash}
              </p>
            </div>
          </div>
        </div>

        <button
          type="button"
          onClick={onBack}
          className="w-full py-3.5 rounded-xl bg-[#F7931A] text-black font-bold hover:bg-[#FFAB00] transition-colors"
        >
          Cüzdana Dön
        </button>
      </div>
    );
  }

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
          <h1 className="text-lg font-bold text-white">Kripto Ödeme Gönder</h1>
          <p className="text-[11px] text-[#8B949E]">Donanım Güvenlik Modülü (HSM) ve MFA Korumalı</p>
        </div>
      </div>

      {/* Asset Selector */}
      <div className="mb-4">
        <label className="text-[11px] font-semibold text-[#8B949E] uppercase tracking-wider block mb-2">
          Gönderilecek Kripto Varlığı Seçin
        </label>
        <CryptoAssetSelectorBar
          selectedAsset={selectedAsset}
          onAssetSelected={(asset) => {
            selectAsset(asset);
            setErrorMsg(null);
          }}
        />
      </div>

      {/* Balance Card */}
      <div className="p-4 rounded-2xl bg-[#12161F] border border-[#2B3447] flex items-center justify-between mb-4">
        <div>
          <p className="text-xs text-[#8B949E]">Kullanılabilir Bakiye</p>
          <p className="text-base font-bold font-mono text-white mt-0.5">
            {selectedAsset === 'LIGHTNING'
              ? CurrencyFormatter.formatSats(walletState.lightningBalanceSats)
              : `${balanceDecimal} ${assetConfig.symbol}`}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xs text-[#8B949E]">Tahmini Değer</p>
          <p className="text-xs font-semibold text-[#00E5FF] mt-0.5">{balanceFiat}</p>
        </div>
      </div>

      {/* Recipient Address */}
      <div className="mb-4">
        <div className="flex items-center justify-between mb-1.5">
          <label className="text-xs font-semibold text-[#8B949E]">Alıcı Adresi veya Fatura</label>
          <button
            type="button"
            onClick={handlePaste}
            className="text-xs text-[#F7931A] hover:underline font-semibold"
          >
            Panodan Yapıştır
          </button>
        </div>
        <div className="relative">
          <input
            type="text"
            value={recipient}
            onChange={(e) => setRecipient(e.target.value)}
            placeholder={
              selectedAsset === 'LIGHTNING'
                ? 'lnbc...'
                : selectedAsset === 'ETH'
                ? '0x...'
                : selectedAsset === 'LTC'
                ? 'ltc1...'
                : selectedAsset === 'XRP'
                ? 'r...'
                : 'bc1q... veya 1...'
            }
            className="w-full bg-[#12161F] border border-[#2B3447] rounded-xl px-3.5 py-3 text-xs font-mono text-white placeholder-[#8B949E] focus:outline-none focus:border-[#F7931A]"
          />
        </div>
      </div>

      {/* Amount */}
      <div className="mb-4">
        <div className="flex items-center justify-between mb-1.5">
          <label className="text-xs font-semibold text-[#8B949E]">
            Gönderim Tutarı ({balanceUnit})
          </label>
          <button
            type="button"
            onClick={handleMax}
            className="text-xs text-[#00E5FF] hover:underline font-semibold"
          >
            Tüm Bakiye (MAX)
          </button>
        </div>
        <div className="relative">
          <input
            type="number"
            step="any"
            value={amountStr}
            onChange={(e) => setAmountStr(e.target.value)}
            placeholder="0.00"
            className="w-full bg-[#12161F] border border-[#2B3447] rounded-xl px-3.5 py-3 text-lg font-mono font-bold text-white placeholder-[#8B949E] focus:outline-none focus:border-[#F7931A]"
          />
          <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-xs font-bold text-[#8B949E]">
            {balanceUnit}
          </span>
        </div>
        {parsedAmount > 0 && (
          <p className="text-[11px] text-[#8B949E] mt-1">
            ≈ {fiatConfig.symbol}{enteredFiat.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} {walletState.activeFiatCurrency}
          </p>
        )}
      </div>

      {/* Network Fee Tier */}
      {selectedAsset === 'BTC' && (
        <div className="mb-4 p-3.5 rounded-2xl bg-[#12161F] border border-[#2B3447]">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-white">Mempool Ağ Ücreti</span>
            <span className="text-xs font-mono text-[#F7931A]">{feeRate} sat/vB (~{feeRate * 140} sats)</span>
          </div>
          <div className="grid grid-cols-3 gap-2">
            {[
              { label: 'Ekonomik', rate: 12, time: '~60 dk' },
              { label: 'Standart', rate: 22, time: '~20 dk' },
              { label: 'Öncelikli', rate: 45, time: '~10 dk' },
            ].map((tier) => (
              <button
                key={tier.label}
                type="button"
                onClick={() => setFeeRate(tier.rate)}
                className={`p-2 rounded-xl text-center border transition-all ${
                  feeRate === tier.rate
                    ? 'bg-[#1B212D] border-[#F7931A] text-white'
                    : 'bg-[#090C10] border-[#2B3447]/60 text-[#8B949E] hover:text-white'
                }`}
              >
                <p className="text-xs font-bold">{tier.label}</p>
                <p className="text-[10px] text-[#8B949E]">{tier.time}</p>
              </button>
            ))}
          </div>
        </div>
      )}

      {selectedAsset === 'LIGHTNING' && (
        <div className="mb-4 p-3 rounded-xl bg-[#00E5FF]/10 border border-[#00E5FF]/30 flex items-center gap-2 text-xs text-[#00E5FF]">
          <Zap className="w-4 h-4 shrink-0" />
          <span>Lightning Network Ücreti: Sabit 1 satoshi (Anında Sonuçlanır)</span>
        </div>
      )}

      {/* Memo */}
      <div className="mb-6">
        <label className="text-xs font-semibold text-[#8B949E] block mb-1.5">
          Açıklama / Not (İsteğe Bağlı)
        </label>
        <input
          type="text"
          value={memo}
          onChange={(e) => setMemo(e.target.value)}
          placeholder="Örn: Sunucu ödemesi, bağış veya donanım alımı"
          maxLength={120}
          className="w-full bg-[#12161F] border border-[#2B3447] rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-[#8B949E] focus:outline-none focus:border-[#F7931A]"
        />
      </div>

      {/* Error */}
      {errorMsg && (
        <div className="mb-4 p-3 rounded-xl bg-[#FF5252]/15 border border-[#FF5252]/40 text-[#FF5252] text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Send Button */}
      <button
        type="button"
        disabled={isAuthorizing}
        onClick={validateAndProceed}
        className="w-full py-3.5 rounded-xl bg-[#F7931A] hover:bg-[#FFAB00] text-black font-bold text-sm transition-colors shadow-lg shadow-[#F7931A]/20 flex items-center justify-center gap-2"
      >
        <Shield className="w-4 h-4" />
        {isAuthorizing ? 'HSM İmzalanıyor...' : 'İmzala ve Gönder'}
      </button>

      {/* MFA Confirmation Modal */}
      {showMfaModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#12161F] border border-[#2B3447] rounded-3xl p-6 w-full max-w-sm text-white shadow-2xl">
            <div className="w-12 h-12 rounded-full bg-[#F7931A]/15 text-[#F7931A] flex items-center justify-center mx-auto mb-3 border border-[#F7931A]/40">
              <Lock className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-center">İşlem Yetkilendirmesi</h3>
            <p className="text-xs text-[#8B949E] text-center mt-1 mb-4">
              Güvenlik politikanız gereği kripto çekim işlemleri MFA veya Biyometrik onay gerektirir.
            </p>

            <div className="mb-4">
              <label className="text-xs text-[#8B949E] block mb-1">6 Haneli TOTP Doğrulama Kodu</label>
              <input
                type="text"
                maxLength={6}
                value={mfaCode}
                onChange={(e) => setMfaCode(e.target.value.replace(/\D/g, ''))}
                placeholder="Örn: 123456"
                className="w-full bg-[#090C10] border border-[#2B3447] rounded-xl py-2.5 text-center text-xl font-mono tracking-widest text-white focus:border-[#F7931A] focus:outline-none"
              />
              {mfaError && <p className="text-[11px] text-[#FF5252] mt-1">{mfaError}</p>}
            </div>

            <div className="space-y-2">
              <button
                type="button"
                onClick={handleMfaConfirm}
                className="w-full py-2.5 rounded-xl bg-[#F7931A] text-black font-bold text-xs hover:bg-[#FFAB00]"
              >
                Kodu Onayla ve Gönder
              </button>

              <button
                type="button"
                onClick={handleBiometricConfirm}
                className="w-full py-2.5 rounded-xl bg-[#1B212D] text-[#00E5FF] border border-[#2B3447] font-semibold text-xs hover:bg-[#2B3447] flex items-center justify-center gap-1.5"
              >
                <Fingerprint className="w-4 h-4" />
                Biyometrik Parmak İziyle Onayla
              </button>

              <button
                type="button"
                onClick={() => setShowMfaModal(false)}
                className="w-full py-2 text-xs text-[#8B949E] hover:text-white"
              >
                İptal Et
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
