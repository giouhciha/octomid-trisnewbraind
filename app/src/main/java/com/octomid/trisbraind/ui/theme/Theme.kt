package com.octomid.trisbraind.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B6E4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBDD),
    onPrimaryContainer = Color(0xFF05271B),
    secondary = Color(0xFFB27B00),
    onSecondary = Color.White,
    background = Color(0xFFF6F7F4),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3DD68C),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF06513A),
    onPrimaryContainer = Color(0xFFB8F5D6),
    secondary = Color(0xFFF2C14E),
    onSecondary = Color(0xFF3A2A00),
    background = Color(0xFF101613),
    surface = Color(0xFF1A211D),
    onSurface = Color(0xFFE2E3DF)
)

@Composable
fun TrisBrainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
