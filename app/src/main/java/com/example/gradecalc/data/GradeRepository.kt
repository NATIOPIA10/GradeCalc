package com.example.gradecalc.data

import androidx.room.withTransaction
import com.example.gradecalc.domain.SemesterLabels
import kotlinx.coroutines.flow.Flow

class GradeRepository(private val db: AppDatabase) {
    private val semesterDao = db.semesterDao()
    private val courseDao = db.courseDao()
    private val scaleDao = db.gradeScaleDao()

    val semesters: Flow<List<SemesterEntity>> = semesterDao.observeAll()
    val courses: Flow<List<CourseEntity>> = courseDao.observeAll()
    val scale: Flow<List<GradeScaleEntry>> = scaleDao.observeAll()

    /** Seeds the default grade scale on first launch. */
    suspend fun ensureSeeded() {
        if (scaleDao.count() == 0) scaleDao.insertAll(DEFAULT_GRADE_SCALE)
    }

    suspend fun addSemester(year: Int, term: Int) = semesterDao.insert(
        SemesterEntity(name = SemesterLabels.label(year, term), year = year, term = term),
    )
    suspend fun deleteSemester(semester: SemesterEntity) = semesterDao.delete(semester)

    suspend fun addCourse(course: CourseEntity) = courseDao.insert(course)
    suspend fun updateCourse(course: CourseEntity) = courseDao.update(course)
    suspend fun deleteCourse(course: CourseEntity) = courseDao.delete(course)

    suspend fun updateScaleEntry(entry: GradeScaleEntry) = scaleDao.update(entry)

    /** Replaces the whole grade scale with a preset, in one transaction so the scale is never empty. */
    suspend fun applyPreset(preset: ScalePreset) {
        db.withTransaction {
            scaleDao.deleteAll()
            scaleDao.insertAll(scaleFor(preset))
        }
    }

    /** Deletes every semester and course (the grade scale stays). Used when the PIN is forgotten. */
    suspend fun eraseAllGrades() {
        db.withTransaction {
            courseDao.deleteAll()
            semesterDao.deleteAll()
        }
    }

    suspend fun resetScale() = applyPreset(ScalePreset.ETHIOPIA)
}
