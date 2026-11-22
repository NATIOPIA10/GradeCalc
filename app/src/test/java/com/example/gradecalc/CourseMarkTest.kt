package com.example.gradecalc

import com.example.gradecalc.domain.Assessment
import com.example.gradecalc.domain.CourseMark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CourseMarkTest {
    private val delta = 1e-9

    @Test fun allAssessmentsDone_addsUpToTotalMark() {
        val items = listOf(Assessment(8.0, 10.0), Assessment(15.0, 20.0), Assessment(40.0, 70.0))
        assertEquals(63.0, CourseMark.percent(items)!!, delta)
    }

    @Test fun pendingAssessment_isLeftOut() {
        val items = listOf(Assessment(8.0, 10.0), Assessment(15.0, 20.0), Assessment(null, 70.0))
        // 23 of 30 completed points
        assertEquals(23.0 / 30.0 * 100.0, CourseMark.percent(items)!!, delta)
    }

    @Test fun nothingDone_isNull() = assertNull(CourseMark.percent(listOf(Assessment(null, 30.0))))

    @Test fun emptyList_isNull() = assertNull(CourseMark.percent(emptyList()))

    @Test fun earnedAboveMax_isIgnored() {
        val items = listOf(Assessment(35.0, 30.0), Assessment(10.0, 20.0))
        assertEquals(50.0, CourseMark.percent(items)!!, delta)
    }

    @Test fun zeroMax_isIgnored() {
        val items = listOf(Assessment(5.0, 0.0), Assessment(10.0, 20.0))
        assertEquals(50.0, CourseMark.percent(items)!!, delta)
    }
}
