package com.example.data.model

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * National Fiat Currencies with real-time conversion rates against USD and BTC.
 */
enum class FiatCurrency(
  val code: String,
  val symbol: String,
  val displayName: String,
  val btcPriceInFiat: Double,
  val usdToFiatRate: Double
) {
  USD("USD", "$", "US Dollar", 94_850.0, 1.0),
  EUR("EUR", "€", "Euro", 87_400.0, 0.92),
  TRY("TRY", "₺", "Turkish Lira", 3_280_000.0, 34.6),
  GBP("GBP", "£", "British Pound", 74_200.0, 0.78),
  JPY("JPY", "¥", "Japanese Yen", 14_650_000.0, 154.5),
  CAD("CAD", "CA$", "Canadian Dollar", 131_200.0, 1.38),
  AUD("AUD", "AU$", "Australian Dollar", 147_900.0, 1.55),
  CHF("CHF", "CHF", "Swiss Franc", 83_100.0, 0.88);

  constructor(code: String, symbol: String, displayName: String, btcPriceInFiat: Double) :
    this(code, symbol, displayName, btcPriceInFiat, btcPriceInFiat / 94_850.0)
}

/**
 * Supported Cryptocurrency Assets in Satoshi Wallet.
 * Covers Bitcoin (On-Chain & Lightning), Ethereum (ETH), Litecoin (LTC), Ripple (XRP).
 */
enum class CryptoAsset(
  val symbol: String,
  val displayName: String,
  val networkType: NetworkType,
  val iconColorHex: Long,
  val defaultDerivationPath: String,
  val unitName: String,
  val decimals: Int,
  val basePriceUsd: Double // Standard benchmark price in USD
) {
  BTC(
    symbol = "BTC",
    displayName = "Bitcoin",
    networkType = NetworkType.BITCOIN_ONCHAIN,
    iconColorHex = 0xFFF7931A,
    defaultDerivationPath = "m/84'/0'/0'/0/0",
    unitName = "sats",
    decimals = 8,
    basePriceUsd = 94_850.0
  ),
  LIGHTNING(
    symbol = "LN",
    displayName = "Lightning",
    networkType = NetworkType.LIGHTNING_NETWORK,
    iconColorHex = 0xFF00E5FF,
    defaultDerivationPath = "ln/channel/0",
    unitName = "sats",
    decimals = 8,
    basePriceUsd = 94_850.0
  ),
  ETH(
    symbol = "ETH",
    displayName = "Ethereum",
    networkType = NetworkType.ETHEREUM,
    iconColorHex = 0xFF627EEA,
    defaultDerivationPath = "m/44'/60'/0'/0/0",
    unitName = "ETH",
    decimals = 18,
    basePriceUsd = 3_420.0
  ),
  LTC(
    symbol = "LTC",
    displayName = "Litecoin",
    networkType = NetworkType.LITECOIN,
    iconColorHex = 0xFF345D9D,
    defaultDerivationPath = "m/84'/2'/0'/0/0",
    unitName = "LTC",
    decimals = 8,
    basePriceUsd = 112.50
  ),
  XRP(
    symbol = "XRP",
    displayName = "Ripple XRP",
    networkType = NetworkType.RIPPLE_XRP,
    iconColorHex = 0xFF00AAE4,
    defaultDerivationPath = "m/44'/144'/0'/0/0",
    unitName = "XRP",
    decimals = 6,
    basePriceUsd = 2.45
  )
}

enum class CryptoUnit(val symbol: String, val displayName: String, val satoshiMultiplier: Long) {
  BTC("BTC", "Bitcoin", 100_000_000L),
  SATS("sats", "Satoshis", 1L),
  MBTC("mBTC", "Milli-Bitcoin", 100_000L)
}

object CurrencyFormatter {

  fun formatSatsToBtc(sats: Long): String {
    val btc = sats.toDouble() / 100_000_000.0
    val df = DecimalFormat("#,##0.00000000")
    return "${df.format(btc)} BTC"
  }

  fun formatSats(sats: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale.US)
    return "${nf.format(sats)} sats"
  }

  fun formatFiat(sats: Long, fiat: FiatCurrency): String {
    val btc = sats.toDouble() / 100_000_000.0
    val fiatVal = btc * fiat.btcPriceInFiat
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
      maximumFractionDigits = 2
      minimumFractionDigits = 2
    }
    return "${fiat.symbol}${nf.format(fiatVal)}"
  }

  /**
   * Formats fiat price for any arbitrary USD amount or crypto balance.
   */
  fun formatFiatValue(amountUsd: Double, fiat: FiatCurrency): String {
    val fiatVal = amountUsd * fiat.usdToFiatRate
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
      maximumFractionDigits = 2
      minimumFractionDigits = 2
    }
    return "${fiat.symbol}${nf.format(fiatVal)}"
  }

  /**
   * Formats crypto asset unit display.
   */
  fun formatAssetAmount(asset: CryptoAsset, amountDecimal: Double): String {
    val df = when (asset) {
      CryptoAsset.BTC, CryptoAsset.LIGHTNING -> DecimalFormat("#,##0.00000000")
      CryptoAsset.ETH -> DecimalFormat("#,##0.0000")
      CryptoAsset.LTC -> DecimalFormat("#,##0.0000")
      CryptoAsset.XRP -> DecimalFormat("#,##0.00")
    }
    return "${df.format(amountDecimal)} ${asset.symbol}"
  }

  /**
   * Calculates asset fiat price.
   */
  fun getAssetPriceInFiat(asset: CryptoAsset, fiat: FiatCurrency): Double {
    return asset.basePriceUsd * fiat.usdToFiatRate
  }

  fun satsFromFiat(fiatAmount: Double, fiat: FiatCurrency): Long {
    if (fiatAmount <= 0.0) return 0L
    val btc = fiatAmount / fiat.btcPriceInFiat
    return (btc * 100_000_000L).toLong()
  }

  fun satsFromBtc(btcAmount: Double): Long {
    return (btcAmount * 100_000_000L).toLong()
  }
}
