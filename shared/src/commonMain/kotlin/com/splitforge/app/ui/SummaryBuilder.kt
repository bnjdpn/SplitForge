package com.splitforge.app.ui

import com.splitforge.app.domain.Athlete
import com.splitforge.app.domain.EstimationEngine
import com.splitforge.app.domain.Plan
import com.splitforge.app.domain.Segment
import com.splitforge.app.domain.Template
import com.splitforge.app.ui.strings.Strings
import com.splitforge.app.util.SummaryData
import com.splitforge.app.util.SummarySegment

object SummaryBuilder {
    fun build(
        template: Template,
        plan: Plan,
        athleteA: Athlete,
        athleteB: Athlete?,
        strings: Strings
    ): SummaryData {
        val segments = template.segments.mapIndexed { index, segment ->
            val seconds = EstimationEngine.estimateSegmentSeconds(segment, index, athleteA, athleteB, plan)
            val assigned = if (segment is Segment.Station) {
                val athleteId = plan.stationAssignments[index] ?: athleteA.id
                if (athleteId == athleteA.id) athleteA.name else athleteB?.name
            } else {
                null
            }
            SummarySegment(index = index, segment = segment, seconds = seconds, assignedAthlete = assigned)
        }
        val total = EstimationEngine.estimateTotalSeconds(template, athleteA, athleteB, plan)
        return SummaryData(
            title = strings.summaryTitle,
            templateName = template.name,
            isDoubles = plan.isDoubles,
            totalSeconds = total,
            athleteAName = athleteA.name,
            athleteBName = athleteB?.name,
            segments = segments
        )
    }
}
