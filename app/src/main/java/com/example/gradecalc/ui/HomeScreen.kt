package com.example.gradecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.data.SemesterEntity
import com.example.gradecalc.domain.GpaCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: UiState,
    onOpenSemester: (Long) -> Unit,
    onAddSemester: (Int, Int) -> Unit,
    onDeleteSemester: (SemesterEntity) -> Unit,
    onWhatIf: () -> Unit,
    onSettings: () -> Unit,
    /** Called with true to share a PDF, false to share an image. */
    onExport: (Boolean) -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<SemesterEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold) },
                colors = appBarColors(),
                actions = {
                    IconButton(onClick = { showShare = true }, enabled = state.semesters.isNotEmpty()) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.cd_share))
                    }
                    IconButton(onClick = onWhatIf) {
                        Icon(Icons.Default.Calculate, contentDescription = stringResource(R.string.cd_target_calc))
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.fab_semester)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HeroCard(
                    title = stringResource(R.string.cgpa_title),
                    gpa = state.cgpa,
                    maxPoints = state.maxPoints,
                    subtitle = stringResource(R.string.credit_hours_graded, formatCredits(state.totalCredits)),
                )
            }
            if (state.semesters.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Text(
                            stringResource(R.string.home_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(20.dp),
                        )
                    }
                }
            } else {
                if (state.trend.count { it.sgpa != null } >= 2) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    stringResource(R.string.trend_title),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(Modifier.height(8.dp))
                                GpaTrendChart(points = state.trend, maxPoints = state.maxPoints)
                            }
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.semesters_heading),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            items(state.semesters, key = { it.semester.id }) { summary ->
                SemesterCard(
                    summary = summary,
                    maxPoints = state.maxPoints,
                    onClick = { onOpenSemester(summary.semester.id) },
                    onDelete = { pendingDelete = summary.semester },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    if (showAdd) {
        SemesterPickerDialog(
            taken = state.semesters
                .map { it.semester.year to it.semester.term }
                .filter { it.first > 0 }
                .toSet(),
            onConfirm = { year, term -> onAddSemester(year, term); showAdd = false },
            onDismiss = { showAdd = false },
        )
    }

    if (showShare) {
        AlertDialog(
            onDismissRequest = { showShare = false },
            title = { Text(stringResource(R.string.share_title)) },
            text = {
                Column {
                    TextButton(onClick = { showShare = false; onExport(true) }) {
                        Text(stringResource(R.string.share_pdf))
                    }
                    TextButton(onClick = { showShare = false; onExport(false) }) {
                        Text(stringResource(R.string.share_image))
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showShare = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }

    pendingDelete?.let { sem ->
        val label = semesterLabel(sem.year, sem.term, sem.name)
        ConfirmDialog(
            title = stringResource(R.string.delete_semester_title, label),
            message = stringResource(R.string.delete_semester_message),
            onConfirm = { onDeleteSemester(sem); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SemesterCard(
    summary: SemesterSummary,
    maxPoints: Double,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ratio = if (summary.gpa == null || maxPoints <= 0.0) 0f else (summary.gpa / maxPoints).toFloat().coerceIn(0f, 1f)
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    semesterLabel(summary.semester.year, summary.semester.term, summary.semester.name),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(
                        R.string.semester_summary,
                        summary.courses.size,
                        formatCredits(summary.gradedCredits),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    GpaCalculator.format(summary.gpa),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.gpa_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete_semester),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
