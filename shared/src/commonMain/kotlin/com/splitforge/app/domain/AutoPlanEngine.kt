package com.splitforge.app.domain

object AutoPlanEngine {
    fun suggestAssignments(template: Template, athleteA: Athlete, athleteB: Athlete): Map<Int, String> {
        val assignments = mutableMapOf<Int, String>()
        var countA = 0
        var countB = 0
        template.segments.forEachIndexed { index, segment ->
            if (segment is Segment.Station) {
                val aSeconds = EstimationEngine.estimateStationSeconds(segment, athleteA)
                val bSeconds = EstimationEngine.estimateStationSeconds(segment, athleteB)
                val preferA = aSeconds <= bSeconds
                val assignToA = if (preferA) {
                    (countA - countB) < 2
                } else {
                    (countB - countA) >= 2
                }
                if (assignToA) {
                    assignments[index] = athleteA.id
                    countA += 1
                } else {
                    assignments[index] = athleteB.id
                    countB += 1
                }
            }
        }
        return assignments
    }
}
