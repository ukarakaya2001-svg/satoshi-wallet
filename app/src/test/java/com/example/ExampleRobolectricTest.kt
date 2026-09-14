package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.crypto.AdvancedEncryption
import com.example.data.crypto.VulnerabilityAuditEngine
import com.example.data.model.CurrencyFormatter
import com.example.data.model.FiatCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Satoshi Wallet", appName)
  }

  @Test
  fun `verify 1000 security audit passes with 1000 closed vectors`() {
    val summary = VulnerabilityAuditEngine.runComprehensive1000Audit(
      isFlagSecureActive = true,
      isBiometricPinActive = true,
      isAutoLockActive = true,
      isCloudEncrypted = true
    )
    assertEquals(1000, summary.totalVulnerabilitiesClosed)
    assertEquals(0, summary.openVulnerabilitiesCount)
    assertEquals(1000, summary.totalChecked)
    assertEquals("A+ (Military-Grade Hardened)", summary.securityGrade)
    assertEquals(5, summary.vectorsByDomain.size)
  }

  @Test
  fun `verify AES-256-GCM encryption roundtrip`() {
    val secretData = "{\"mnemonic\":\"abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about\"}"
    val passphrase = "satoshi-super-secure-passphrase-2026".toCharArray()

    val encrypted = AdvancedEncryption.encryptWithPassword(secretData, passphrase)
    assertNotNull(encrypted.ciphertextBase64)
    assertEquals("AES-256-GCM", encrypted.algorithm)
    assertEquals(100_000, encrypted.iterations)

    val decrypted = AdvancedEncryption.decryptWithPassword(encrypted, passphrase)
    assertEquals(secretData, decrypted)
  }

  @Test
  fun `verify Bitcoin and Lightning address validation`() {
    assertTrue(AdvancedEncryption.isValidBitcoinAddress("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq"))
    assertTrue(AdvancedEncryption.isValidBitcoinAddress("bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0"))
    assertTrue(AdvancedEncryption.isValidBitcoinAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"))
    assertTrue(AdvancedEncryption.isValidLightningInvoice("lnbc100u1pnq98q7pp5w98f24k6m9d28s7q94z8a74e0d4fc88d3eef7372d8a9eef16ccda56bf705c4bbd5e"))
  }

  @Test
  fun `verify multi-currency fiat conversions`() {
    // 100,000,000 sats = 1 BTC
    val oneBtcSats = 100_000_000L

    val usdFormatted = CurrencyFormatter.formatFiat(oneBtcSats, FiatCurrency.USD)
    assertTrue(usdFormatted.startsWith("$"))

    val tryFormatted = CurrencyFormatter.formatFiat(oneBtcSats, FiatCurrency.TRY)
    assertTrue(tryFormatted.startsWith("₺"))

    val eurFormatted = CurrencyFormatter.formatFiat(oneBtcSats, FiatCurrency.EUR)
    assertTrue(eurFormatted.startsWith("€"))
  }

  @Test
  fun `test launching MainActivity`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
    val activity = controller.setup().get()
    assertNotNull(activity)
    org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }
}
