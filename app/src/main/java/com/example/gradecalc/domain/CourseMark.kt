package com.example.gradecalc.domain

/** One assessment: the mark earned (null if not done yet) and the most it can give (its weight). */
data class Assessment(val earned: Double?, val max: Double)

/**
 * Course mark from assessments. Example: Assignment 8/10, Mid exam 15/20, Final 40/70 gives
 * 63 of 100 points, a mark of 63%. Assessments not done yet are left out, so the result is the
 * mark so far, based on the work completed.
 */
object CourseMark {
    private fun counts(a: Assessment): Boolean = a.earned != null && a.max > 0.0 && a.earned in 0.0..a.max

    fun earnedSum(items: List<Assessment>): Double = items.filter { counts(it) }.sumOf { it.earned ?: 0.0 }

    fun maxSum(items: List<Assessment>): Double = items.filter { counts(it) }.sumOf { it.max }

    /** Mark out of 100 from the completed assessments, or null when none is complete. */
    fun percent(items: List<Assessment>): Double? {
        val max = maxSum(items)
        return if (max <= 0.0) null else earnedSum(items) / max * 100.0
    }
}
