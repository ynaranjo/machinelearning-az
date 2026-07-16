package com.kidsguard.app.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.kidsguard.app.data.PreferencesManager

/**
 * ViewModel de los límites de tiempo (MVVM): límite diario, horario de
 * dormir y límites por categoría del perfil activo.
 */
class TimeLimitsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PreferencesManager(app)

    // ---------- Límite diario ----------

    /** -1 = sin límite. */
    var dailyLimitMinutes: Int
        get() = prefs.dailyLimitMinutes
        set(value) { prefs.dailyLimitMinutes = value }

    // ---------- Hora de dormir ----------

    var bedtimeEnabled: Boolean
        get() = prefs.bedtimeEnabled
        set(value) { prefs.bedtimeEnabled = value }

    var bedtimeStartMinutes: Int
        get() = prefs.bedtimeStartMinutes
        set(value) { prefs.bedtimeStartMinutes = value }

    var bedtimeEndMinutes: Int
        get() = prefs.bedtimeEndMinutes
        set(value) { prefs.bedtimeEndMinutes = value }

    // ---------- Límites por categoría ----------

    fun categoryLimits(): Map<Int, Int> = prefs.categoryLimits()

    fun categoryLimitFor(category: Int): Int? = prefs.categoryLimitFor(category)

    fun setCategoryLimit(category: Int, minutes: Int?) {
        prefs.setCategoryLimit(category, minutes)
    }
}
