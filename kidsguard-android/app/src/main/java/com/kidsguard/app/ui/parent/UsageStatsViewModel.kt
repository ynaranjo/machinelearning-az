package com.kidsguard.app.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.kidsguard.app.data.AppRepository
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.model.AppInfo

/**
 * ViewModel del uso de hoy (MVVM): total del día, lista de apps permitidas
 * ordenada por uso y gestión del límite por app del perfil activo.
 */
class UsageStatsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PreferencesManager(app)

    data class UsageData(
        val apps: List<AppInfo>,
        val usageSeconds: Map<String, Int>,
        val limits: Map<String, Int>,
        val totalSeconds: Int
    )

    /** Datos de uso de hoy, con las apps ordenadas de más a menos usadas. */
    fun load(): UsageData {
        val usage = prefs.usageMapToday()
        val apps = AppRepository.getAllowedApps(getApplication(), prefs.allowedApps)
            .sortedByDescending { usage[it.packageName] ?: 0 }
        return UsageData(
            apps = apps,
            usageSeconds = usage,
            limits = prefs.appLimits(),
            totalSeconds = prefs.totalUsageSecondsToday()
        )
    }

    fun appLimitFor(packageName: String): Int? = prefs.appLimitFor(packageName)

    fun setAppLimit(packageName: String, minutes: Int?) {
        prefs.setAppLimit(packageName, minutes)
    }
}
