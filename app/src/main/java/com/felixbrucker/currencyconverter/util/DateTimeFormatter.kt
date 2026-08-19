package com.felixbrucker.currencyconverter.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.time.Instant

object DateTimeFormatter {
    fun formatRelative(timestamp: Long): String {
        if (timestamp <= 0) return "Never"
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val diffMillis = abs(diff)
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val isInThePast = diff >= 0
        fun makeTimeString(relativeTime: String): String {
            return if (isInThePast) {
                "$relativeTime ago"
            } else {
                "In $relativeTime"
            }
        }

        return when {
            diffSeconds < 10 -> "Just now"
            diffSeconds < 60 -> makeTimeString("${diffSeconds}s")
            diffMinutes == 1L -> makeTimeString("1 min")
            diffMinutes < 60 -> makeTimeString("$diffMinutes mins")
            diffHours == 1L -> makeTimeString("1 hour")
            diffHours < 24 -> makeTimeString("$diffHours hours")
            else -> {
                val sdf = SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun formatExact(timestamp: Long): String {
        if (timestamp <= 0) return "Not available"
        val sdf = SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatExact(instant: Instant): String {
        return formatExact(instant.toEpochMilliseconds())
    }

    fun formatRelative(instant: Instant): String {
        return formatRelative(instant.toEpochMilliseconds())
    }
}
