package com.kidsguard.app.util

import java.util.Calendar
import java.util.Locale

object TimeRules {

    /** Comprueba si la hora actual cae dentro del horario de dormir (admite cruce de medianoche). */
    fun isInBedtime(startMinutes: Int, endMinutes: Int, now: Calendar = Calendar.getInstance()): Boolean {
        if (startMinutes == endMinutes) return false
        val current = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return if (startMinutes < endMinutes) {
            current in startMinutes until endMinutes
        } else {
            current >= startMinutes || current < endMinutes
        }
    }

    /** Formatea segundos como "1h 05m" o "23m". */
    fun formatDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val hours = minutes / 60
        val rest = minutes % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%dh %02dm", hours, rest)
        } else {
            String.format(Locale.getDefault(), "%dm", rest)
        }
    }

    /** Formatea minutos desde medianoche como "21:00". */
    fun formatTimeOfDay(minutesOfDay: Int): String =
        String.format(Locale.getDefault(), "%02d:%02d", minutesOfDay / 60, minutesOfDay % 60)
}
