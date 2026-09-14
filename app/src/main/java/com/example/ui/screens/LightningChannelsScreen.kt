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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyFormatter
import com.example.data.model.LightningChannel
import com.example.data.model.WalletState
import com.example.ui.theme.LightningCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.StatusSuccess

@Composable
fun LightningChannelsScreen(
  walletState: WalletState,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val totalLocal = walletState.lightningChannels.sumOf { it.localCapacitySats }
  val totalRemote = walletState.lightningChannels.sumOf { it.remoteCapacitySats }
  val totalCapacity = totalLocal + totalRemote

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
  ) {
    // Top Bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .testTag("lightning_back_button")
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
            text = "Lightning Layer-2 Hub",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Instant Peer-to-Peer Payment Channels",
            color = Color.Gray,
            fontSize = 12.sp
          )
        }
      }
    }

    // Node ID Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
          listOf(LightningCyan.copy(alpha = 0.5f), ObsidianCardBorder)
        ))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                  .background(LightningCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Bolt,
                  contentDescription = null,
                  tint = LightningCyan,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Lightning Node ID",
                  color = Color.White,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Dual-Funded Onion Routing Active",
                  color = StatusSuccess,
                  fontSize = 11.sp
                )
              }
            }
            IconButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Node ID", walletState.lightningNodeId))
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Node ID", tint = LightningCyan)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = walletState.lightningNodeId,
            color = Color.LightGray,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }

    // Liquidity Capacity Balance Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "CHANNEL LIQUIDITY CAPACITY",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Progress bar of Local vs Remote balance
          val localFraction = if (totalCapacity > 0) totalLocal.toFloat() / totalCapacity else 0.5f
          LinearProgressIndicator(
            progress = { localFraction },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp)),
            color = LightningCyan,
            trackColor = Color(0xFF1E2838)
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(text = "Outbound (Can Send)", color = Color.Gray, fontSize = 11.sp)
              Text(
                text = CurrencyFormatter.formatSats(totalLocal),
                color = LightningCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(text = "Inbound (Can Receive)", color = Color.Gray, fontSize = 11.sp)
              Text(
                text = CurrencyFormatter.formatSats(totalRemote),
                color = Color.LightGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Protocol Highlights
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        LightningMetricCard(
          icon = Icons.Default.Speed,
          title = "Settlement",
          value = "< 1 Second",
          modifier = Modifier.weight(1f)
        )
        LightningMetricCard(
          icon = Icons.Default.Router,
          title = "Routing Fee",
          value = "1 Satoshi",
          modifier = Modifier.weight(1f)
        )
        LightningMetricCard(
          icon = Icons.Default.Layers,
          title = "Channels",
          value = "${walletState.lightningChannels.size} Active",
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Text(
        text = "ACTIVE PAYMENT CHANNELS",
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
      )
    }

    items(walletState.lightningChannels) { channel ->
      ChannelItemCard(channel = channel)
    }
  }
}

@Composable
fun LightningMetricCard(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Icon(imageVector = icon, contentDescription = null, tint = LightningCyan, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.height(6.dp))
      Text(text = title, color = Color.Gray, fontSize = 11.sp)
      Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
fun ChannelItemCard(channel: LightningChannel) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
      listOf(ObsidianCardBorder, ObsidianCardBorder.copy(alpha = 0.5f))
    ))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = channel.remoteNodeAlias,
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StatusSuccess.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(text = "ONLINE", color = StatusSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(text = "ID: ${channel.channelId}", color = Color.Gray, fontSize = 11.sp)

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = "Local: ${CurrencyFormatter.formatSats(channel.localCapacitySats)}", color = LightningCyan, fontSize = 12.sp)
        Text(text = "Remote: ${CurrencyFormatter.formatSats(channel.remoteCapacitySats)}", color = Color.Gray, fontSize = 12.sp)
      }
    }
  }
}
