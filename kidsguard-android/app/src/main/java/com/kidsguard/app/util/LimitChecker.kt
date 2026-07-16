package com.kidsguard.app.util

/**
 * Aritmética pura de los límites de tiempo (sin dependencias de Android),
 * testable con JUnit. La usa BlockEvaluator.
 *
 * Un límite negativo significa «sin límite». Los minutos extra concedidos
 * por un adulto se suman siempre al máximo permitido.
 */
object LimitChecker {

    /** ¿Se alcanzó un límite en minutos, dado el uso en segundos? */
    fun isLimitReached(usageSeconds: Int, limitMinutes: Int, extraMinutes: Int = 0): Boolean {
        if (limitMinutes < 0) return false
        val allowedSeconds = (limitMinutes + extraMinutes) * 60
        return usageSeconds >= allowedSeconds
    }

    /** Segundos restantes hasta el límite (0 si se alcanzó o no hay límite). */
    fun remainingSeconds(usageSeconds: Int, limitMinutes: Int, extraMinutes: Int = 0): Int {
        if (limitMinutes < 0) return 0
        val allowedSeconds = (limitMinutes + extraMinutes) * 60
        return (allowedSeconds - usageSeconds).coerceAtLeast(0)
    }
}
