package com.example.gradecalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.domain.PinPolicy
import com.example.gradecalc.domain.PinResult
import kotlinx.coroutines.delay

/**
 * Full-screen PIN pad shown instead of the app while it is locked.
 * [onSubmit] checks the PIN. [initialLockoutMs] is a "too many tries" wait that is still running.
 */
@Composable
fun LockScreen(
    initialLockoutMs: Long,
    onSubmit: (String) -> PinResult,
    onForgotConfirmed: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var wrongAttemptsLeft by remember { mutableStateOf<Int?>(null) }
    var lockedUntil by remember { mutableLongStateOf(System.currentTimeMillis() + initialLockoutMs) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showForgot by remember { mutableStateOf(false) }

    LaunchedEffect(lockedUntil) {
        while (System.currentTimeMillis() < lockedUntil) {
            now = System.currentTimeMillis()
            delay(250)
        }
        now = System.currentTimeMillis()
    }

    val lockedOut = lockedUntil > now
    val secondsLeft = ((lockedUntil - now + 999) / 1000).toInt().coerceAtLeast(0)

    fun submit() {
        if (!PinPolicy.isValid(pin) || lockedOut) return
        when (val result = onSubmit(pin)) {
            PinResult.Ok -> Unit
            is PinResult.Wrong -> wrongAttemptsLeft = result.attemptsLeft
            is PinResult.LockedOut -> {
                wrongAttemptsLeft = null
                lockedUntil = System.currentTimeMillis() + result.remainingMs
            }
        }
        pin = ""
    }

    fun press(digit: Char) {
        if (!lockedOut && pin.length < PinPolicy.MAX_LENGTH) pin += digit
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(PaddingValues(horizontal = 24.dp, vertical = 16.dp)),
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(R.string.pin_lock_title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                val dots = maxOf(PinPolicy.MIN_LENGTH, pin.length)
                repeat(dots) { index ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < pin.length) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                            ),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                val message = when {
                    lockedOut -> stringResource(R.string.pin_locked_out, secondsLeft)
                    wrongAttemptsLeft != null -> stringResource(R.string.pin_wrong, wrongAttemptsLeft ?: 0)
                    else -> ""
                }
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(14.dp))

            val rows = listOf(listOf('1', '2', '3'), listOf('4', '5', '6'), listOf('7', '8', '9'))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { digit -> KeyButton(onClick = { press(digit) }) { Text(digit.toString(), style = MaterialTheme.typography.titleLarge) } }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KeyButton(onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }) {
                        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = stringResource(R.string.cd_backspace))
                    }
                    KeyButton(onClick = { press('0') }) { Text("0", style = MaterialTheme.typography.titleLarge) }
                    KeyButton(onClick = ::submit, enabled = PinPolicy.isValid(pin) && !lockedOut) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.action_unlock))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { showForgot = true }) { Text(stringResource(R.string.pin_forgot)) }
        }
    }

    if (showForgot) {
        ConfirmDialog(
            title = stringResource(R.string.pin_forgot_title),
            message = stringResource(R.string.pin_forgot_message),
            confirmText = stringResource(R.string.pin_forgot_confirm),
            onConfirm = {
                showForgot = false
                onForgotConfirmed()
            },
            onDismiss = { showForgot = false },
        )
    }
}

@Composable
private fun KeyButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(72.dp),
    ) { content() }
}
