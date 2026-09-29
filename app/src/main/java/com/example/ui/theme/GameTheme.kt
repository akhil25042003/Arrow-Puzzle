package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class GameThemePalette(
    val name: String,
    val backgroundBrush: Brush,
    val surfaceColor: Color,
    val boardBackground: Color,
    val boardBorder: Color,
    val cellEmptyBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentPrimary: Color,
    val arrowColors: List<Color>,
    val obstacleColor: Color,
    val obstaclePatternColor: Color,
    val rotatorBadgeColor: Color,
    val isDark: Boolean = true
)

val NeonNightPalette = GameThemePalette(
    name = "Neon Cyber",
    backgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFF140F2D), Color(0xFF0A0718))
    ),
    surfaceColor = Color(0xFF1C163D),
    boardBackground = Color(0xFF161131),
    boardBorder = Color(0xFF332768),
    cellEmptyBackground = Color(0x15FFFFFF),
    textPrimary = Color(0xFFF0EDFD),
    textSecondary = Color(0xFF9E95C7),
    accentPrimary = Color(0xFF00E5FF),
    arrowColors = listOf(
        Color(0xFF00E5FF), // Neon Cyan
        Color(0xFFFF2A6D), // Laser Pink
        Color(0xFFFFD600), // Electric Amber
        Color(0xFFB388FF)  // Ultraviolet
    ),
    obstacleColor = Color(0xFF282C40),
    obstaclePatternColor = Color(0xFF3F4460),
    rotatorBadgeColor = Color(0xFFFF9100),
    isDark = true
)

val ZenMinimalPalette = GameThemePalette(
    name = "Zen Minimal",
    backgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFFF9F7F2), Color(0xFFEFECE4))
    ),
    surfaceColor = Color(0xFFFFFFFF),
    boardBackground = Color(0xFFE8E4DA),
    boardBorder = Color(0xFFD3CDBC),
    cellEmptyBackground = Color(0x20000000),
    textPrimary = Color(0xFF2C2A29),
    textSecondary = Color(0xFF75706B),
    accentPrimary = Color(0xFFE65100),
    arrowColors = listOf(
        Color(0xFFD84315), // Deep Rust
        Color(0xFF2E7D32), // Forest Sage
        Color(0xFF1565C0), // Ink Slate
        Color(0xFFF57F17)  // Warm Ochre
    ),
    obstacleColor = Color(0xFF8D8378),
    obstaclePatternColor = Color(0xFFAAA095),
    rotatorBadgeColor = Color(0xFF6D4C41),
    isDark = false
)

val NordicGlacierPalette = GameThemePalette(
    name = "Nordic Frost",
    backgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFF0B192C), Color(0xFF050D18))
    ),
    surfaceColor = Color(0xFF142B47),
    boardBackground = Color(0xFF0F223A),
    boardBorder = Color(0xFF1E3E62),
    cellEmptyBackground = Color(0x18FFFFFF),
    textPrimary = Color(0xFFE0F2FE),
    textSecondary = Color(0xFF7DD3FC),
    accentPrimary = Color(0xFF38BDF8),
    arrowColors = listOf(
        Color(0xFF38BDF8), // Sky Ice
        Color(0xFF34D399), // Mint Aurora
        Color(0xFF818CF8), // Periwinkle
        Color(0xFFF472B6)  // Arctic Rose
    ),
    obstacleColor = Color(0xFF1F3552),
    obstaclePatternColor = Color(0xFF314F75),
    rotatorBadgeColor = Color(0xFF0284C7),
    isDark = true
)

val SunsetGlowPalette = GameThemePalette(
    name = "Sunset Glow",
    backgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFF25112E), Color(0xFF120718))
    ),
    surfaceColor = Color(0xFF361942),
    boardBackground = Color(0xFF2A1334),
    boardBorder = Color(0xFF502463),
    cellEmptyBackground = Color(0x20FFFFFF),
    textPrimary = Color(0xFFFFF0F5),
    textSecondary = Color(0xFFD8A8D8),
    accentPrimary = Color(0xFFFF6D00),
    arrowColors = listOf(
        Color(0xFFFF5722), // Deep Sunset
        Color(0xFFFFB300), // Golden Hour
        Color(0xFFE91E63), // Twilight Berry
        Color(0xFFAB47BC)  // Violet Dusk
    ),
    obstacleColor = Color(0xFF4A255B),
    obstaclePatternColor = Color(0xFF64367A),
    rotatorBadgeColor = Color(0xFFFF8F00),
    isDark = true
)

val AvailablePalettes = listOf(
    NeonNightPalette,
    ZenMinimalPalette,
    NordicGlacierPalette,
    SunsetGlowPalette
)

val LocalGamePalette = staticCompositionLocalOf { NeonNightPalette }

@Composable
fun getGamePalette(index: Int): GameThemePalette {
    return AvailablePalettes.getOrElse(index) { NeonNightPalette }
}
