package com.example.gradecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.data.CourseEntity
import com.example.gradecalc.data.GradeScaleEntry
import com.example.gradecalc.data.gradeForScore
import com.example.gradecalc.domain.GpaCalculator
import com.example.gradecalc.domain.Retakes

/**
 * Add/edit a course. By default the student types a mark out of 100 and the grade is worked out
 * from the grade scale, so they do not need to know the scale. They can also work the mark out from
 * assessments, or switch to picking a letter.
 *
 * [retakeLabels] maps a course key (see [Retakes.keyOf]) to the semester where the same course already
 * exists, so the dialog can tell the student that only the latest graded attempt counts.
 */
@Composable
fun CourseDialog(
    initial: CourseEntity?,
    semesterId: Long,
    scale: List<GradeScaleEntry>,
    existingNames: List<String> = emptyList(),
    retakeLabels: Map<String, String> = emptyMap(),
    onSave: (CourseEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var creditsText by remember { mutableStateOf(initial?.creditHours?.let { formatCredits(it) } ?: "") }
    var useLetter by remember {
        mutableStateOf(initial != null && initial.scorePercent == null && initial.gradeLetter != null)
    }
    var scoreText by remember { mutableStateOf(initial?.scorePercent?.let { formatCredits(it) } ?: "") }
    var letter by remember { mutableStateOf(initial?.gradeLetter) }
    var menuOpen by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }

    val credits = creditsText.toDoubleOrNull()
    val score = scoreText.toDoubleOrNull()
    val scoreOk = scoreText.isBlank() || (score != null && score in 0.0..100.0)
    val duplicate = existingNames.any { it.trim().equals(name.trim(), ignoreCase = true) }
    val valid = name.isNotBlank() && !duplicate && credits != null && credits > 0 && (useLetter || scoreOk)
    val preview = if (!useLetter && score != null && scoreOk) gradeForScore(score, scale) else null
    val retakeOf = retakeLabels[Retakes.keyOf(name)]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.add_course else R.string.edit_course)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.course_name)) },
                    singleLine = true,
                    isError = duplicate,
                    supportingText = {
                        when {
                            duplicate -> Text(stringResource(R.string.course_exists))
                            retakeOf != null -> Text(stringResource(R.string.retake_hint, retakeOf))
                        }
                    },
                )
                OutlinedTextField(
                    value = creditsText,
                    onValueChange = { creditsText = it },
                    label = { Text(stringResource(R.string.credit_hours)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                if (useLetter) {
                    Box {
                        OutlinedButton(onClick = { menuOpen = true }) {
                            Text(
                                stringResource(
                                    R.string.grade_button,
                                    letter ?: stringResource(R.string.not_graded),
                                ),
                            )
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.not_graded_item)) },
                                onClick = { letter = null; menuOpen = false },
                            )
                            scale.forEach { entry ->
                                DropdownMenuItem(
                                    text = { Text("${entry.letter}  (${GpaCalculator.format(entry.points)})") },
                                    onClick = { letter = entry.letter; menuOpen = false },
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { scoreText = it },
                        label = { Text(stringResource(R.string.mark_label)) },
                        singleLine = true,
                        isError = !scoreOk,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = {
                            Text(
                                when {
                                    !scoreOk -> stringResource(R.string.mark_error)
                                    preview != null -> stringResource(
                                        R.string.mark_preview,
                                        preview.letter,
                                        GpaCalculator.format(preview.points),
                                    )
                                    else -> stringResource(R.string.mark_hint)
                                },
                            )
                        },
                    )
                    TextButton(onClick = { showCalculator = true }) {
                        Text(stringResource(R.string.calc_from_assessments))
                    }
                }
                TextButton(onClick = { useLetter = !useLetter }) {
                    Text(stringResource(if (useLetter) R.string.switch_to_mark else R.string.switch_to_letter))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        CourseEntity(
                            id = initial?.id ?: 0L,
                            semesterId = semesterId,
                            name = name.trim(),
                            creditHours = credits ?: 0.0,
                            gradeLetter = if (useLetter) letter else null,
                            scorePercent = if (useLetter) null else score,
                        ),
                    )
                },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )

    if (showCalculator) {
        AssessmentCalculatorDialog(
            onUse = { mark ->
                scoreText = formatCredits(mark)
                showCalculator = false
            },
            onDismiss = { showCalculator = false },
        )
    }
}
