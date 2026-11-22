package com.example.gradecalc

import com.example.gradecalc.data.CourseEntity
import com.example.gradecalc.data.DEFAULT_GRADE_SCALE
import com.example.gradecalc.data.gradeForScore
import com.example.gradecalc.data.resolveGrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradeResolverTest {
    private val scale = DEFAULT_GRADE_SCALE

    private fun letterFor(score: Double) = gradeForScore(score, scale)?.letter

    @Test fun topMark_isAPlus() = assertEquals("A+", letterFor(100.0))
    @Test fun boundary_90_isAPlus() = assertEquals("A+", letterFor(90.0))
    @Test fun pointFive_roundsUpIntoHigherBand() = assertEquals("A+", letterFor(89.5))
    @Test fun below_pointFive_roundsDown() = assertEquals("A", letterFor(89.4))
    @Test fun mark74_isB() = assertEquals("B", letterFor(74.0))
    @Test fun mark74_point4_isB() = assertEquals("B", letterFor(74.4))
    @Test fun mark74_point5_isBPlus() = assertEquals("B+", letterFor(74.5))
    @Test fun mark39_point5_isD() = assertEquals("D", letterFor(39.5))
    @Test fun boundary_85_isA() = assertEquals("A", letterFor(85.0))
    @Test fun below84_point5_isAMinus() = assertEquals("A-", letterFor(84.4))
    @Test fun mark84_point9_roundsUpToA() = assertEquals("A", letterFor(84.9))
    @Test fun midRange_isCPlus() = assertEquals("C+", letterFor(62.0))
    @Test fun lowestPass_isD() = assertEquals("D", letterFor(40.0))
    @Test fun below40_isF() = assertEquals("F", letterFor(39.0))
    @Test fun zero_isF() = assertEquals("F", letterFor(0.0))

    private fun course(letter: String?, score: Double?) =
        CourseEntity(id = 1, semesterId = 1, name = "X", creditHours = 3.0, gradeLetter = letter, scorePercent = score)

    @Test fun mark_winsOverLetter() =
        assertEquals("B+", resolveGrade(course("A", 77.0), scale)?.letter)

    @Test fun letterOnly_isUsedWhenNoMark() =
        assertEquals("B", resolveGrade(course("B", null), scale)?.letter)

    @Test fun neither_isUngraded() = assertNull(resolveGrade(course(null, null), scale))
}
