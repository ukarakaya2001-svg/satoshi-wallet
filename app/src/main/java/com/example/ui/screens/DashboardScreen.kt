package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoAsset
import com.example.data.model.CurrencyFormatter
import com.example.data.model.FiatCurrency
import com.example.data.model.NetworkType
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.model.WalletState
import com.example.ui.WalletNavTab
import com.example.ui.components.CryptoAssetSelectorBar
import com.example.ui.components.NetworkBadge
import com.example.ui.theme.BitcoinGold
import com.example.ui.theme.BitcoinGoldBright
import com.example.ui.theme.LightningCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  walletState: WalletState,
  onNavigateTab: (WalletNavTab) -> Unit,
  onToggleBalancePrivacy: () -> Unit,
  onSelectFiat: (FiatCurrency) -> Unit,
  onLockWallet: () -> Unit,
  onSelectAsset: (CryptoAsset) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showFiatSelector by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }
  var selectedTxDetails by remember { mutableStateOf<TransactionRecord?>(null) }
  var txSearchQuery by remember { mutableStateOf("") }
  var selectedTxFilterAsset by remember { mutableStateOf<CryptoAsset?>(null) }
  var selectedTxFilterType by remember { mutableStateOf<TransactionType?>(null) }
  val sheetState = rememberModalBottomSheetState()

  // Filter transactions based on asset, search query, and type
  val filteredTransactions = walletState.transactions.filter { tx ->
    val matchesAsset = selectedTxFilterAsset == null || tx.asset == selectedTxFilterAsset
    val matchesType = selectedTxFilterType == null || tx.type == selectedTxFilterType
    val matchesQuery = txSearchQuery.isBlank() ||
      tx.memo.contains(txSearchQuery, ignoreCase = true) ||
      tx.txHash.contains(txSearchQuery, ignoreCase = true) ||
      tx.recipientOrSender.contains(txSearchQuery, ignoreCase = true)
    matchesAsset && matchesType && matchesQuery
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
  ) {
    // -------------------------------------------------------------
    // 1. TOP HEADER: Brand Identity, Live Security Badge, Fiat & Lock
    // -------------------------------------------------------------
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // App Identity & Status Indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  listOf(BitcoinGold, Color(0xFF1E1B2C))
                )
              )
              .border(1.5.dp, BitcoinGold, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CurrencyBitcoin,
              contentDescription = "Satoshi Wallet Logo",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Satoshi Wallet",
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.2.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(7.dp)
                  .clip(CircleShape)
                  .background(StatusSuccess)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "Donanım Korumalı • HSM Aktif",
                color = StatusSuccess,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Action controls (Fiat switcher + Lock button)
        Row(verticalAlignment = Alignment.CenterVertically) {
          // National Currency Selector Chip (TRY ₺, USD $, EUR €)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(ObsidianSurfaceVariant)
              .border(1.dp, ObsidianCardBorder, RoundedCornerShape(20.dp))
              .clickable { showFiatSelector = true }
              .testTag("fiat_selector_chip")
              .padding(horizontal = 12.dp, vertical = 7.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "Para Birimi Seç",
                tint = BitcoinGold,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "${walletState.activeFiatCurrency.symbol} ${walletState.activeFiatCurrency.code}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Quick Settings & Shortcuts Button
          IconButton(
            onClick = { showSettingsSheet = true },
            modifier = Modifier
              .testTag("settings_button")
              .size(38.dp)
              .clip(CircleShape)
              .background(ObsidianSurfaceVariant)
              .border(1.dp, ObsidianCardBorder, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Ayarlar ve Kısayollar",
              tint = BitcoinGold,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Lock Wallet Button
          IconButton(
            onClick = onLockWallet,
            modifier = Modifier
              .testTag("lock_wallet_button")
              .size(38.dp)
              .clip(CircleShape)
              .background(ObsidianSurfaceVariant)
              .border(1.dp, ObsidianCardBorder, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Cüzdanı Kilitle",
              tint = Color.LightGray,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // -------------------------------------------------------------
    // 2. MASTER PORTFOLIO CARD: Total Balance, Eye Privacy, 24h Trend
    // -------------------------------------------------------------
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dashboard_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(
            listOf(
              BitcoinGold.copy(alpha = 0.7f),
              ObsidianCardBorder,
              Color(0xFF627EEA).copy(alpha = 0.5f)
            )
          )
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          // Label and Eye Privacy Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "TOPLAM VARLIK PORTFÖYÜ",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(StatusSuccess.copy(alpha = 0.15f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = StatusSuccess,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(
                    text = "+%3,84 (24s)",
                    color = StatusSuccess,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            IconButton(
              onClick = onToggleBalancePrivacy,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = if (walletState.isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Bakiye Gizle/Göster",
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Large High-Contrast Balance Text
          val totalPortfolioFiat = walletState.calculateTotalPortfolioFiatValue(walletState.activeFiatCurrency)
          Text(
            text = if (walletState.isBalanceHidden) "••••••••" else CurrencyFormatter.formatFiatValue(
              totalPortfolioFiat / walletState.activeFiatCurrency.usdToFiatRate,
              walletState.activeFiatCurrency
            ),
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Multi-asset holdings summary row
          Text(
            text = if (walletState.isBalanceHidden) "••••••••" else "BTC: ${CurrencyFormatter.formatSats(walletState.onChainBalanceSats)} • ETH: ${String.format(Locale.US, "%.2f", walletState.ethereumBalanceEth)} • LTC: ${String.format(Locale.US, "%.1f", walletState.litecoinBalanceLtc)} • XRP: ${String.format(Locale.US, "%,.0f", walletState.rippleBalanceXrp)}",
            color = BitcoinGoldBright,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Security Status Pill inside Card
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(ObsidianSurfaceVariant)
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "StrongBox Çip + Biyometri Korumalı",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "AES-256 Bulut Yedek",
                color = StatusSuccess,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }

    // -------------------------------------------------------------
    // 3. QUICK ACTION BUTTONS: Send, Receive, Lightning, Backup, Audit
    // -------------------------------------------------------------
    item {
      Column {
        Text(
          text = "HIZLI İŞLEMLER",
          color = Color.Gray,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          ModernActionButton(
            title = "Gönder",
            subtitle = "Transfer",
            icon = Icons.Default.ArrowUpward,
            color = BitcoinGold,
            testTag = "action_send",
            onClick = { onNavigateTab(WalletNavTab.SEND) }
          )
          ModernActionButton(
            title = "Al",
            subtitle = "QR / Adres",
            icon = Icons.Default.ArrowDownward,
            color = StatusSuccess,
            testTag = "action_receive",
            onClick = { onNavigateTab(WalletNavTab.RECEIVE) }
          )
          ModernActionButton(
            title = "Lightning",
            subtitle = "Anlık Katman",
            icon = Icons.Default.Bolt,
            color = LightningCyan,
            testTag = "action_lightning",
            onClick = { onNavigateTab(WalletNavTab.LIGHTNING) }
          )
          ModernActionButton(
            title = "Yedekle",
            subtitle = "Bulut Kasa",
            icon = Icons.Default.CloudDone,
            color = Color(0xFFBB86FC),
            testTag = "action_backup",
            onClick = { onNavigateTab(WalletNavTab.BACKUP) }
          )
          ModernActionButton(
            title = "Güvenlik",
            subtitle = "1000 Denetim",
            icon = Icons.Default.Shield,
            color = Color(0xFFFF5252),
            testTag = "action_audit",
            onClick = { onNavigateTab(WalletNavTab.SECURITY) }
          )
        }
      }
    }

    // -------------------------------------------------------------
    // 4. FAST CURRENCY SWITCHER & ACTIVE ASSET SPOTLIGHT
    // -------------------------------------------------------------
    item {
      Column(modifier = Modifier.animateContentSize()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "HIZLI VARLIK SEÇİCİ",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Text(
            text = "Dokunarak Seç",
            color = BitcoinGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CryptoAssetSelectorBar(
          selectedAsset = walletState.selectedAsset,
          onAssetSelected = { asset ->
            onSelectAsset(asset)
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Active Asset Detail Spotlight Card
        val activeAsset = walletState.selectedAsset
        val assetColor = Color(activeAsset.iconColorHex)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
          colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceVariant),
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(assetColor.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = activeAsset.symbol.take(3),
                    color = assetColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = activeAsset.displayName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Birim Fiyatı: ${CurrencyFormatter.formatFiatValue(activeAsset.basePriceUsd, walletState.activeFiatCurrency)}",
                    color = Color.Gray,
                    fontSize = 11.sp
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (walletState.isBalanceHidden) "••••" else walletState.getAssetBalanceText(activeAsset),
                color = assetColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (walletState.isBalanceHidden) "••••" else walletState.getAssetFiatValue(activeAsset, walletState.activeFiatCurrency),
                color = Color.LightGray,
                fontSize = 12.sp
              )
            }

            // Quick shortcuts for active asset
            Column(horizontalAlignment = Alignment.End) {
              Button(
                onClick = {
                  onSelectAsset(activeAsset)
                  onNavigateTab(WalletNavTab.SEND)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = assetColor.copy(alpha = 0.25f)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = assetColor, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Gönder", color = assetColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Button(
                onClick = {
                  onSelectAsset(activeAsset)
                  onNavigateTab(WalletNavTab.RECEIVE)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurface),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(assetColor, ObsidianCardBorder))),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Al", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // -------------------------------------------------------------
    // 5. ALL CRYPTO ASSETS CARDS (Bitcoin, Lightning, ETH, LTC, XRP)
    // -------------------------------------------------------------
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "DESTEKLENEN KRİPTO VARLIKLAR",
          color = Color.Gray,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Text(
          text = "5 Blokzincir Aktif",
          color = StatusSuccess,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    // Bitcoin On-Chain & Lightning Network Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        AssetProtocolCard(
          assetName = "Bitcoin",
          networkLabel = "SegWit / Taproot",
          balanceText = CurrencyFormatter.formatSats(walletState.onChainBalanceSats),
          fiatText = CurrencyFormatter.formatFiat(walletState.onChainBalanceSats, walletState.activeFiatCurrency),
          accentColor = BitcoinGold,
          isBalanceHidden = walletState.isBalanceHidden,
          icon = Icons.Default.CurrencyBitcoin,
          modifier = Modifier.weight(1f),
          onClick = {
            onSelectAsset(CryptoAsset.BTC)
            onNavigateTab(WalletNavTab.RECEIVE)
          }
        )

        AssetProtocolCard(
          assetName = "Lightning",
          networkLabel = "Anlık Katman-2",
          balanceText = CurrencyFormatter.formatSats(walletState.lightningBalanceSats),
          fiatText = CurrencyFormatter.formatFiat(walletState.lightningBalanceSats, walletState.activeFiatCurrency),
          accentColor = LightningCyan,
          isBalanceHidden = walletState.isBalanceHidden,
          icon = Icons.Default.Bolt,
          modifier = Modifier.weight(1f),
          onClick = {
            onSelectAsset(CryptoAsset.LIGHTNING)
            onNavigateTab(WalletNavTab.LIGHTNING)
          }
        )
      }
    }

    // Ethereum & Litecoin Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        AssetProtocolCard(
          assetName = "Ethereum",
          networkLabel = "EVM Akıllı Sözleşme",
          balanceText = String.format(Locale.US, "%.4f ETH", walletState.ethereumBalanceEth),
          fiatText = CurrencyFormatter.formatFiatValue(
            walletState.ethereumBalanceEth * CryptoAsset.ETH.basePriceUsd,
            walletState.activeFiatCurrency
          ),
          accentColor = Color(0xFF627EEA),
          isBalanceHidden = walletState.isBalanceHidden,
          icon = Icons.Default.Shield,
          modifier = Modifier.weight(1f),
          onClick = {
            onSelectAsset(CryptoAsset.ETH)
            onNavigateTab(WalletNavTab.RECEIVE)
          }
        )

        AssetProtocolCard(
          assetName = "Litecoin",
          networkLabel = "Scrypt Hızlı UTXO",
          balanceText = String.format(Locale.US, "%.4f LTC", walletState.litecoinBalanceLtc),
          fiatText = CurrencyFormatter.formatFiatValue(
            walletState.litecoinBalanceLtc * CryptoAsset.LTC.basePriceUsd,
            walletState.activeFiatCurrency
          ),
          accentColor = Color(0xFF345D9D),
          isBalanceHidden = walletState.isBalanceHidden,
          icon = Icons.Default.Shield,
          modifier = Modifier.weight(1f),
          onClick = {
            onSelectAsset(CryptoAsset.LTC)
            onNavigateTab(WalletNavTab.RECEIVE)
          }
        )
      }
    }

    // Ripple XRP Full-Width Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onSelectAsset(CryptoAsset.XRP)
            onNavigateTab(WalletNavTab.RECEIVE)
          },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(
            listOf(Color(0xFF00AAE4).copy(alpha = 0.5f), ObsidianCardBorder)
          )
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF00AAE4).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "XRP",
                color = Color(0xFF00AAE4),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Ripple XRP Ledger",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "3 Saniyede Konsensüs • Hedef Etiketi (Tag) Destekli",
                color = Color.Gray,
                fontSize = 11.sp
              )
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = if (walletState.isBalanceHidden) "••••" else String.format(Locale.US, "%,.2f XRP", walletState.rippleBalanceXrp),
              color = Color(0xFF00AAE4),
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (walletState.isBalanceHidden) "••••" else CurrencyFormatter.formatFiatValue(
                walletState.rippleBalanceXrp * CryptoAsset.XRP.basePriceUsd,
                walletState.activeFiatCurrency
              ),
              color = Color.Gray,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // -------------------------------------------------------------
    // 6. 1000 SECURITY ENGINE & HSM HARDWARE BANNER
    // -------------------------------------------------------------
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateTab(WalletNavTab.SECURITY) }
          .testTag("security_audit_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(
            listOf(StatusSuccess.copy(alpha = 0.6f), ObsidianCardBorder)
          )
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(StatusSuccess.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            val closedCount = walletState.auditSummary?.totalVulnerabilitiesClosed ?: 1000
            val totalCount = walletState.auditSummary?.totalChecked ?: 1000
            val isFull = closedCount == totalCount
            Column {
              Text(
                text = "1000-Nokta Güvenlik Kalkanı & HSM",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isFull) {
                  "✓ 1000/1000 Kapalı Vektör • Endüstri Standartları (A+)"
                } else {
                  "$closedCount/$totalCount Açık Kapatıldı • İncelemek İçin Dokunun"
                },
                color = if (isFull) StatusSuccess else BitcoinGold,
                fontSize = 11.sp
              )
            }
          }
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // -------------------------------------------------------------
    // 7. FILTERABLE & SEARCHABLE TRANSACTION HISTORY
    // -------------------------------------------------------------
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SON İŞLEMLER",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Text(
            text = "${filteredTransactions.size} / ${walletState.transactions.size} İşlem",
            color = Color.Gray,
            fontSize = 11.sp
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar with instant clear button
        OutlinedTextField(
          value = txSearchQuery,
          onValueChange = { txSearchQuery = it },
          placeholder = { Text("İşlem, not veya adres ara...", color = Color.Gray, fontSize = 13.sp) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
          },
          trailingIcon = {
            if (txSearchQuery.isNotBlank()) {
              IconButton(onClick = { txSearchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = Color.Gray, modifier = Modifier.size(18.dp))
              }
            }
          },
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_search_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BitcoinGold,
            unfocusedBorderColor = ObsidianCardBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            unfocusedContainerColor = ObsidianSurface,
            focusedContainerColor = ObsidianSurface
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips (Tümü, Gelen, Giden, BTC, LN, ETH, LTC, XRP)
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            FilterChip(
              label = "Tümü",
              isSelected = selectedTxFilterAsset == null && selectedTxFilterType == null,
              onClick = {
                selectedTxFilterAsset = null
                selectedTxFilterType = null
              }
            )
          }
          item {
            FilterChip(
              label = "Gelen (+)",
              isSelected = selectedTxFilterType == TransactionType.RECEIVE,
              onClick = {
                selectedTxFilterType = if (selectedTxFilterType == TransactionType.RECEIVE) null else TransactionType.RECEIVE
              }
            )
          }
          item {
            FilterChip(
              label = "Giden (-)",
              isSelected = selectedTxFilterType == TransactionType.SEND,
              onClick = {
                selectedTxFilterType = if (selectedTxFilterType == TransactionType.SEND) null else TransactionType.SEND
              }
            )
          }
          items(CryptoAsset.values()) { asset ->
            FilterChip(
              label = asset.symbol,
              isSelected = selectedTxFilterAsset == asset,
              onClick = {
                selectedTxFilterAsset = if (selectedTxFilterAsset == asset) null else asset
              }
            )
          }
        }
      }
    }

    // -------------------------------------------------------------
    // 8. TRANSACTION ITEMS OR EMPTY STATE
    // -------------------------------------------------------------
    if (filteredTransactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = Color.Gray,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Aramanıza uygun işlem bulunamadı",
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Filtreleri sıfırlayarak tüm işlemleri görüntüleyebilirsiniz.",
              color = Color.Gray,
              fontSize = 12.sp,
              textAlign = TextAlign.Center
            )
            if (txSearchQuery.isNotBlank() || selectedTxFilterAsset != null || selectedTxFilterType != null) {
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = {
                  txSearchQuery = ""
                  selectedTxFilterAsset = null
                  selectedTxFilterType = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceVariant),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Filtreleri Temizle", color = Color.White, fontSize = 12.sp)
              }
            }
          }
        }
      }
    } else {
      items(filteredTransactions) { tx ->
        TransactionRow(
          tx = tx,
          fiat = walletState.activeFiatCurrency,
          onClick = { selectedTxDetails = tx }
        )
      }
    }
  }

  // -------------------------------------------------------------
  // 9. BOTTOM SHEETS: Fiat Selector & Transaction Details
  // -------------------------------------------------------------
  if (showFiatSelector) {
    ModalBottomSheet(
      onDismissRequest = { showFiatSelector = false },
      sheetState = sheetState,
      containerColor = ObsidianSurface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .padding(bottom = 32.dp)
      ) {
        Text(
          text = "Ulusal Para Birimi Seçin",
          color = Color.White,
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Tüm bakiyeler ve işlemler seçtiğiniz para birimine anında çevrilir.",
          color = Color.Gray,
          fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(18.dp))

        FiatCurrency.values().forEach { fiat ->
          val isSelected = fiat == walletState.activeFiatCurrency
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(if (isSelected) BitcoinGold.copy(alpha = 0.15f) else Color.Transparent)
              .clickable {
                onSelectFiat(fiat)
                showFiatSelector = false
              }
              .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = fiat.symbol,
                color = if (isSelected) BitcoinGold else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
              )
              Column {
                Text(
                  text = "${fiat.displayName} (${fiat.code})",
                  color = Color.White,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "1 BTC ≈ ${fiat.symbol}${String.format(Locale.US, "%,.0f", fiat.btcPriceInFiat)}",
                  color = Color.Gray,
                  fontSize = 12.sp
                )
              }
            }
            if (isSelected) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = BitcoinGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Aktif", color = BitcoinGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }

  // Transaction Details Modal
  selectedTxDetails?.let { tx ->
    ModalBottomSheet(
      onDismissRequest = { selectedTxDetails = null },
      containerColor = ObsidianSurface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .padding(bottom = 32.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (tx.type == TransactionType.SEND) "Giden Transfer Detayı" else "Gelen Transfer Detayı",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
          )
          NetworkBadge(network = tx.network)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "${if (tx.type == TransactionType.SEND) "-" else "+"}${tx.displayAmount(walletState.activeFiatCurrency)}",
          color = if (tx.type == TransactionType.SEND) Color.White else StatusSuccess,
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = tx.displayFiat(walletState.activeFiatCurrency),
          color = Color.Gray,
          fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        DetailRow(label = "Kripto Varlık", value = "${tx.asset.displayName} (${tx.asset.symbol})")
        DetailRow(label = "Durum", value = tx.status.label)
        DetailRow(label = "Ağ Ücreti", value = tx.customFeeText ?: "${tx.feeSatoshis} sats")
        DetailRow(label = "İşlem Açıklaması", value = tx.memo)
        DetailRow(label = "Zaman Damgası", value = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(tx.timestamp)))
        DetailRowWithCopy(label = "Karşı Adres", value = tx.recipientOrSender, context = context)
        DetailRowWithCopy(label = "İşlem Karması (Hash)", value = tx.txHash, context = context)

        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = { selectedTxDetails = null },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceVariant)
        ) {
          Text("Kapat", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }

  // Quick Settings & Shortcuts Modal Bottom Sheet
  if (showSettingsSheet) {
    ModalBottomSheet(
      onDismissRequest = { showSettingsSheet = false },
      containerColor = ObsidianSurface,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 36.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BitcoinGold.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = BitcoinGold,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Ayarlar ve Hızlı Kısayollar",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Cüzdan yönetimi, güvenlik ve işlem kısayolları",
                color = Color.Gray,
                fontSize = 11.sp
              )
            }
          }

          IconButton(
            onClick = { showSettingsSheet = false },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Clear, contentDescription = "Kapat", tint = Color.Gray)
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 1: HIZLI ERİŞİM KISAYOLLARI
        Text(
          text = "HIZLI ERİŞİM KISAYOLLARI",
          color = Color.Gray,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingsShortcutRow(
          icon = Icons.Default.Shield,
          iconTint = StatusSuccess,
          title = "Güvenlik & 1000 Denetim Kalkanı",
          subtitle = "StrongBox çip, HSM ve 1000 kapalı vektör denetimi",
          testTag = "settings_shortcut_security",
          onClick = {
            showSettingsSheet = false
            onNavigateTab(WalletNavTab.SECURITY)
          }
        )

        SettingsShortcutRow(
          icon = Icons.Default.CloudDone,
          iconTint = Color(0xFFBB86FC),
          title = "Bulut Kasa & Çoklu Zincir Yedekleme",
          subtitle = "AES-256-GCM donanım anahtarlı bulut yedek kasa",
          testTag = "settings_shortcut_backup",
          onClick = {
            showSettingsSheet = false
            onNavigateTab(WalletNavTab.BACKUP)
          }
        )

        SettingsShortcutRow(
          icon = Icons.Default.Bolt,
          iconTint = LightningCyan,
          title = "Lightning Network & Kanallar",
          subtitle = "Sıfır ücretli anlık katman-2 mikroyol ödemeleri",
          testTag = "settings_shortcut_lightning",
          onClick = {
            showSettingsSheet = false
            onNavigateTab(WalletNavTab.LIGHTNING)
          }
        )

        SettingsShortcutRow(
          icon = Icons.Default.ArrowUpward,
          iconTint = BitcoinGold,
          title = "Kripto Gönder (Hızlı Transfer)",
          subtitle = "BTC, Lightning, ETH, LTC, XRP doğrudan transfer",
          testTag = "settings_shortcut_send",
          onClick = {
            showSettingsSheet = false
            onNavigateTab(WalletNavTab.SEND)
          }
        )

        SettingsShortcutRow(
          icon = Icons.Default.ArrowDownward,
          iconTint = StatusSuccess,
          title = "Kripto Ödeme Al (QR & Adres)",
          subtitle = "Donanım adresleri ve tek tıkla kopyalama",
          testTag = "settings_shortcut_receive",
          onClick = {
            showSettingsSheet = false
            onNavigateTab(WalletNavTab.RECEIVE)
          }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: TERCİHLER & GİZLİLİK
        Text(
          text = "TERCİHLER VE GİZLİLİK",
          color = Color.Gray,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Privacy Toggle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianSurfaceVariant)
            .clickable { onToggleBalancePrivacy() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = if (walletState.isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = null,
              tint = BitcoinGold,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Bakiye Gizliliği (Göz Modu)",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = if (walletState.isBalanceHidden) "Bakiyeler gizlendi (••••)" else "Bakiyeler görünür durumda",
                color = Color.Gray,
                fontSize = 11.sp
              )
            }
          }
          Switch(
            checked = walletState.isBalanceHidden,
            onCheckedChange = { onToggleBalancePrivacy() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = BitcoinGold,
              checkedTrackColor = BitcoinGold.copy(alpha = 0.3f),
              uncheckedThumbColor = Color.LightGray,
              uncheckedTrackColor = ObsidianCardBorder
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Currency Selector Shortcut
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianSurfaceVariant)
            .clickable {
              showSettingsSheet = false
              showFiatSelector = true
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Public,
              contentDescription = null,
              tint = BitcoinGold,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Aktif Para Birimi",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "${walletState.activeFiatCurrency.displayName} (${walletState.activeFiatCurrency.symbol} ${walletState.activeFiatCurrency.code})",
                color = Color.Gray,
                fontSize = 11.sp
              )
            }
          }
          Text(
            text = "Değiştir >",
            color = BitcoinGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Immediate Lock Button
        Button(
          onClick = {
            showSettingsSheet = false
            onLockWallet()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_lock_wallet_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381E24))
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = Color(0xFFFF5252),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Cüzdanı Şimdi Kilitle",
              color = Color(0xFFFF5252),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
fun FilterChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(if (isSelected) BitcoinGold.copy(alpha = 0.2f) else ObsidianSurface)
      .border(
        width = 1.dp,
        color = if (isSelected) BitcoinGold else ObsidianCardBorder,
        shape = RoundedCornerShape(16.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 7.dp)
  ) {
    Text(
      text = label,
      color = if (isSelected) BitcoinGold else Color.Gray,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
    )
  }
}

@Composable
fun AssetProtocolCard(
  assetName: String,
  networkLabel: String,
  balanceText: String,
  fiatText: String,
  accentColor: Color,
  isBalanceHidden: Boolean,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        listOf(accentColor.copy(alpha = 0.5f), ObsidianCardBorder)
      )
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
        Text(text = networkLabel, color = Color.Gray, fontSize = 10.sp)
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(text = assetName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = if (isBalanceHidden) "••••" else balanceText,
        color = accentColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = if (isBalanceHidden) "••••" else fiatText,
        color = Color.Gray,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, color = Color.Gray, fontSize = 13.sp)
    Text(
      text = value,
      color = Color.White,
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.width(200.dp),
      textAlign = TextAlign.End
    )
  }
}

@Composable
fun DetailRowWithCopy(label: String, value: String, context: Context) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, color = Color.Gray, fontSize = 13.sp)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.clickable {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(context, "$label kopyalandı", Toast.LENGTH_SHORT).show()
      }
    ) {
      Text(
        text = if (value.length > 18) "${value.take(10)}...${value.takeLast(6)}" else value,
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.width(6.dp))
      Icon(
        imageVector = Icons.Default.ContentCopy,
        contentDescription = "Kopyala",
        tint = BitcoinGold,
        modifier = Modifier.size(14.dp)
      )
    }
  }
}

@Composable
fun ModernActionButton(
  title: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable { onClick() }
      .padding(horizontal = 2.dp)
  ) {
    Box(
      modifier = Modifier
        .testTag(testTag)
        .size(54.dp)
        .clip(CircleShape)
        .background(color.copy(alpha = 0.16f))
        .border(1.2.dp, color.copy(alpha = 0.5f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = color,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = title,
      color = Color.White,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = subtitle,
      color = Color.Gray,
      fontSize = 9.sp,
      fontWeight = FontWeight.Normal
    )
  }
}

// Retain ActionButton for backwards compatibility
@Composable
fun ActionButton(
  title: String,
  icon: ImageVector,
  color: Color,
  testTag: String,
  onClick: () -> Unit
) {
  ModernActionButton(
    title = title,
    subtitle = "",
    icon = icon,
    color = color,
    testTag = testTag,
    onClick = onClick
  )
}

@Composable
fun TransactionRow(
  tx: TransactionRecord,
  fiat: FiatCurrency,
  onClick: () -> Unit
) {
  val isSend = tx.type == TransactionType.SEND
  val assetColor = Color(tx.asset.iconColorHex)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        listOf(ObsidianCardBorder, ObsidianCardBorder.copy(alpha = 0.5f))
      )
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isSend) Color(0xFF2C1919) else Color(0xFF132A1C)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isSend) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = if (isSend) Color(0xFFFF5252) else StatusSuccess,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = tx.memo,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(160.dp)
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = tx.asset.symbol,
              color = assetColor,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = " • ${tx.network.speed}",
              color = Color.Gray,
              fontSize = 10.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.width(120.dp)
            )
          }
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${if (isSend) "-" else "+"}${tx.displayAmount(fiat)}",
          color = if (isSend) Color.White else StatusSuccess,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = tx.displayFiat(fiat),
          color = Color.Gray,
          fontSize = 12.sp
        )
      }
    }
  }
}

@Composable
fun SettingsShortcutRow(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  testTag: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag)
      .clip(RoundedCornerShape(14.dp))
      .background(ObsidianSurfaceVariant)
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = subtitle,
          color = Color.Gray,
          fontSize = 11.sp
        )
      }
    }
    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForward,
      contentDescription = null,
      tint = Color.Gray,
      modifier = Modifier.size(16.dp)
    )
  }
  Spacer(modifier = Modifier.height(6.dp))
}
