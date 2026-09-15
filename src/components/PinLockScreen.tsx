import React, { useState } from 'react';
import { Fingerprint, Delete, Shield, KeyRound } from 'lucide-react';
import { motion } from 'motion/react';

interface Props {
  onUnlock: (pin: string) => boolean;
  onBiometricUnlock: () => Promise<boolean>;
}

export const PinLockScreen: React.FC<Props> = ({ onUnlock, onBiometricUnlock }) => {
  const [pin, setPin] = useState<string>('');
  const [isError, setIsError] = useState<boolean>(false);
  const [biometricMsg, setBiometricMsg] = useState<string | null>(null);

  const handleDigit = (d: string) => {
    if (pin.length >= 4) return;
    setIsError(false);
    const newPin = pin + d;
    setPin(newPin);

    if (newPin.length === 4) {
      const success = onUnlock(newPin);
      if (!success) {
        setIsError(true);
        setTimeout(() => {
          setPin('');
        }, 500);
      }
    }
  };

  const handleBackspace = () => {
    setIsError(false);
    setPin((prev) => prev.slice(0, -1));
  };

  const handleBiometric = async () => {
    setBiometricMsg('Biyometrik sensör taranıyor...');
    const ok = await onBiometricUnlock();
    if (!ok) {
      setBiometricMsg('Biyometrik doğrulama başarısız oldu.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-[#090C10] flex flex-col justify-between items-center p-6 select-none">
      <div className="flex-1 flex flex-col items-center justify-center max-w-sm w-full">
        {/* Emblem */}
        <motion.div
          initial={{ scale: 0.8, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ duration: 0.4 }}
          className="w-20 h-20 rounded-full bg-gradient-to-br from-[#F7931A] to-[#1E1B2C] border-2 border-[#F7931A] flex items-center justify-center shadow-2xl shadow-[#F7931A]/20 mb-5"
        >
          <span className="text-white text-3xl font-black">₿</span>
        </motion.div>

        <h1 className="text-2xl font-bold text-white tracking-tight">Satoshi Wallet</h1>
        <p className="text-xs text-[#8B949E] mt-1 text-center font-medium">
          {isError ? (
            <span className="text-[#FF5252] font-semibold">Hatalı PIN. Lütfen tekrar deneyin.</span>
          ) : (
            'PIN Kodunu Girin (Varsayılan: 2121) veya Biyometriyi Kullanın'
          )}
        </p>

        {biometricMsg && (
          <p className="text-[11px] text-[#00E5FF] mt-2 font-medium">{biometricMsg}</p>
        )}

        {/* 4 PIN Dots */}
        <motion.div
          animate={isError ? { x: [-10, 10, -8, 8, -4, 4, 0] } : {}}
          transition={{ duration: 0.4 }}
          className="flex items-center gap-4 my-8"
        >
          {[0, 1, 2, 3].map((idx) => {
            const isFilled = idx < pin.length;
            return (
              <div
                key={idx}
                className={`w-4 h-4 rounded-full transition-all duration-200 ${
                  isError
                    ? 'bg-[#FF5252] scale-110 shadow-lg shadow-[#FF5252]/40'
                    : isFilled
                    ? 'bg-[#F7931A] scale-110 shadow-lg shadow-[#F7931A]/40'
                    : 'bg-[#12161F] border border-[#2B3447]'
                }`}
              />
            );
          })}
        </motion.div>

        {/* Keypad */}
        <div className="grid grid-cols-3 gap-4 w-full max-w-[280px]">
          {['1', '2', '3', '4', '5', '6', '7', '8', '9'].map((digit) => (
            <button
              key={digit}
              type="button"
              onClick={() => handleDigit(digit)}
              className="h-16 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] active:scale-95 text-white text-2xl font-semibold border border-[#2B3447]/60 flex items-center justify-center transition-all shadow-md"
            >
              {digit}
            </button>
          ))}

          {/* Biometrics */}
          <button
            type="button"
            onClick={handleBiometric}
            className="h-16 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] active:scale-95 text-[#00E5FF] border border-[#2B3447]/60 flex items-center justify-center transition-all"
            title="Biyometrik Giriş"
          >
            <Fingerprint className="w-7 h-7" />
          </button>

          {/* 0 */}
          <button
            type="button"
            onClick={() => handleDigit('0')}
            className="h-16 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] active:scale-95 text-white text-2xl font-semibold border border-[#2B3447]/60 flex items-center justify-center transition-all shadow-md"
          >
            0
          </button>

          {/* Backspace */}
          <button
            type="button"
            onClick={handleBackspace}
            className="h-16 rounded-2xl bg-[#12161F] hover:bg-[#1B212D] active:scale-95 text-[#8B949E] hover:text-white border border-[#2B3447]/60 flex items-center justify-center transition-all"
            title="Sil"
          >
            <Delete className="w-6 h-6" />
          </button>
        </div>
      </div>

      <div className="flex items-center gap-2 text-[11px] text-[#8B949E]">
        <Shield className="w-3.5 h-3.5 text-[#00E676]" />
        <span>FIPS 140-3 StrongBox Donanım HSM Koruması Devrede</span>
      </div>
    </div>
  );
};
