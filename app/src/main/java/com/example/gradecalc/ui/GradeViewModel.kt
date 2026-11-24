package com.example.gradecalc.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradecalc.data.AppDatabase
import com.example.gradecalc.data.CourseEntity
import com.example.gradecalc.data.GradeRepository
import com.example.gradecalc.data.GradeScaleEntry
import com.example.gradecalc.data.PinStore
import com.example.gradecalc.data.ScalePreset
import com.example.gradecalc.data.SemesterEntity
import com.example.gradecalc.data.resolveGrade
import com.example.gradecalc.domain.CourseAttempt
import com.example.gradecalc.domain.GpaCalculator
import com.example.gradecalc.domain.GradeEntry
import com.example.gradecalc.domain.PinPolicy
import com.example.gradecalc.domain.PinResult
import com.example.gradecalc.domain.Retakes
import com.example.gradecalc.domain.SemesterLabels
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SemesterSummary(
    val semester: SemesterEntity,
    val courses: List<CourseEntity>,
    /** GPA of this semester on its own, as it was at the time. */
    val gpa: Double?,
    val gradedCredits: Double,
)

/** One point on the GPA chart. */
data class TrendPoint(val label: String, val sgpa: Double?, val cgpa: Double?)

data class UiState(
    val semesters: List<SemesterSummary> = emptyList(),
    val scale: List<GradeScaleEntry> = emptyList(),
    /** CGPA with retakes applied: only the latest graded attempt of each course counts. */
    val cgpa: Double? = null,
    val totalCredits: Double = 0.0,
    val totalQualityPoints: Double = 0.0,
    /** Graded attempts that were replaced by a later attempt and so do not count in the CGPA. */
    val supersededIds: Set<Long> = emptySet(),
    val trend: List<TrendPoint> = emptyList(),
) {
    /** Highest grade point in the scale, used to tell whether a target is reachable. */
    val maxPoints: Double get() = scale.maxOfOrNull { it.points } ?: 4.0
}

class GradeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = GradeRepository(AppDatabase.get(application))
    private val prefs = application.getSharedPreferences("gradecalc_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(prefs.getString("lang", null) ?: defaultLanguage())

    /** "en" or "am". Starts as the phone's language when that is Amharic, otherwise English. */
    val language: StateFlow<String> = _language.asStateFlow()

    fun setLanguage(code: String) {
        _language.value = code
        prefs.edit().putString("lang", code).apply()
    }

    // ---- PIN lock ----

    private val pinStore = PinStore(prefs)
    private val _pinEnabled = MutableStateFlow(pinStore.isSet)
    private val _locked = MutableStateFlow(pinStore.isSet)
    private var backgroundedAt = 0L

    /** True while a PIN is set. */
    val pinEnabled: StateFlow<Boolean> = _pinEnabled.asStateFlow()

    /** True while the lock screen must be shown. Starts locked whenever a PIN is set. */
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    /** Milliseconds left in a "too many tries" wait, or 0. */
    fun lockoutRemainingMs(): Long =
        PinPolicy.lockoutRemainingMs(pinStore.failedAttempts, pinStore.lastFailureAt, System.currentTimeMillis())

    private fun checkPin(pin: String): PinResult {
        val now = System.currentTimeMillis()
        var failed = pinStore.failedAttempts
        val remaining = PinPolicy.lockoutRemainingMs(failed, pinStore.lastFailureAt, now)
        if (remaining > 0) return PinResult.LockedOut(remaining)
        if (failed >= PinPolicy.MAX_ATTEMPTS) {
            failed = 0
            pinStore.saveFailures(0, 0L)
        }
        if (pinStore.verify(pin)) {
            pinStore.saveFailures(0, 0L)
            return PinResult.Ok
        }
        failed++
        pinStore.saveFailures(failed, now)
        return if (failed >= PinPolicy.MAX_ATTEMPTS) PinResult.LockedOut(PinPolicy.LOCKOUT_MS)
        else PinResult.Wrong(PinPolicy.MAX_ATTEMPTS - failed)
    }

    fun unlock(pin: String): PinResult {
        val result = checkPin(pin)
        if (result is PinResult.Ok) _locked.value = false
        return result
    }

    /** Sets a first PIN. Returns false when the PIN is not 4 to 6 digits. */
    fun setPin(pin: String): Boolean {
        if (!PinPolicy.isValid(pin)) return false
        pinStore.set(pin)
        _pinEnabled.value = true
        return true
    }

    fun changePin(current: String, new: String): PinResult {
        if (!PinPolicy.isValid(new)) return PinResult.Wrong(PinPolicy.MAX_ATTEMPTS)
        val result = checkPin(current)
        if (result is PinResult.Ok) pinStore.set(new)
        return result
    }

    fun removePin(current: String): PinResult {
        val result = checkPin(current)
        if (result is PinResult.Ok) {
            pinStore.clear()
            _pinEnabled.value = false
            _locked.value = false
        }
        return result
    }

    /** "Forgot PIN": deletes all semesters and courses and removes the PIN. */
    fun eraseEverything() {
        viewModelScope.launch {
            repo.eraseAllGrades()
            pinStore.clear()
            _pinEnabled.value = false
            _locked.value = false
        }
    }

    /** Called when the app leaves the screen. Rotation is not leaving. */
    fun onAppStopped(changingConfiguration: Boolean) {
        if (!changingConfiguration) backgroundedAt = System.currentTimeMillis()
    }

    /** Called when the app comes back. Locks again if it was away for 30 seconds or more. */
    fun onAppStarted() {
        if (PinPolicy.shouldAutoLock(_pinEnabled.value, backgroundedAt, System.currentTimeMillis())) {
            _locked.value = true
        }
        backgroundedAt = 0L
    }

    private fun defaultLanguage(): String = if (Locale.getDefault().language == "am") "am" else "en"

    val state: StateFlow<UiState> = combine(repo.semesters, repo.courses, repo.scale) { semesters, courses, scale ->
        fun entryOf(c: CourseEntity) = GradeEntry(resolveGrade(c, scale)?.points, c.creditHours)

        // Semesters arrive ordered by year, then semester, so this list is oldest to newest.
        val summaries = semesters.map { s ->
            val own = courses.filter { it.semesterId == s.id }
            val entries = own.map(::entryOf)
            SemesterSummary(s, own, GpaCalculator.gpa(entries), GpaCalculator.gradedCredits(entries))
        }

        val attemptsBySemester = summaries.map { s ->
            s.courses.map { CourseAttempt(it.id, Retakes.keyOf(it.name), entryOf(it)) }
        }
        val countedIds = Retakes.countedIds(attemptsBySemester)
        val allAttempts = attemptsBySemester.flatten()
        val countedEntries = allAttempts.filter { it.id in countedIds }.map { it.entry }
        val superseded = allAttempts
            .filter { it.entry.points != null && it.id !in countedIds }
            .map { it.id }
            .toSet()

        val cumulative = Retakes.cumulativeSeries(attemptsBySemester)
        val trend = summaries.mapIndexed { i, s ->
            val short = SemesterLabels.short(s.semester.year, s.semester.term)
            TrendPoint(if (short == "#") "#${i + 1}" else short, s.gpa, cumulative[i])
        }

        UiState(
            semesters = summaries,
            scale = scale,
            cgpa = GpaCalculator.gpa(countedEntries),
            totalCredits = GpaCalculator.gradedCredits(countedEntries),
            totalQualityPoints = GpaCalculator.qualityPoints(countedEntries),
            supersededIds = superseded,
            trend = trend,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        viewModelScope.launch { repo.ensureSeeded() }
    }

    fun addSemester(year: Int, term: Int) = viewModelScope.launch {
        val alreadyAdded = state.value.semesters.any { it.semester.year == year && it.semester.term == term }
        if (!alreadyAdded) repo.addSemester(year, term)
    }

    fun deleteSemester(semester: SemesterEntity) = viewModelScope.launch { repo.deleteSemester(semester) }

    fun saveCourse(course: CourseEntity) = viewModelScope.launch {
        if (course.id == 0L) repo.addCourse(course) else repo.updateCourse(course)
    }

    fun deleteCourse(course: CourseEntity) = viewModelScope.launch { repo.deleteCourse(course) }

    fun updateScaleEntry(entry: GradeScaleEntry) = viewModelScope.launch { repo.updateScaleEntry(entry) }
    fun resetScale() = viewModelScope.launch { repo.resetScale() }
    fun applyPreset(preset: ScalePreset) = viewModelScope.launch { repo.applyPreset(preset) }
}
