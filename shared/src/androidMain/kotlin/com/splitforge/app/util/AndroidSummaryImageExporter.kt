package com.splitforge.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.splitforge.app.domain.Segment
import com.splitforge.app.ui.strings.Strings

class AndroidSummaryImageExporter(private val strings: Strings) : SummaryImageExporter {
    override fun export(summary: SummaryData): ByteArray {
        val width = 1080
        val padding = 64f
        val titleSize = 56f
        val bodySize = 40f
        val lineHeight = 52f
        val lines = buildLines(summary)
        val height = (padding * 2 + lineHeight * lines.size + 40).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = titleSize
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = bodySize
        }

        var y = padding + titleSize
        lines.forEachIndexed { index, line ->
            val paint = if (index == 0) titlePaint else bodyPaint
            canvas.drawText(line, padding, y, paint)
            y += lineHeight
        }

        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun buildLines(summary: SummaryData): List<String> {
        val lines = mutableListOf<String>()
        lines.add(summary.title)
        lines.add(summary.templateName)
        val mode = if (summary.isDoubles) strings.doubles else strings.solo
        lines.add("$mode · ${TimeFormatter.formatSeconds(summary.totalSeconds, strings)}")
        lines.add("${strings.athleteATitle}: ${summary.athleteAName}")
        summary.athleteBName?.let { lines.add("${strings.athleteBTitle}: $it") }
        summary.segments.forEach { segment ->
            lines.add(segmentLine(segment))
        }
        return lines
    }

    private fun segmentLine(segment: SummarySegment): String {
        val time = TimeFormatter.formatSeconds(segment.seconds, strings)
        val label = when (val seg = segment.segment) {
            is Segment.Run -> "${strings.runLabel} ${strings.formatDistance(seg.distanceKm)}"
            is Segment.Station -> seg.name
        }
        val assignee = segment.assignedAthlete?.let { " ($it)" } ?: ""
        return "${segment.index + 1}. $label$assignee · $time"
    }
}
