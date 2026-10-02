package de.autocheck.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AutoCheckColors = darkColorScheme(
    primary = Color(0xFFE21D32),
    secondary = Color(0xFF7D8794),
    background = Color(0xFF050608),
    surface = Color(0xFF11141A),
    surfaceVariant = Color(0xFF1A1E26),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun AutoCheckTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AutoCheckColors,
        content = content
    )
}
