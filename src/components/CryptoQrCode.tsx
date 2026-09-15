import React, { useEffect, useRef } from 'react';
import QRCode from 'qrcode';

interface Props {
  value: string;
  size?: number;
  badgeSymbol?: string;
  badgeColor?: string;
}

export const CryptoQrCode: React.FC<Props> = ({
  value,
  size = 220,
  badgeSymbol = '₿',
  badgeColor = '#F7931A',
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    if (!canvasRef.current || !value) return;

    QRCode.toCanvas(
      canvasRef.current,
      value,
      {
        width: size,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#FFFFFF',
        },
        errorCorrectionLevel: 'M',
      },
      (err) => {
        if (err) console.error('QR code generation error:', err);
      }
    );
  }, [value, size]);

  return (
    <div className="relative inline-flex items-center justify-center p-3.5 bg-white rounded-2xl shadow-xl shadow-black/60 border-2 border-[#2B3447]/40">
      <canvas ref={canvasRef} className="rounded-lg" />
      {badgeSymbol && (
        <div
          className="absolute inset-0 m-auto w-10 h-10 rounded-full flex items-center justify-center text-white text-base font-black shadow-md border-2 border-white"
          style={{ backgroundColor: badgeColor }}
        >
          {badgeSymbol}
        </div>
      )}
    </div>
  );
};
