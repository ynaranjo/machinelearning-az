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
import com.kidsguard.app.util.AdultNotifier
import com.kidsguard.app.util.BlockEvaluator
import com.kidsguard.app.util.Permissions

/**
 * Servicio en primer plano que sondea la app activa cada segundo.
 *
 * Con el servicio de accesibilidad activo, el bloqueo instantáneo ocurre
 * allí; este servicio actúa como respaldo, lleva la cuenta del tiempo de
 * uso (para los límites diarios y por app) y vigila que los permisos de
 * protección sigan concedidos, avisando al adulto si se revocan.
 */
class AppMonitorService : Service() {

    private lateinit var prefs: PreferencesManager
    private val handler = Handler(Looper.getMainLooper())
    private var lastForegroundPackage: String? = null
    private var ticksSincePermissionCheck = 0

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
        maybeCheckPermissions()

        val foreground = currentForegroundPackage() ?: return
        if (BlockEvaluator.isIgnored(this, foreground)) return

        val reason = BlockEvaluator.evaluate(this, prefs, foreground)
        if (reason == null) {
            // App permitida y dentro de los límites: contabilizar uso.
            prefs.addUsageSeconds(foreground, (POLL_INTERVAL_MS / 1000L).toInt())
            return
        }
        BlockEvaluator.block(this, foreground, reason)
    }

    /** Cada ~60s comprueba que los permisos de protección sigan activos. */
    private fun maybeCheckPermissions() {
        if (++ticksSincePermissionCheck < PERMISSION_CHECK_TICKS) return
        ticksSincePermissionCheck = 0
        if (!Permissions.hasUsageAccess(this)) {
            AdultNotifier.notifyPermissionLost(this, getString(R.string.perm_usage_title))
        }
        if (!Permissions.hasOverlay(this)) {
            AdultNotifier.notifyPermissionLost(this, getString(R.string.perm_overlay_title))
        }
        if (!Permissions.hasAccessibility(this)) {
            AdultNotifier.notifyPermissionLost(
                this, getString(R.string.perm_accessibility_title)
            )
        }
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
        private const val PERMISSION_CHECK_TICKS = 60

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
