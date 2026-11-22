package com.example.gradecalc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Background and text color for a grade badge: A green, B blue, C amber, D orange, F red. */
@Composable
fun gradeColors(letter: String?): Pair<Color, Color> {
    val dark = isSystemInDarkTheme()
    return when (letter?.firstOrNull()) {
        'A' -> if (dark) Color(0xFF1F4D2B) to Color(0xFFA8E6B4) else Color(0xFFE0F4E4) to Color(0xFF1B5E20)
        'B' -> if (dark) Color(0xFF24408F) to Color(0xFFBFD3FF) else Color(0xFFDCE8FF) to Color(0xFF1E40AF)
        'C' -> if (dark) Color(0xFF5A4300) to Color(0xFFFFE08A) else Color(0xFFFFF1CC) to Color(0xFF7A4B00)
        'D', 'E' -> if (dark) Color(0xFF6B3300) to Color(0xFFFFC9A3) else Color(0xFFFFE1CC) to Color(0xFF8A3B00)
        'F' -> if (dark) Color(0xFF6E1F1F) to Color(0xFFFFB4AB) else Color(0xFFFADBD8) to Color(0xFF9B1C1C)
        else -> if (dark) Color(0xFF262C40) to Color(0xFFB9BFD1) else Color(0xFFE9ECF4) to Color(0xFF444B5E)
    }
}
