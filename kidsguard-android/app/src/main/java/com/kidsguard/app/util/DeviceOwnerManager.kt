package com.kidsguard.app.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.ui.launcher.KidsHomeActivity

/**
 * Modo kiosco: cuando KidsGuard es *device owner* (propietario del
 * dispositivo, activado por ADB o aprovisionamiento QR), puede aplicar
 * políticas mucho más fuertes que el overlay:
 *
 *  - LockTask: el niño queda literalmente encerrado en las apps permitidas.
 *  - Launcher persistente sin diálogo del sistema.
 *  - Barra de estado desactivada (sin ajustes rápidos).
 *  - Modo seguro y restablecimiento de fábrica bloqueados.
 *
 * Todas las llamadas son no-op si la app no es device owner, de modo que
 * el resto del código puede invocarlas sin comprobar nada.
 */
object DeviceOwnerManager {

    fun isDeviceOwner(context: Context): Boolean =
        dpm(context).isDeviceOwnerApp(context.packageName)

    /** Políticas al ACTIVAR el modo niños. */
    fun applyChildModePolicies(context: Context, prefs: PreferencesManager) {
        if (!isDeviceOwner(context)) return
        val dpm = dpm(context)
        val admin = Permissions.adminComponent(context)
        runCatching {
            dpm.setLockTaskPackages(admin, lockTaskPackages(context, prefs))
            dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
            dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
            dpm.setStatusBarDisabled(admin, true)

            val homeFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            dpm.addPersistentPreferredActivity(
                admin, homeFilter,
                ComponentName(context, KidsHomeActivity::class.java)
            )
        }
    }

    /** Políticas al DESACTIVAR el modo niños. */
    fun clearChildModePolicies(context: Context) {
        if (!isDeviceOwner(context)) return
        val dpm = dpm(context)
        val admin = Permissions.adminComponent(context)
        runCatching {
            dpm.setLockTaskPackages(admin, arrayOf(context.packageName))
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
            dpm.setStatusBarDisabled(admin, false)
            dpm.clearPackagePersistentPreferredActivities(admin, context.packageName)
        }
    }

    /** Reaplica la lista de apps permitidas al LockTask (perfil/apps cambiaron). */
    fun refreshLockTaskPackages(context: Context, prefs: PreferencesManager) {
        if (!isDeviceOwner(context)) return
        runCatching {
            dpm(context).setLockTaskPackages(
                Permissions.adminComponent(context),
                lockTaskPackages(context, prefs)
            )
        }
    }

    private fun lockTaskPackages(context: Context, prefs: PreferencesManager): Array<String> =
        (prefs.allowedApps + context.packageName).toTypedArray()

    private fun dpm(context: Context): DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
}
