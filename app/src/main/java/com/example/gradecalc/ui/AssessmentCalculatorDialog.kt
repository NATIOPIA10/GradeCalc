package com.example.gradecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.domain.Assessment
import com.example.gradecalc.domain.CourseMark
import com.example.gradecalc.domain.GpaCalculator

private data class AssessmentRow(val name: String, val earned: String, val max: String)

/**
 * Works out a course mark from its assessments (assignment, mid exam, final exam, ...).
 * Each row has the mark earned and the most it can give; the result is the mark out of 100.
 */
@Composable
fun AssessmentCalculatorDialog(
    onUse: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val assignment = stringResource(R.string.assess_default_assignment)
    val mid = stringResource(R.string.assess_default_mid)
    val finalExam = stringResource(R.string.assess_default_final)
    val rows = remember {
        mutableStateListOf(
            AssessmentRow(assignment, "", ""),
            AssessmentRow(mid, "", ""),
            AssessmentRow(finalExam, "", ""),
        )
    }

    val items = rows.map { Assessment(it.earned.toDoubleOrNull(), it.max.toDoubleOrNull() ?: 0.0) }
    val percent = CourseMark.percent(items)
    val weightSum = items.sumOf { it.max }
    val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.assess_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    stringResource(R.string.assess_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                rows.forEachIndexed { index, row ->
                    val earned = row.earned.toDoubleOrNull()
                    val max = row.max.toDoubleOrNull()
                    val tooHigh = earned != null && max != null && earned > max
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = row.name,
                                onValueChange = { rows[index] = row.copy(name = it) },
                                label = { Text(stringResource(R.string.assess_name)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { rows.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_remove))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = row.earned,
                                onValueChange = { rows[index] = row.copy(earned = it) },
                                label = { Text(stringResource(R.string.assess_earned)) },
                                singleLine = true,
                                isError = tooHigh,
                                keyboardOptions = decimal,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = row.max,
                                onValueChange = { rows[index] = row.copy(max = it) },
                                label = { Text(stringResource(R.string.assess_max)) },
                                singleLine = true,
                                keyboardOptions = decimal,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                TextButton(onClick = { rows.add(AssessmentRow("", "", "")) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(R.string.assess_add))
                }
                Text(
                    text = if (percent == null) {
                        stringResource(R.string.assess_empty)
                    } else {
                        stringResource(
                            R.string.assess_result,
                            formatCredits(GpaCalculator.round2(percent)),
                            formatCredits(GpaCalculator.round2(CourseMark.earnedSum(items))),
                            formatCredits(GpaCalculator.round2(CourseMark.maxSum(items))),
                        )
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (weightSum > 0.0 && weightSum != 100.0) {
                    Text(
                        stringResource(R.string.assess_sum_note, formatCredits(GpaCalculator.round2(weightSum))),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = percent != null,
                onClick = { if (percent != null) onUse(GpaCalculator.round2(percent)) },
            ) { Text(stringResource(R.string.assess_use)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
