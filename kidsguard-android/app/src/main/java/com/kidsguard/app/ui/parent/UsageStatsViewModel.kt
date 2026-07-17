package com.kidsguard.app.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kidsguard.app.data.AppRepository
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel del uso de hoy (MVVM + corrutinas): total del día, lista de apps
 * permitidas ordenada por uso y gestión del límite por app del perfil activo.
 *
 * La consulta a PackageManager y la carga de iconos se hacen fuera del hilo
 * principal; el resultado se publica en un StateFlow observable.
 */
class UsageStatsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PreferencesManager(app)

    data class UsageData(
        val apps: List<AppInfo>,
        val usageSeconds: Map<String, Int>,
        val limits: Map<String, Int>,
        val totalSeconds: Int
    )

    private val _state = MutableStateFlow<UsageData?>(null)
    val state: StateFlow<UsageData?> = _state.asStateFlow()

    /** Recalcula los datos de uso en segundo plano y los publica. */
    fun refresh() {
        viewModelScope.launch {
            _state.value = withContext(Dispatchers.Default) { compute() }
        }
    }

    private fun compute(): UsageData {
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
        refresh()
    }
}
