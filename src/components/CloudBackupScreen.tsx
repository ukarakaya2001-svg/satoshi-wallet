import React, { useState } from 'react';
import {
  ArrowLeft,
  Cloud,
  Shield,
  Eye,
  EyeOff,
  Copy,
  Check,
  Lock,
  KeyRound,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react';
import { useWallet } from '../context/WalletContext';

interface Props {
  onBack: () => void;
}

export const CloudBackupScreen: React.FC<Props> = ({ onBack }) => {
  const { walletState, createCloudBackup, restoreCloudBackup } = useWallet();
  const backup = walletState.cloudBackup;

  const [isMnemonicRevealed, setIsMnemonicRevealed] = useState(false);
  const [copiedMnemonic, setCopiedMnemonic] = useState(false);
  const [copiedChecksum, setCopiedChecksum] = useState(false);

  // Create backup form
  const [passphrase, setPassphrase] = useState('');
  const [confirmPassphrase, setConfirmPassphrase] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [createMsg, setCreateMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Restore form
  const [restorePassphrase, setRestorePassphrase] = useState('');
  const [isRestoring, setIsRestoring] = useState(false);
  const [restoreMsg, setRestoreMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const handleCopyMnemonic = () => {
    navigator.clipboard.writeText(walletState.mnemonicWords.join(' '));
    setCopiedMnemonic(true);
    setTimeout(() => setCopiedMnemonic(false), 2000);
  };

  const handleCopyChecksum = () => {
    if (backup.cloudChecksumSha256) {
      navigator.clipboard.writeText(backup.cloudChecksumSha256);
      setCopiedChecksum(true);
      setTimeout(() => setCopiedChecksum(false), 2000);
    }
  };

  const handleCreateBackup = async () => {
    setCreateMsg(null);
    if (passphrase.length < 8) {
      setCreateMsg({ type: 'error', text: 'Parola en az 8 karakter uzunluğunda olmalıdır.' });
      return;
    }
    if (passphrase !== confirmPassphrase) {
      setCreateMsg({ type: 'error', text: 'Parolalar eşleşmiyor. Lütfen tekrar kontrol edin.' });
      return;
    }

    setIsCreating(true);
    try {
      await createCloudBackup(passphrase);
      setCreateMsg({
        type: 'success',
        text: 'Sıfır-Bilgi AES-256-GCM yedeği 100,000 PBKDF2 turuyla başarıyla şifrelendi!',
      });
      setPassphrase('');
      setConfirmPassphrase('');
    } catch (err: any) {
      setCreateMsg({ type: 'error', text: err.message || 'Yedekleme oluşturulamadı.' });
    } finally {
      setIsCreating(false);
    }
  };

  const handleRestoreBackup = async () => {
    setRestoreMsg(null);
    if (!restorePassphrase) {
      setRestoreMsg({ type: 'error', text: 'Lütfen yedek parolanızı girin.' });
      return;
    }

    setIsRestoring(true);
    try {
      const ok = await restoreCloudBackup(restorePassphrase);
      if (ok) {
        setRestoreMsg({
          type: 'success',
          text: 'Bulut yedeği SHA-256 bütünlük kontrolünden geçti ve başarıyla doğrulandı!',
        });
        setRestorePassphrase('');
      } else {
        setRestoreMsg({ type: 'error', text: 'Hatalı parola veya geçersiz şifreleme anahtarı.' });
      }
    } catch (err: any) {
      setRestoreMsg({ type: 'error', text: err.message || 'Geri yükleme başarısız.' });
    } finally {
      setIsRestoring(false);
    }
  };

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
          <h1 className="text-lg font-bold text-white">Şifreli Bulut Yedekleme</h1>
          <p className="text-[11px] text-[#8B949E]">Sıfır-Bilgi AES-256-GCM Çoklu Varlık Kasası</p>
        </div>
      </div>

      {/* Cloud Backup Status Card */}
      <div className="p-5 rounded-2xl bg-[#12161F] border border-[#00E676]/40 shadow-xl shadow-black/50 mb-5">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-[#00E676]/15 text-[#00E676] flex items-center justify-center">
              <Cloud className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white">Şifreli Bulut Kasası Aktif</h2>
              <p className="text-[11px] text-[#8B949E]">
                Son Yedekleme: {backup.lastBackupTimestamp ? new Date(backup.lastBackupTimestamp).toLocaleString('tr-TR') : 'Henüz yapılmadı'}
              </p>
            </div>
          </div>
          <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#00E676]/20 text-[#00E676] font-bold">
            GÜNCEL
          </span>
        </div>

        <div className="grid grid-cols-2 gap-2 text-[11px] bg-[#090C10] p-3 rounded-xl border border-[#2B3447]/60 mb-3">
          <div>
            <span className="text-[#8B949E] block">Şifreleme Şeması</span>
            <span className="font-mono text-white font-semibold">{backup.backupCipher}</span>
          </div>
          <div>
            <span className="text-[#8B949E] block">Anahtar Türetme (KDF)</span>
            <span className="font-mono text-white font-semibold">PBKDF2 ({backup.kdfRounds.toLocaleString()} Tur)</span>
          </div>
        </div>

        {backup.cloudChecksumSha256 && (
          <div className="text-[10px]">
            <div className="flex items-center justify-between mb-1">
              <span className="text-[#8B949E]">Bütünlük Özeti (SHA-256 Checksum)</span>
              <button
                type="button"
                onClick={handleCopyChecksum}
                className="text-[#00E5FF] hover:underline flex items-center gap-1"
              >
                {copiedChecksum ? <Check className="w-3 h-3 text-[#00E676]" /> : <Copy className="w-3 h-3" />}
                {copiedChecksum ? 'Kopyalandı' : 'Kopyala'}
              </button>
            </div>
            <p className="font-mono text-[#8B949E] break-all bg-[#090C10] p-2 rounded-lg">
              {backup.cloudChecksumSha256}
            </p>
          </div>
        )}
      </div>

      {/* 12-Word Seed Phrase */}
      <div className="p-5 rounded-2xl bg-[#12161F] border border-[#2B3447] mb-5">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <KeyRound className="w-4 h-4 text-[#F7931A]" />
            <h2 className="text-xs font-bold text-white uppercase tracking-wider">
              12 Kelimelik BIP-39 Kurtarma İfadesi
            </h2>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setIsMnemonicRevealed(!isMnemonicRevealed)}
              className="text-xs text-[#F7931A] hover:underline font-semibold flex items-center gap-1"
            >
              {isMnemonicRevealed ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
              {isMnemonicRevealed ? 'Gizle' : 'Göster'}
            </button>
            {isMnemonicRevealed && (
              <button
                type="button"
                onClick={handleCopyMnemonic}
                className="text-xs text-[#00E5FF] hover:underline font-semibold flex items-center gap-1"
              >
                {copiedMnemonic ? <Check className="w-3.5 h-3.5 text-[#00E676]" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedMnemonic ? 'Kopyalandı' : 'Kopyala'}
              </button>
            )}
          </div>
        </div>

        {isMnemonicRevealed ? (
          <div className="grid grid-cols-3 gap-2">
            {walletState.mnemonicWords.map((word, idx) => (
              <div
                key={idx}
                className="flex items-center gap-2 p-2 rounded-xl bg-[#090C10] border border-[#2B3447]/60"
              >
                <span className="text-[10px] text-[#8B949E] font-mono">{idx + 1}.</span>
                <span className="text-xs font-mono font-bold text-white">{word}</span>
              </div>
            ))}
          </div>
        ) : (
          <div
            onClick={() => setIsMnemonicRevealed(true)}
            className="p-6 text-center bg-[#090C10] rounded-xl border border-dashed border-[#2B3447] cursor-pointer hover:border-[#F7931A] transition-colors"
          >
            <Lock className="w-6 h-6 text-[#8B949E] mx-auto mb-2" />
            <p className="text-xs text-white font-semibold">Kurtarma İfadesini Görmek İçin Tıklayın</p>
            <p className="text-[10px] text-[#8B949E] mt-0.5">
              Bu kelimeleri asla kimseyle paylaşmayın. Çalınması halinde paranız kaybolabilir.
            </p>
          </div>
        )}
      </div>

      {/* Create Backup Form */}
      <div className="p-5 rounded-2xl bg-[#12161F] border border-[#2B3447] mb-5 space-y-3">
        <h2 className="text-xs font-bold text-white uppercase tracking-wider">
          Yeni Sıfır-Bilgi Bulut Yedeği Oluştur
        </h2>
        <p className="text-[11px] text-[#8B949E]">
          Cihazınızdaki tüm özel anahtarlar ve Lightning kanalları AES-256-GCM ile şifrelenir. Şifre çözme anahtarı asla sunucuya iletilmez.
        </p>

        <div>
          <label className="text-[10px] text-[#8B949E] block mb-1">Güçlü Yedekleme Parolası (Min 8 Karakter)</label>
          <input
            type="password"
            value={passphrase}
            onChange={(e) => setPassphrase(e.target.value)}
            placeholder="••••••••"
            className="w-full bg-[#090C10] border border-[#2B3447] rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-[#8B949E] focus:outline-none focus:border-[#F7931A]"
          />
        </div>

        <div>
          <label className="text-[10px] text-[#8B949E] block mb-1">Parolayı Tekrar Edin</label>
          <input
            type="password"
            value={confirmPassphrase}
            onChange={(e) => setConfirmPassphrase(e.target.value)}
            placeholder="••••••••"
            className="w-full bg-[#090C10] border border-[#2B3447] rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-[#8B949E] focus:outline-none focus:border-[#F7931A]"
          />
        </div>

        {createMsg && (
          <div
            className={`p-3 rounded-xl text-xs flex items-center gap-2 ${
              createMsg.type === 'success'
                ? 'bg-[#00E676]/15 border border-[#00E676]/40 text-[#00E676]'
                : 'bg-[#FF5252]/15 border border-[#FF5252]/40 text-[#FF5252]'
            }`}
          >
            {createMsg.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 shrink-0" />
            ) : (
              <AlertCircle className="w-4 h-4 shrink-0" />
            )}
            <span>{createMsg.text}</span>
          </div>
        )}

        <button
          type="button"
          disabled={isCreating}
          onClick={handleCreateBackup}
          className="w-full py-3 rounded-xl bg-[#F7931A] hover:bg-[#FFAB00] text-black font-bold text-xs transition-colors flex items-center justify-center gap-2"
        >
          <Lock className="w-3.5 h-3.5" />
          {isCreating ? '100,000 PBKDF2 Turu Hesaplanıyor...' : 'AES-256-GCM ile Şifrele ve Yedekle'}
        </button>
      </div>

      {/* Restore Backup Form */}
      <div className="p-5 rounded-2xl bg-[#12161F] border border-[#2B3447] space-y-3">
        <h2 className="text-xs font-bold text-white uppercase tracking-wider">
          Bulut Yedeğini Test Et / Geri Yükle
        </h2>
        <p className="text-[11px] text-[#8B949E]">
          Mevcut şifreli bulut anlık görüntüsünü parolanız ile çözüp SHA-256 bütünlüğünü doğrulayın.
        </p>

        <div>
          <label className="text-[10px] text-[#8B949E] block mb-1">Yedekleme Parolası</label>
          <input
            type="password"
            value={restorePassphrase}
            onChange={(e) => setRestorePassphrase(e.target.value)}
            placeholder="••••••••"
            className="w-full bg-[#090C10] border border-[#2B3447] rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-[#8B949E] focus:outline-none focus:border-[#00E5FF]"
          />
        </div>

        {restoreMsg && (
          <div
            className={`p-3 rounded-xl text-xs flex items-center gap-2 ${
              restoreMsg.type === 'success'
                ? 'bg-[#00E676]/15 border border-[#00E676]/40 text-[#00E676]'
                : 'bg-[#FF5252]/15 border border-[#FF5252]/40 text-[#FF5252]'
            }`}
          >
            {restoreMsg.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 shrink-0" />
            ) : (
              <AlertCircle className="w-4 h-4 shrink-0" />
            )}
            <span>{restoreMsg.text}</span>
          </div>
        )}

        <button
          type="button"
          disabled={isRestoring}
          onClick={handleRestoreBackup}
          className="w-full py-2.5 rounded-xl bg-[#1B212D] hover:bg-[#2B3447] text-[#00E5FF] border border-[#2B3447] font-semibold text-xs transition-colors flex items-center justify-center gap-2"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isRestoring ? 'animate-spin' : ''}`} />
          {isRestoring ? 'Şifre Çözülüyor...' : 'Şifreli Yedeği Doğrula ve Geri Yükle'}
        </button>
      </div>
    </div>
  );
};
