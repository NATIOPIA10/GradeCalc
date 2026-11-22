package com.example.gradecalc.data

import androidx.annotation.StringRes
import com.example.gradecalc.R

/**
 * Ready-made grade scales. These are common scales, not official policies of any one university,
 * so students should check them against their own university and edit the points if needed.
 */
enum class ScalePreset(@StringRes val labelRes: Int) {
    ETHIOPIA(R.string.preset_ethiopia),
    US_PLUS_MINUS(R.string.preset_us_pm),
    US_SIMPLE(R.string.preset_us_simple),
    FIVE_POINT(R.string.preset_five),
}

private fun row(letter: String, points: Double, min: Int, max: Int, order: Int) =
    GradeScaleEntry(letter, points, min, max, order)

fun scaleFor(preset: ScalePreset): List<GradeScaleEntry> = when (preset) {
    ScalePreset.ETHIOPIA -> DEFAULT_GRADE_SCALE
    ScalePreset.US_PLUS_MINUS -> listOf(
        row("A", 4.0, 93, 100, 0),
        row("A-", 3.7, 90, 92, 1),
        row("B+", 3.3, 87, 89, 2),
        row("B", 3.0, 83, 86, 3),
        row("B-", 2.7, 80, 82, 4),
        row("C+", 2.3, 77, 79, 5),
        row("C", 2.0, 73, 76, 6),
        row("C-", 1.7, 70, 72, 7),
        row("D+", 1.3, 67, 69, 8),
        row("D", 1.0, 60, 66, 9),
        row("F", 0.0, 0, 59, 10),
    )
    ScalePreset.US_SIMPLE -> listOf(
        row("A", 4.0, 90, 100, 0),
        row("B", 3.0, 80, 89, 1),
        row("C", 2.0, 70, 79, 2),
        row("D", 1.0, 60, 69, 3),
        row("F", 0.0, 0, 59, 4),
    )
    ScalePreset.FIVE_POINT -> listOf(
        row("A", 5.0, 70, 100, 0),
        row("B", 4.0, 60, 69, 1),
        row("C", 3.0, 50, 59, 2),
        row("D", 2.0, 45, 49, 3),
        row("E", 1.0, 40, 44, 4),
        row("F", 0.0, 0, 39, 5),
    )
}
