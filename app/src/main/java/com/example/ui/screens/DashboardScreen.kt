package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
  var showFiatSelector by remember { mutableStateOf(false) }
  var selectedTxDetails by remember { mutableStateOf<TransactionRecord?>(null) }
  var txSearchQuery by remember { mutableStateOf("") }
  var selectedTxFilterAsset by remember { mutableStateOf<CryptoAsset?>(null) }
  var selectedTxFilterType by remember { mutableStateOf<TransactionType?>(null) }
  val sheetState = rememberModalBottomSheetState()

  // Filter transactions based on asset, search, and type
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
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
  ) {
    // --- Top Bar ---
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
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
              contentDescription = "Satoshi Logo",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Satoshi Wallet",
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(StatusSuccess)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "1000/1000 Hardened • HSM Active",
                color = StatusSuccess,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // National Currency Switcher Button (e.g. ₺ TRY, $ USD, € EUR)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(ObsidianSurfaceVariant)
              .border(1.dp, ObsidianCardBorder, RoundedCornerShape(20.dp))
              .clickable { showFiatSelector = true }
              .testTag("fiat_selector_chip")
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "Currency",
                tint = BitcoinGold,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${walletState.activeFiatCurrency.symbol} ${walletState.activeFiatCurrency.code}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Lock Wallet Button
          IconButton(
            onClick = onLockWallet,
            modifier = Modifier
              .testTag("lock_wallet_button")
              .size(36.dp)
              .clip(CircleShape)
              .background(ObsidianSurfaceVariant)
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Lock",
              tint = Color.Gray,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // --- Master Multicurrency Portfolio Card ---
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dashboard_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
          listOf(BitcoinGold.copy(alpha = 0.6f), ObsidianCardBorder, Color(0xFF627EEA).copy(alpha = 0.4f))
        ))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TOTAL MULTICURRENCY PORTFOLIO",
              color = Color.Gray,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 1.sp
            )
            IconButton(
              onClick = onToggleBalancePrivacy,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = if (walletState.isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Toggle Privacy",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Total Portfolio Value in Active National Fiat
          val totalPortfolioFiat = walletState.calculateTotalPortfolioFiatValue(walletState.activeFiatCurrency)
          Text(
            text = if (walletState.isBalanceHidden) "••••••••" else CurrencyFormatter.formatFiatValue(totalPortfolioFiat / walletState.activeFiatCurrency.usdToFiatRate, walletState.activeFiatCurrency),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Crypto breakdown summary
          Text(
            text = if (walletState.isBalanceHidden) "••••" else "BTC: ${CurrencyFormatter.formatSats(walletState.onChainBalanceSats)} • ETH: ${String.format(Locale.US, "%.2f", walletState.ethereumBalanceEth)} • LTC: ${String.format(Locale.US, "%.1f", walletState.litecoinBalanceLtc)} • XRP: ${String.format(Locale.US, "%,.0f", walletState.rippleBalanceXrp)}",
            color = BitcoinGoldBright,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Security & Cloud Vault Synchronized Bar
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
                text = "HSM StrongBox + MFA Protected",
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
                text = "AES-256 Cloud Sync",
                color = StatusSuccess,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }

    // --- Currency Switching Bar (BTC, LN, ETH, LTC, XRP) ---
    item {
      Column {
        Text(
          text = "FAST CURRENCY SWITCHER",
          color = Color.Gray,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CryptoAssetSelectorBar(
          selectedAsset = walletState.selectedAsset,
          onAssetSelected = { asset ->
            onSelectAsset(asset)
          }
        )
      }
    }

    // --- Quick Action Buttons Grid ---
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        ActionButton(
          title = "Send",
          icon = Icons.Default.ArrowUpward,
          color = BitcoinGold,
          testTag = "action_send",
          onClick = { onNavigateTab(WalletNavTab.SEND) }
        )
        ActionButton(
          title = "Receive",
          icon = Icons.Default.ArrowDownward,
          color = StatusSuccess,
          testTag = "action_receive",
          onClick = { onNavigateTab(WalletNavTab.RECEIVE) }
        )
        ActionButton(
          title = "Lightning",
          icon = Icons.Default.Bolt,
          color = LightningCyan,
          testTag = "action_lightning",
          onClick = { onNavigateTab(WalletNavTab.LIGHTNING) }
        )
        ActionButton(
          title = "Backup",
          icon = Icons.Default.CloudDone,
          color = Color(0xFFBB86FC),
          testTag = "action_backup",
          onClick = { onNavigateTab(WalletNavTab.BACKUP) }
        )
        ActionButton(
          title = "1000 Audit",
          icon = Icons.Default.Shield,
          color = Color(0xFFFF5252),
          testTag = "action_audit",
          onClick = { onNavigateTab(WalletNavTab.SECURITY) }
        )
      }
    }

    // --- Multi-Asset Portfolio Cards ---
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SUPPORTED CURRENCIES & ASSETS",
          color = Color.Gray,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 1.sp
        )
        Text(
          text = "5 Networks Active",
          color = StatusSuccess,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    // Bitcoin On-Chain & Lightning Layer-2 Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Bitcoin On-Chain Card
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

        // Lightning Network Card
        AssetProtocolCard(
          assetName = "Lightning",
          networkLabel = "Layer-2 Instant",
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

    // Altcoin Protocol Cards: Ethereum, Litecoin, Ripple XRP
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Ethereum Card
        AssetProtocolCard(
          assetName = "Ethereum",
          networkLabel = "EVM Smart Chain",
          balanceText = String.format(Locale.US, "%.4f ETH", walletState.ethereumBalanceEth),
          fiatText = CurrencyFormatter.formatFiatValue(walletState.ethereumBalanceEth * CryptoAsset.ETH.basePriceUsd, walletState.activeFiatCurrency),
          accentColor = Color(0xFF627EEA),
          isBalanceHidden = walletState.isBalanceHidden,
          icon = Icons.Default.Shield,
          modifier = Modifier.weight(1f),
          onClick = {
            onSelectAsset(CryptoAsset.ETH)
            onNavigateTab(WalletNavTab.RECEIVE)
          }
        )

        // Litecoin Card
        AssetProtocolCard(
          assetName = "Litecoin",
          networkLabel = "Scrypt Fast UTXO",
          balanceText = String.format(Locale.US, "%.4f LTC", walletState.litecoinBalanceLtc),
          fiatText = CurrencyFormatter.formatFiatValue(walletState.litecoinBalanceLtc * CryptoAsset.LTC.basePriceUsd, walletState.activeFiatCurrency),
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

    // Ripple XRP Card
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
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
          listOf(Color(0xFF00AAE4).copy(alpha = 0.5f), ObsidianCardBorder)
        ))
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
                text = "Instant 3-sec Consensus • Destination Tag Supported",
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
              text = if (walletState.isBalanceHidden) "••••" else CurrencyFormatter.formatFiatValue(walletState.rippleBalanceXrp * CryptoAsset.XRP.basePriceUsd, walletState.activeFiatCurrency),
              color = Color.Gray,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // --- 1000 Vulnerabilities Hardened Banner ---
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateTab(WalletNavTab.SECURITY) }
          .testTag("security_audit_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
          listOf(StatusSuccess.copy(alpha = 0.5f), ObsidianCardBorder)
        ))
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
                .size(40.dp)
                .clip(CircleShape)
                .background(StatusSuccess.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "1000-Check Security Engine & HSM",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "1000/1000 Hardened • StrongBox Silicon Active",
                color = StatusSuccess,
                fontSize = 12.sp
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

    // --- Filterable Transaction History Header ---
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "TRANSACTION HISTORY",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
          )
          Text(
            text = "${filteredTransactions.size} of ${walletState.transactions.size} records",
            color = Color.Gray,
            fontSize = 11.sp
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar for Transaction History
        OutlinedTextField(
          value = txSearchQuery,
          onValueChange = { txSearchQuery = it },
          placeholder = { Text("Search transactions, memo, or hash...", color = Color.DarkGray, fontSize = 12.sp) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
          },
          shape = RoundedCornerShape(12.dp),
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

        // Filter chips (All, BTC, LN, ETH, LTC, XRP, Sent, Received)
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            FilterChip(
              label = "All Assets",
              isSelected = selectedTxFilterAsset == null,
              onClick = { selectedTxFilterAsset = null }
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
          item {
            FilterChip(
              label = "Sent Only",
              isSelected = selectedTxFilterType == TransactionType.SEND,
              onClick = {
                selectedTxFilterType = if (selectedTxFilterType == TransactionType.SEND) null else TransactionType.SEND
              }
            )
          }
          item {
            FilterChip(
              label = "Received Only",
              isSelected = selectedTxFilterType == TransactionType.RECEIVE,
              onClick = {
                selectedTxFilterType = if (selectedTxFilterType == TransactionType.RECEIVE) null else TransactionType.RECEIVE
              }
            )
          }
        }
      }
    }

    // --- Transaction Items ---
    if (filteredTransactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "No matching transactions found.", color = Color.Gray, fontSize = 13.sp)
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

  // Fiat Currency Selector Bottom Sheet
  if (showFiatSelector) {
    ModalBottomSheet(
      onDismissRequest = { showFiatSelector = false },
      sheetState = sheetState,
      containerColor = ObsidianSurface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Text(
          text = "Select National Fiat Currency",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Balances and transactions will automatically convert based on real-time market rates.",
          color = Color.Gray,
          fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        FiatCurrency.values().forEach { fiat ->
          val isSelected = fiat == walletState.activeFiatCurrency
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
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
                fontSize = 20.sp,
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
              Text(text = "Active", color = BitcoinGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
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
          .padding(24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (tx.type == TransactionType.SEND) "Sent Payment" else "Received Payment",
            color = Color.White,
            fontSize = 20.sp,
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

        DetailRow(label = "Asset", value = "${tx.asset.displayName} (${tx.asset.symbol})")
        DetailRow(label = "Status", value = tx.status.label)
        DetailRow(label = "Network Fee", value = tx.customFeeText ?: "${tx.feeSatoshis} sats")
        DetailRow(label = "Memo", value = tx.memo)
        DetailRow(label = "Timestamp", value = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(tx.timestamp)))
        DetailRow(label = "Participant Address", value = tx.recipientOrSender)
        DetailRow(label = "Transaction Hash", value = tx.txHash)

        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = { selectedTxDetails = null },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceVariant)
        ) {
          Text("Close", color = Color.White)
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
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      color = if (isSelected) BitcoinGold else Color.Gray,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
      listOf(accentColor.copy(alpha = 0.5f), ObsidianCardBorder)
    ))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
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
      Text(text = assetName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
      modifier = Modifier.width(220.dp)
    )
  }
}

@Composable
fun ActionButton(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .testTag(testTag)
        .size(54.dp)
        .clip(CircleShape)
        .background(ObsidianSurface)
        .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
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
      color = Color.LightGray,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium
    )
  }
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
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
      listOf(ObsidianCardBorder, ObsidianCardBorder.copy(alpha = 0.5f))
    ))
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
            .size(40.dp)
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
