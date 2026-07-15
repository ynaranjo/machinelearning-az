package com.kidsguard.app.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.kidsguard.app.R

/**
 * Administrador de dispositivo: mientras esté activo, Android exige
 * desactivarlo (y por tanto conocer el PIN de ajustes) antes de poder
 * desinstalar KidsGuard.
 */
class AdminReceiver : DeviceAdminReceiver() {

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        context.getString(R.string.admin_disable_warning)
}
