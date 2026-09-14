package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoAsset
import com.example.data.model.CurrencyFormatter
import com.example.data.model.NetworkType
import com.example.data.model.WalletState
import com.example.ui.SendUiState
import com.example.ui.components.CryptoAssetSelectorBar
import com.example.ui.components.NetworkBadge
import com.example.ui.theme.BitcoinGold
import com.example.ui.theme.LightningCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess

@Composable
fun SendScreen(
  walletState: WalletState,
  sendState: SendUiState,
  onRecipientChanged: (String) -> Unit,
  onAmountChanged: (String) -> Unit,
  onMemoChanged: (String) -> Unit,
  onNetworkChanged: (NetworkType) -> Unit,
  onFeeRateChanged: (Int) -> Unit,
  onExecuteSend: () -> Unit,
  onBack: () -> Unit,
  onAssetChanged: (CryptoAsset) -> Unit = {},
  onMfaCodeChanged: (String) -> Unit = {},
  onRequestSendConfirmation: () -> Unit = onExecuteSend,
  onConfirmMfaAndSend: () -> Unit = onExecuteSend,
  onBiometricAuthorize: () -> Unit = {},
  onDismissMfaPrompt: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val currentAsset = sendState.selectedAsset
  val assetColor = Color(currentAsset.iconColorHex)
  val balanceText = walletState.getAssetBalanceText(currentAsset)
  val balanceFiat = walletState.getAssetFiatValue(currentAsset, walletState.activeFiatCurrency)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBg)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp)
      .padding(top = 16.dp, bottom = 96.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .testTag("send_back_button")
          .clip(CircleShape)
          .background(ObsidianSurfaceVariant)
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = Color.White
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = "Send Crypto Payment",
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Protected with Hardware Security Module (HSM) & MFA",
          color = Color.Gray,
          fontSize = 11.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Multi-Asset Selector Bar
    Text(
      text = "SELECT ASSET TO SEND",
      color = Color.Gray,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    CryptoAssetSelectorBar(
      selectedAsset = currentAsset,
      onAssetSelected = onAssetChanged
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Available Balance Info Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
      border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
        listOf(assetColor.copy(alpha = 0.5f), ObsidianCardBorder)
      ))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Available Balance", color = Color.Gray, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = balanceText,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "≈ $balanceFiat",
            color = assetColor,
            fontSize = 12.sp
          )
        }
        NetworkBadge(network = currentAsset.networkType)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Recipient Address Input
    Text(
      text = "RECIPIENT ADDRESS / INVOICE",
      color = Color.Gray,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = sendState.recipient,
      onValueChange = onRecipientChanged,
      placeholder = {
        val hint = when (currentAsset) {
          CryptoAsset.BTC -> "Paste bc1q, bc1p, or 1/3 address..."
          CryptoAsset.LIGHTNING -> "Paste Lightning BOLT-11 invoice (lnbc...)"
          CryptoAsset.ETH -> "Paste Ethereum address (0x...)"
          CryptoAsset.LTC -> "Paste Litecoin address (ltc1...)"
          CryptoAsset.XRP -> "Paste Ripple XRP address (r...)"
        }
        Text(hint, color = Color.DarkGray, fontSize = 13.sp)
      },
      shape = RoundedCornerShape(14.dp),
      trailingIcon = {
        IconButton(onClick = {
          // Paste sample valid address for user convenience
          val sampleAddr = when (currentAsset) {
            CryptoAsset.BTC -> "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq"
            CryptoAsset.LIGHTNING -> "lnbc3500u1pnq9sampletestinvoicewithvalidprefix8947261908472918"
            CryptoAsset.ETH -> "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045"
            CryptoAsset.LTC -> "ltc1q98k7u7kvy5l643lydnw9re59gtzzwf5m42hsw"
            CryptoAsset.XRP -> "rEb8TK3gBgk5auZyyb6BiCc283VitJheP"
          }
          onRecipientChanged(sampleAddr)
        }) {
          Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan / Sample", tint = BitcoinGold)
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("send_recipient_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = assetColor,
        unfocusedBorderColor = ObsidianCardBorder,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        unfocusedContainerColor = ObsidianSurface,
        focusedContainerColor = ObsidianSurface
      )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Amount Input
    Text(
      text = "AMOUNT (${currentAsset.symbol})",
      color = Color.Gray,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = sendState.amountInput,
      onValueChange = onAmountChanged,
      placeholder = {
        val hint = when (currentAsset) {
          CryptoAsset.BTC, CryptoAsset.LIGHTNING -> "e.g. 50000"
          CryptoAsset.ETH -> "e.g. 0.15"
          CryptoAsset.LTC -> "e.g. 2.5"
          CryptoAsset.XRP -> "e.g. 100.0"
        }
        Text(hint, color = Color.DarkGray, fontSize = 14.sp)
      },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      shape = RoundedCornerShape(14.dp),
      trailingIcon = {
        TextButton(onClick = {
          val maxStr = when (currentAsset) {
            CryptoAsset.BTC -> "500000"
            CryptoAsset.LIGHTNING -> "250000"
            CryptoAsset.ETH -> "0.50"
            CryptoAsset.LTC -> "10.0"
            CryptoAsset.XRP -> "200.0"
          }
          onAmountChanged(maxStr)
        }) {
          Text("MAX", color = BitcoinGold, fontWeight = FontWeight.Bold)
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("send_amount_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = assetColor,
        unfocusedBorderColor = ObsidianCardBorder,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        unfocusedContainerColor = ObsidianSurface,
        focusedContainerColor = ObsidianSurface
      )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Memo Input
    Text(
      text = "TRANSACTION MEMO / NOTE (OPTIONAL)",
      color = Color.Gray,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = sendState.memo,
      onValueChange = onMemoChanged,
      placeholder = { Text("Encrypted memo stored in local vault...", color = Color.DarkGray, fontSize = 13.sp) },
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("send_memo_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = assetColor,
        unfocusedBorderColor = ObsidianCardBorder,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        unfocusedContainerColor = ObsidianSurface,
        focusedContainerColor = ObsidianSurface
      )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Hardware Security & MFA Notice Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceVariant)
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Memory,
          contentDescription = null,
          tint = StatusSuccess,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Hardware Security Module (HSM) Signing",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Private key never leaves the StrongBox tamper-resistant hardware. Requires MFA verification.",
            color = Color.Gray,
            fontSize = 11.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Error Feedback
    sendState.errorMessage?.let { error ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1919)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StatusError, ObsidianCardBorder)))
      ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusError, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = error, color = StatusError, fontSize = 13.sp)
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // Success Feedback
    sendState.successMessage?.let { success ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A1C)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StatusSuccess, ObsidianCardBorder)))
      ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = success, color = StatusSuccess, fontSize = 13.sp)
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // Send Button
    Button(
      onClick = onRequestSendConfirmation,
      enabled = !sendState.isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("send_submit_button"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = BitcoinGold,
        contentColor = Color.Black
      )
    ) {
      if (sendState.isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text("Signing with HSM Silicon...", fontWeight = FontWeight.Bold)
      } else {
        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Review & Send Payment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    }
  }

  // Multi-Factor Authentication (MFA) Verification Dialog
  if (sendState.isMfaPromptVisible) {
    AlertDialog(
      onDismissRequest = onDismissMfaPrompt,
      containerColor = ObsidianSurface,
      shape = RoundedCornerShape(20.dp),
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Shield, contentDescription = null, tint = BitcoinGold, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("MFA Authorization Required", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(
            text = "To authorize this ${currentAsset.displayName} transaction, enter your 6-digit Multi-Factor Authentication (TOTP) code or biometric confirmation.",
            color = Color.LightGray,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = sendState.mfaCodeInput,
            onValueChange = onMfaCodeChanged,
            placeholder = { Text("6-digit TOTP Code (e.g. 212121)", color = Color.Gray, fontSize = 13.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mfa_code_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BitcoinGold,
              unfocusedBorderColor = ObsidianCardBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          sendState.mfaError?.let { err ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = err, color = StatusError, fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Biometric 1-Tap Authorization Alternative
          Button(
            onClick = onBiometricAuthorize,
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("mfa_biometric_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.6f))
          ) {
            Icon(
              imageVector = Icons.Default.Fingerprint,
              contentDescription = null,
              tint = StatusSuccess,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Authorize with Biometrics (Fast)",
              color = StatusSuccess,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(ObsidianSurfaceVariant)
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Memory, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "HSM Keymaster: EC-secp256r1 Hardware Ready",
              color = StatusSuccess,
              fontSize = 11.sp
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = onConfirmMfaAndSend,
          colors = ButtonDefaults.buttonColors(containerColor = BitcoinGold, contentColor = Color.Black),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Sign & Broadcast", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = onDismissMfaPrompt) {
          Text("Cancel", color = Color.Gray)
        }
      }
    )
  }
}
