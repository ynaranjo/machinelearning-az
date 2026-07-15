package com.kidsguard.app.util

import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.ui.block.BlockedActivity

/**
 * Lógica de bloqueo compartida entre el servicio de accesibilidad
 * (bloqueo instantáneo) y el servicio de sondeo (respaldo + contador de uso).
 */
object BlockEvaluator {

    private const val BLOCK_COOLDOWN_MS = 1500L

    @Volatile
    private var lastBlockAt = 0L

    private var imePackagesCache: Set<String>? = null
    private var homePackagesCache: Set<String>? = null

    /**
     * Paquetes del sistema que nunca se bloquean: interfaz del sistema,
     * diálogos de permisos y apps de emergencia/llamadas por seguridad.
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

    fun isIgnored(context: Context, packageName: String): Boolean {
        if (packageName == context.packageName) return true
        if (packageName in IGNORED_PACKAGES) return true
        // Los teclados generan eventos de ventana propios: no bloquearlos.
        if (packageName in imePackages(context)) return true
        // No bloquear launchers: evitaría volver a la pantalla de inicio.
        if (packageName in homePackages(context)) return true
        return false
    }

    /** Devuelve el motivo de bloqueo, o null si la app puede usarse ahora. */
    fun evaluate(context: Context, prefs: PreferencesManager, packageName: String): BlockReason? {
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

    /** Muestra la pantalla de bloqueo (con cooldown) y avisa al adulto. */
    fun block(context: Context, packageName: String, reason: BlockReason) {
        val now = System.currentTimeMillis()
        if (now - lastBlockAt < BLOCK_COOLDOWN_MS) return
        lastBlockAt = now

        context.startActivity(
            Intent(context, BlockedActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(BlockedActivity.EXTRA_REASON, reason.name)
                .putExtra(BlockedActivity.EXTRA_PACKAGE, packageName)
        )

        if (reason == BlockReason.NOT_ALLOWED) {
            AdultNotifier.notifyBlockedAttempt(context, packageName)
        }
    }

    private fun imePackages(context: Context): Set<String> {
        imePackagesCache?.let { return it }
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.inputMethodList.map { it.packageName }.toSet()
            .also { imePackagesCache = it }
    }

    private fun homePackages(context: Context): Set<String> {
        homePackagesCache?.let { return it }
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return context.packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName }
            .filter { it != context.packageName }
            .toSet()
            .also { homePackagesCache = it }
    }
}
