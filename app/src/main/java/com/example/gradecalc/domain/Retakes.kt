package com.example.gradecalc.domain

/** One attempt at a course. [key] identifies the course across semesters (its normalised name). */
data class CourseAttempt(val id: Long, val key: String, val entry: GradeEntry)

/**
 * Retake handling: when a course is taken again, only the latest graded attempt counts in the CGPA.
 * Each semester still shows its own GPA as it was at the time. An attempt that is not graded yet
 * does not replace the older grade.
 */
object Retakes {
    fun keyOf(name: String): String = name.trim().lowercase()

    /** Ids of the attempts that count. [semesters] must be ordered oldest to newest. */
    fun countedIds(semesters: List<List<CourseAttempt>>): Set<Long> {
        val latest = LinkedHashMap<String, Long>()
        for (semester in semesters) {
            for (attempt in semester) {
                if (attempt.entry.points != null && attempt.entry.creditHours > 0) {
                    latest[attempt.key] = attempt.id
                }
            }
        }
        return latest.values.toSet()
    }

    fun countedEntries(semesters: List<List<CourseAttempt>>): List<GradeEntry> {
        val ids = countedIds(semesters)
        return semesters.flatten().filter { it.id in ids }.map { it.entry }
    }

    /** CGPA after each semester, with retakes applied up to that point. */
    fun cumulativeSeries(semesters: List<List<CourseAttempt>>): List<Double?> =
        semesters.indices.map { k -> GpaCalculator.gpa(countedEntries(semesters.subList(0, k + 1))) }
}
