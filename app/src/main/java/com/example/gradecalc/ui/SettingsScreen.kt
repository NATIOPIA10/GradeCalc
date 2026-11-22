package com.example.gradecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.data.GradeScaleEntry
import com.example.gradecalc.data.ScalePreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    scale: List<GradeScaleEntry>,
    language: String,
    onLanguage: (String) -> Unit,
    onPreset: (ScalePreset) -> Unit,
    onUpdate: (GradeScaleEntry) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    // Bumped on reset or preset change so the text fields reload the new values.
    var reloadCount by remember { mutableIntStateOf(0) }
    var pendingPreset by remember { mutableStateOf<ScalePreset?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scale_title), fontWeight = FontWeight.Bold) },
                colors = appBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.language_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            item { LanguageChips(language = language, onChange = onLanguage) }

            item {
                Text(
                    stringResource(R.string.presets_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            item {
                Text(
                    stringResource(R.string.presets_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(ScalePreset.values().toList(), key = { it.name }) { preset ->
                OutlinedButton(
                    onClick = { pendingPreset = preset },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(preset.labelRes)) }
            }

            item {
                Text(
                    stringResource(R.string.scale_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            item {
                Text(
                    stringResource(R.string.scale_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(scale, key = { it.letter }) { entry ->
                var text by remember(entry.letter, reloadCount) { mutableStateOf(entry.points.toString()) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        GradeBadge(entry.letter)
                        Text(
                            "${entry.minPercent}-${entry.maxPercent}%",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = text,
                            onValueChange = { input ->
                                text = input
                                val p = input.toDoubleOrNull()
                                if (p != null && p in 0.0..10.0) onUpdate(entry.copy(points = p))
                            },
                            label = { Text(stringResource(R.string.points)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(120.dp),
                        )
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = { onReset(); reloadCount++ },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.reset_scale)) }
            }
        }
    }

    pendingPreset?.let { preset ->
        ConfirmDialog(
            title = stringResource(R.string.preset_confirm_title, stringResource(preset.labelRes)),
            message = stringResource(R.string.preset_confirm_message),
            confirmText = stringResource(R.string.action_switch),
            onConfirm = {
                onPreset(preset)
                reloadCount++
                pendingPreset = null
            },
            onDismiss = { pendingPreset = null },
        )
    }
}
