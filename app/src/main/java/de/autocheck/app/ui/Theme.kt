package de.autocheck.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AutoCheckColors = darkColorScheme(
    // Warmes, elegantes Gold
    primary = Color(0xFFD4AF37),

    // Dezente Nebenfarbe
    secondary = Color(0xFF8C7A45),

    // Dunkler Hintergrund
    background = Color(0xFF050608),

    // Karten / Flächen
    surface = Color(0xFF11141A),

    // Alternative Flächenfarbe
    surfaceVariant = Color(0xFF1A1E26),

    // Helle Schrift
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun AutoCheckTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AutoCheckColors,
        content = content
    )
}
