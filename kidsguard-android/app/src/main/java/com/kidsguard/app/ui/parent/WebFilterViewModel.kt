package com.kidsguard.app.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.util.WebFilter

/**
 * ViewModel del filtrado web (semilla de la migración a MVVM). Concentra la
 * lógica de estado y persistencia del perfil activo, de modo que la Activity
 * solo pinta y delega. El resto de pantallas seguirán este mismo patrón.
 */
class WebFilterViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PreferencesManager(app)

    var browserEnabled: Boolean
        get() = prefs.browserEnabled
        set(value) { prefs.browserEnabled = value }

    var whitelistMode: Boolean
        get() = prefs.webWhitelistMode
        set(value) { prefs.webWhitelistMode = value }

    /** Dominios de la lista activa (blanca o negra) ordenados alfabéticamente. */
    fun currentDomains(): List<String> =
        (if (whitelistMode) prefs.allowedDomains else prefs.blockedDomains).sorted()

    /** Añade un dominio (normalizado) a la lista activa. Devuelve false si es inválido. */
    fun addDomain(input: String): Boolean {
        val domain = WebFilter.normalizeDomain(input)
        if (domain.isEmpty()) return false
        save(current() + domain)
        return true
    }

    fun removeDomain(domain: String) {
        save(current() - domain)
    }

    private fun current(): Set<String> =
        if (whitelistMode) prefs.allowedDomains else prefs.blockedDomains

    private fun save(domains: Set<String>) {
        if (whitelistMode) prefs.allowedDomains = domains
        else prefs.blockedDomains = domains
    }
}
