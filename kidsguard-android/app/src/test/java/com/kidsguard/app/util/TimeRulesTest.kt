package com.kidsguard.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TimeRulesTest {

    private fun at(hour: Int, minute: Int): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

    // ---------- isInBedtime ----------

    @Test
    fun `mismo inicio y fin nunca bloquea`() {
        assertFalse(TimeRules.isInBedtime(21 * 60, 21 * 60, at(21, 0)))
    }

    @Test
    fun `rango normal dentro del mismo dia`() {
        val start = 13 * 60
        val end = 15 * 60
        assertFalse(TimeRules.isInBedtime(start, end, at(12, 59)))
        assertTrue(TimeRules.isInBedtime(start, end, at(13, 0)))
        assertTrue(TimeRules.isInBedtime(start, end, at(14, 30)))
        assertFalse(TimeRules.isInBedtime(start, end, at(15, 0)))
    }

    @Test
    fun `rango que cruza medianoche`() {
        val start = 21 * 60
        val end = 7 * 60
        assertTrue(TimeRules.isInBedtime(start, end, at(21, 0)))
        assertTrue(TimeRules.isInBedtime(start, end, at(23, 59)))
        assertTrue(TimeRules.isInBedtime(start, end, at(0, 0)))
        assertTrue(TimeRules.isInBedtime(start, end, at(6, 59)))
        assertFalse(TimeRules.isInBedtime(start, end, at(7, 0)))
        assertFalse(TimeRules.isInBedtime(start, end, at(12, 0)))
        assertFalse(TimeRules.isInBedtime(start, end, at(20, 59)))
    }

    // ---------- formatDuration ----------

    @Test
    fun `duraciones menores a una hora se muestran en minutos`() {
        assertEquals("0m", TimeRules.formatDuration(0))
        assertEquals("0m", TimeRules.formatDuration(59))
        assertEquals("1m", TimeRules.formatDuration(60))
        assertEquals("59m", TimeRules.formatDuration(59 * 60 + 59))
    }

    @Test
    fun `duraciones de una hora o mas incluyen horas y minutos`() {
        assertEquals("1h 00m", TimeRules.formatDuration(3600))
        assertEquals("1h 05m", TimeRules.formatDuration(3900))
        assertEquals("2h 30m", TimeRules.formatDuration(2 * 3600 + 30 * 60))
    }

    // ---------- formatTimeOfDay ----------

    @Test
    fun `hora del dia con relleno de ceros`() {
        assertEquals("21:00", TimeRules.formatTimeOfDay(21 * 60))
        assertEquals("07:05", TimeRules.formatTimeOfDay(7 * 60 + 5))
        assertEquals("00:00", TimeRules.formatTimeOfDay(0))
    }
}
