package com.splitforge.app.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Segment {
    @Serializable
    @SerialName("run")
    data class Run(val distanceKm: Double) : Segment()

    @Serializable
    @SerialName("station")
    data class Station(
        val name: String,
        val category: StationCategory,
        val difficulty: Int,
        val baseTimeSeconds: Int
    ) : Segment()
}

@Serializable
enum class StationCategory {
    ENGINE,
    STRENGTH,
    MIXED
}

@Serializable
data class Template(
    val id: String,
    val name: String,
    val segments: List<Segment>
)

@Serializable
data class Athlete(
    val id: String,
    val name: String,
    val runPaceMinPerKm: Double,
    val engineRating: Int,
    val strengthRating: Int
)

@Serializable
data class Plan(
    val templateId: String,
    val isDoubles: Boolean,
    val stationAssignments: Map<Int, String>
)

@Serializable
data class AppState(
    val templates: List<Template>,
    val athleteA: Athlete,
    val athleteB: Athlete?,
    val lastSelectedTemplateId: String?,
    val lastPlan: Plan?,
    val isDoubles: Boolean,
    val isProCached: Boolean
)
