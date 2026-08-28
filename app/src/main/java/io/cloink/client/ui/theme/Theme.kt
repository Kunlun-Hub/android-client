package io.cloink.client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
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
    darkTheme: Boolean = when (ThemeRuntime.mode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    SideEffect {
        var current = context
        while (current is ContextWrapper && current !is Activity) current = current.baseContext
        (current as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CloinkTypography,
        content = content,
    )
}
