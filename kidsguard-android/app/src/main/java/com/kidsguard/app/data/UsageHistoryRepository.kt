package com.kidsguard.app.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.kidsguard.app.data.db.KidsGuardDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Historial de uso persistente (Room). El contador «vivo» del día sigue en
 * PreferencesManager (lectura síncrona rápida para el bloqueo); este
 * repositorio acumula en memoria y vuelca a la base de datos cada 30 s,
 * conservando los últimos 30 días para los informes.
 */
object UsageHistoryRepository {

    private const val FLUSH_INTERVAL_MS = 30_000L
    private const val KEEP_DAYS = 30

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private data class PendingKey(val date: String, val packageName: String, val profileId: Int)

    private val pending = HashMap<PendingKey, Int>()
    private var lastFlushAt = 0L
    private var prunedThisProcess = false

    data class DayUsage(val date: String, val totalSeconds: Int)

    data class WeeklyReport(
        /** Últimos 7 días, del más antiguo al más reciente. */
        val days: List<DayUsage>,
        /** Top de apps de la semana: paquete -> segundos. */
        val topApps: List<Pair<String, Int>>
    )

    /** Acumula segundos de uso; vuelca a Room como máximo cada 30 s. */
    fun record(context: Context, profileId: Int, packageName: String, seconds: Int) {
        val batch: Map<PendingKey, Int>?
        synchronized(pending) {
            val key = PendingKey(today(), packageName, profileId)
            pending[key] = (pending[key] ?: 0) + seconds
            val now = System.currentTimeMillis()
            batch = if (now - lastFlushAt >= FLUSH_INTERVAL_MS) {
                lastFlushAt = now
                drainLocked()
            } else {
                null
            }
        }
        if (batch != null) writeAsync(context.applicationContext, batch)
    }

    /** Vuelca inmediatamente lo pendiente (antes de generar un informe). */
    fun flush(context: Context) {
        val batch = synchronized(pending) { drainLocked() }
        if (batch.isNotEmpty()) writeAsync(context.applicationContext, batch)
    }

    /** Calcula el informe de los últimos 7 días y lo entrega en el hilo principal. */
    fun weeklyReport(context: Context, profileId: Int, onResult: (WeeklyReport) -> Unit) {
        val appContext = context.applicationContext
        flush(appContext)
        executor.execute {
            val dao = KidsGuardDatabase.get(appContext).usageDao()
            val rows = dao.usageSince(profileId, dateDaysAgo(6))
            val days = (6 downTo 0).map { dateDaysAgo(it) }.map { date ->
                DayUsage(date, rows.filter { it.date == date }.sumOf { it.seconds })
            }
            val topApps = rows
                .groupBy { it.packageName }
                .mapValues { entry -> entry.value.sumOf { it.seconds } }
                .entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key to it.value }
            mainHandler.post { onResult(WeeklyReport(days, topApps)) }
        }
    }

    private fun drainLocked(): Map<PendingKey, Int> {
        val copy = HashMap(pending)
        pending.clear()
        return copy
    }

    private fun writeAsync(appContext: Context, batch: Map<PendingKey, Int>) {
        executor.execute {
            val dao = KidsGuardDatabase.get(appContext).usageDao()
            batch.forEach { (key, seconds) ->
                dao.addSeconds(key.date, key.packageName, key.profileId, seconds)
            }
            if (!prunedThisProcess) {
                prunedThisProcess = true
                dao.prune(dateDaysAgo(KEEP_DAYS))
            }
        }
    }

    private fun today(): String = dateDaysAgo(0)

    private fun dateDaysAgo(days: Int): String {
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }
}
