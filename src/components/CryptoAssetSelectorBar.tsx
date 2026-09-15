import React from 'react';
import { CryptoAsset } from '../types';
import { CRYPTO_ASSETS } from '../utils/currency';

interface Props {
  selectedAsset: CryptoAsset;
  onAssetSelected: (asset: CryptoAsset) => void;
}

export const CryptoAssetSelectorBar: React.FC<Props> = ({ selectedAsset, onAssetSelected }) => {
  const assets: CryptoAsset[] = ['BTC', 'LIGHTNING', 'ETH', 'LTC', 'XRP'];

  return (
    <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
      {assets.map((asset) => {
        const config = CRYPTO_ASSETS[asset];
        const isSelected = selectedAsset === asset;

        return (
          <button
            key={asset}
            type="button"
            onClick={() => onAssetSelected(asset)}
            className={`flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-semibold whitespace-nowrap transition-all border ${
              isSelected
                ? 'bg-[#1B212D] border-[#2B3447] text-white shadow-lg shadow-black/40'
                : 'bg-[#12161F] border-[#2B3447]/50 text-[#8B949E] hover:text-white hover:border-[#2B3447]'
            }`}
          >
            <span
              className="w-2.5 h-2.5 rounded-full inline-block shrink-0 shadow-sm"
              style={{ backgroundColor: config.iconColorHex }}
            />
            <span>{config.displayName}</span>
            <span className="text-[10px] px-1.5 py-0.5 rounded bg-black/40 text-[#8B949E]">
              {config.symbol}
            </span>
          </button>
        );
      })}
    </div>
  );
};
