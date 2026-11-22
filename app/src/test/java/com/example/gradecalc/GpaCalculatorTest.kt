package com.example.gradecalc

import com.example.gradecalc.domain.GpaCalculator
import com.example.gradecalc.domain.GradeEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GpaCalculatorTest {

    private val delta = 1e-9

    @Test
    fun emptySemester_hasNoGpa() {
        assertNull(GpaCalculator.gpa(emptyList()))
    }

    @Test
    fun singleCourse_gpaEqualsItsPoints() {
        assertEquals(3.5, GpaCalculator.gpa(listOf(GradeEntry(3.5, 3.0)))!!, delta)
    }

    @Test
    fun mixedCredits_areWeighted() {
        // (4.0*3 + 2.0*5) / 8 = 2.75
        val entries = listOf(GradeEntry(4.0, 3.0), GradeEntry(2.0, 5.0))
        assertEquals(2.75, GpaCalculator.gpa(entries)!!, delta)
    }

    @Test
    fun failedCourse_countsAsZeroPoints() {
        // (4.0*3 + 0.0*3) / 6 = 2.0
        val entries = listOf(GradeEntry(4.0, 3.0), GradeEntry(0.0, 3.0))
        assertEquals(2.0, GpaCalculator.gpa(entries)!!, delta)
    }

    @Test
    fun ungradedCourse_isExcluded() {
        val entries = listOf(GradeEntry(3.0, 3.0), GradeEntry(null, 4.0))
        assertEquals(3.0, GpaCalculator.gpa(entries)!!, delta)
    }

    @Test
    fun zeroCreditCourse_isIgnored() {
        val entries = listOf(GradeEntry(3.0, 3.0), GradeEntry(4.0, 0.0))
        assertEquals(3.0, GpaCalculator.gpa(entries)!!, delta)
    }

    @Test
    fun cgpa_isWeightedAcrossSemesters_notAverageOfGpas() {
        val s1 = listOf(GradeEntry(4.0, 3.0))                          // GPA 4.0, 3 credits
        val s2 = listOf(GradeEntry(2.0, 9.0))                          // GPA 2.0, 9 credits
        // (12 + 18) / 12 = 2.5, not (4 + 2) / 2 = 3.0
        assertEquals(2.5, GpaCalculator.cgpa(listOf(s1, s2))!!, delta)
    }

    @Test
    fun requiredNextGpa_matchesHandCalculation() {
        // Currently 60 quality points over 20 credits (CGPA 3.0). Target 3.2 with 15 more credits:
        // (3.2 * 35 - 60) / 15 = 52 / 15 = 3.4666...
        val needed = GpaCalculator.requiredNextGpa(60.0, 20.0, 3.2, 15.0)!!
        assertEquals(52.0 / 15.0, needed, delta)
    }

    @Test
    fun requiredNextGpa_zeroNextCredits_isNull() {
        assertNull(GpaCalculator.requiredNextGpa(60.0, 20.0, 3.2, 0.0))
    }

    @Test
    fun formatting_roundsToTwoDecimals() {
        assertEquals("3.47", GpaCalculator.format(3.4666))
        assertEquals("-", GpaCalculator.format(null))
    }
}
