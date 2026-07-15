package com.kidsguard.app.service

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kidsguard.app.data.PreferencesManager
import java.util.concurrent.TimeUnit

/**
 * Vigilante periódico: si el modo niños está activo pero el sistema (o un
 * fabricante agresivo con la batería) mató el servicio de vigilancia, lo
 * vuelve a levantar. Se ejecuta cada 15 minutos (mínimo de WorkManager).
 */
class ServiceWatchdogWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val prefs = PreferencesManager(applicationContext)
        if (prefs.childModeActive) {
            // Con SYSTEM_ALERT_WINDOW concedido, la app está exenta de la
            // restricción de arrancar servicios foreground desde background.
            runCatching { AppMonitorService.start(applicationContext) }
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "kidsguard_watchdog"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ServiceWatchdogWorker>(
                15, TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
