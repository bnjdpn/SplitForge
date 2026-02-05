@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.splitforge.app.util

import com.splitforge.app.domain.Segment
import com.splitforge.app.ui.strings.Strings
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIRectFill
import platform.UIKit.drawAtPoint
import platform.posix.memcpy

class IosSummaryImageExporter(private val strings: Strings) : SummaryImageExporter {
    override fun export(summary: SummaryData): ByteArray {
        val width = 1080.0
        val padding = 64.0
        val titleSize = 56.0
        val bodySize = 40.0
        val lineHeight = 52.0
        val lines = buildLines(summary)
        val height = padding * 2 + lineHeight * lines.size + 40.0
        val format = UIGraphicsImageRendererFormat.defaultFormat()
        val renderer = UIGraphicsImageRenderer(size = CGSizeMake(width, height), format = format)
        val image = renderer.imageWithActions { _ ->
            UIColor.whiteColor.setFill()
            UIRectFill(CGRectMake(0.0, 0.0, width, height))

            val titleAttributes: Map<Any?, Any> = mapOf(
                platform.UIKit.NSFontAttributeName to UIFont.boldSystemFontOfSize(titleSize),
                platform.UIKit.NSForegroundColorAttributeName to UIColor.blackColor
            )
            val bodyAttributes: Map<Any?, Any> = mapOf(
                platform.UIKit.NSFontAttributeName to UIFont.systemFontOfSize(bodySize),
                platform.UIKit.NSForegroundColorAttributeName to UIColor.darkGrayColor
            )
            var y = padding
            lines.forEachIndexed { index, line ->
                val attributes = if (index == 0) titleAttributes else bodyAttributes
                val nsLine = line as NSString
                nsLine.drawAtPoint(CGPointMake(padding, y), withAttributes = attributes)
                y += lineHeight
            }
        }
        val data = UIImagePNGRepresentation(image)
        return data?.toByteArray() ?: ByteArray(0)
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

private fun NSData.toByteArray(): ByteArray {
    val length = length.toInt()
    val byteArray = ByteArray(length)
    byteArray.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length.convert())
    }
    return byteArray
}
