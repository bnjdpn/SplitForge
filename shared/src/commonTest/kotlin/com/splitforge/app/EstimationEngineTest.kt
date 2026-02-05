package com.splitforge.app

import com.splitforge.app.domain.Athlete
import com.splitforge.app.domain.EstimationEngine
import com.splitforge.app.domain.Plan
import com.splitforge.app.domain.Segment
import com.splitforge.app.domain.StationCategory
import com.splitforge.app.domain.Template
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EstimationEngineTest {
    private val athleteA = Athlete(
        id = "A",
        name = "A",
        runPaceMinPerKm = 5.0,
        engineRating = 5,
        strengthRating = 3
    )

    private val athleteB = Athlete(
        id = "B",
        name = "B",
        runPaceMinPerKm = 6.0,
        engineRating = 2,
        strengthRating = 4
    )

    @Test
    fun runSecondsUsesPace() {
        val seconds = EstimationEngine.estimateRunSeconds(1.0, 5.0)
        assertEquals(300, seconds)
    }

    @Test
    fun stationSecondsApplyRatingAndDifficulty() {
        val station = Segment.Station(
            name = "Station 1",
            category = StationCategory.ENGINE,
            difficulty = 3,
            baseTimeSeconds = 200
        )
        val seconds = EstimationEngine.estimateStationSeconds(station, athleteA)
        assertEquals(172, seconds)
    }

    @Test
    fun totalSecondsIncludesTransitions() {
        val template = Template(
            id = "T1",
            name = "Test",
            segments = listOf(
                Segment.Run(distanceKm = 1.0),
                Segment.Station(
                    name = "Station 1",
                    category = StationCategory.STRENGTH,
                    difficulty = 3,
                    baseTimeSeconds = 120
                )
            )
        )
        val plan = Plan(
            templateId = "T1",
            isDoubles = false,
            stationAssignments = mapOf(1 to athleteA.id)
        )
        val total = EstimationEngine.estimateTotalSeconds(template, athleteA, athleteB, plan)
        val runSeconds = 300
        val stationSeconds = EstimationEngine.estimateStationSeconds(template.segments[1] as Segment.Station, athleteA)
        val expected = runSeconds + stationSeconds + (2 * EstimationEngine.transitionPenaltySeconds)
        assertEquals(expected, total)
    }

    @Test
    fun mixedCategoryUsesAverage() {
        val station = Segment.Station(
            name = "Station 2",
            category = StationCategory.MIXED,
            difficulty = 4,
            baseTimeSeconds = 150
        )
        val seconds = EstimationEngine.estimateStationSeconds(station, athleteB)
        assertTrue(seconds > 0)
    }
}
