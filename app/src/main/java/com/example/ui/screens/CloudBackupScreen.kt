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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoAsset
import com.example.data.model.WalletState
import com.example.ui.BackupUiState
import com.example.ui.theme.BitcoinGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudBackupScreen(
  walletState: WalletState,
  backupState: BackupUiState,
  onPassphraseChanged: (String) -> Unit,
  onConfirmPassphraseChanged: (String) -> Unit,
  onToggleMnemonicRevealed: () -> Unit,
  onCreateCloudBackup: () -> Unit,
  onBack: () -> Unit,
  onRestorePassphraseChanged: (String) -> Unit = {},
  onRestoreCloudBackup: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val backup = walletState.cloudBackup

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
          .testTag("backup_back_button")
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
          text = "Encrypted Cloud Backup",
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Zero-Knowledge AES-256-GCM Multi-Asset Vault",
          color = Color.Gray,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Cloud Backup Status Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("cloud_backup_status_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
      border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
        listOf(StatusSuccess.copy(alpha = 0.5f), ObsidianCardBorder)
      ))
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
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
                .background(StatusSuccess.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CloudDone, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Cloud Vault Synchronized",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "End-to-End Encrypted (Zero-Knowledge)",
                color = StatusSuccess,
                fontSize = 12.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BackupDetailRow(label = "Cipher Algorithm", value = backup.backupCipher)
        BackupDetailRow(label = "Key Derivation", value = "PBKDF2WithHmacSHA256 (100,000 Rounds)")
        val lastTs = backup.lastBackupTimestamp
        BackupDetailRow(
          label = "Last Synced",
          value = if (lastTs != null && lastTs > 0L) SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(lastTs)) else "Never"
        )
        BackupDetailRow(label = "Cloud Storage Target", value = backup.cloudLocation)
        val checksum = backup.cloudChecksumSha256
        BackupDetailRow(label = "Vault Checksum", value = if (checksum != null) "${checksum.take(20)}..." else "Pending Sync")

        Spacer(modifier = Modifier.height(14.dp))

        // Multi-Asset Coverage Chips
        Text(text = "BACKED UP ASSETS IN VAULT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          CryptoAsset.values().forEach { asset ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(asset.iconColorHex).copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(text = "${asset.symbol} Active", color = Color(asset.iconColorHex), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Create New Encrypted Backup Section
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Create Encrypted Cloud Backup",
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Enter a master passphrase to encrypt your BIP-39 mnemonic, HSM attestation, and all multi-asset account balances with AES-256-GCM.",
          color = Color.Gray,
          fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = backupState.passphraseInput,
          onValueChange = onPassphraseChanged,
          label = { Text("Backup Passphrase (min 8 chars)", color = Color.Gray) },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("backup_passphrase_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BitcoinGold,
            unfocusedBorderColor = ObsidianCardBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            unfocusedContainerColor = ObsidianSurfaceVariant,
            focusedContainerColor = ObsidianSurfaceVariant
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = backupState.confirmPassphraseInput,
          onValueChange = onConfirmPassphraseChanged,
          label = { Text("Confirm Passphrase", color = Color.Gray) },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("backup_confirm_passphrase_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BitcoinGold,
            unfocusedBorderColor = ObsidianCardBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            unfocusedContainerColor = ObsidianSurfaceVariant,
            focusedContainerColor = ObsidianSurfaceVariant
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status / Error message
        backupState.backupStatusMessage?.let { msg ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (backupState.isError) Color(0xFF2C1919) else Color(0xFF132A1C))
          ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (backupState.isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (backupState.isError) StatusError else StatusSuccess,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = msg, color = if (backupState.isError) StatusError else StatusSuccess, fontSize = 12.sp)
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        // Backup Action Button
        Button(
          onClick = onCreateCloudBackup,
          enabled = !backupState.isGeneratingBackup,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("create_cloud_backup_button"),
          colors = ButtonDefaults.buttonColors(containerColor = BitcoinGold, contentColor = Color.Black)
        ) {
          if (backupState.isGeneratingBackup) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Computing PBKDF2 (100,000 Rounds)...", fontWeight = FontWeight.Bold)
          } else {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Encrypt & Sync to Cloud Vault", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Restore & Decryption Verification Section
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Test Zero-Knowledge Cloud Restore",
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Verify that your encrypted payload can be successfully decrypted and validated against its SHA-256 MAC tag.",
          color = Color.Gray,
          fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = backupState.restorePassphraseInput,
          onValueChange = onRestorePassphraseChanged,
          label = { Text("Enter Passphrase to Test Restore", color = Color.Gray) },
          visualTransformation = PasswordVisualTransformation(),
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BitcoinGold,
            unfocusedBorderColor = ObsidianCardBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            unfocusedContainerColor = ObsidianSurfaceVariant,
            focusedContainerColor = ObsidianSurfaceVariant
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = onRestoreCloudBackup,
          enabled = !backupState.isRestoring,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceVariant)
        ) {
          if (backupState.isRestoring) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
          } else {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Decrypt & Verify Vault Integrity", color = Color.White, fontSize = 13.sp)
          }
        }

        backupState.restoreResultText?.let { res ->
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = res,
            color = if (res.contains("SUCCESS")) StatusSuccess else StatusError,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Cold Storage Mnemonic Seed Phrase Section
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("mnemonic_backup_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Key, contentDescription = null, tint = BitcoinGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "BIP-39 Mnemonic Seed Phrase",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onToggleMnemonicRevealed) {
            Icon(
              imageVector = if (backupState.isMnemonicRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = "Toggle Mnemonic",
              tint = BitcoinGold
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (backupState.isMnemonicRevealed) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(ObsidianSurfaceVariant)
              .padding(14.dp)
          ) {
            val words = walletState.mnemonicWords
            for (i in 0 until words.size step 3) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                for (j in 0 until 3) {
                  val idx = i + j
                  if (idx < words.size) {
                    Text(
                      text = "${idx + 1}. ${words[idx]}",
                      color = Color.White,
                      fontSize = 13.sp,
                      fontFamily = FontFamily.Monospace,
                      modifier = Modifier.weight(1f)
                    )
                  }
                }
              }
            }
          }
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(ObsidianSurfaceVariant)
              .clickable { onToggleMnemonicRevealed() }
              .padding(20.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Tap to Reveal 12-Word Master Recovery Seed",
              color = BitcoinGold,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}

@Composable
fun BackupDetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, color = Color.Gray, fontSize = 12.sp)
    Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
  }
}
