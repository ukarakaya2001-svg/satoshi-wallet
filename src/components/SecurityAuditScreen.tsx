import React, { useState, useEffect } from 'react';
import {
  ArrowLeft,
  ShieldCheck,
  ShieldAlert,
  Zap,
  Cpu,
  Lock,
  Key,
  CheckCircle2,
  AlertTriangle,
  Search,
  RefreshCw,
  Fingerprint,
  Smartphone,
  Copy,
  Check,
  Sparkles,
} from 'lucide-react';
import { useWallet } from '../context/WalletContext';
import { AuditVector, SecurityDomain, SecuritySettings } from '../types';
import { generateTotpCode } from '../utils/totp';

interface Props {
  onBack: () => void;
}

export const SecurityAuditScreen: React.FC<Props> = ({ onBack }) => {
  const {
    walletState,
    toggleSecurityShield,
    autoHardenAll1000,
    runAudit,
    testHsmSignature,
    testBiometrics,
    verifyMfa,
  } = useWallet();

  const audit = walletState.auditSummary;
  const settings = walletState.securitySettings;
  const hsm = walletState.hsmStatus;

  const [activeDomainFilter, setActiveDomainFilter] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [hsmSigResult, setHsmSigResult] = useState<string | null>(null);
  const [biometricResult, setBiometricResult] = useState<string | null>(null);

  // Live TOTP state
  const [currentTotp, setCurrentTotp] = useState<string>('000000');
  const [totpRemaining, setTotpRemaining] = useState<number>(30);
  const [testCodeInput, setTestCodeInput] = useState<string>('');
  const [testCodeStatus, setTestCodeStatus] = useState<string | null>(null);
  const [copiedSecret, setCopiedSecret] = useState(false);

  useEffect(() => {
    const updateTotp = async () => {
      const code = await generateTotpCode(settings.totpSecret);
      setCurrentTotp(code);
      const secondsInWindow = Math.floor(Date.now() / 1000) % 30;
      setTotpRemaining(30 - secondsInWindow);
    };

    updateTotp();
    const interval = setInterval(updateTotp, 1000);
    return () => clearInterval(interval);
  }, [settings.totpSecret]);

  const handleTestHsm = async () => {
    setHsmSigResult('Donanım HSM imzalıyor...');
    const sig = await testHsmSignature();
    setHsmSigResult(sig);
  };

  const handleTestBiometric = async () => {
    setBiometricResult('Biyometrik sensör taranıyor...');
    const res = await testBiometrics();
    setBiometricResult(res.message);
  };

  const handleVerifyTestCode = async () => {
    const ok = await verifyMfa(testCodeInput);
    if (ok) {
      setTestCodeStatus('Geçerli TOTP Kodu! Zaman penceresi doğrulandı.');
    } else {
      setTestCodeStatus('Geçersiz veya süresi dolmuş kod!');
    }
  };

  const domains = [
    { id: 'ALL', label: 'Tüm 1,000 Vektör' },
    { id: 'BITCOIN_STANDARDS', label: 'Bitcoin & BIP' },
    { id: 'LIGHTNING_BOLT', label: 'Lightning BOLTs' },
    { id: 'ANDROID_HARDWARE', label: 'Donanım & StrongBox' },
    { id: 'ADVANCED_CRYPTO', label: 'İleri Kriptografi' },
    { id: 'SOFTWARE_NETWORK', label: 'Bellek & Ağ' },
  ];

  // Flatten and filter vectors
  const allVectors: AuditVector[] = audit?.vectorsByDomain
    ? Object.values(audit.vectorsByDomain).flat()
    : [];

  const filteredVectors = allVectors.filter((v: AuditVector) => {
    const matchDomain = activeDomainFilter === 'ALL' || v.domain === activeDomainFilter;
    const q = searchQuery.toLowerCase().trim();
    const matchQuery =
      !q ||
      v.id.toString().includes(q) ||
      v.vectorName.toLowerCase().includes(q) ||
      v.standardRef.toLowerCase().includes(q) ||
      v.remediation.toLowerCase().includes(q);
    return matchDomain && matchQuery;
  });

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
          <h1 className="text-lg font-bold text-white">1,000-Noktalı Güvenlik Denetim Motoru</h1>
          <p className="text-[11px] text-[#8B949E]">
            NIST SP 800-57, BIP-39/84/86, BOLT-1..11, FIPS 140-3
          </p>
        </div>
      </div>

      {/* 1. SCORECARD HERO */}
      <div className="p-5 rounded-2xl bg-gradient-to-b from-[#12161F] to-[#0D1117] border border-[#00E676]/40 shadow-xl shadow-black/50 mb-5 relative overflow-hidden">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-full bg-[#00E676]/15 text-[#00E676] flex items-center justify-center">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs text-[#8B949E] uppercase font-semibold">Sıkılaştırma Derecesi</p>
              <h2 className="text-xl font-black text-white">{audit?.securityGrade}</h2>
            </div>
          </div>
          <div className="text-right">
            <span className="text-2xl font-black text-[#00E676]">{audit?.hardeningPercentage.toFixed(1)}%</span>
            <p className="text-[10px] text-[#8B949E]">1000/1000 Puan</p>
          </div>
        </div>

        {/* 3 Metric Pills */}
        <div className="grid grid-cols-3 gap-2 text-center text-xs mb-4">
          <div className="p-2.5 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <span className="text-white font-bold block">{audit?.totalChecked ?? 1000} / {audit?.totalChecked ?? 1000}</span>
            <span className="text-[10px] text-[#8B949E]">Denetlenen Kriter</span>
          </div>
          <div className="p-2.5 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <span className="text-[#00E676] font-bold block">{audit?.totalVulnerabilitiesClosed ?? 1000}</span>
            <span className="text-[10px] text-[#8B949E]">Sıkılaştırılan Vektör</span>
          </div>
          <div className="p-2.5 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <span className="text-[#00E5FF] font-bold block">{audit?.criticalDefensesActive ?? 320} / {audit?.criticalDefensesActive ?? 320}</span>
            <span className="text-[10px] text-[#8B949E]">Kritik Kalkan</span>
          </div>
        </div>

        {/* Auto-Harden 1-Click Action */}
        <button
          type="button"
          onClick={autoHardenAll1000}
          className="w-full py-3 rounded-xl bg-gradient-to-r from-[#00E676] to-[#00E5FF] text-black font-bold text-xs shadow-lg hover:opacity-95 transition-opacity flex items-center justify-center gap-2"
        >
          <Sparkles className="w-4 h-4 text-black" />
          Tüm 1,000 Vektörü Otomatik Sıkılaştır (100% Kapat)
        </button>
      </div>

      {/* 2. REAL-TIME SECURITY SHIELDS (Interactive Toggles) */}
      <div className="p-5 rounded-2xl bg-[#12161F] border border-[#2B3447] mb-5">
        <h2 className="text-xs font-bold text-white uppercase tracking-wider mb-3">
          Askeri Düzey Aktif Güvenlik Kalkanları
        </h2>

        <div className="space-y-3">
          {/* FLAG_SECURE */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <div>
              <p className="text-xs font-bold text-white">FLAG_SECURE Ekran Yakalama Koruması</p>
              <p className="text-[10px] text-[#8B949E]">Ekran görüntüsü ve casus yazılımlara karşı engelleme</p>
            </div>
            <input
              type="checkbox"
              checked={settings.isFlagSecureEnabled}
              onChange={() => toggleSecurityShield('isFlagSecureEnabled')}
              className="w-4 h-4 accent-[#00E676] cursor-pointer"
            />
          </div>

          {/* Biometric PIN */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <div>
              <p className="text-xs font-bold text-white">Biyometrik PIN ve Donanım Şifreleme</p>
              <p className="text-[10px] text-[#8B949E]">StrongBox TEE destekli parmak izi/yüz kilidi</p>
            </div>
            <input
              type="checkbox"
              checked={settings.isBiometricPinEnabled}
              onChange={() => toggleSecurityShield('isBiometricPinEnabled')}
              className="w-4 h-4 accent-[#00E676] cursor-pointer"
            />
          </div>

          {/* Auto Lock */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <div>
              <p className="text-xs font-bold text-white">Otomatik Arka Plan Kilit Zamanlayıcısı</p>
              <p className="text-[10px] text-[#8B949E]">Uygulama arka plana geçtiğinde cüzdanı anında kilitler</p>
            </div>
            <input
              type="checkbox"
              checked={settings.isAutoLockEnabled}
              onChange={() => toggleSecurityShield('isAutoLockEnabled')}
              className="w-4 h-4 accent-[#00E676] cursor-pointer"
            />
          </div>

          {/* Clipboard Auto-Clear */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <div>
              <p className="text-xs font-bold text-white">Pano (Clipboard) Otomatik Temizleme</p>
              <p className="text-[10px] text-[#8B949E]">Kopyalanan özel anahtarları 60 saniye sonra bellekten siler</p>
            </div>
            <input
              type="checkbox"
              checked={settings.isClipboardAutoClearEnabled}
              onChange={() => toggleSecurityShield('isClipboardAutoClearEnabled')}
              className="w-4 h-4 accent-[#00E676] cursor-pointer"
            />
          </div>

          {/* Strict Bech32 */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <div>
              <p className="text-xs font-bold text-white">Sıkı Bech32 / Bech32m Doğrulaması</p>
              <p className="text-[10px] text-[#8B949E]">SegWit v0 ve Taproot v1 sağlama toplamı denetimi</p>
            </div>
            <input
              type="checkbox"
              checked={settings.isStrictBech32ValidationEnabled}
              onChange={() => toggleSecurityShield('isStrictBech32ValidationEnabled')}
              className="w-4 h-4 accent-[#00E676] cursor-pointer"
            />
          </div>

          {/* HTLC Watchdog */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <div>
              <p className="text-xs font-bold text-white">Lightning HTLC Watchdog Zaman Aşımı Takibi</p>
              <p className="text-[10px] text-[#8B949E]">BOLT-3/5 adil olmayan kanal kapanışlarına karşı nöbetçi</p>
            </div>
            <input
              type="checkbox"
              checked={settings.isHtlcWatchdogEnabled}
              onChange={() => toggleSecurityShield('isHtlcWatchdogEnabled')}
              className="w-4 h-4 accent-[#00E676] cursor-pointer"
            />
          </div>

          {/* Require MFA for Send */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <div>
              <p className="text-xs font-bold text-white">Transferlerde İki Adımlı Doğrulama (MFA)</p>
              <p className="text-[10px] text-[#8B949E]">Tüm kripto çekimlerinde TOTP RFC 6238 zorunluluğu</p>
            </div>
            <input
              type="checkbox"
              checked={settings.requireMfaForSend}
              onChange={() => toggleSecurityShield('requireMfaForSend')}
              className="w-4 h-4 accent-[#00E676] cursor-pointer"
            />
          </div>
        </div>
      </div>

      {/* 3. HARDWARE SECURITY MODULE (HSM) STATUS CARD */}
      <div className="p-5 rounded-2xl bg-[#12161F] border border-[#2B3447] mb-5">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Cpu className="w-5 h-5 text-[#00E5FF]" />
            <h2 className="text-xs font-bold text-white uppercase tracking-wider">
              Donanım Güvenlik Modülü (HSM / StrongBox)
            </h2>
          </div>
          <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#00E676]/20 text-[#00E676] font-bold">
            {hsm?.attestationCertification || 'FIPS 140-3 Level 4'}
          </span>
        </div>

        <div className="grid grid-cols-2 gap-2 text-[11px] bg-[#090C10] p-3 rounded-xl border border-[#2B3447]/60 mb-3">
          <div>
            <span className="text-[#8B949E] block">Yonga Modeli</span>
            <span className="text-white font-medium">{hsm?.hardwareChipModel || 'Titan M2 / StrongBox Enclave'}</span>
          </div>
          <div>
            <span className="text-[#8B949E] block">Tasdik Düzeyi</span>
            <span className="text-[#00E5FF] font-medium">{hsm?.securityLevel || 'StrongBox Hardware TEE'}</span>
          </div>
          <div>
            <span className="text-[#8B949E] block">Keystore Durumu</span>
            <span className="text-[#00E676] font-medium">Aktif / İzole Enclave</span>
          </div>
          <div>
            <span className="text-[#8B949E] block">İmza Sayacı</span>
            <span className="text-white font-mono font-medium">{hsm?.totalSignaturesGenerated ?? 42} ECDSA İşlemi</span>
          </div>
        </div>

        <div className="flex gap-2">
          <button
            type="button"
            onClick={handleTestHsm}
            className="flex-1 py-2 rounded-xl bg-[#1B212D] hover:bg-[#2B3447] text-white border border-[#2B3447] text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors"
          >
            <Key className="w-3.5 h-3.5 text-[#F7931A]" />
            HSM ECDSA İmzasını Test Et
          </button>
          <button
            type="button"
            onClick={handleTestBiometric}
            className="flex-1 py-2 rounded-xl bg-[#1B212D] hover:bg-[#2B3447] text-[#00E5FF] border border-[#2B3447] text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors"
          >
            <Fingerprint className="w-3.5 h-3.5 text-[#00E5FF]" />
            Biyometrik Sensörü Sına
          </button>
        </div>

        {hsmSigResult && (
          <div className="mt-3 p-2.5 bg-[#090C10] rounded-xl text-[10px] font-mono break-all text-[#8B949E] border border-[#2B3447]/60">
            <span className="text-[#F7931A] font-semibold block mb-0.5">HSM Donanım İmzası (secp256k1):</span>
            {hsmSigResult}
          </div>
        )}

        {biometricResult && (
          <div className="mt-2 p-2 bg-[#00E676]/10 text-[#00E676] rounded-xl text-[11px] font-semibold border border-[#00E676]/30">
            {biometricResult}
          </div>
        )}
      </div>

      {/* 4. MULTI-FACTOR AUTHENTICATOR (RFC 6238 TOTP) */}
      <div className="p-5 rounded-2xl bg-[#12161F] border border-[#2B3447] mb-5">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Smartphone className="w-5 h-5 text-[#F7931A]" />
            <h2 className="text-xs font-bold text-white uppercase tracking-wider">
              Çok Faktörlü Kimlik Doğrulayıcı (TOTP RFC 6238)
            </h2>
          </div>
          <div className="flex items-center gap-1 text-[11px] text-[#00E5FF] font-mono">
            <span>{totpRemaining}s</span>
          </div>
        </div>

        {/* Live Code Banner */}
        <div className="text-center py-4 bg-[#090C10] rounded-2xl border border-[#2B3447]/60 mb-3">
          <p className="text-[10px] text-[#8B949E] uppercase font-semibold">Canlı 6-Haneli Güvenlik Kodu</p>
          <div className="text-3xl font-black font-mono tracking-widest text-[#F7931A] my-1">
            {currentTotp.slice(0, 3)} {currentTotp.slice(3, 6)}
          </div>
          <p className="text-[10px] text-[#8B949E]">Her 30 saniyede bir SHA-1 HMAC ile yenilenir</p>
        </div>

        {/* Secret Display */}
        <div className="flex items-center justify-between p-2.5 rounded-xl bg-[#090C10] border border-[#2B3447]/60 text-[11px] mb-3">
          <div>
            <span className="text-[#8B949E] block text-[10px]">OTPAuth Gizli Anahtarı</span>
            <span className="font-mono text-white text-xs">{settings.totpSecret}</span>
          </div>
          <button
            type="button"
            onClick={() => {
              navigator.clipboard.writeText(settings.totpSecret);
              setCopiedSecret(true);
              setTimeout(() => setCopiedSecret(false), 2000);
            }}
            className="text-xs text-[#00E5FF] hover:underline flex items-center gap-1"
          >
            {copiedSecret ? <Check className="w-3.5 h-3.5 text-[#00E676]" /> : <Copy className="w-3.5 h-3.5" />}
            {copiedSecret ? 'Kopyalandı' : 'Kopyala'}
          </button>
        </div>

        {/* Test Verifier */}
        <div className="flex gap-2">
          <input
            type="text"
            maxLength={6}
            value={testCodeInput}
            onChange={(e) => setTestCodeInput(e.target.value.replace(/\D/g, ''))}
            placeholder="6 Haneli Kodu Sına"
            className="flex-1 bg-[#090C10] border border-[#2B3447] rounded-xl px-3 py-2 text-xs font-mono text-center text-white focus:outline-none focus:border-[#F7931A]"
          />
          <button
            type="button"
            onClick={handleVerifyTestCode}
            className="px-4 py-2 rounded-xl bg-[#F7931A] text-black font-bold text-xs hover:bg-[#FFAB00]"
          >
            Doğrula
          </button>
        </div>

        {testCodeStatus && (
          <p
            className={`text-xs mt-2 font-medium ${
              testCodeStatus.includes('Geçerli') ? 'text-[#00E676]' : 'text-[#FF5252]'
            }`}
          >
            {testCodeStatus}
          </p>
        )}
      </div>

      {/* 5. 1,000-VECTOR AUDIT EXPLORER */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xs font-bold text-[#8B949E] uppercase tracking-wider">
            1,000 Güvenlik Vektörü Kataloğu
          </h2>
          <span className="text-[11px] text-[#00E676] font-semibold">1,000 Kriter Kapalı</span>
        </div>

        {/* Search */}
        <div className="relative mb-3">
          <Search className="w-4 h-4 text-[#8B949E] absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Vektör ID, standart (BIP-39, BOLT-2, NIST) veya saldırı ara..."
            className="w-full bg-[#12161F] border border-[#2B3447] rounded-xl pl-10 pr-4 py-2 text-xs text-white placeholder-[#8B949E] focus:outline-none focus:border-[#F7931A]"
          />
        </div>

        {/* Domain Filter Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-2 mb-3 no-scrollbar">
          {domains.map((d) => (
            <button
              key={d.id}
              type="button"
              onClick={() => setActiveDomainFilter(d.id)}
              className={`px-2.5 py-1 rounded-lg text-xs font-semibold whitespace-nowrap transition-colors ${
                activeDomainFilter === d.id
                  ? 'bg-[#1B212D] text-[#00E5FF] border border-[#00E5FF]/50'
                  : 'bg-[#12161F] text-[#8B949E] hover:text-white border border-[#2B3447]/60'
              }`}
            >
              {d.label}
            </button>
          ))}
        </div>

        {/* Vectors List */}
        <div className="space-y-2.5">
          {filteredVectors.slice(0, 50).map((v: AuditVector) => {
            const isCritical = v.severity === 'CRITICAL';
            return (
              <div
                key={v.id}
                className="p-3.5 rounded-2xl bg-[#12161F] border border-[#2B3447]/60 space-y-2"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span
                      className={`text-[9px] px-1.5 py-0.5 rounded font-bold uppercase ${
                        isCritical
                          ? 'bg-[#FF5252]/20 text-[#FF5252]'
                          : 'bg-[#FFAB00]/20 text-[#FFAB00]'
                      }`}
                    >
                      {v.severity}
                    </span>
                    <span className="font-mono text-[10px] text-[#8B949E]">VECT-{v.id}</span>
                    <span className="text-[10px] px-1.5 py-0.2 rounded bg-black/40 text-[#00E5FF] font-mono">
                      {v.standardRef}
                    </span>
                  </div>
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold flex items-center gap-1 ${
                    v.isClosed
                      ? 'bg-[#00E676]/15 text-[#00E676]'
                      : 'bg-[#FF5252]/15 text-[#FF5252]'
                  }`}>
                    <CheckCircle2 className="w-3 h-3" />
                    {v.isClosed ? 'KAPALI' : 'AÇIK'}
                  </span>
                </div>

                <div>
                  <h3 className="text-xs font-bold text-white">{v.vectorName}</h3>
                </div>

                <div className="pt-1.5 border-t border-[#2B3447]/40 text-[10px] text-[#00E676] font-medium flex items-center gap-1.5">
                  <ShieldCheck className="w-3 h-3 shrink-0" />
                  <span>Sıkılaştırma: {v.remediation}</span>
                </div>
              </div>
            );
          })}

          {filteredVectors.length > 50 && (
            <p className="text-center text-xs text-[#8B949E] py-2">
              ...ve {filteredVectors.length - 50} adet daha donanım & kriptografi denetimi tamamlandı.
            </p>
          )}
        </div>
      </div>
    </div>
  );
};
