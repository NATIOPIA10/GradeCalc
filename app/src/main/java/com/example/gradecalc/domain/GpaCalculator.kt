package com.example.gradecalc.domain

import kotlin.math.round

/** One graded course. [points] is null when the course has no grade yet. */
data class GradeEntry(val points: Double?, val creditHours: Double)

object GpaCalculator {

    /** Sum of points x credit hours over graded courses with credits > 0. */
    fun qualityPoints(entries: List<GradeEntry>): Double =
        entries.sumOf { e -> if (e.points != null && e.creditHours > 0) e.points * e.creditHours else 0.0 }

    /** Total credit hours counted in the GPA (graded courses only). */
    fun gradedCredits(entries: List<GradeEntry>): Double =
        entries.sumOf { e -> if (e.points != null && e.creditHours > 0) e.creditHours else 0.0 }

    /** GPA for a list of courses, or null when nothing is graded yet. */
    fun gpa(entries: List<GradeEntry>): Double? {
        val credits = gradedCredits(entries)
        if (credits <= 0.0) return null
        return qualityPoints(entries) / credits
    }

    /** CGPA across all semesters (same formula over every course). */
    fun cgpa(semesters: List<List<GradeEntry>>): Double? = gpa(semesters.flatten())

    /**
     * GPA needed in the next semester to reach [targetCgpa].
     * The result can be above the maximum scale value, which means the target is unreachable.
     * Returns null when [nextCredits] is not positive.
     */
    fun requiredNextGpa(
        currentQualityPoints: Double,
        currentCredits: Double,
        targetCgpa: Double,
        nextCredits: Double,
    ): Double? {
        if (nextCredits <= 0.0) return null
        return (targetCgpa * (currentCredits + nextCredits) - currentQualityPoints) / nextCredits
    }

    fun round2(value: Double): Double = round(value * 100.0) / 100.0

    fun format(value: Double?): String = if (value == null) "-" else String.format("%.2f", round2(value))
}
