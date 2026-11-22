package com.example.gradecalc.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.gradecalc.R
import com.example.gradecalc.domain.GpaCalculator
import com.example.gradecalc.ui.theme.gradeColors

/** Top bar that blends into the screen background. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    titleContentColor = MaterialTheme.colorScheme.primary,
    navigationIconContentColor = MaterialTheme.colorScheme.primary,
    actionIconContentColor = MaterialTheme.colorScheme.primary,
)

/** Circular GPA gauge (270 degree arc) that animates to its value. */
@Composable
fun GpaRing(
    gpa: Double?,
    maxPoints: Double,
    trackColor: Color,
    progressColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    ringSize: Dp = 132.dp,
) {
    val target = if (gpa == null || maxPoints <= 0.0) 0f else (gpa / maxPoints).toFloat().coerceIn(0f, 1f)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(target) { progress.animateTo(target, tween(durationMillis = 900)) }

    Box(modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.11f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = 135f,
                sweepAngle = 270f * progress.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                GpaCalculator.format(gpa),
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
            )
            Text(
                stringResource(R.string.out_of, GpaCalculator.format(maxPoints)),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.75f),
            )
        }
    }
}

/** Big gradient card with the GPA ring on the left and a title on the right. */
@Composable
fun HeroCard(
    title: String,
    gpa: Double?,
    maxPoints: Double,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    val gradient = if (dark) {
        Brush.linearGradient(listOf(Color(0xFF1B4D35), Color(0xFF2A7A55)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF1B6B43), Color(0xFF2F9565)))
    }
    Box(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(gradient)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            GpaRing(
                gpa = gpa,
                maxPoints = maxPoints,
                trackColor = Color.White.copy(alpha = 0.22f),
                progressColor = Color(0xFFD7F5A8),
                textColor = Color.White,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.85f))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
            }
        }
    }
}

/** Rounded square showing a letter grade, colored by grade family. */
@Composable
fun GradeBadge(letter: String?, modifier: Modifier = Modifier) {
    val (badgeBg, badgeFg) = gradeColors(letter)
    Box(
        modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(badgeBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter ?: "-",
            color = badgeFg,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
