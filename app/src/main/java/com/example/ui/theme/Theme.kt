package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val LocalReduceMotion = compositionLocalOf { false }
val LocalHighContrast = compositionLocalOf { false }

private val ScoutLightColorScheme = lightColorScheme(
    primary = ScoutPrimaryAction,
    onPrimary = ScoutSurface,
    primaryContainer = ScoutSurfaceVariant,
    onPrimaryContainer = ScoutTextPrimary,
    secondary = ScoutTextSecondary,
    onSecondary = ScoutSurface,
    background = ScoutBackground,
    onBackground = ScoutTextPrimary,
    surface = ScoutSurface,
    onSurface = ScoutTextPrimary,
    surfaceVariant = ScoutSurfaceVariant,
    onSurfaceVariant = ScoutTextSecondary,
    outline = ScoutDivider,
    error = ScoutErrorDanger,
    onError = ScoutSurface
)

private val HighContrastColorScheme = lightColorScheme(
    primary = Color(0xFF0B7EB8),
    onPrimary = Color.White,
    primaryContainer = Color.White,
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color.Black,
    outline = Color.Black,
    error = Color(0xFFC53030),
    onError = Color.White
)

@Composable
fun RwandaScoutTheme(
    reduceMotion: Boolean = false,
    highContrast: Boolean = false,
    largerText: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (highContrast) HighContrastColorScheme else ScoutLightColorScheme
    val currentDensity = LocalDensity.current
    val density = remember(currentDensity, largerText) {
        Density(
            density = currentDensity.density,
            fontScale = if (largerText) currentDensity.fontScale * 1.20f else currentDensity.fontScale
        )
    }

    CompositionLocalProvider(
        LocalDensity provides density,
        LocalReduceMotion provides reduceMotion,
        LocalHighContrast provides highContrast
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
