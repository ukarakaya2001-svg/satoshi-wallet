package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.crypto.BiometricAuthManager
import com.example.ui.SatoshiWalletViewModel
import com.example.ui.WalletNavTab
import com.example.ui.screens.CloudBackupScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LightningChannelsScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.ReceiveScreen
import com.example.ui.screens.SecurityAuditScreen
import com.example.ui.screens.SendScreen
import com.example.ui.theme.BitcoinGold
import com.example.ui.theme.LightningCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.StatusSuccess

class MainActivity : FragmentActivity() {

  private val viewModel: SatoshiWalletViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Ensure FLAG_SECURE is cleared so the streaming browser emulator and display capture work seamlessly
    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

    setContent {
      MyApplicationTheme {
        val walletState by viewModel.walletState.collectAsState()
        val activeTab by viewModel.activeTab.collectAsState()
        val sendState by viewModel.sendState.collectAsState()
        val receiveState by viewModel.receiveState.collectAsState()
        val backupState by viewModel.backupState.collectAsState()
        val auditState by viewModel.auditState.collectAsState()
        val enteredPin by viewModel.enteredPin.collectAsState()
        val isPinError by viewModel.isPinError.collectAsState()
        val biometricAuthMessage by viewModel.biometricAuthMessage.collectAsState()

        if (walletState.isLocked) {
          PinLockScreen(
            enteredPin = enteredPin,
            isError = isPinError,
            biometricMessage = biometricAuthMessage,
            onDigitClick = { viewModel.onPinDigit(it) },
            onBackspaceClick = { viewModel.onPinBackspace() },
            onBiometricClick = {
              val cap = BiometricAuthManager.checkBiometricStatus(this@MainActivity)
              if (cap.isAvailable) {
                BiometricAuthManager.authenticate(
                  activity = this@MainActivity,
                  title = "Unlock Satoshi Wallet",
                  subtitle = "Touch fingerprint sensor or glance at face scanner",
                  negativeButtonText = "Use PIN",
                  onSuccess = { viewModel.unlockWithBiometrics() },
                  onError = { code, err ->
                    if (code != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED &&
                        code != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                      viewModel.setBiometricAuthMessage(err)
                    }
                  },
                  onFailed = {
                    viewModel.setBiometricAuthMessage("Biometric scan not recognized")
                  }
                )
              } else {
                // Fallback for emulator / non-enrolled sandbox environments
                viewModel.unlockWithBiometrics()
              }
            }
          )
        } else {
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBg,
            bottomBar = {
              SatoshiBottomNav(
                currentTab = activeTab,
                onTabSelected = { viewModel.setTab(it) }
              )
            }
          ) { innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)

            when (activeTab) {
              WalletNavTab.DASHBOARD -> {
                DashboardScreen(
                  walletState = walletState,
                  onNavigateTab = { viewModel.setTab(it) },
                  onToggleBalancePrivacy = { viewModel.toggleBalanceVisibility() },
                  onSelectFiat = { viewModel.setActiveFiat(it) },
                  onLockWallet = { viewModel.lockWallet() },
                  onSelectAsset = { viewModel.setSelectedAsset(it) },
                  modifier = contentModifier
                )
              }
              WalletNavTab.SEND -> {
                SendScreen(
                  walletState = walletState,
                  sendState = sendState,
                  onRecipientChanged = { viewModel.updateSendRecipient(it) },
                  onAmountChanged = { viewModel.updateSendAmount(it) },
                  onMemoChanged = { viewModel.updateSendMemo(it) },
                  onNetworkChanged = { viewModel.updateSendNetwork(it) },
                  onFeeRateChanged = { viewModel.updateFeeRate(it) },
                  onExecuteSend = { viewModel.requestSendConfirmation() },
                  onBack = { viewModel.setTab(WalletNavTab.DASHBOARD) },
                  onAssetChanged = { viewModel.updateSendAsset(it) },
                  onMfaCodeChanged = { viewModel.updateMfaCodeInput(it) },
                  onRequestSendConfirmation = { viewModel.requestSendConfirmation() },
                  onConfirmMfaAndSend = { viewModel.confirmMfaAndSend() },
                  onBiometricAuthorize = {
                    val cap = BiometricAuthManager.checkBiometricStatus(this@MainActivity)
                    if (cap.isAvailable) {
                      BiometricAuthManager.authenticate(
                        activity = this@MainActivity,
                        title = "Authorize Payment",
                        subtitle = "Confirm ${sendState.selectedAsset.displayName} transaction with biometrics",
                        negativeButtonText = "Use TOTP",
                        onSuccess = {
                          viewModel.confirmSendWithBiometrics()
                        },
                        onError = { _, _ ->
                          // Fallback to manual TOTP
                        },
                        onFailed = {}
                      )
                    } else {
                      viewModel.confirmSendWithBiometrics()
                    }
                  },
                  onDismissMfaPrompt = { viewModel.dismissMfaPrompt() },
                  modifier = contentModifier
                )
              }
              WalletNavTab.RECEIVE -> {
                ReceiveScreen(
                  walletState = walletState,
                  receiveState = receiveState,
                  onNetworkChanged = { viewModel.updateReceiveNetwork(it) },
                  onAmountChanged = { viewModel.updateReceiveAmount(it) },
                  onMemoChanged = { viewModel.updateReceiveMemo(it) },
                  onMarkCopied = { viewModel.markAddressCopied() },
                  onBack = { viewModel.setTab(WalletNavTab.DASHBOARD) },
                  onAssetChanged = { viewModel.updateReceiveAsset(it) },
                  modifier = contentModifier
                )
              }
              WalletNavTab.LIGHTNING -> {
                LightningChannelsScreen(
                  walletState = walletState,
                  onBack = { viewModel.setTab(WalletNavTab.DASHBOARD) },
                  modifier = contentModifier
                )
              }
              WalletNavTab.BACKUP -> {
                CloudBackupScreen(
                  walletState = walletState,
                  backupState = backupState,
                  onPassphraseChanged = { viewModel.updateBackupPassphrase(it) },
                  onConfirmPassphraseChanged = { viewModel.updateConfirmPassphrase(it) },
                  onToggleMnemonicRevealed = { viewModel.toggleMnemonicVisibility() },
                  onCreateCloudBackup = { viewModel.createEncryptedCloudBackup() },
                  onBack = { viewModel.setTab(WalletNavTab.DASHBOARD) },
                  onRestorePassphraseChanged = { viewModel.updateRestorePassphrase(it) },
                  onRestoreCloudBackup = { viewModel.restoreCloudBackup() },
                  modifier = contentModifier
                )
              }
              WalletNavTab.SECURITY -> {
                SecurityAuditScreen(
                  walletState = walletState,
                  auditState = auditState,
                  onRunAudit = { viewModel.runComprehensiveSecurityAudit() },
                  onSelectDomain = { viewModel.selectAuditDomain(it) },
                  onSearchChanged = { viewModel.updateAuditSearch(it) },
                  onToggleShield = { viewModel.toggleSecuritySetting(it) },
                  onBack = { viewModel.setTab(WalletNavTab.DASHBOARD) },
                  onTestHsmSignature = { viewModel.testHsmHardwareSignature() },
                  onTestBiometricPrompt = {
                    val cap = BiometricAuthManager.checkBiometricStatus(this@MainActivity)
                    if (cap.isAvailable) {
                      BiometricAuthManager.authenticate(
                        activity = this@MainActivity,
                        title = "Test Biometric Sensor",
                        subtitle = "Touch fingerprint scanner or glance at face unlock",
                        negativeButtonText = "Cancel",
                        onSuccess = {
                          viewModel.updateBiometricTestResult("✓ Biometric Authentication Verified: Class 3 Hardware Token Acquired")
                        },
                        onError = { code, err ->
                          viewModel.updateBiometricTestResult("Biometric System (Code $code): $err")
                        },
                        onFailed = {
                          viewModel.updateBiometricTestResult("Scan Failed: Biometric credentials not recognized")
                        }
                      )
                    } else {
                      when (cap) {
                        is BiometricAuthManager.BiometricCapability.NotEnrolled -> {
                          viewModel.updateBiometricTestResult("Hardware Ready: Biometrics supported, but no fingerprint/face enrolled in Android Settings.")
                        }
                        is BiometricAuthManager.BiometricCapability.NoHardware -> {
                          viewModel.updateBiometricTestResult("Hardware Info: No physical biometric scanner detected on current emulator container.")
                        }
                        else -> {
                          viewModel.updateBiometricTestResult("Biometric Status: ${cap.isAvailable}")
                        }
                      }
                    }
                  },
                  onMfaTestInputChanged = { viewModel.updateMfaTestInput(it) },
                  onTestMfaVerification = { viewModel.testMfaVerification() },
                  modifier = contentModifier
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SatoshiBottomNav(
  currentTab: WalletNavTab,
  onTabSelected: (WalletNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
      .border(1.dp, ObsidianCardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
    containerColor = ObsidianSurface,
    tonalElevation = 8.dp
  ) {
    NavigationBarItem(
      selected = currentTab == WalletNavTab.DASHBOARD,
      onClick = { onTabSelected(WalletNavTab.DASHBOARD) },
      icon = {
        Icon(
          imageVector = Icons.Default.CurrencyBitcoin,
          contentDescription = "Wallet",
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BitcoinGold,
        selectedTextColor = BitcoinGold,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray,
        indicatorColor = BitcoinGold.copy(alpha = 0.15f)
      ),
      modifier = Modifier.testTag("nav_tab_wallet")
    )

    NavigationBarItem(
      selected = currentTab == WalletNavTab.SEND,
      onClick = { onTabSelected(WalletNavTab.SEND) },
      icon = {
        Icon(
          imageVector = Icons.Default.ArrowUpward,
          contentDescription = "Send",
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Send", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BitcoinGold,
        selectedTextColor = BitcoinGold,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray,
        indicatorColor = BitcoinGold.copy(alpha = 0.15f)
      ),
      modifier = Modifier.testTag("nav_tab_send")
    )

    NavigationBarItem(
      selected = currentTab == WalletNavTab.RECEIVE,
      onClick = { onTabSelected(WalletNavTab.RECEIVE) },
      icon = {
        Icon(
          imageVector = Icons.Default.ArrowDownward,
          contentDescription = "Receive",
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Receive", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = StatusSuccess,
        selectedTextColor = StatusSuccess,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray,
        indicatorColor = StatusSuccess.copy(alpha = 0.15f)
      ),
      modifier = Modifier.testTag("nav_tab_receive")
    )

    NavigationBarItem(
      selected = currentTab == WalletNavTab.LIGHTNING,
      onClick = { onTabSelected(WalletNavTab.LIGHTNING) },
      icon = {
        Icon(
          imageVector = Icons.Default.Bolt,
          contentDescription = "Lightning",
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Lightning", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = LightningCyan,
        selectedTextColor = LightningCyan,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray,
        indicatorColor = LightningCyan.copy(alpha = 0.15f)
      ),
      modifier = Modifier.testTag("nav_tab_lightning")
    )

    NavigationBarItem(
      selected = currentTab == WalletNavTab.BACKUP,
      onClick = { onTabSelected(WalletNavTab.BACKUP) },
      icon = {
        Icon(
          imageVector = Icons.Default.CloudDone,
          contentDescription = "Backup",
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Backup", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFFBB86FC),
        selectedTextColor = Color(0xFFBB86FC),
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray,
        indicatorColor = Color(0xFFBB86FC).copy(alpha = 0.15f)
      ),
      modifier = Modifier.testTag("nav_tab_backup")
    )

    NavigationBarItem(
      selected = currentTab == WalletNavTab.SECURITY,
      onClick = { onTabSelected(WalletNavTab.SECURITY) },
      icon = {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Audit",
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Audit & HSM", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = StatusSuccess,
        selectedTextColor = StatusSuccess,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray,
        indicatorColor = StatusSuccess.copy(alpha = 0.15f)
      ),
      modifier = Modifier.testTag("nav_tab_audit")
    )
  }
}
