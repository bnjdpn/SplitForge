package com.splitforge.app.util

import com.splitforge.app.ui.strings.Strings

object TimeFormatter {
    fun formatSeconds(totalSeconds: Int, strings: Strings): String {
        var remaining = totalSeconds
        val hours = remaining / 3600
        remaining %= 3600
        val minutes = remaining / 60
        val seconds = remaining % 60
        return buildString {
            if (hours > 0) {
                append(hours)
                append(strings.hoursShort)
                append(" ")
            }
            if (minutes > 0 || hours > 0) {
                append(minutes)
                append(strings.minutesShort)
                append(" ")
            }
            append(seconds)
            append(strings.secondsShort)
        }.trim()
    }
}
