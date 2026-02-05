package com.splitforge.app.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object EstimationEngine {
    const val transitionPenaltySeconds = 6

    fun estimateRunSeconds(distanceKm: Double, paceMinPerKm: Double): Int {
        return (distanceKm * paceMinPerKm * 60.0).roundToInt()
    }

    fun estimateStationSeconds(station: Segment.Station, athlete: Athlete): Int {
        val rating = when (station.category) {
            StationCategory.ENGINE -> athlete.engineRating.toDouble()
            StationCategory.STRENGTH -> athlete.strengthRating.toDouble()
            StationCategory.MIXED -> (athlete.engineRating + athlete.strengthRating) / 2.0
        }
        var factor = 1.0 - ((rating - 3.0) * 0.07)
        factor *= (1 + (station.difficulty - 3) * 0.04)
        factor = min(1.40, max(0.70, factor))
        return (station.baseTimeSeconds * factor).roundToInt()
    }

    fun estimateTotalSeconds(
        template: Template,
        athleteA: Athlete,
        athleteB: Athlete?,
        plan: Plan
    ): Int {
        val segments = template.segments
        val runPace = if (plan.isDoubles && athleteB != null) {
            (athleteA.runPaceMinPerKm + athleteB.runPaceMinPerKm) / 2.0
        } else {
            athleteA.runPaceMinPerKm
        }
        val totalSegments = segments.size
        val base = segments.mapIndexed { index, segment ->
            when (segment) {
                is Segment.Run -> estimateRunSeconds(segment.distanceKm, runPace)
                is Segment.Station -> {
                    val assignedId = plan.stationAssignments[index] ?: athleteA.id
                    val athlete = if (assignedId == athleteA.id || athleteB == null) athleteA else athleteB
                    estimateStationSeconds(segment, athlete)
                }
            }
        }.sum()
        return base + (totalSegments * transitionPenaltySeconds)
    }

    fun estimateStationWorkloadSeconds(
        template: Template,
        athleteA: Athlete,
        athleteB: Athlete?,
        plan: Plan
    ): Pair<Int, Int> {
        var aSeconds = 0
        var bSeconds = 0
        template.segments.forEachIndexed { index, segment ->
            if (segment is Segment.Station) {
                val assignedId = plan.stationAssignments[index] ?: athleteA.id
                val forA = assignedId == athleteA.id || athleteB == null
                val seconds = if (forA) estimateStationSeconds(segment, athleteA) else estimateStationSeconds(segment, athleteB!!)
                if (forA) aSeconds += seconds else bSeconds += seconds
            }
        }
        return aSeconds to bSeconds
    }

    fun estimateSegmentSeconds(
        segment: Segment,
        index: Int,
        athleteA: Athlete,
        athleteB: Athlete?,
        plan: Plan
    ): Int {
        return when (segment) {
            is Segment.Run -> {
                val runPace = if (plan.isDoubles && athleteB != null) {
                    (athleteA.runPaceMinPerKm + athleteB.runPaceMinPerKm) / 2.0
                } else {
                    athleteA.runPaceMinPerKm
                }
                estimateRunSeconds(segment.distanceKm, runPace)
            }
            is Segment.Station -> {
                val assignedId = plan.stationAssignments[index] ?: athleteA.id
                val athlete = if (assignedId == athleteA.id || athleteB == null) athleteA else athleteB
                estimateStationSeconds(segment, athlete)
            }
        }
    }

    fun deltaSeconds(previousTotal: Int?, currentTotal: Int): Int {
        if (previousTotal == null) return 0
        return currentTotal - previousTotal
    }

    fun stationBalanceDifference(plan: Plan): Int {
        val counts = plan.stationAssignments.values.groupingBy { it }.eachCount()
        val a = counts.values.maxOrNull() ?: 0
        val b = counts.values.minOrNull() ?: 0
        return abs(a - b)
    }
}
