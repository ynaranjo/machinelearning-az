package com.kidsguard.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class KidsGuardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val monitor = NotificationChannel(
            CHANNEL_MONITOR,
            getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
        }
        val alerts = NotificationChannel(
            CHANNEL_ALERTS,
            getString(R.string.alert_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).apply {
            createNotificationChannel(monitor)
            createNotificationChannel(alerts)
        }
    }

    companion object {
        const val CHANNEL_MONITOR = "kidsguard_monitor"
        const val CHANNEL_ALERTS = "kidsguard_alerts"
    }
}
