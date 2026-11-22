package com.example.gradecalc

import com.example.gradecalc.domain.CourseAttempt
import com.example.gradecalc.domain.GradeEntry
import com.example.gradecalc.domain.Retakes
import org.junit.Assert.assertEquals
import org.junit.Test

class RetakesTest {
    private val delta = 1e-9

    private fun attempt(id: Long, name: String, points: Double?, credits: Double = 3.0) =
        CourseAttempt(id, Retakes.keyOf(name), GradeEntry(points, credits))

    @Test fun keyOf_ignoresCaseAndSpaces() =
        assertEquals(Retakes.keyOf("Calculus I"), Retakes.keyOf("  calculus i "))

    @Test fun retake_replacesOlderGrade() {
        val sem1 = listOf(attempt(1, "Math", 2.0))
        val sem2 = listOf(attempt(2, "math", 4.0))
        assertEquals(setOf(2L), Retakes.countedIds(listOf(sem1, sem2)))
    }

    @Test fun retake_withWorseGrade_stillReplaces() {
        val sem1 = listOf(attempt(1, "Math", 3.0))
        val sem2 = listOf(attempt(2, "Math", 1.0))
        assertEquals(setOf(2L), Retakes.countedIds(listOf(sem1, sem2)))
    }

    @Test fun ungradedRetake_doesNotReplace() {
        val sem1 = listOf(attempt(1, "Math", 2.0))
        val sem2 = listOf(attempt(2, "Math", null))
        assertEquals(setOf(1L), Retakes.countedIds(listOf(sem1, sem2)))
    }

    @Test fun differentCourses_bothCount() {
        val sem1 = listOf(attempt(1, "Math", 2.0), attempt(2, "Physics", 3.0))
        assertEquals(setOf(1L, 2L), Retakes.countedIds(listOf(sem1)))
    }

    @Test fun cgpa_usesOnlyLatestAttempt() {
        val sem1 = listOf(attempt(1, "Math", 2.0))
        val sem2 = listOf(attempt(2, "Math", 4.0))
        val entries = Retakes.countedEntries(listOf(sem1, sem2))
        assertEquals(4.0, com.example.gradecalc.domain.GpaCalculator.gpa(entries)!!, delta)
    }

    @Test fun cumulativeSeries_appliesRetakesStepByStep() {
        val sem1 = listOf(attempt(1, "Math", 2.0))
        val sem2 = listOf(attempt(2, "Math", 4.0), attempt(3, "Physics", 3.0))
        val series = Retakes.cumulativeSeries(listOf(sem1, sem2))
        assertEquals(2.0, series[0]!!, delta)
        // after semester 2: Math retake (4.0 x 3) + Physics (3.0 x 3) = 21 / 6 = 3.5
        assertEquals(3.5, series[1]!!, delta)
    }
}
