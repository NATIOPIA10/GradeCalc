package com.example.gradecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R

/** Shows 3 instead of 3.0, and 2.5 as is. */
fun formatCredits(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

/** ECTS credits shown by the portal: 1 credit hour = 5/3 ECTS, rounded (3 -> 5, 2 -> 3, 4 -> 7). */
fun ectsFor(creditHours: Double): Int = Math.floor(creditHours * 5.0 / 3.0 + 0.5).toInt()

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = stringResource(R.string.action_delete),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Two chips to switch the app language between English and Amharic. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageChips(language: String, onChange: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = language == "en",
            onClick = { onChange("en") },
            label = { Text(stringResource(R.string.language_english)) },
        )
        FilterChip(
            selected = language == "am",
            onClick = { onChange("am") },
            label = { Text(stringResource(R.string.language_amharic)) },
        )
    }
}
