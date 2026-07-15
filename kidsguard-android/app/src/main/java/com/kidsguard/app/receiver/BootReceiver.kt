package com.kidsguard.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.service.AppMonitorService

/**
 * Reinicia la protección al encender el dispositivo o tras actualizar la app.
 * Como KidsGuard es el launcher predeterminado, la pantalla infantil
 * también se abre automáticamente al arrancar.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        val prefs = PreferencesManager(context)
        if (prefs.childModeActive) {
            AppMonitorService.start(context)
        }
    }
}
