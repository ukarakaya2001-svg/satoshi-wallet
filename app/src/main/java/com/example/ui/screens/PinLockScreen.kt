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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BitcoinGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess

@Composable
fun PinLockScreen(
  enteredPin: String,
  isError: Boolean,
  biometricMessage: String? = null,
  onDigitClick: (String) -> Unit,
  onBackspaceClick: () -> Unit,
  onBiometricClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBg)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    // Logo & Header
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(BitcoinGold, Color(0xFF1E1B2C))))
          .border(2.dp, BitcoinGold, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CurrencyBitcoin,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(40.dp)
        )
      }
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "Satoshi Wallet",
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = if (isError) "Hatalı PIN. Lütfen tekrar deneyin." else "PIN Kodunu Girin (Varsayılan: 2121) veya Biyometriyi Kullanın",
        color = if (isError) StatusError else Color.Gray,
        fontSize = 13.sp
      )

      if (!biometricMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = biometricMessage,
          color = if (biometricMessage.contains("Error") || biometricMessage.contains("failed") || biometricMessage.contains("not recognized")) StatusError else StatusSuccess,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 4 PIN Dots
      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        for (i in 0 until 4) {
          val isFilled = i < enteredPin.length
          Box(
            modifier = Modifier
              .size(16.dp)
              .clip(CircleShape)
              .background(
                when {
                  isError -> StatusError
                  isFilled -> BitcoinGold
                  else -> ObsidianSurface
                }
              )
              .border(
                1.5.dp,
                when {
                  isError -> StatusError
                  isFilled -> BitcoinGold
                  else -> ObsidianCardBorder
                },
                CircleShape
              )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Quick Biometric Launch Button
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(StatusSuccess.copy(alpha = 0.12f))
          .border(1.dp, StatusSuccess.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
          .clickable { onBiometricClick() }
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("biometric_unlock_pill")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Fingerprint / Face Unlock",
            tint = StatusSuccess,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Biyometrik ile Kilidi Aç (Parmak İzi / Yüz)",
            color = StatusSuccess,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // Keypad Grid
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      KeypadRow(listOf("1", "2", "3"), onDigitClick)
      KeypadRow(listOf("4", "5", "6"), onDigitClick)
      KeypadRow(listOf("7", "8", "9"), onDigitClick)
      Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Biometric Quick Unlock Key
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(StatusSuccess.copy(alpha = 0.15f))
            .border(1.dp, StatusSuccess.copy(alpha = 0.5f), CircleShape)
            .clickable { onBiometricClick() }
            .testTag("keypad_biometrics"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Biometric Prompt",
            tint = StatusSuccess,
            modifier = Modifier.size(32.dp)
          )
        }

        KeypadDigit(digit = "0", onClick = onDigitClick)

        // Backspace
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(ObsidianSurface)
            .border(1.dp, ObsidianCardBorder, CircleShape)
            .clickable { onBackspaceClick() }
            .testTag("keypad_backspace"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            tint = Color.LightGray,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }
  }
}

@Composable
fun KeypadRow(digits: List<String>, onDigitClick: (String) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(0.85f),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    digits.forEach { digit ->
      KeypadDigit(digit = digit, onClick = onDigitClick)
    }
  }
}

@Composable
fun KeypadDigit(digit: String, onClick: (String) -> Unit) {
  Box(
    modifier = Modifier
      .testTag("keypad_$digit")
      .size(72.dp)
      .clip(CircleShape)
      .background(ObsidianSurface)
      .border(1.dp, ObsidianCardBorder, CircleShape)
      .clickable { onClick(digit) },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = digit,
      color = Color.White,
      fontSize = 24.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}
