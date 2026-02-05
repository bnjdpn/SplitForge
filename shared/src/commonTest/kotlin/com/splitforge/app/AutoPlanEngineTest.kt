package com.splitforge.app

import com.splitforge.app.domain.Athlete
import com.splitforge.app.domain.AutoPlanEngine
import com.splitforge.app.domain.Segment
import com.splitforge.app.domain.StationCategory
import com.splitforge.app.domain.Template
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AutoPlanEngineTest {
    @Test
    fun suggestSplitBalancesAssignments() {
        val athleteA = Athlete("A", "A", 5.0, 5, 2)
        val athleteB = Athlete("B", "B", 5.2, 2, 5)
        val template = Template(
            id = "T1",
            name = "Test",
            segments = buildList {
                repeat(6) { index ->
                    add(
                        Segment.Station(
                            name = "Station ${index + 1}",
                            category = if (index % 2 == 0) StationCategory.ENGINE else StationCategory.STRENGTH,
                            difficulty = 3,
                            baseTimeSeconds = 180
                        )
                    )
                }
            }
        )
        val assignments = AutoPlanEngine.suggestAssignments(template, athleteA, athleteB)
        val countA = assignments.values.count { it == athleteA.id }
        val countB = assignments.values.count { it == athleteB.id }
        assertTrue(kotlin.math.abs(countA - countB) <= 2)
        assertEquals(6, assignments.size)
    }

    @Test
    fun suggestSplitChoosesFasterWhenBalanced() {
        val athleteA = Athlete("A", "A", 5.0, 5, 5)
        val athleteB = Athlete("B", "B", 5.0, 1, 1)
        val template = Template(
            id = "T2",
            name = "Test",
            segments = listOf(
                Segment.Station(
                    name = "Station 1",
                    category = StationCategory.ENGINE,
                    difficulty = 3,
                    baseTimeSeconds = 200
                ),
                Segment.Station(
                    name = "Station 2",
                    category = StationCategory.ENGINE,
                    difficulty = 3,
                    baseTimeSeconds = 200
                )
            )
        )
        val assignments = AutoPlanEngine.suggestAssignments(template, athleteA, athleteB)
        val countA = assignments.values.count { it == athleteA.id }
        assertTrue(countA >= 1)
    }
}
