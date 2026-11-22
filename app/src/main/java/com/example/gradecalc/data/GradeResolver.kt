package com.example.gradecalc.data

/** Rounds a mark half up to a whole number, as the university does: 74.5 becomes 75, 74.4 stays 74. */
fun roundMark(score: Double): Int = Math.floor(score + 0.5).toInt()

/**
 * The scale band a mark out of 100 falls into. The mark is rounded half up to a whole number first,
 * then the highest band whose minimum it reaches is used.
 */
fun gradeForScore(score: Double, scale: List<GradeScaleEntry>): GradeScaleEntry? {
    val rounded = roundMark(score)
    return scale.filter { it.minPercent <= rounded }.maxByOrNull { it.minPercent }
}

/** The grade used for a course: from its mark when it has one, otherwise from its picked letter. */
fun resolveGrade(course: CourseEntity, scale: List<GradeScaleEntry>): GradeScaleEntry? =
    course.scorePercent?.let { gradeForScore(it, scale) }
        ?: course.gradeLetter?.let { letter -> scale.firstOrNull { it.letter == letter } }
