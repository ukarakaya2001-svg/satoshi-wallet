package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoAsset
import com.example.data.model.NetworkType
import com.example.data.model.WalletState
import com.example.ui.ReceiveUiState
import com.example.ui.components.CryptoAssetSelectorBar
import com.example.ui.components.CryptoQrCanvas
import com.example.ui.components.NetworkBadge
import com.example.ui.theme.BitcoinGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.StatusSuccess

@Composable
fun ReceiveScreen(
  walletState: WalletState,
  receiveState: ReceiveUiState,
  onNetworkChanged: (NetworkType) -> Unit,
  onAmountChanged: (String) -> Unit,
  onMemoChanged: (String) -> Unit,
  onMarkCopied: () -> Unit,
  onBack: () -> Unit,
  onAssetChanged: (CryptoAsset) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentAsset = receiveState.selectedAsset
  val assetColor = Color(currentAsset.iconColorHex)
  var selectedBtcType by remember { mutableStateOf("SegWit") }

  val activeAddressOrInvoice = when (currentAsset) {
    CryptoAsset.BTC -> if (selectedBtcType == "SegWit") walletState.onChainAddress else walletState.taprootAddress
    CryptoAsset.LIGHTNING -> receiveState.generatedInvoice.ifBlank { "lnbc500u1pnq9satoshi..." }
    CryptoAsset.ETH -> walletState.ethereumAddress
    CryptoAsset.LTC -> walletState.litecoinAddress
    CryptoAsset.XRP -> walletState.rippleAddress
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBg)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp)
      .padding(top = 16.dp, bottom = 96.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .testTag("receive_back_button")
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
          text = "Kripto Ödeme Al",
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Donanım Korumalı Adresler • Anında QR Kod",
          color = Color.Gray,
          fontSize = 11.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Multi-Asset Selector Bar
    Text(
      text = "ALMAK İSTEDİĞİNİZ VARLIĞI SEÇİN",
      color = Color.Gray,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 1.sp,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    CryptoAssetSelectorBar(
      selectedAsset = currentAsset,
      onAssetSelected = onAssetChanged
    )

    Spacer(modifier = Modifier.height(16.dp))

    // QR Code Container with Canvas Finder Patterns
    Card(
      modifier = Modifier
        .testTag("receive_qr_card")
        .size(240.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CryptoQrCanvas(
          data = activeAddressOrInvoice,
          asset = currentAsset,
          modifier = Modifier.size(220.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Network & Protocol Badge
    NetworkBadge(network = currentAsset.networkType)

    Spacer(modifier = Modifier.height(14.dp))

    // Address Display Card with 1-Tap Copy
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable {
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          val clip = ClipData.newPlainText("Crypto Address", activeAddressOrInvoice)
          clipboard.setPrimaryClip(clip)
          onMarkCopied()
        }
        .testTag("receive_address_copy_card"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(
        listOf(assetColor.copy(alpha = 0.4f), ObsidianCardBorder)
      ))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = if (currentAsset == CryptoAsset.LIGHTNING) "LIGHTNING BOLT-11 INVOICE" else "${currentAsset.displayName.uppercase()} RECEIVE ADDRESS",
          color = Color.Gray,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = activeAddressOrInvoice,
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.Center,
          lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (receiveState.isCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
            contentDescription = null,
            tint = if (receiveState.isCopied) StatusSuccess else BitcoinGold,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (receiveState.isCopied) "Panoya Kopyalandı! ✓" else "Adresi Kopyalamak İçin Dokunun",
            color = if (receiveState.isCopied) StatusSuccess else BitcoinGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Ripple Destination Tag notice if XRP selected
    if (currentAsset == CryptoAsset.XRP) {
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceVariant)
      ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Destination Tag: ${walletState.rippleDestinationTag} (Required for central exchanges)",
            color = Color(0xFF00AAE4),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Amount & Memo (optional)
    OutlinedTextField(
      value = receiveState.amountInput,
      onValueChange = onAmountChanged,
      label = { Text("Specify Amount (${currentAsset.symbol}) - Optional", color = Color.Gray) },
      placeholder = { Text("e.g. 0.05", color = Color.DarkGray) },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      shape = RoundedCornerShape(14.dp),
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("receive_amount_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = assetColor,
        unfocusedBorderColor = ObsidianCardBorder,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        unfocusedContainerColor = ObsidianSurface,
        focusedContainerColor = ObsidianSurface
      )
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = receiveState.memo,
      onValueChange = onMemoChanged,
      label = { Text("Payment Description / Memo - Optional", color = Color.Gray) },
      placeholder = { Text("e.g. Invoice #2140", color = Color.DarkGray) },
      shape = RoundedCornerShape(14.dp),
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("receive_memo_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = assetColor,
        unfocusedBorderColor = ObsidianCardBorder,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        unfocusedContainerColor = ObsidianSurface,
        focusedContainerColor = ObsidianSurface
      )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Security Assurance Note
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceVariant)
    ) {
      Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Security, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Address derived from BIP-44/84/86 master key secured in Android StrongBox Keymaster. Clipboard auto-clears after 45 seconds.",
          color = Color.Gray,
          fontSize = 11.sp
        )
      }
    }
  }
}
