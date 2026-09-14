package com.example.data.model

import com.example.data.crypto.HardwareSecurityModule
import com.example.data.crypto.VulnerabilityAuditEngine

enum class NetworkType(val title: String, val speed: String, val badgeColorHex: Long) {
  BITCOIN_ONCHAIN("Bitcoin SegWit/Taproot", "10-60 mins (On-Chain)", 0xFFF7931A),
  LIGHTNING_NETWORK("Lightning Network", "Instant (<1 sec)", 0xFF00E5FF),
  ETHEREUM("Ethereum (ERC-20)", "12-60 secs (EVM)", 0xFF627EEA),
  LITECOIN("Litecoin (Scrypt)", "2.5 mins (Fast UTXO)", 0xFF345D9D),
  RIPPLE_XRP("XRP Ledger (XRPL)", "3-5 secs (Consensus)", 0xFF00AAE4)
}

enum class TransactionType {
  SEND, RECEIVE
}

enum class TransactionStatus(val label: String) {
  CONFIRMED("Confirmed"),
  PENDING("Confirming..."),
  ROUTING("Routing HTLC")
}

data class TransactionRecord(
  val id: String,
  val txHash: String,
  val network: NetworkType,
  val type: TransactionType,
  val amountSatoshis: Long, // Kept for BTC / backward-compatibility
  val feeSatoshis: Long,
  val recipientOrSender: String,
  val timestamp: Long,
  val memo: String,
  val status: TransactionStatus,
  val asset: CryptoAsset = CryptoAsset.BTC,
  val customAmountText: String? = null,
  val customFeeText: String? = null
) {
  fun displayAmount(fiat: FiatCurrency): String {
    return customAmountText ?: CurrencyFormatter.formatSats(amountSatoshis)
  }

  fun displayFiat(fiat: FiatCurrency): String {
    val usdVal = when (asset) {
      CryptoAsset.BTC, CryptoAsset.LIGHTNING -> (amountSatoshis.toDouble() / 100_000_000.0) * CryptoAsset.BTC.basePriceUsd
      CryptoAsset.ETH -> (customAmountText?.replace(" ETH", "")?.toDoubleOrNull() ?: 0.0) * CryptoAsset.ETH.basePriceUsd
      CryptoAsset.LTC -> (customAmountText?.replace(" LTC", "")?.toDoubleOrNull() ?: 0.0) * CryptoAsset.LTC.basePriceUsd
      CryptoAsset.XRP -> (customAmountText?.replace(" XRP", "")?.toDoubleOrNull() ?: 0.0) * CryptoAsset.XRP.basePriceUsd
    }
    return CurrencyFormatter.formatFiatValue(usdVal, fiat)
  }
}

data class LightningChannel(
  val channelId: String,
  val remoteNodeAlias: String,
  val localCapacitySats: Long,
  val remoteCapacitySats: Long,
  val status: String,
  val htlcCount: Int
)

data class CloudBackupInfo(
  val lastBackupTimestamp: Long?,
  val backupCipher: String = "AES-256-GCM",
  val kdfRounds: Int = 100_000,
  val cloudLocation: String = "Encrypted Vault (Cloud Sync)",
  val cloudChecksumSha256: String?,
  val isCloudSyncActive: Boolean = true,
  val backupCount: Int = 0,
  val backedUpAssets: List<String> = listOf("BTC", "LN", "ETH", "LTC", "XRP")
)

data class SecuritySettings(
  val isFlagSecureEnabled: Boolean = true,
  val isBiometricPinEnabled: Boolean = true,
  val isAutoLockEnabled: Boolean = true,
  val isClipboardAutoClearEnabled: Boolean = true,
  val isStrictBech32ValidationEnabled: Boolean = true,
  val isHtlcWatchdogEnabled: Boolean = true,
  val isHardwareKeystoreActive: Boolean = true,
  val isMfaEnabled: Boolean = true,
  val requireMfaForSend: Boolean = true,
  val totpSecret: String = "JBSWY3DPEHPK3PXP"
)

data class WalletState(
  val isInitialized: Boolean = false,
  val isLocked: Boolean = false,
  // Bitcoin & Lightning Balances
  val onChainBalanceSats: Long = 1_850_000L, // ~0.0185 BTC
  val lightningBalanceSats: Long = 650_000L,  // ~650,000 sats
  // Altcoin Balances
  val ethereumBalanceEth: Double = 1.45,       // 1.45 ETH
  val litecoinBalanceLtc: Double = 42.50,      // 42.50 LTC
  val rippleBalanceXrp: Double = 1_250.00,     // 1,250 XRP
  // Addresses & Derivations
  val onChainAddress: String = "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
  val taprootAddress: String = "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0",
  val lightningNodeId: String = "03a1098b1b22e7561f6874e0d4fc88d3eef7372d8a9eef16ccda56bf705c4bbd5e",
  val ethereumAddress: String = "0x71C845137c37D21F45942730C7550B94E347A221",
  val litecoinAddress: String = "ltc1q98k7u7kvy5l643lydnw9re59gtzzwf5m42hsw",
  val rippleAddress: String = "rEb8TK3gBgk5auZyyb6BiCc283VitJheP",
  val rippleDestinationTag: Long = 1048576L,
  // Current Selected Active Asset Filter
  val selectedAsset: CryptoAsset = CryptoAsset.BTC,
  val activeFiatCurrency: FiatCurrency = FiatCurrency.USD,
  val isBalanceHidden: Boolean = false,
  val mnemonicWords: List<String> = emptyList(),
  val transactions: List<TransactionRecord> = emptyList(),
  val lightningChannels: List<LightningChannel> = emptyList(),
  val cloudBackup: CloudBackupInfo = CloudBackupInfo(
    lastBackupTimestamp = System.currentTimeMillis() - 3600_000L * 4,
    cloudChecksumSha256 = "8f4c2b9a7813a17e0e7a2b0c39df4a16b9d62884a26189e36511a56ec94b3017",
    isCloudSyncActive = true,
    backupCount = 3
  ),
  val securitySettings: SecuritySettings = SecuritySettings(),
  val auditSummary: VulnerabilityAuditEngine.AuditSummary? = null,
  val hsmStatus: HardwareSecurityModule.HsmStatus? = null
) {
  val totalBalanceSats: Long get() = onChainBalanceSats + lightningBalanceSats

  /**
   * Total value of all crypto holdings calculated in USD then converted to active fiat.
   */
  fun calculateTotalPortfolioFiatValue(fiat: FiatCurrency): Double {
    val btcUsd = (totalBalanceSats.toDouble() / 100_000_000.0) * CryptoAsset.BTC.basePriceUsd
    val ethUsd = ethereumBalanceEth * CryptoAsset.ETH.basePriceUsd
    val ltcUsd = litecoinBalanceLtc * CryptoAsset.LTC.basePriceUsd
    val xrpUsd = rippleBalanceXrp * CryptoAsset.XRP.basePriceUsd
    val totalUsd = btcUsd + ethUsd + ltcUsd + xrpUsd
    return totalUsd * fiat.usdToFiatRate
  }

  fun getAssetBalanceText(asset: CryptoAsset): String {
    return when (asset) {
      CryptoAsset.BTC -> CurrencyFormatter.formatSats(onChainBalanceSats)
      CryptoAsset.LIGHTNING -> CurrencyFormatter.formatSats(lightningBalanceSats)
      CryptoAsset.ETH -> String.format(java.util.Locale.US, "%.4f ETH", ethereumBalanceEth)
      CryptoAsset.LTC -> String.format(java.util.Locale.US, "%.4f LTC", litecoinBalanceLtc)
      CryptoAsset.XRP -> String.format(java.util.Locale.US, "%,.2f XRP", rippleBalanceXrp)
    }
  }

  fun getAssetFiatValue(asset: CryptoAsset, fiat: FiatCurrency): String {
    val usdVal = when (asset) {
      CryptoAsset.BTC -> (onChainBalanceSats.toDouble() / 100_000_000.0) * CryptoAsset.BTC.basePriceUsd
      CryptoAsset.LIGHTNING -> (lightningBalanceSats.toDouble() / 100_000_000.0) * CryptoAsset.LIGHTNING.basePriceUsd
      CryptoAsset.ETH -> ethereumBalanceEth * CryptoAsset.ETH.basePriceUsd
      CryptoAsset.LTC -> litecoinBalanceLtc * CryptoAsset.LTC.basePriceUsd
      CryptoAsset.XRP -> rippleBalanceXrp * CryptoAsset.XRP.basePriceUsd
    }
    return CurrencyFormatter.formatFiatValue(usdVal, fiat)
  }

  fun getAddressForAsset(asset: CryptoAsset): String {
    return when (asset) {
      CryptoAsset.BTC -> onChainAddress
      CryptoAsset.LIGHTNING -> lightningNodeId
      CryptoAsset.ETH -> ethereumAddress
      CryptoAsset.LTC -> litecoinAddress
      CryptoAsset.XRP -> rippleAddress
    }
  }
}
