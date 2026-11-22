package com.example.gradecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.data.CourseEntity
import com.example.gradecalc.data.resolveGrade
import com.example.gradecalc.domain.GpaCalculator
import com.example.gradecalc.domain.Retakes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterScreen(
    semesterId: Long,
    state: UiState,
    onBack: () -> Unit,
    onSaveCourse: (CourseEntity) -> Unit,
    onDeleteCourse: (CourseEntity) -> Unit,
) {
    val summary = state.semesters.firstOrNull { it.semester.id == semesterId }
    // Semester list is empty on the very first frame; only leave once data has loaded and it is gone.
    val loaded = state.scale.isNotEmpty()
    LaunchedEffect(summary == null, loaded) {
        if (summary == null && loaded) onBack()
    }

    var editing by remember { mutableStateOf<CourseEntity?>(null) }
    var adding by remember { mutableStateOf(false) }

    // Courses with the same name in the other semesters, so the dialog can explain retakes.
    val context = LocalContext.current
    val retakeLabels: Map<String, String> = state.semesters
        .filter { it.semester.id != semesterId }
        .flatMap { other ->
            val label = semesterLabel(context, other.semester.year, other.semester.term, other.semester.name)
            other.courses.map { Retakes.keyOf(it.name) to label }
        }
        .toMap()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        summary?.let { semesterLabel(it.semester.year, it.semester.term, it.semester.name) } ?: "",
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = appBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { adding = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.fab_course)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        if (summary == null) return@Scaffold
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                HeroCard(
                    title = stringResource(R.string.semester_gpa_title),
                    gpa = summary.gpa,
                    maxPoints = state.maxPoints,
                    subtitle = stringResource(R.string.credit_hours_graded, formatCredits(summary.gradedCredits)),
                )
            }
            if (summary.courses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Text(
                            stringResource(R.string.semester_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(20.dp),
                        )
                    }
                }
            } else {
                item {
                    Text(
                        stringResource(R.string.courses_heading),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            items(summary.courses, key = { it.id }) { course ->
                CourseCard(
                    course = course,
                    state = state,
                    superseded = course.id in state.supersededIds,
                    onClick = { editing = course },
                    onDelete = { onDeleteCourse(course) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    if (adding && summary != null) {
        CourseDialog(
            initial = null,
            semesterId = summary.semester.id,
            scale = state.scale,
            existingNames = summary.courses.map { it.name },
            retakeLabels = retakeLabels,
            onSave = { onSaveCourse(it); adding = false },
            onDismiss = { adding = false },
        )
    }
    editing?.let { course ->
        CourseDialog(
            initial = course,
            semesterId = course.semesterId,
            scale = state.scale,
            existingNames = summary?.courses?.filter { it.id != course.id }?.map { it.name } ?: emptyList(),
            retakeLabels = retakeLabels,
            onSave = { onSaveCourse(it); editing = null },
            onDismiss = { editing = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseCard(
    course: CourseEntity,
    state: UiState,
    superseded: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grade = resolveGrade(course, state.scale)
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            GradeBadge(grade?.letter)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(course.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(
                        R.string.course_credit_line,
                        formatCredits(course.creditHours),
                        ectsFor(course.creditHours),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (superseded) {
                    Text(
                        stringResource(R.string.retaken_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            if (grade != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        GpaCalculator.format(grade.points),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    course.scorePercent?.let {
                        Text(
                            "${formatCredits(it)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete_course),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
