package com.example.gradecalc

import com.example.gradecalc.domain.SemesterLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SemesterLabelsTest {
    @Test fun label_matchesPortalStyle() =
        assertEquals("Year III, Semester Two", SemesterLabels.label(3, 2))

    @Test fun label_firstSemester() =
        assertEquals("Year I, Semester One", SemesterLabels.label(1, 1))

    @Test fun short_usesRomanYearAndNumber() =
        assertEquals("III.2", SemesterLabels.short(3, 2))

    @Test fun short_legacySemester_isHash() =
        assertEquals("#", SemesterLabels.short(0, 0))

    @Test fun nextFree_startsAtYearOneSemesterOne() =
        assertEquals(1 to 1, SemesterLabels.nextFree(emptySet()))

    @Test fun nextFree_movesToSecondSemester() =
        assertEquals(1 to 2, SemesterLabels.nextFree(setOf(1 to 1)))

    @Test fun nextFree_movesToNextYear() =
        assertEquals(2 to 1, SemesterLabels.nextFree(setOf(1 to 1, 1 to 2)))

    @Test fun nextFree_skipsGaps() =
        assertEquals(1 to 2, SemesterLabels.nextFree(setOf(1 to 1, 2 to 1)))

    @Test fun nextFree_nullWhenEverythingTaken() {
        val all = SemesterLabels.YEARS.flatMap { y -> SemesterLabels.TERMS.map { t -> y to t } }.toSet()
        assertNull(SemesterLabels.nextFree(all))
    }
}
