package com.example.gradecalc.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradecalc.R

/** Line chart of the semester GPA and the cumulative GPA, one point per semester. */
@Composable
fun GpaTrendChart(points: List<TrendPoint>, maxPoints: Double, modifier: Modifier = Modifier) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val sgpaColor = MaterialTheme.colorScheme.primary
    val cgpaColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(176.dp)) {
            val n = points.size
            val padX = 20.dp.toPx()
            val padTop = 8.dp.toPx()
            val labelBand = 22.dp.toPx()
            val plotW = size.width - 2 * padX
            val plotH = size.height - padTop - labelBand

            fun xAt(i: Int): Float = if (n <= 1) padX + plotW / 2f else padX + plotW * i / (n - 1).toFloat()
            fun yAt(v: Double): Float {
                val ratio = if (maxPoints <= 0.0) 0f else (v / maxPoints).toFloat().coerceIn(0f, 1f)
                return padTop + plotH * (1f - ratio)
            }

            for (k in 0..4) {
                val y = padTop + plotH * k / 4f
                drawLine(gridColor, Offset(padX, y), Offset(size.width - padX, y), strokeWidth = 1.dp.toPx())
            }

            fun drawSeries(values: List<Double?>, color: Color) {
                var previous: Offset? = null
                values.forEachIndexed { i, value ->
                    if (value == null) {
                        previous = null
                        return@forEachIndexed
                    }
                    val point = Offset(xAt(i), yAt(value))
                    previous?.let { drawLine(color, it, point, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round) }
                    drawCircle(color, radius = 4.5.dp.toPx(), center = point)
                    previous = point
                }
            }
            drawSeries(points.map { it.cgpa }, cgpaColor)
            drawSeries(points.map { it.sgpa }, sgpaColor)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = labelColor.toArgb()
                textSize = 11.sp.toPx()
                textAlign = Paint.Align.CENTER
            }
            drawIntoCanvas { canvas ->
                points.forEachIndexed { i, p ->
                    canvas.nativeCanvas.drawText(p.label, xAt(i), size.height - 4.dp.toPx(), paint)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(sgpaColor, stringResource(R.string.legend_sgpa))
            LegendDot(cgpaColor, stringResource(R.string.legend_cgpa))
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
