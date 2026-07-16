package com.kidsguard.app.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.data.remote.SyncManager
import java.util.concurrent.TimeUnit

/**
 * Sincronización periódica con el backend del adulto (cada 15 min, con
 * conexión disponible). Solo actúa si la nube está activada y emparejada.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val prefs = PreferencesManager(applicationContext)
        if (!prefs.cloudEnabled || !prefs.isPaired) return Result.success()
        return if (SyncManager.syncOnce(applicationContext)) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "kidsguard_cloud_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
