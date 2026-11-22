package com.example.gradecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.domain.SemesterLabels

/**
 * Pick the year (I to V) and the semester (One or Two) from lists instead of typing a name.
 * It starts on the next semester that has not been added yet, and refuses one that already exists.
 */
@Composable
fun SemesterPickerDialog(
    taken: Set<Pair<Int, Int>>,
    onConfirm: (year: Int, term: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val start = remember { SemesterLabels.nextFree(taken) ?: (1 to 1) }
    var year by remember { mutableIntStateOf(start.first) }
    var term by remember { mutableIntStateOf(start.second) }
    var yearMenu by remember { mutableStateOf(false) }
    var termMenu by remember { mutableStateOf(false) }

    val duplicate = (year to term) in taken
    val fullLabel = semesterLabel(year, term, "")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_semester)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedButton(onClick = { yearMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(yearText(year))
                    }
                    DropdownMenu(expanded = yearMenu, onDismissRequest = { yearMenu = false }) {
                        SemesterLabels.YEARS.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(yearText(y)) },
                                onClick = { year = y; yearMenu = false },
                            )
                        }
                    }
                }
                Box {
                    OutlinedButton(onClick = { termMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(termText(term))
                    }
                    DropdownMenu(expanded = termMenu, onDismissRequest = { termMenu = false }) {
                        SemesterLabels.TERMS.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(termText(t)) },
                                onClick = { term = t; termMenu = false },
                            )
                        }
                    }
                }
                if (duplicate) {
                    Text(
                        stringResource(R.string.picker_exists, fullLabel),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        fullLabel,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(year, term) }, enabled = !duplicate) {
                Text(stringResource(R.string.action_add))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
