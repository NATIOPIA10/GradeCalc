package com.example.gradecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gradecalc.R
import com.example.gradecalc.domain.GpaCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatIfScreen(state: UiState, onBack: () -> Unit) {
    var targetText by remember { mutableStateOf("") }
    var creditsText by remember { mutableStateOf("") }

    val target = targetText.toDoubleOrNull()
    val nextCredits = creditsText.toDoubleOrNull()

    // The card is tinted as a warning when the target cannot be reached or the input is wrong.
    var warning = false
    val message: String = when {
        target == null || nextCredits == null -> stringResource(R.string.whatif_enter)
        nextCredits <= 0.0 -> {
            warning = true
            stringResource(R.string.whatif_credits_positive)
        }
        else -> {
            val needed = GpaCalculator.requiredNextGpa(
                currentQualityPoints = state.totalQualityPoints,
                currentCredits = state.totalCredits,
                targetCgpa = target,
                nextCredits = nextCredits,
            )
            when {
                needed == null -> {
                    warning = true
                    stringResource(R.string.whatif_invalid)
                }
                needed > state.maxPoints -> {
                    warning = true
                    stringResource(
                        R.string.whatif_unreachable,
                        GpaCalculator.format(needed),
                        GpaCalculator.format(state.maxPoints),
                    )
                }
                needed <= 0.0 -> stringResource(R.string.whatif_met)
                else -> stringResource(R.string.whatif_need, GpaCalculator.format(needed))
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.whatif_title), fontWeight = FontWeight.Bold) },
                colors = appBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeroCard(
                title = stringResource(R.string.current_cgpa),
                gpa = state.cgpa,
                maxPoints = state.maxPoints,
                subtitle = stringResource(R.string.credit_hours_graded, formatCredits(state.totalCredits)),
            )
            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it },
                label = { Text(stringResource(R.string.target_cgpa)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = creditsText,
                onValueChange = { creditsText = it },
                label = { Text(stringResource(R.string.next_credits)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (warning) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Text(
                    message,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (warning) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(20.dp),
                )
            }
        }
    }
}
