package io.cloink.client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = CloinkOrange,
    onPrimary = Color.White,
    secondary = CloinkGreen,
    onSecondary = Color.White,
    background = CanvasLight,
    onBackground = Ink,
    surface = SurfaceLight,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEBEEF0),
    onSurfaceVariant = Graphite,
    outlineVariant = DividerLight,
)

private val DarkColors = darkColorScheme(
    primary = CloinkOrangeDark,
    onPrimary = Color(0xFF321508),
    secondary = CloinkGreenDark,
    onSecondary = Color(0xFF05271C),
    background = CanvasDark,
    onBackground = Color(0xFFF0F2F4),
    surface = SurfaceDark,
    onSurface = Color(0xFFF0F2F4),
    surfaceVariant = Color(0xFF292D31),
    onSurfaceVariant = Color(0xFFB9C0C6),
    outlineVariant = DividerDark,
)

@Composable
fun CloinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CloinkTypography,
        content = content,
    )
}
