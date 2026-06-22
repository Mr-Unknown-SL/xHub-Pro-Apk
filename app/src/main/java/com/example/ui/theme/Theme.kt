package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = PremiumDarkBackground,
    surface = PremiumDarkSurface,
    surfaceVariant = PremiumDarkSurfaceVariant,
    outline = PremiumDarkBorder,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    primaryContainer = Color(0xFF1F2B48),
    onPrimaryContainer = Color(0xFFEAF5FF)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF0077B6),
    secondary = Color(0xFF0096C7),
    tertiary = Color(0xFF03045E),
    background = Color(0xFFF4F7FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2EAF4),
    outline = Color(0xFFCDD6E2),
    onBackground = Color(0xFF0F141D),
    onSurface = Color(0xFF0F141D),
    onSurfaceVariant = Color(0xFF4C5E7A),
    primaryContainer = Color(0xFFE0EAFC),
    onPrimaryContainer = Color(0xFF005F9E)
  )

@Composable
fun MyApplicationTheme(
  themeMode: String = "system",
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
    "dark" -> true
    "light" -> false
    else -> isSystemInDarkTheme()
  }
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
