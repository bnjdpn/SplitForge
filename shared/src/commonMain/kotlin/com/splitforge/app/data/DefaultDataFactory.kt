package com.splitforge.app.data

import com.splitforge.app.domain.AppState
import com.splitforge.app.domain.Athlete
import com.splitforge.app.domain.Plan
import com.splitforge.app.domain.Segment
import com.splitforge.app.domain.StationCategory
import com.splitforge.app.domain.Template
import com.splitforge.app.ui.strings.Strings
import com.splitforge.app.util.IdGenerator

object DefaultDataFactory {
    fun create(strings: Strings): AppState {
        val template = defaultTemplate(strings)
        val athleteA = Athlete(
            id = IdGenerator.newId(),
            name = strings.athleteAName,
            runPaceMinPerKm = 5.0,
            engineRating = 3,
            strengthRating = 3
        )
        val athleteB = Athlete(
            id = IdGenerator.newId(),
            name = strings.athleteBName,
            runPaceMinPerKm = 5.2,
            engineRating = 3,
            strengthRating = 3
        )
        val plan = Plan(
            templateId = template.id,
            isDoubles = false,
            stationAssignments = template.segments.mapIndexedNotNull { index, segment ->
                if (segment is Segment.Station) index to athleteA.id else null
            }.toMap()
        )
        return AppState(
            templates = listOf(template),
            athleteA = athleteA,
            athleteB = athleteB,
            lastSelectedTemplateId = template.id,
            lastPlan = plan,
            isDoubles = false,
            isProCached = false
        )
    }

    fun defaultTemplate(strings: Strings): Template {
        val segments = buildList {
            repeat(8) { index ->
                add(Segment.Run(distanceKm = 1.0))
                val stationIndex = index + 1
                add(
                    Segment.Station(
                        name = strings.stationName(stationIndex),
                        category = if (stationIndex % 2 == 0) StationCategory.STRENGTH else StationCategory.ENGINE,
                        difficulty = 3,
                        baseTimeSeconds = 180
                    )
                )
            }
        }
        return Template(
            id = IdGenerator.newId(),
            name = strings.defaultTemplateName,
            segments = segments
        )
    }
}
