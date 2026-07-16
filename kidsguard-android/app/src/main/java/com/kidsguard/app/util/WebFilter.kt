package com.kidsguard.app.util

import android.net.Uri
import com.kidsguard.app.data.PreferencesManager

/**
 * Reglas de filtrado web aplicadas por el navegador infantil.
 *
 * Dos modos por perfil:
 *  - LISTA NEGRA (por defecto): se permite todo salvo los dominios bloqueados.
 *  - LISTA BLANCA: solo se permiten los dominios de la lista (todo lo demás
 *    queda bloqueado); ideal para los más pequeños.
 *
 * En ambos modos se fuerza SafeSearch en los buscadores principales y el
 * modo restringido de YouTube (vía cabecera, en el navegador).
 */
object WebFilter {

    fun isAllowed(prefs: PreferencesManager, url: String): Boolean {
        val host = hostOf(url) ?: return true
        val blocked = prefs.blockedDomains.any { host.matchesDomain(it) }
        return if (prefs.webWhitelistMode) {
            prefs.allowedDomains.any { host.matchesDomain(it) } && !blocked
        } else {
            !blocked
        }
    }

    /**
     * Devuelve la URL con SafeSearch forzado si es un buscador conocido;
     * si no, la misma URL.
     */
    fun withSafeSearch(url: String): String {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return url
        val host = uri.host?.lowercase() ?: return url
        return when {
            host.contains("google.") && uri.getQueryParameter("safe") == null ->
                appendParam(uri, "safe", "active")
            host.contains("bing.") && uri.getQueryParameter("adlt") == null ->
                appendParam(uri, "adlt", "strict")
            host.contains("duckduckgo.") && uri.getQueryParameter("kp") == null ->
                appendParam(uri, "kp", "1")
            else -> url
        }
    }

    /** ¿El host es YouTube? (para activar el modo restringido por cabecera). */
    fun isYouTube(url: String): Boolean =
        hostOf(url)?.let {
            it.contains("youtube.") || it.contains("youtube-nocookie.")
        } == true

    /** Normaliza lo que escribe el adulto a un dominio limpio ("ejemplo.com"). */
    fun normalizeDomain(input: String): String =
        input.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore('/')

    private fun appendParam(uri: Uri, key: String, value: String): String =
        uri.buildUpon().appendQueryParameter(key, value).build().toString()

    private fun hostOf(url: String): String? =
        runCatching { Uri.parse(url).host?.lowercase() }.getOrNull()

    /** El host coincide con el dominio o es un subdominio suyo. */
    private fun String.matchesDomain(domain: String): Boolean {
        val d = domain.trim().lowercase().removePrefix("www.")
        if (d.isEmpty()) return false
        val h = removePrefix("www.")
        return h == d || h.endsWith(".$d")
    }
}
