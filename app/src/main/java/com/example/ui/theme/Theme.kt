package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
  primary = SanctuaryBurgundy,
  onPrimary = Color.White,
  primaryContainer = SanctuaryRoseTint,
  onPrimaryContainer = SanctuaryBurgundyDark,
  secondary = LiturgicalGold,
  onSecondary = Color.White,
  secondaryContainer = LiturgicalGoldCream,
  onSecondaryContainer = Color(0xFF3E2900),
  tertiary = SanctuaryOlive,
  onTertiary = Color.White,
  tertiaryContainer = SanctuaryOliveTint,
  onTertiaryContainer = Color(0xFF0E2617),
  background = SanctuaryCream,
  onBackground = SanctuaryEspresso,
  surface = SanctuaryCream,
  onSurface = SanctuaryEspresso,
  surfaceVariant = SanctuaryWarmSurface,
  onSurfaceVariant = SanctuaryUmber,
  surfaceContainerLowest = SanctuaryIvory,
  surfaceContainerLow = SanctuaryIvory,
  surfaceContainer = SanctuaryWarmSurface,
  surfaceContainerHigh = SanctuaryWarmSurfaceHigh,
  outline = SanctuaryLinenOutline,
  outlineVariant = SanctuaryLinenSubtle,
  error = SanctuaryError,
  onError = Color.White,
  errorContainer = SanctuaryErrorContainer,
  onErrorContainer = Color(0xFF410E0B),
)

private val DarkColorScheme = darkColorScheme(
  primary = SanctuaryBurgundyLight,
  onPrimary = Color(0xFF4D0E1B),
  primaryContainer = SanctuaryBurgundy,
  onPrimaryContainer = Color(0xFFFFD9DF),
  secondary = LiturgicalGoldLight,
  onSecondary = Color(0xFF3D2A00),
  secondaryContainer = Color(0xFF5A4009),
  onSecondaryContainer = Color(0xFFFFE4A1),
  tertiary = Color(0xFF9ACFA8),
  onTertiary = Color(0xFF0E2B18),
  tertiaryContainer = Color(0xFF254D32),
  onTertiaryContainer = Color(0xFFCDEBD4),
  background = DarkSanctuaryBg,
  onBackground = DarkSanctuaryOnSurface,
  surface = DarkSanctuaryBg,
  onSurface = DarkSanctuaryOnSurface,
  surfaceVariant = DarkSanctuarySurface,
  onSurfaceVariant = DarkSanctuaryMuted,
  surfaceContainerLowest = Color(0xFF150E0E),
  surfaceContainerLow = DarkSanctuarySurface,
  surfaceContainer = DarkSanctuarySurface,
  surfaceContainerHigh = DarkSanctuarySurfaceHigh,
  outline = DarkSanctuaryOutline,
  outlineVariant = Color(0xFF443633),
)

private val SanctuaryShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun StAnthonysTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = SanctuaryShapes,
    content = content,
  )
}
