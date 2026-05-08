package com.guxiaopan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFB71C1C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFCDD2),
    onPrimaryContainer = Color(0xFF3E0016),
    secondary = Color(0xFF6B5B00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF9C4),
    onSecondaryContainer = Color(0xFF221B00),
    tertiary = Color(0xFF006B5B),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1A1A1A),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

@Composable
fun GuXiaoPanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
