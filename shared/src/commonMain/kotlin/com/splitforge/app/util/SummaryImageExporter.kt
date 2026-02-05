package com.splitforge.app.util

import com.splitforge.app.domain.Segment

interface SummaryImageExporter {
    fun export(summary: SummaryData): ByteArray
}

data class SummaryData(
    val title: String,
    val templateName: String,
    val isDoubles: Boolean,
    val totalSeconds: Int,
    val athleteAName: String,
    val athleteBName: String?,
    val segments: List<SummarySegment>
)

data class SummarySegment(
    val index: Int,
    val segment: Segment,
    val seconds: Int,
    val assignedAthlete: String?
)
