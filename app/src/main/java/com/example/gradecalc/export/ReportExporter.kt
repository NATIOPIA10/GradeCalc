package com.example.gradecalc.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.example.gradecalc.R
import com.example.gradecalc.data.resolveGrade
import com.example.gradecalc.domain.GpaCalculator
import com.example.gradecalc.ui.UiState
import com.example.gradecalc.ui.ectsFor
import com.example.gradecalc.ui.formatCredits
import com.example.gradecalc.ui.semesterLabel
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.Date

/** Builds the grade report as a PDF or a PNG image and opens the Android share sheet for it. */
object ReportExporter {
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40f
    private const val GREEN = 0xFF1F7A4D.toInt()

    private sealed class Row(val height: Float) {
        class Title(val text: String) : Row(30f)
        class Sub(val text: String) : Row(20f)
        class Gap(h: Float) : Row(h)
        class Stat(val label: String, val value: String) : Row(22f)
        class SemesterHeader(val title: String, val gpa: String) : Row(28f)
        class ColumnHeader(val cells: List<String>) : Row(20f)
        class Course(val cells: List<String>) : Row(20f)
        class Note(val text: String) : Row(18f)
    }

    /** Creates the report file in the app's cache folder. Call from a background thread. */
    fun create(context: Context, state: UiState, asPdf: Boolean): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, if (asPdf) "GradeCalc_report.pdf" else "GradeCalc_report.png")
        val rows = buildRows(context, state)
        if (asPdf) writePdf(file, rows) else writePng(file, rows)
        return file
    }

    /** Opens the share sheet for [file]. */
    fun share(context: Context, file: File, asPdf: Boolean) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = if (asPdf) "application/pdf" else "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, context.getString(R.string.share_chooser))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun buildRows(context: Context, state: UiState): List<Row> {
        val locale = context.resources.configuration.locales[0]
        val date = DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date())
        val rows = mutableListOf<Row>()
        rows += Row.Title(context.getString(R.string.report_title))
        rows += Row.Sub(context.getString(R.string.report_generated, date))
        rows += Row.Gap(10f)
        rows += Row.Stat(context.getString(R.string.report_cgpa), GpaCalculator.format(state.cgpa))
        rows += Row.Stat(context.getString(R.string.report_credits), formatCredits(state.totalCredits))
        rows += Row.Gap(12f)

        var anySuperseded = false
        val headers = listOf(
            context.getString(R.string.report_col_course),
            context.getString(R.string.report_col_credits),
            context.getString(R.string.report_col_ects),
            context.getString(R.string.report_col_grade),
            context.getString(R.string.report_col_points),
        )
        for (s in state.semesters) {
            val title = semesterLabel(context, s.semester.year, s.semester.term, s.semester.name)
            rows += Row.SemesterHeader(title, context.getString(R.string.report_semester_gpa, GpaCalculator.format(s.gpa)))
            rows += Row.ColumnHeader(headers)
            for (c in s.courses) {
                val grade = resolveGrade(c, state.scale)
                val superseded = c.id in state.supersededIds
                if (superseded) anySuperseded = true
                rows += Row.Course(
                    listOf(
                        c.name + if (superseded) " *" else "",
                        formatCredits(c.creditHours),
                        ectsFor(c.creditHours).toString(),
                        grade?.letter ?: "-",
                        grade?.let { GpaCalculator.format(it.points) } ?: "-",
                    ),
                )
            }
            rows += Row.Gap(10f)
        }
        if (anySuperseded) rows += Row.Note(context.getString(R.string.report_footnote))
        return rows
    }

    private fun paginate(rows: List<Row>, pageHeight: Float): List<List<Row>> {
        val pages = mutableListOf<MutableList<Row>>(mutableListOf())
        var y = MARGIN
        for (row in rows) {
            if (y + row.height > pageHeight - MARGIN && pages.last().isNotEmpty()) {
                pages.add(mutableListOf())
                y = MARGIN
            }
            pages.last().add(row)
            y += row.height
        }
        return pages
    }

    private fun writePdf(file: File, rows: List<Row>) {
        val document = PdfDocument()
        try {
            paginate(rows, PAGE_H.toFloat()).forEachIndexed { index, pageRows ->
                val page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, index + 1).create())
                drawRows(page.canvas, pageRows)
                document.finishPage(page)
            }
            FileOutputStream(file).use { document.writeTo(it) }
        } finally {
            document.close()
        }
    }

    private fun writePng(file: File, rows: List<Row>) {
        val scale = 2f
        val totalHeight = rows.sumOf { it.height.toDouble() }.toFloat() + 2 * MARGIN
        val bitmap = Bitmap.createBitmap((PAGE_W * scale).toInt(), (totalHeight * scale).toInt(), Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.scale(scale, scale)
            drawRows(canvas, rows)
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally {
            bitmap.recycle()
        }
    }

    private fun text(size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            isFakeBoldText = bold
            textAlign = align
        }

    private fun fit(value: String, paint: TextPaint, width: Float): String =
        TextUtils.ellipsize(value, paint, width, TextUtils.TruncateAt.END).toString()

    private fun drawRows(canvas: Canvas, rows: List<Row>) {
        val dark = Color.parseColor("#1B1F1C")
        val muted = Color.parseColor("#5C645E")
        val title = text(20f, GREEN, bold = true)
        val sub = text(11f, muted)
        val statLabel = text(12f, dark)
        val statValue = text(12f, dark, bold = true, align = Paint.Align.RIGHT)
        val headerText = text(12f, Color.WHITE, bold = true)
        val headerGpa = text(12f, Color.WHITE, bold = true, align = Paint.Align.RIGHT)
        val columnHead = text(10f, muted, bold = true)
        val columnHeadCenter = text(10f, muted, bold = true, align = Paint.Align.CENTER)
        val cell = text(11f, dark)
        val cellCenter = text(11f, dark, align = Paint.Align.CENTER)
        val note = text(10f, muted)
        val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GREEN }
        val linePaint = Paint().apply { color = Color.parseColor("#D9DED9"); strokeWidth = 0.6f }

        val left = MARGIN
        val right = PAGE_W - MARGIN
        val centers = floatArrayOf(310f, 365f, 420f, 490f)

        var y = MARGIN
        for (row in rows) {
            when (row) {
                is Row.Title -> canvas.drawText(fit(row.text, title, right - left), left, y + 22f, title)
                is Row.Sub -> canvas.drawText(row.text, left, y + 14f, sub)
                is Row.Gap -> Unit
                is Row.Stat -> {
                    canvas.drawText(row.label, left, y + 15f, statLabel)
                    canvas.drawText(row.value, right, y + 15f, statValue)
                }
                is Row.SemesterHeader -> {
                    canvas.drawRoundRect(RectF(left, y + 2f, right, y + 26f), 6f, 6f, bandPaint)
                    canvas.drawText(fit(row.title, headerText, 320f), left + 10f, y + 19f, headerText)
                    canvas.drawText(row.gpa, right - 10f, y + 19f, headerGpa)
                }
                is Row.ColumnHeader -> {
                    canvas.drawText(fit(row.cells[0], columnHead, 200f), left + 10f, y + 14f, columnHead)
                    for (i in 0..3) canvas.drawText(row.cells[i + 1], centers[i], y + 14f, columnHeadCenter)
                    canvas.drawLine(left, y + 19f, right, y + 19f, linePaint)
                }
                is Row.Course -> {
                    canvas.drawText(fit(row.cells[0], cell, 215f), left + 10f, y + 14f, cell)
                    for (i in 0..3) canvas.drawText(row.cells[i + 1], centers[i], y + 14f, cellCenter)
                }
                is Row.Note -> canvas.drawText(fit(row.text, note, right - left), left, y + 12f, note)
            }
            y += row.height
        }
    }
}
