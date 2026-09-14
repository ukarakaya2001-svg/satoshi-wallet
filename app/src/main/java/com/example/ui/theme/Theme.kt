package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SatoshiDarkColorScheme = darkColorScheme(
  primary = BitcoinGold,
  onPrimary = Color.Black,
  primaryContainer = BitcoinGoldDark,
  onPrimaryContainer = Color.White,
  secondary = LightningCyan,
  onSecondary = Color.Black,
  secondaryContainer = LightningCyanDark,
  onSecondaryContainer = Color.White,
  tertiary = StatusSuccess,
  onTertiary = Color.Black,
  background = ObsidianBg,
  onBackground = TextPrimary,
  surface = ObsidianSurface,
  onSurface = TextPrimary,
  surfaceVariant = ObsidianSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = ObsidianCardBorder
)

private val SatoshiLightColorScheme = lightColorScheme(
  primary = BitcoinGoldDark,
  onPrimary = Color.White,
  secondary = LightningCyanDark,
  onSecondary = Color.White,
  tertiary = StatusSuccess,
  onTertiary = Color.White,
  background = Color(0xFFF6F8FA),
  onBackground = Color(0xFF1F2328),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF1F2328),
  surfaceVariant = Color(0xFFEAEFF5),
  onSurfaceVariant = Color(0xFF656D76),
  outline = Color(0xFFD0D7DE)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek obsidian dark theme for crypto wallet
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) SatoshiDarkColorScheme else SatoshiLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
