package com.splitforge.app.util

import kotlin.math.roundToInt

object PaceParser {
    fun parse(input: String): Double? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        return if (":" in trimmed) {
            val parts = trimmed.split(":")
            if (parts.size != 2) return null
            val minutes = parts[0].toDoubleOrNull() ?: return null
            val seconds = parts[1].toDoubleOrNull() ?: return null
            if (seconds >= 60 || seconds < 0) return null
            minutes + (seconds / 60.0)
        } else {
            trimmed.toDoubleOrNull()
        }
    }

    fun format(paceMinPerKm: Double): String {
        val totalSeconds = (paceMinPerKm * 60.0).roundToInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
