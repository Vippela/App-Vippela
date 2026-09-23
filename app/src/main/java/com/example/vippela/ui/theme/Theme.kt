package com.example.vippela.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class VippelaPalette(
    val background: Color,
    val card: Color,
    val cardElevated: Color,
    val onBackground: Color,
    val onCard: Color,
    val muted: Color,
    val accent: Color,
    val accentSoft: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val surfaceMuted: Color,
    val track: Color,
    val divider: Color,
    val chip: Color,
    val field: Color,
    val step: Color,
    val primaryButton: Color,
    val onPrimaryButton: Color,
    val isDark: Boolean,
)

private val LightPalette = VippelaPalette(
    background = VippelaColors.LightBackground,
    card = VippelaColors.LightCard,
    cardElevated = VippelaColors.LightCardElevated,
    onBackground = VippelaColors.Purple,
    onCard = VippelaColors.Purple,
    muted = VippelaColors.LightMuted,
    accent = VippelaColors.Accent,
    accentSoft = VippelaColors.PurpleSoft,
    success = VippelaColors.Green,
    danger = VippelaColors.Danger,
    warning = VippelaColors.Warning,
    surfaceMuted = VippelaColors.LightSurfaceMuted,
    track = VippelaColors.LightTrack,
    divider = VippelaColors.LightDivider,
    chip = VippelaColors.LightChip,
    field = VippelaColors.LightField,
    step = VippelaColors.LightStep,
    primaryButton = VippelaColors.Purple,
    onPrimaryButton = Color.White,
    isDark = false,
)

private val DarkPalette = VippelaPalette(
    background = VippelaColors.DarkBackground,
    card = VippelaColors.DarkCard,
    cardElevated = VippelaColors.DarkCardElevated,
    onBackground = Color(0xFFEDEAF8),
    onCard = Color(0xFFEDEAF8),
    muted = VippelaColors.DarkMuted,
    accent = VippelaColors.Accent,
    accentSoft = VippelaColors.DarkPurple,
    success = VippelaColors.Green,
    danger = Color(0xFFFF6B6B),
    warning = VippelaColors.Warning,
    surfaceMuted = VippelaColors.DarkSurfaceMuted,
    track = VippelaColors.DarkTrack,
    divider = VippelaColors.DarkDivider,
    chip = VippelaColors.DarkChip,
    field = VippelaColors.DarkField,
    step = VippelaColors.DarkStep,
    primaryButton = Color(0xFF7B63F0),
    onPrimaryButton = Color.White,
    isDark = true,
)

val LocalVippelaPalette = staticCompositionLocalOf { LightPalette }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB9A6F0),
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkPalette.background,
    surface = DarkPalette.card,
    onBackground = DarkPalette.onBackground,
    onSurface = DarkPalette.onCard,
)

private val LightColorScheme = lightColorScheme(
    primary = VippelaColors.Purple,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = LightPalette.background,
    surface = LightPalette.card,
    onBackground = LightPalette.onBackground,
    onSurface = LightPalette.onCard,
)

@Composable
fun VippelaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val palette = if (darkTheme) DarkPalette else LightPalette

    CompositionLocalProvider(LocalVippelaPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
