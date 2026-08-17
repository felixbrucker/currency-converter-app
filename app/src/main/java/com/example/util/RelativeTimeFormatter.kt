package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RelativeTimeFormatter {

    fun format(timestamp: Long): String {
        if (timestamp <= 0) return "Never"
        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        return when {
            diffSeconds < 10 -> "Just now"
            diffSeconds < 60 -> "${diffSeconds}s ago"
            diffMinutes == 1L -> "1 min ago"
            diffMinutes < 60 -> "$diffMinutes mins ago"
            diffHours == 1L -> "1 hour ago"
            diffHours < 24 -> "$diffHours hours ago"
            diffDays == 1L -> "Yesterday"
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
}
