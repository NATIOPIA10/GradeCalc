package com.example.gradecalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.data.ScalePreset

/**
 * First-launch screen: pick the language and the grading scale, then start.
 * The chosen scale is applied when the student taps "Get started".
 */
@Composable
fun WelcomeScreen(
    language: String,
    onLanguage: (String) -> Unit,
    onStart: (ScalePreset) -> Unit,
) {
    var selected by remember { mutableStateOf(ScalePreset.ETHIOPIA) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LanguageChips(language = language, onChange = onLanguage)
            GpaRing(
                gpa = 3.5,
                maxPoints = 4.0,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                progressColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                ringSize = 150.dp,
            )
            Text(
                stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(R.string.welcome_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Step(1, stringResource(R.string.step1_title), stringResource(R.string.step1_desc))
            Step(2, stringResource(R.string.step2_title), stringResource(R.string.step2_desc))
            Step(3, stringResource(R.string.step3_title), stringResource(R.string.step3_desc))

            Text(
                stringResource(R.string.welcome_scale_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Column {
                ScalePreset.values().forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = preset == selected,
                                onClick = { selected = preset },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = preset == selected, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(preset.labelRes), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Text(
                stringResource(R.string.presets_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))
            Button(onClick = { onStart(selected) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.welcome_start), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun Step(number: Int, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                number.toString(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
