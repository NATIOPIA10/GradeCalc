package com.example.gradecalc.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Display label, e.g. "Year III, Semester Two". */
    val name: String,
    /** 1..5 from the picker; 0 for semesters created before the picker existed. */
    val year: Int = 0,
    /** 1 or 2 from the picker; 0 for semesters created before the picker existed. */
    val term: Int = 0,
)

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = SemesterEntity::class,
            parentColumns = ["id"],
            childColumns = ["semesterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("semesterId")],
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val name: String,
    val creditHours: Double,
    /** Letter grade key into the grade scale; null means not graded yet. */
    val gradeLetter: String?,
    /** Mark out of 100. When set, the grade is worked out from the grade scale automatically. */
    val scorePercent: Double? = null,
)

@Entity(tableName = "grade_scale")
data class GradeScaleEntry(
    @PrimaryKey val letter: String,
    val points: Double,
    val minPercent: Int,
    val maxPercent: Int,
    val sortOrder: Int,
)
