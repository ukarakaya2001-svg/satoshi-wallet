import React, { createContext, useContext, useEffect, useState, useMemo } from 'react';
import {
  CryptoAsset,
  FiatCurrency,
  LightningChannel,
  SecuritySettings,
  TransactionRecord,
  WalletNavTab,
  WalletState,
} from '../types';
import { CRYPTO_ASSETS, CurrencyFormatter } from '../utils/currency';
import {
  encryptWithPassword,
  generateMnemonic,
  sanitizeMemo,
} from '../utils/crypto';
import { VulnerabilityAuditEngine } from '../utils/auditEngine';
import { HardwareSecurityModule } from '../utils/hsm';
import { generateTotpCode, verifyTotpCode } from '../utils/totp';

const STORAGE_KEY = 'satoshi_wallet_state_v1';
const PIN_CODE = '2121';

const INITIAL_MNEMONIC = [
  'satoshi', 'quantum', 'vault', 'matrix', 'orbital', 'crypto',
  'harvest', 'legacy', 'shuttle', 'pioneer', 'summit', 'diamond'
];

const INITIAL_CHANNELS: LightningChannel[] = [
  {
    channelId: 'chn_001_blockstream_core',
    remoteNodeAlias: 'Blockstream Core Routing #01',
    localCapacitySats: 450_000,
    remoteCapacitySats: 550_000,
    status: 'ACTIVE_ONLINE',
    htlcCount: 14,
  },
  {
    channelId: 'chn_002_acinq_phoenix',
    remoteNodeAlias: 'ACINQ Phoenix Liquidity Pool',
    localCapacitySats: 200_000,
    remoteCapacitySats: 800_000,
    status: 'ACTIVE_ONLINE',
    htlcCount: 9,
  },
];

const INITIAL_TRANSACTIONS: TransactionRecord[] = [
  {
    id: 'tx_btc_01',
    txHash: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    network: 'BITCOIN_ONCHAIN',
    type: 'RECEIVE',
    amountSatoshis: 1_250_000,
    feeSatoshis: 1_250,
    recipientOrSender: 'bc1q87x9p43j2y6zfl0w5mn8p09vx3r2qll59kzz78',
    timestamp: Date.now() - 1000 * 60 * 60 * 2,
    memo: 'Kraken Institutional OTC Cold Settlement',
    status: 'CONFIRMED',
    asset: 'BTC',
  },
  {
    id: 'tx_ln_02',
    txHash: '4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b',
    network: 'LIGHTNING_NETWORK',
    type: 'SEND',
    amountSatoshis: 25_000,
    feeSatoshis: 1,
    recipientOrSender: 'lnbc250u1p3x59r...satoshi_node',
    timestamp: Date.now() - 1000 * 60 * 60 * 18,
    memo: 'Bitrefill eSIM & VPN Zero-Latency Lightning',
    status: 'CONFIRMED',
    asset: 'LIGHTNING',
  },
  {
    id: 'tx_eth_03',
    txHash: '0x8f2d59a2c1b7e64f89d01247a32b9187e5b64219c8f5d32109e4a8b7c61f2e9a',
    network: 'ETHEREUM',
    type: 'RECEIVE',
    amountSatoshis: 145_000_000,
    feeSatoshis: 21_000,
    recipientOrSender: '0x71C...498f',
    timestamp: Date.now() - 1000 * 60 * 60 * 36,
    memo: 'Uniswap v3 Liquidity Pool Yield Staking',
    status: 'CONFIRMED',
    asset: 'ETH',
    customAmountText: '1.4500 ETH',
    customFeeText: '0.0012 ETH (15 Gwei)',
  },
  {
    id: 'tx_ltc_04',
    txHash: '9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b',
    network: 'LITECOIN',
    type: 'RECEIVE',
    amountSatoshis: 42_500_000,
    feeSatoshis: 10_000,
    recipientOrSender: 'ltc1q43j...8w',
    timestamp: Date.now() - 1000 * 60 * 60 * 72,
    memo: 'BitPay Merchant POS Settlement',
    status: 'CONFIRMED',
    asset: 'LTC',
    customAmountText: '42.5000 LTC',
    customFeeText: '0.0001 LTC',
  },
  {
    id: 'tx_xrp_05',
    txHash: '1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b',
    network: 'RIPPLE_XRP',
    type: 'SEND',
    amountSatoshis: 250_000_000,
    feeSatoshis: 12,
    recipientOrSender: 'rEb8TK3gBgk5auZyyb6BiHg24ey2q4FP3',
    timestamp: Date.now() - 1000 * 60 * 60 * 120,
    memo: 'XRPL Cross-Border Ripple Settlement',
    status: 'CONFIRMED',
    asset: 'XRP',
    customAmountText: '250.00 XRP',
    customFeeText: '0.000012 XRP (12 Drops)',
  },
];

const DEFAULT_SECURITY: SecuritySettings = {
  isFlagSecureEnabled: true,
  isBiometricPinEnabled: true,
  isAutoLockEnabled: true,
  isClipboardAutoClearEnabled: true,
  isStrictBech32ValidationEnabled: true,
  isHtlcWatchdogEnabled: true,
  isHardwareKeystoreActive: true,
  isMfaEnabled: true,
  requireMfaForSend: true,
  totpSecret: 'JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP', // standard base32 seed
};

interface SendParams {
  asset: CryptoAsset;
  recipient: string;
  amount: number;
  memo: string;
  feeRate: number;
}

interface WalletContextType {
  walletState: WalletState;
  activeTab: WalletNavTab;
  setActiveTab: (tab: WalletNavTab) => UnitOrVoid;
  isUnlocked: boolean;
  unlockWallet: (pin: string) => boolean;
  unlockWithBiometric: () => Promise<boolean>;
  lockWallet: () => void;
  toggleBalancePrivacy: () => void;
  selectFiat: (fiat: FiatCurrency) => void;
  selectAsset: (asset: CryptoAsset) => void;
  executeSend: (params: SendParams) => Promise<TransactionRecord>;
  generateLightningInvoice: (amountSats: number, memo: string) => string;
  createCloudBackup: (passphrase: string) => Promise<void>;
  restoreCloudBackup: (passphrase: string) => Promise<boolean>;
  toggleSecurityShield: (key: keyof SecuritySettings) => void;
  autoHardenAll1000: () => void;
  runAudit: () => void;
  testHsmSignature: () => Promise<string>;
  testBiometrics: () => Promise<{ success: boolean; message: string }>;
  verifyMfa: (code: string) => Promise<boolean>;
}

type UnitOrVoid = void;

const WalletContext = createContext<WalletContextType | null>(null);

export const WalletProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [activeTab, setActiveTab] = useState<WalletNavTab>('DASHBOARD');
  const [isUnlocked, setIsUnlocked] = useState<boolean>(true); // start unlocked for convenient inspection

  const [state, setState] = useState<WalletState>(() => {
    const defaultAudit = VulnerabilityAuditEngine.runComprehensive1000Audit(
      DEFAULT_SECURITY.isFlagSecureEnabled,
      DEFAULT_SECURITY.isBiometricPinEnabled,
      DEFAULT_SECURITY.isAutoLockEnabled,
      true
    );

    const initialState: WalletState = {
      isInitialized: true,
      isLocked: false,
      onChainBalanceSats: 1_850_000, // 0.0185 BTC
      lightningBalanceSats: 650_000, // 650k sats
      ethereumBalanceEth: 1.45,
      litecoinBalanceLtc: 42.5,
      rippleBalanceXrp: 1250.0,
      onChainAddress: 'bc1q87x9p43j2y6zfl0w5mn8p09vx3r2qll59kzz78',
      taprootAddress: 'bc1p5d7rjq7g6rd2ee0hz0sdqw3t4nvda9nxrjp9nh8w4cwm27efx5pq9k2q7w',
      lightningNodeId: '03864ef025fde8fb587d989186ce6a4a186895eeec252413091714b921b4f06606',
      ethereumAddress: '0x71C5696543ae55317E221B6686326E6dEc59498f',
      litecoinAddress: 'ltc1q87x9p43j2y6zfl0w5mn8p09vx3r2qll58w3xkl',
      rippleAddress: 'rEb8TK3gBgk5auZyyb6BiHg24ey2q4FP3',
      rippleDestinationTag: 104928,
      selectedAsset: 'BTC',
      activeFiatCurrency: 'USD',
      isBalanceHidden: false,
      mnemonicWords: INITIAL_MNEMONIC,
      transactions: INITIAL_TRANSACTIONS,
      lightningChannels: INITIAL_CHANNELS,
      cloudBackup: {
        lastBackupTimestamp: Date.now() - 1000 * 60 * 60 * 24 * 3,
        backupCipher: 'AES-256-GCM',
        kdfRounds: 100_000,
        cloudLocation: 'Zero-Knowledge Encrypted Secure Vault',
        cloudChecksumSha256: '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08',
        isCloudSyncActive: true,
        backupCount: 3,
        backedUpAssets: ['BTC', 'LN', 'ETH', 'LTC', 'XRP'],
      },
      securitySettings: DEFAULT_SECURITY,
      auditSummary: defaultAudit,
      hsmStatus: HardwareSecurityModule.getHsmStatus(),
    };

    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        return {
          ...initialState,
          ...parsed,
          auditSummary: VulnerabilityAuditEngine.runComprehensive1000Audit(
            parsed.securitySettings?.isFlagSecureEnabled ?? DEFAULT_SECURITY.isFlagSecureEnabled,
            parsed.securitySettings?.isBiometricPinEnabled ?? DEFAULT_SECURITY.isBiometricPinEnabled,
            parsed.securitySettings?.isAutoLockEnabled ?? DEFAULT_SECURITY.isAutoLockEnabled,
            true,
            parsed.auditSummary?.isAutoRemediated ?? false
          ),
        };
      }
    } catch (e) {
      // Fallback to initial
    }
    return initialState;
  });

  useEffect(() => {
    try {
      const toSave = {
        onChainBalanceSats: state.onChainBalanceSats,
        lightningBalanceSats: state.lightningBalanceSats,
        ethereumBalanceEth: state.ethereumBalanceEth,
        litecoinBalanceLtc: state.litecoinBalanceLtc,
        rippleBalanceXrp: state.rippleBalanceXrp,
        selectedAsset: state.selectedAsset,
        activeFiatCurrency: state.activeFiatCurrency,
        isBalanceHidden: state.isBalanceHidden,
        mnemonicWords: state.mnemonicWords,
        transactions: state.transactions,
        lightningChannels: state.lightningChannels,
        cloudBackup: state.cloudBackup,
        securitySettings: state.securitySettings,
      };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(toSave));
    } catch (err) {
      // ignore
    }
  }, [state]);

  const unlockWallet = (pin: string): boolean => {
    if (pin === PIN_CODE) {
      setIsUnlocked(true);
      setState((prev) => ({ ...prev, isLocked: false }));
      return true;
    }
    return false;
  };

  const unlockWithBiometric = async (): Promise<boolean> => {
    const res = await HardwareSecurityModule.promptBiometricAuth('Cüzdan Giriş Kilidi Açma');
    if (res.success) {
      setIsUnlocked(true);
      setState((prev) => ({ ...prev, isLocked: false }));
      return true;
    }
    return false;
  };

  const lockWallet = () => {
    setIsUnlocked(false);
    setState((prev) => ({ ...prev, isLocked: true }));
  };

  const toggleBalancePrivacy = () => {
    setState((prev) => ({ ...prev, isBalanceHidden: !prev.isBalanceHidden }));
  };

  const selectFiat = (fiat: FiatCurrency) => {
    setState((prev) => ({ ...prev, activeFiatCurrency: fiat }));
  };

  const selectAsset = (asset: CryptoAsset) => {
    setState((prev) => ({ ...prev, selectedAsset: asset }));
  };

  const executeSend = async ({ asset, recipient, amount, memo, feeRate }: SendParams): Promise<TransactionRecord> => {
    // Hardware signing via HSM
    const hsmSig = await HardwareSecurityModule.signTransactionPayload(`${asset}:${recipient}:${amount}:${memo}`);
    const txHash = '0x' + hsmSig.signatureHex.slice(8, 72);

    let satoshis = 0;
    let customAmountText: string | undefined;
    let customFeeText: string | undefined;

    if (asset === 'BTC') {
      satoshis = Math.round(amount * 100_000_000);
      customFeeText = `${feeRate} sat/vB (~${feeRate * 140} sats)`;
    } else if (asset === 'LIGHTNING') {
      satoshis = Math.round(amount);
      customFeeText = '1 sat (Instant BOLT-11)';
    } else if (asset === 'ETH') {
      satoshis = Math.round(amount * 100_000_000);
      customAmountText = `${amount.toFixed(4)} ETH`;
      customFeeText = `${feeRate} Gwei`;
    } else if (asset === 'LTC') {
      satoshis = Math.round(amount * 100_000_000);
      customAmountText = `${amount.toFixed(4)} LTC`;
      customFeeText = '0.0001 LTC';
    } else if (asset === 'XRP') {
      satoshis = Math.round(amount * 1_000_000);
      customAmountText = `${amount.toFixed(2)} XRP`;
      customFeeText = '12 Drops';
    }

    const networkMap: Record<CryptoAsset, TransactionRecord['network']> = {
      BTC: 'BITCOIN_ONCHAIN',
      LIGHTNING: 'LIGHTNING_NETWORK',
      ETH: 'ETHEREUM',
      LTC: 'LITECOIN',
      XRP: 'RIPPLE_XRP',
    };

    const newTx: TransactionRecord = {
      id: `tx_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
      txHash,
      network: networkMap[asset],
      type: 'SEND',
      amountSatoshis: satoshis,
      feeSatoshis: asset === 'LIGHTNING' ? 1 : feeRate * 140,
      recipientOrSender: recipient,
      timestamp: Date.now(),
      memo: sanitizeMemo(memo) || `${asset} Transfer (HSM Signed)`,
      status: 'CONFIRMED',
      asset,
      customAmountText,
      customFeeText,
    };

    setState((prev) => {
      let updatedBtc = prev.onChainBalanceSats;
      let updatedLn = prev.lightningBalanceSats;
      let updatedEth = prev.ethereumBalanceEth;
      let updatedLtc = prev.litecoinBalanceLtc;
      let updatedXrp = prev.rippleBalanceXrp;

      if (asset === 'BTC') {
        updatedBtc = Math.max(0, updatedBtc - satoshis - (feeRate * 140));
      } else if (asset === 'LIGHTNING') {
        updatedLn = Math.max(0, updatedLn - satoshis - 1);
      } else if (asset === 'ETH') {
        updatedEth = Math.max(0, updatedEth - amount);
      } else if (asset === 'LTC') {
        updatedLtc = Math.max(0, updatedLtc - amount);
      } else if (asset === 'XRP') {
        updatedXrp = Math.max(0, updatedXrp - amount);
      }

      return {
        ...prev,
        onChainBalanceSats: updatedBtc,
        lightningBalanceSats: updatedLn,
        ethereumBalanceEth: updatedEth,
        litecoinBalanceLtc: updatedLtc,
        rippleBalanceXrp: updatedXrp,
        transactions: [newTx, ...prev.transactions],
        hsmStatus: HardwareSecurityModule.getHsmStatus(),
      };
    });

    return newTx;
  };

  const generateLightningInvoice = (amountSats: number, memo: string): string => {
    const randomHex = Array.from(crypto.getRandomValues(new Uint8Array(24)))
      .map((b) => b.toString(16).padStart(2, '0'))
      .join('');
    const sanitized = sanitizeMemo(memo) ? '1p' + randomHex.slice(0, 8) : '1pnq';
    return `lnbc${amountSats}u${sanitized}9satoshi${randomHex}`;
  };

  const createCloudBackup = async (passphrase: string) => {
    const backupData = JSON.stringify({
      mnemonic: state.mnemonicWords,
      onChainBalance: state.onChainBalanceSats,
      lightningBalance: state.lightningBalanceSats,
      timestamp: Date.now(),
      channels: state.lightningChannels,
    });

    const encrypted = await encryptWithPassword(backupData, passphrase);

    setState((prev) => ({
      ...prev,
      cloudBackup: {
        lastBackupTimestamp: Date.now(),
        backupCipher: 'AES-256-GCM',
        kdfRounds: 100_000,
        cloudLocation: 'Zero-Knowledge Encrypted Secure Vault',
        cloudChecksumSha256: encrypted.sha256Checksum,
        isCloudSyncActive: true,
        backupCount: prev.cloudBackup.backupCount + 1,
        backedUpAssets: ['BTC', 'LN', 'ETH', 'LTC', 'XRP'],
      },
    }));
  };

  const restoreCloudBackup = async (passphrase: string): Promise<boolean> => {
    if (passphrase.length < 8) return false;
    // Simulate cloud decrypt & restore
    await new Promise((r) => setTimeout(r, 600));
    return true;
  };

  const toggleSecurityShield = (key: keyof SecuritySettings) => {
    setState((prev) => {
      const updatedSettings = {
        ...prev.securitySettings,
        [key]: !prev.securitySettings[key],
      };
      const updatedAudit = VulnerabilityAuditEngine.runComprehensive1000Audit(
        updatedSettings.isFlagSecureEnabled,
        updatedSettings.isBiometricPinEnabled,
        updatedSettings.isAutoLockEnabled,
        true,
        prev.auditSummary?.isAutoRemediated ?? false
      );
      return {
        ...prev,
        securitySettings: updatedSettings,
        auditSummary: updatedAudit,
      };
    });
  };

  const autoHardenAll1000 = () => {
    setState((prev) => {
      const hardenedSettings: SecuritySettings = {
        ...prev.securitySettings,
        isFlagSecureEnabled: true,
        isBiometricPinEnabled: true,
        isAutoLockEnabled: true,
        isClipboardAutoClearEnabled: true,
        isStrictBech32ValidationEnabled: true,
        isHtlcWatchdogEnabled: true,
        isHardwareKeystoreActive: true,
        isMfaEnabled: true,
        requireMfaForSend: true,
      };
      const audit = VulnerabilityAuditEngine.runComprehensive1000Audit(
        true,
        true,
        true,
        true,
        true
      );
      return {
        ...prev,
        securitySettings: hardenedSettings,
        auditSummary: audit,
      };
    });
  };

  const runAudit = () => {
    setState((prev) => ({
      ...prev,
      auditSummary: VulnerabilityAuditEngine.runComprehensive1000Audit(
        prev.securitySettings.isFlagSecureEnabled,
        prev.securitySettings.isBiometricPinEnabled,
        prev.securitySettings.isAutoLockEnabled,
        true,
        prev.auditSummary?.isAutoRemediated ?? false
      ),
    }));
  };

  const testHsmSignature = async (): Promise<string> => {
    const res = await HardwareSecurityModule.signTransactionPayload('TEST_ATTESTATION_MESSAGE');
    setState((prev) => ({
      ...prev,
      hsmStatus: HardwareSecurityModule.getHsmStatus(),
    }));
    return res.signatureHex;
  };

  const testBiometrics = async (): Promise<{ success: boolean; message: string }> => {
    return HardwareSecurityModule.promptBiometricAuth('Donanım Biyometrik Testi');
  };

  const verifyMfa = async (code: string): Promise<boolean> => {
    return verifyTotpCode(state.securitySettings.totpSecret, code);
  };

  return (
    <WalletContext.Provider
      value={{
        walletState: state,
        activeTab,
        setActiveTab,
        isUnlocked,
        unlockWallet,
        unlockWithBiometric,
        lockWallet,
        toggleBalancePrivacy,
        selectFiat,
        selectAsset,
        executeSend,
        generateLightningInvoice,
        createCloudBackup,
        restoreCloudBackup,
        toggleSecurityShield,
        autoHardenAll1000,
        runAudit,
        testHsmSignature,
        testBiometrics,
        verifyMfa,
      }}
    >
      {children}
    </WalletContext.Provider>
  );
};

export const useWallet = () => {
  const ctx = useContext(WalletContext);
  if (!ctx) throw new Error('useWallet must be used within a WalletProvider');
  return ctx;
};
