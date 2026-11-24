package com.example.gradecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.domain.PinPolicy
import com.example.gradecalc.domain.PinResult

enum class PinMode { SET, CHANGE, REMOVE }

/**
 * Asks for the PINs a [mode] needs. [onSubmit] gets (current PIN, new PIN) and returns the result;
 * the dialog closes itself through [onDismiss] when the result is Ok.
 */
@Composable
fun PinDialog(
    mode: PinMode,
    onSubmit: (current: String, new: String) -> PinResult,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var current by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val needsCurrent = mode != PinMode.SET
    val needsNew = mode != PinMode.REMOVE

    fun digits(input: String) = input.filter { it in '0'..'9' }.take(PinPolicy.MAX_LENGTH)

    fun submit() {
        if (needsNew) {
            if (!PinPolicy.isValid(newPin)) { error = context.getString(R.string.pin_invalid); return }
            if (newPin != confirm) { error = context.getString(R.string.pin_mismatch); return }
        }
        when (val result = onSubmit(current, newPin)) {
            PinResult.Ok -> onDismiss()
            is PinResult.Wrong -> { error = context.getString(R.string.pin_wrong, result.attemptsLeft); current = "" }
            is PinResult.LockedOut -> {
                error = context.getString(R.string.pin_locked_out, ((result.remainingMs + 999) / 1000).toInt())
                current = ""
            }
        }
    }

    val title = when (mode) {
        PinMode.SET -> R.string.pin_set
        PinMode.CHANGE -> R.string.pin_change
        PinMode.REMOVE -> R.string.pin_remove
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (needsCurrent) PinField(current, { current = digits(it); error = null }, stringResource(R.string.pin_current))
                if (needsNew) {
                    PinField(newPin, { newPin = digits(it); error = null }, stringResource(R.string.pin_new))
                    PinField(confirm, { confirm = digits(it); error = null }, stringResource(R.string.pin_confirm))
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            val ready = (!needsCurrent || current.isNotEmpty()) && (!needsNew || (newPin.isNotEmpty() && confirm.isNotEmpty()))
            TextButton(onClick = ::submit, enabled = ready) {
                Text(stringResource(if (mode == PinMode.REMOVE) R.string.pin_remove else R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun PinField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
    )
}
