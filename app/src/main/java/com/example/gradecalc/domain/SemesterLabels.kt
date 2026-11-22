package com.example.gradecalc.domain

/** Year/semester choices and labels, matching the portal ("Year III, Semester Two"). */
object SemesterLabels {
    val YEARS = 1..5
    val TERMS = 1..2

    fun roman(year: Int): String = when (year) {
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        else -> year.toString()
    }

    fun termWord(term: Int): String = when (term) {
        1 -> "One"
        2 -> "Two"
        else -> term.toString()
    }

    fun yearLabel(year: Int): String = "Year ${roman(year)}"
    fun termLabel(term: Int): String = "Semester ${termWord(term)}"
    fun label(year: Int, term: Int): String = "${yearLabel(year)}, ${termLabel(term)}"

    /** Short axis label such as "III.2"; "#" for old semesters that have no year and semester number. */
    fun short(year: Int, term: Int): String = if (year <= 0 || term <= 0) "#" else "${roman(year)}.$term"

    /** The first year/semester (in order) that has not been added yet, or null when all are taken. */
    fun nextFree(taken: Set<Pair<Int, Int>>): Pair<Int, Int>? {
        for (y in YEARS) for (t in TERMS) if ((y to t) !in taken) return y to t
        return null
    }
}
