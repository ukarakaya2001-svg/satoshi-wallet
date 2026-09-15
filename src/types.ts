export type NetworkType =
  | 'BITCOIN_ONCHAIN'
  | 'LIGHTNING_NETWORK'
  | 'ETHEREUM'
  | 'LITECOIN'
  | 'RIPPLE_XRP';

export interface NetworkConfig {
  id: NetworkType;
  title: string;
  speed: string;
  badgeColorHex: string;
}

export type CryptoAsset = 'BTC' | 'LIGHTNING' | 'ETH' | 'LTC' | 'XRP';

export interface CryptoAssetConfig {
  symbol: string;
  displayName: string;
  networkType: NetworkType;
  iconColorHex: string;
  defaultDerivationPath: string;
  unitName: string;
  decimals: number;
  basePriceUsd: number;
}

export type FiatCurrency = 'USD' | 'EUR' | 'TRY' | 'GBP' | 'JPY' | 'CAD' | 'AUD' | 'CHF';

export interface FiatConfig {
  code: FiatCurrency;
  symbol: string;
  displayName: string;
  btcPriceInFiat: number;
  usdToFiatRate: number;
}

export type TransactionType = 'SEND' | 'RECEIVE';

export type TransactionStatus = 'CONFIRMED' | 'PENDING' | 'ROUTING';

export interface TransactionRecord {
  id: string;
  txHash: string;
  network: NetworkType;
  type: TransactionType;
  amountSatoshis: number;
  feeSatoshis: number;
  recipientOrSender: string;
  timestamp: number;
  memo: string;
  status: TransactionStatus;
  asset: CryptoAsset;
  customAmountText?: string;
  customFeeText?: string;
}

export interface LightningChannel {
  channelId: string;
  remoteNodeAlias: string;
  localCapacitySats: number;
  remoteCapacitySats: number;
  status: string;
  htlcCount: number;
}

export interface CloudBackupInfo {
  lastBackupTimestamp: number | null;
  backupCipher: string;
  kdfRounds: number;
  cloudLocation: string;
  cloudChecksumSha256: string | null;
  isCloudSyncActive: boolean;
  backupCount: number;
  backedUpAssets: string[];
}

export interface SecuritySettings {
  isFlagSecureEnabled: boolean;
  isBiometricPinEnabled: boolean;
  isAutoLockEnabled: boolean;
  isClipboardAutoClearEnabled: boolean;
  isStrictBech32ValidationEnabled: boolean;
  isHtlcWatchdogEnabled: boolean;
  isHardwareKeystoreActive: boolean;
  isMfaEnabled: boolean;
  requireMfaForSend: boolean;
  totpSecret: string;
}

export type SecurityDomain =
  | 'BITCOIN_STANDARDS'
  | 'LIGHTNING_BOLT'
  | 'ANDROID_HARDWARE'
  | 'ADVANCED_CRYPTO'
  | 'SOFTWARE_NETWORK';

export type Severity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export interface AuditVector {
  id: number;
  domain: SecurityDomain;
  vectorName: string;
  standardRef: string;
  isClosed: boolean;
  remediation: string;
  severity: Severity;
}

export interface AuditSummary {
  totalChecked: number;
  totalPassed: number;
  totalVulnerabilitiesClosed: number;
  securityGrade: string;
  hardeningPercentage: number;
  criticalDefensesActive: number;
  vectorsByDomain: Record<SecurityDomain, AuditVector[]>;
  isAutoRemediated: boolean;
  remediationTimestamp: number | null;
}

export interface HsmStatus {
  isHsmActive: boolean;
  isStrongBoxBacked: boolean;
  securityLevel: string;
  keyAlgorithm: string;
  hardwareChipModel: string;
  attestationCertification: string;
  totalSignaturesGenerated: number;
  tamperResistanceGrade: string;
}

export interface EncryptedPayload {
  algorithm: string;
  kdf: string;
  iterations: number;
  saltBase64: string;
  ivBase64: string;
  ciphertextBase64: string;
  sha256Checksum: string;
}

export interface WalletState {
  isInitialized: boolean;
  isLocked: boolean;
  onChainBalanceSats: number;
  lightningBalanceSats: number;
  ethereumBalanceEth: number;
  litecoinBalanceLtc: number;
  rippleBalanceXrp: number;
  onChainAddress: string;
  taprootAddress: string;
  lightningNodeId: string;
  ethereumAddress: string;
  litecoinAddress: string;
  rippleAddress: string;
  rippleDestinationTag: number;
  selectedAsset: CryptoAsset;
  activeFiatCurrency: FiatCurrency;
  isBalanceHidden: boolean;
  mnemonicWords: string[];
  transactions: TransactionRecord[];
  lightningChannels: LightningChannel[];
  cloudBackup: CloudBackupInfo;
  securitySettings: SecuritySettings;
  auditSummary: AuditSummary | null;
  hsmStatus: HsmStatus | null;
}

export type WalletNavTab =
  | 'DASHBOARD'
  | 'SEND'
  | 'RECEIVE'
  | 'LIGHTNING'
  | 'BACKUP'
  | 'SECURITY';
