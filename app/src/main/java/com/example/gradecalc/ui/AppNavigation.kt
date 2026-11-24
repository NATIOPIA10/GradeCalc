package com.example.gradecalc.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gradecalc.R
import com.example.gradecalc.export.ReportExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS = "gradecalc_prefs"
private const val KEY_ONBOARDED = "onboarded"

@Composable
fun AppNavigation(vm: GradeViewModel) {
    val nav = rememberNavController()
    val state by vm.state.collectAsState()
    val language by vm.language.collectAsState()
    val pinEnabled by vm.pinEnabled.collectAsState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    val startDestination = remember { if (prefs.getBoolean(KEY_ONBOARDED, false)) "home" else "welcome" }

    fun export(asPdf: Boolean) {
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { ReportExporter.create(context, state, asPdf) }
            }
            result
                .onSuccess { file -> ReportExporter.share(context, file, asPdf) }
                .onFailure { Toast.makeText(context, context.getString(R.string.share_failed), Toast.LENGTH_SHORT).show() }
        }
    }

    NavHost(
        navController = nav,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut() },
    ) {
        composable("welcome") {
            WelcomeScreen(
                language = language,
                onLanguage = vm::setLanguage,
                onStart = { preset ->
                    vm.applyPreset(preset)
                    prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
                    nav.navigate("home") { popUpTo("welcome") { inclusive = true } }
                },
            )
        }
        composable("home") {
            HomeScreen(
                state = state,
                onOpenSemester = { nav.navigate("semester/$it") },
                onAddSemester = vm::addSemester,
                onDeleteSemester = vm::deleteSemester,
                onWhatIf = { nav.navigate("whatif") },
                onSettings = { nav.navigate("settings") },
                onExport = ::export,
            )
        }
        composable(
            route = "semester/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: -1L
            SemesterScreen(
                semesterId = id,
                state = state,
                onBack = { nav.popBackStack() },
                onSaveCourse = vm::saveCourse,
                onDeleteCourse = vm::deleteCourse,
            )
        }
        composable("whatif") {
            WhatIfScreen(state = state, onBack = { nav.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(
                scale = state.scale,
                language = language,
                onLanguage = vm::setLanguage,
                onPreset = vm::applyPreset,
                onUpdate = vm::updateScaleEntry,
                onReset = vm::resetScale,
                pinEnabled = pinEnabled,
                onSetPin = vm::setPin,
                onChangePin = vm::changePin,
                onRemovePin = vm::removePin,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
