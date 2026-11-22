package com.example.gradecalc.data

/**
 * Default Ethiopian university 4.0 scale. Schools differ, so every value is editable in Settings.
 * Confirm these values against your own university's grading policy.
 */
val DEFAULT_GRADE_SCALE: List<GradeScaleEntry> = listOf(
    GradeScaleEntry("A+", 4.00, 90, 100, 0),
    GradeScaleEntry("A", 4.00, 85, 89, 1),
    GradeScaleEntry("A-", 3.75, 80, 84, 2),
    GradeScaleEntry("B+", 3.50, 75, 79, 3),
    GradeScaleEntry("B", 3.00, 70, 74, 4),
    GradeScaleEntry("B-", 2.75, 65, 69, 5),
    GradeScaleEntry("C+", 2.50, 60, 64, 6),
    GradeScaleEntry("C", 2.00, 50, 59, 7),
    GradeScaleEntry("C-", 1.75, 45, 49, 8),
    GradeScaleEntry("D", 1.00, 40, 44, 9),
    GradeScaleEntry("F", 0.00, 0, 39, 10),
)
