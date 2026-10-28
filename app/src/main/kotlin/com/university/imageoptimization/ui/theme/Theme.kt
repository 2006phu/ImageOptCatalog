package com.university.imageoptimization.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryBlue = Color(0xFF1565C0)
val SecondaryTeal = Color(0xFF00897B)
val BaselineOrange = Color(0xFFD84315)
val OptimizedGreen = Color(0xFF2E7D32)
val SurfaceDarkCard = Color(0xFF1E293B)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryTeal,
    tertiary = OptimizedGreen,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    secondary = Color(0xFF4DB6AC),
    tertiary = Color(0xFF81C784)
)

@Composable
fun ImageOptCatalogTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
