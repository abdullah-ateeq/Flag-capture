package com.example.flagcapture.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.flagcapture.model.AppThemeMode

private val TacticalDarkColorScheme = darkColorScheme(
    primary = Player1Primary,
    onPrimary = TextPrimaryDark,
    primaryContainer = Player1Container,
    onPrimaryContainer = Player1Light,
    secondary = Player2Primary,
    onSecondary = TextPrimaryDark,
    secondaryContainer = Player2Container,
    onSecondaryContainer = Player2Light,
    tertiary = FlagGold,
    onTertiary = CharcoalDark,
    tertiaryContainer = FlagGoldContainer,
    onTertiaryContainer = FlagGoldBright,
    background = CharcoalDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = BorderDark
)

private val TacticalLightColorScheme = lightColorScheme(
    primary = Player1Primary,
    onPrimary = TextPrimaryDark,
    primaryContainer = Color(0xFFFFDCD4),
    onPrimaryContainer = Player1Dark,
    secondary = Player2Primary,
    onSecondary = TextPrimaryDark,
    secondaryContainer = Color(0xFFD2EEFA),
    onSecondaryContainer = Player2Dark,
    tertiary = FlagGold,
    onTertiary = CharcoalDark,
    tertiaryContainer = Color(0xFFFFF2D0),
    onTertiaryContainer = FlagGoldDark,
    background = SlateBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = BorderLight
)

@Composable
fun FlagCaptureTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) TacticalDarkColorScheme else TacticalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}