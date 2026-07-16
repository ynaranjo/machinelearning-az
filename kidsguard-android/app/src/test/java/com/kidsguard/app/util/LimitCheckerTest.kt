package com.kidsguard.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LimitCheckerTest {

    // ---------- isLimitReached ----------

    @Test
    fun `limite negativo nunca se alcanza`() {
        assertFalse(LimitChecker.isLimitReached(999_999, -1))
    }

    @Test
    fun `se alcanza al igualar o superar el limite`() {
        // 30 min = 1800 s
        assertFalse(LimitChecker.isLimitReached(1799, 30))
        assertTrue(LimitChecker.isLimitReached(1800, 30))
        assertTrue(LimitChecker.isLimitReached(2000, 30))
    }

    @Test
    fun `los minutos extra amplian el limite`() {
        // 30 + 15 = 45 min = 2700 s
        assertFalse(LimitChecker.isLimitReached(1800, 30, extraMinutes = 15))
        assertFalse(LimitChecker.isLimitReached(2699, 30, extraMinutes = 15))
        assertTrue(LimitChecker.isLimitReached(2700, 30, extraMinutes = 15))
    }

    @Test
    fun `limite cero se alcanza de inmediato`() {
        assertTrue(LimitChecker.isLimitReached(0, 0))
    }

    // ---------- remainingSeconds ----------

    @Test
    fun `restante sin limite es cero`() {
        assertEquals(0, LimitChecker.remainingSeconds(100, -1))
    }

    @Test
    fun `restante se calcula y nunca es negativo`() {
        assertEquals(1800, LimitChecker.remainingSeconds(0, 30))
        assertEquals(300, LimitChecker.remainingSeconds(1500, 30))
        assertEquals(0, LimitChecker.remainingSeconds(1800, 30))
        assertEquals(0, LimitChecker.remainingSeconds(9999, 30))
    }

    @Test
    fun `restante suma los minutos extra`() {
        // 30 + 30 = 60 min = 3600 s; usados 1800 -> quedan 1800
        assertEquals(1800, LimitChecker.remainingSeconds(1800, 30, extraMinutes = 30))
    }
}
