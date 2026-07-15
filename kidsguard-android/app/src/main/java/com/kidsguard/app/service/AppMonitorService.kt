package com.kidsguard.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kidsguard.app.KidsGuardApp
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.ui.MainActivity
import com.kidsguard.app.ui.block.BlockedActivity
import com.kidsguard.app.util.BlockReason
import com.kidsguard.app.util.TimeRules

/**
 * Servicio en primer plano que vigila la app activa cada segundo.
 * Si la app no está permitida, o se agotó el tiempo, muestra la pantalla de bloqueo.
 */
class AppMonitorService : Service() {

    private lateinit var prefs: PreferencesManager
    private val handler = Handler(Looper.getMainLooper())
    private var lastForegroundPackage: String? = null
    private var lastBlockAt = 0L

    private val tick = object : Runnable {
        override fun run() {
            try {
                checkForegroundApp()
            } catch (_: Exception) {
                // Nunca dejar morir el bucle de vigilancia.
            }
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkForegroundApp() {
        if (!prefs.childModeActive) return

        val foreground = currentForegroundPackage() ?: return
        if (foreground == packageName || foreground in IGNORED_PACKAGES) return

        val reason = evaluate(foreground)
        if (reason == null) {
            // App permitida y dentro de los límites: contabilizar uso.
            prefs.addUsageSeconds(foreground, (POLL_INTERVAL_MS / 1000L).toInt())
            return
        }
        block(foreground, reason)
    }

    private fun evaluate(packageName: String): BlockReason? {
        if (packageName !in prefs.allowedApps) return BlockReason.NOT_ALLOWED

        if (prefs.bedtimeEnabled &&
            TimeRules.isInBedtime(prefs.bedtimeStartMinutes, prefs.bedtimeEndMinutes)
        ) {
            return BlockReason.BEDTIME
        }

        val dailyLimit = prefs.dailyLimitMinutes
        if (dailyLimit >= 0 && prefs.totalUsageSecondsToday() >= dailyLimit * 60) {
            return BlockReason.DAILY_LIMIT
        }

        val appLimit = prefs.appLimitFor(packageName)
        if (appLimit != null && prefs.usageSecondsFor(packageName) >= appLimit * 60) {
            return BlockReason.APP_LIMIT
        }
        return null
    }

    private fun block(blockedPackage: String, reason: BlockReason) {
        val now = System.currentTimeMillis()
        if (now - lastBlockAt < BLOCK_COOLDOWN_MS) return
        lastBlockAt = now

        val intent = Intent(this, BlockedActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(BlockedActivity.EXTRA_REASON, reason.name)
            .putExtra(BlockedActivity.EXTRA_PACKAGE, blockedPackage)
        startActivity(intent)
    }

    /**
     * Último paquete que pasó a primer plano según UsageStatsManager.
     * Requiere el permiso especial de acceso a datos de uso.
     */
    private fun currentForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(now - LOOKBACK_MS, now)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            @Suppress("DEPRECATION")
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundPackage = event.packageName
            }
        }
        return lastForegroundPackage
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, KidsGuardApp.CHANNEL_MONITOR)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val POLL_INTERVAL_MS = 1000L
        private const val LOOKBACK_MS = 10_000L
        private const val BLOCK_COOLDOWN_MS = 1500L

        /**
         * Paquetes del sistema que nunca se bloquean. Incluye la interfaz del sistema
         * y las apps de emergencia/llamadas entrantes por seguridad.
         */
        private val IGNORED_PACKAGES = setOf(
            "com.android.systemui",
            "android",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.android.emergency",
            "com.android.phone",
            "com.android.incallui",
            "com.google.android.dialer.incallui"
        )

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context, Intent(context, AppMonitorService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AppMonitorService::class.java))
        }
    }
}
