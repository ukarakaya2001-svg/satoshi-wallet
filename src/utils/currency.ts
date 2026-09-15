import { CryptoAsset, CryptoAssetConfig, FiatConfig, FiatCurrency, NetworkConfig, NetworkType } from '../types';

export const NETWORKS: Record<NetworkType, NetworkConfig> = {
  BITCOIN_ONCHAIN: {
    id: 'BITCOIN_ONCHAIN',
    title: 'Bitcoin SegWit/Taproot',
    speed: '10-60 mins (On-Chain)',
    badgeColorHex: '#F7931A',
  },
  LIGHTNING_NETWORK: {
    id: 'LIGHTNING_NETWORK',
    title: 'Lightning Network',
    speed: 'Instant (<1 sec)',
    badgeColorHex: '#00E5FF',
  },
  ETHEREUM: {
    id: 'ETHEREUM',
    title: 'Ethereum (ERC-20)',
    speed: '12-60 secs (EVM)',
    badgeColorHex: '#627EEA',
  },
  LITECOIN: {
    id: 'LITECOIN',
    title: 'Litecoin (Scrypt)',
    speed: '2.5 mins (Fast UTXO)',
    badgeColorHex: '#345D9D',
  },
  RIPPLE_XRP: {
    id: 'RIPPLE_XRP',
    title: 'XRP Ledger (XRPL)',
    speed: '3-5 secs (Consensus)',
    badgeColorHex: '#00AAE4',
  },
};

export const CRYPTO_ASSETS: Record<CryptoAsset, CryptoAssetConfig> = {
  BTC: {
    symbol: 'BTC',
    displayName: 'Bitcoin',
    networkType: 'BITCOIN_ONCHAIN',
    iconColorHex: '#F7931A',
    defaultDerivationPath: "m/84'/0'/0'/0/0",
    unitName: 'sats',
    decimals: 8,
    basePriceUsd: 94850.0,
  },
  LIGHTNING: {
    symbol: 'LN',
    displayName: 'Lightning',
    networkType: 'LIGHTNING_NETWORK',
    iconColorHex: '#00E5FF',
    defaultDerivationPath: 'ln/channel/0',
    unitName: 'sats',
    decimals: 8,
    basePriceUsd: 94850.0,
  },
  ETH: {
    symbol: 'ETH',
    displayName: 'Ethereum',
    networkType: 'ETHEREUM',
    iconColorHex: '#627EEA',
    defaultDerivationPath: "m/44'/60'/0'/0/0",
    unitName: 'ETH',
    decimals: 18,
    basePriceUsd: 3420.0,
  },
  LTC: {
    symbol: 'LTC',
    displayName: 'Litecoin',
    networkType: 'LITECOIN',
    iconColorHex: '#345D9D',
    defaultDerivationPath: "m/84'/2'/0'/0/0",
    unitName: 'LTC',
    decimals: 8,
    basePriceUsd: 112.5,
  },
  XRP: {
    symbol: 'XRP',
    displayName: 'Ripple XRP',
    networkType: 'RIPPLE_XRP',
    iconColorHex: '#00AAE4',
    defaultDerivationPath: "m/44'/144'/0'/0/0",
    unitName: 'XRP',
    decimals: 6,
    basePriceUsd: 2.45,
  },
};

export const FIAT_CURRENCIES: Record<FiatCurrency, FiatConfig> = {
  USD: { code: 'USD', symbol: '$', displayName: 'US Dollar', btcPriceInFiat: 94850.0, usdToFiatRate: 1.0 },
  EUR: { code: 'EUR', symbol: '€', displayName: 'Euro', btcPriceInFiat: 87400.0, usdToFiatRate: 0.92 },
  TRY: { code: 'TRY', symbol: '₺', displayName: 'Turkish Lira', btcPriceInFiat: 3280000.0, usdToFiatRate: 34.6 },
  GBP: { code: 'GBP', symbol: '£', displayName: 'British Pound', btcPriceInFiat: 74200.0, usdToFiatRate: 0.78 },
  JPY: { code: 'JPY', symbol: '¥', displayName: 'Japanese Yen', btcPriceInFiat: 14650000.0, usdToFiatRate: 154.5 },
  CAD: { code: 'CAD', symbol: 'CA$', displayName: 'Canadian Dollar', btcPriceInFiat: 131200.0, usdToFiatRate: 1.38 },
  AUD: { code: 'AUD', symbol: 'AU$', displayName: 'Australian Dollar', btcPriceInFiat: 147900.0, usdToFiatRate: 1.55 },
  CHF: { code: 'CHF', symbol: 'CHF', displayName: 'Swiss Franc', btcPriceInFiat: 83100.0, usdToFiatRate: 0.88 },
};

export class CurrencyFormatter {
  static formatSats(sats: number): string {
    return `${sats.toLocaleString('en-US')} sats`;
  }

  static formatSatsToBtc(sats: number): string {
    const btc = sats / 100_000_000;
    return `${btc.toFixed(8)} BTC`;
  }

  static formatFiat(sats: number, fiat: FiatCurrency): string {
    const btc = sats / 100_000_000;
    const config = FIAT_CURRENCIES[fiat];
    const fiatVal = btc * config.btcPriceInFiat;
    return `${config.symbol}${fiatVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  static formatFiatValue(amountUsd: number, fiat: FiatCurrency): string {
    const config = FIAT_CURRENCIES[fiat];
    const fiatVal = amountUsd * config.usdToFiatRate;
    return `${config.symbol}${fiatVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  static formatAssetAmount(asset: CryptoAsset, amountDecimal: number): string {
    const config = CRYPTO_ASSETS[asset];
    const fractionDigits = asset === 'BTC' || asset === 'LIGHTNING' ? 8 : asset === 'XRP' ? 2 : 4;
    return `${amountDecimal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: fractionDigits })} ${config.symbol}`;
  }

  static getAssetPriceInFiat(asset: CryptoAsset, fiat: FiatCurrency): number {
    const assetConfig = CRYPTO_ASSETS[asset];
    const fiatConfig = FIAT_CURRENCIES[fiat];
    return assetConfig.basePriceUsd * fiatConfig.usdToFiatRate;
  }
}
