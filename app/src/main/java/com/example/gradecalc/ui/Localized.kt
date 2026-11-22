package com.example.gradecalc.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.gradecalc.R
import com.example.gradecalc.domain.SemesterLabels
import java.util.Locale

/**
 * A context that still wraps the original activity (so back handling, sharing and so on keep working)
 * but loads its strings and resources for another language.
 */
private class LanguageContext(base: Context, private val localizedResources: Resources) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
}

/** Shows [content] in the chosen app language ("en" or "am"), whatever the phone's language is. */
@Composable
fun WithLanguage(code: String, content: @Composable () -> Unit) {
    val base = LocalContext.current
    val localized = remember(code, base) {
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale.forLanguageTag(code))
        LanguageContext(base, base.createConfigurationContext(config).resources)
    }
    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides localized.resources.configuration,
        content = content,
    )
}

/** "Year III, Semester Two" in the app language. Old semesters without a year and number keep their typed name. */
fun semesterLabel(context: Context, year: Int, term: Int, fallback: String): String {
    if (year <= 0 || term <= 0) return fallback
    val termWord = context.getString(if (term == 1) R.string.term_one else R.string.term_two)
    return context.getString(
        R.string.semester_full,
        context.getString(R.string.year_label, SemesterLabels.roman(year)),
        context.getString(R.string.term_label, termWord),
    )
}

@Composable
fun semesterLabel(year: Int, term: Int, fallback: String): String =
    semesterLabel(LocalContext.current, year, term, fallback)

@Composable
fun termText(term: Int): String =
    stringResource(R.string.term_label, stringResource(if (term == 1) R.string.term_one else R.string.term_two))

@Composable
fun yearText(year: Int): String =
    stringResource(R.string.year_label, SemesterLabels.roman(year))
