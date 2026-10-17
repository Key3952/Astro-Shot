package com.example.astro.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Deep Space Color Palette
val SpaceBlack = Color(0xFF070A13)
val SpaceCardBg = Color(0xFF0F1524)
val SpaceCardBorder = Color(0xFF1E283D)
val SpaceSurface = Color(0xFF141C30)
val ElectricCyan = Color(0xFF38BDF8)
val NeonCyanVariant = Color(0xFF0284C7)
val GoldenStar = Color(0xFFFBBF24)
val AuroraGreen = Color(0xFF34D399)
val CosmicPurple = Color(0xFFA855F7)
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Red Night-Vision Color Palette (Protects dark visual adaptation)
val RedSpaceBlack = Color(0xFF000000)
val RedCardBg = Color(0xFF160202)
val RedCardBorder = Color(0xFF3B0808)
val RedSurface = Color(0xFF220404)
val PureRed = Color(0xFFFF2E2E)
val DeepRed = Color(0xFFB91C1C)
val SoftRed = Color(0xFFEF4444)
val TextRedPrimary = Color(0xFFFF8585)
val TextRedSecondary = Color(0xFFCC5555)
val TextRedMuted = Color(0xFF882222)

data class AstroPalette(
    val isRedMode: Boolean,
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val auroraGlow: Color,
    val warning: Color
)

val NormalAstroPalette = AstroPalette(
    isRedMode = false,
    background = SpaceBlack,
    surface = SpaceSurface,
    cardBg = SpaceCardBg,
    cardBorder = SpaceCardBorder,
    primary = ElectricCyan,
    secondary = CosmicPurple,
    accent = GoldenStar,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    auroraGlow = AuroraGreen,
    warning = Color(0xFFFB923C)
)

val RedNightVisionPalette = AstroPalette(
    isRedMode = true,
    background = RedSpaceBlack,
    surface = RedSurface,
    cardBg = RedCardBg,
    cardBorder = RedCardBorder,
    primary = PureRed,
    secondary = DeepRed,
    accent = PureRed,
    textPrimary = TextRedPrimary,
    textSecondary = TextRedSecondary,
    textMuted = TextRedMuted,
    auroraGlow = PureRed,
    warning = SoftRed
)

val LocalAstroPalette = staticCompositionLocalOf { NormalAstroPalette }

private val NormalColorScheme: ColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.Black,
    secondary = CosmicPurple,
    onSecondary = Color.White,
    tertiary = GoldenStar,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = SpaceCardBg,
    onSurface = TextPrimary,
    surfaceVariant = SpaceSurface,
    onSurfaceVariant = TextSecondary,
    outline = SpaceCardBorder
)

private val RedColorScheme: ColorScheme = darkColorScheme(
    primary = PureRed,
    onPrimary = Color.Black,
    secondary = DeepRed,
    onSecondary = Color.Black,
    tertiary = PureRed,
    background = RedSpaceBlack,
    onBackground = TextRedPrimary,
    surface = RedCardBg,
    onSurface = TextRedPrimary,
    surfaceVariant = RedSurface,
    onSurfaceVariant = TextRedSecondary,
    outline = RedCardBorder
)

@Composable
fun AstroAppTheme(
    isRedNightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = if (isRedNightMode) RedNightVisionPalette else NormalAstroPalette
    val colorScheme = if (isRedNightMode) RedColorScheme else NormalColorScheme

    CompositionLocalProvider(LocalAstroPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = com.example.ui.theme.Typography,
            content = content
        )
    }
}
