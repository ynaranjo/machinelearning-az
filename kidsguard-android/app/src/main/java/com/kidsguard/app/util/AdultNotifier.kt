package com.kidsguard.app.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kidsguard.app.KidsGuardApp
import com.kidsguard.app.R
import com.kidsguard.app.ui.MainActivity

/**
 * Alertas para el adulto: intentos de abrir apps bloqueadas y
 * permisos de protección revocados. Con cooldown para no saturar.
 */
object AdultNotifier {

    private const val BLOCKED_COOLDOWN_MS = 10 * 60 * 1000L
    private const val PERMISSION_COOLDOWN_MS = 30 * 60 * 1000L

    private val lastNotifiedAt = mutableMapOf<String, Long>()

    fun notifyBlockedAttempt(context: Context, packageName: String) {
        if (!cooldownElapsed("blocked:$packageName", BLOCKED_COOLDOWN_MS)) return
        notify(
            context,
            id = packageName.hashCode(),
            title = context.getString(R.string.alert_blocked_attempt_title),
            text = context.getString(
                R.string.alert_blocked_attempt_text, appLabel(context, packageName)
            )
        )
    }

    fun notifyPermissionLost(context: Context, permissionLabel: String) {
        if (!cooldownElapsed("perm:$permissionLabel", PERMISSION_COOLDOWN_MS)) return
        notify(
            context,
            id = permissionLabel.hashCode(),
            title = context.getString(R.string.alert_permission_lost_title),
            text = context.getString(R.string.alert_permission_lost_text, permissionLabel)
        )
    }

    @Synchronized
    private fun cooldownElapsed(key: String, cooldownMs: Long): Boolean {
        val now = System.currentTimeMillis()
        val last = lastNotifiedAt[key] ?: 0L
        if (now - last < cooldownMs) return false
        lastNotifiedAt[key] = now
        return true
    }

    @SuppressLint("MissingPermission")
    private fun notify(context: Context, id: Int, title: String, text: String) {
        if (!Permissions.hasNotifications(context)) return
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, KidsGuardApp.CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permiso de notificaciones revocado entre la comprobación y el envío.
        }
    }

    private fun appLabel(context: Context, packageName: String): String =
        runCatching {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault(packageName)
}
