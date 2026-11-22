package com.example.gradecalc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Palette taken from the university portal: dark green bar, light green accents, grey striped rows,
// blue for headings and links.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1F7A4D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFF0D8),
    onPrimaryContainer = Color(0xFF0E3B25),
    secondary = Color(0xFF3A7BC8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE8FA),
    onSecondaryContainer = Color(0xFF12305C),
    background = Color(0xFFF6F8F6),
    onBackground = Color(0xFF1B1F1C),
    surface = Color.White,
    onSurface = Color(0xFF1B1F1C),
    surfaceVariant = Color(0xFFEFF1EF),
    onSurfaceVariant = Color(0xFF444B46),
    outline = Color(0xFFC5CCC7),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD6AE),
    onPrimary = Color(0xFF0E3B25),
    primaryContainer = Color(0xFF1E5A3C),
    onPrimaryContainer = Color(0xFFDFF0D8),
    secondary = Color(0xFF9CC2F5),
    onSecondary = Color(0xFF12305C),
    secondaryContainer = Color(0xFF284A7B),
    onSecondaryContainer = Color(0xFFDCE8FA),
    background = Color(0xFF0F1512),
    onBackground = Color(0xFFE2E7E3),
    surface = Color(0xFF18201C),
    onSurface = Color(0xFFE2E7E3),
    surfaceVariant = Color(0xFF252E29),
    onSurfaceVariant = Color(0xFFB8C0BA),
    outline = Color(0xFF45504A),
    error = Color(0xFFF2B8B5),
)

private val AppTypography = Typography().let { base ->
    base.copy(
        displayMedium = base.displayMedium.copy(fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun GradeCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
