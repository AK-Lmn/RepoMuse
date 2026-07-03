package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Slate900,
    secondary = AccentTeal,
    onSecondary = Slate900,
    tertiary = Slate400,
    background = Slate900,
    surface = Slate800,
    onBackground = Slate100,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate100
  )

private val LightColorScheme = DarkColorScheme // Enforce Dark Theme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Force our custom theme
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
