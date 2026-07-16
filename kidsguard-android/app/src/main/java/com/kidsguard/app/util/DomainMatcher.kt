package com.kidsguard.app.util

/**
 * Lógica pura de coincidencia de dominios (sin dependencias de Android),
 * de modo que puede probarse con tests unitarios normales. La usa WebFilter.
 */
object DomainMatcher {

    /** Normaliza lo que escribe el adulto a un dominio limpio ("ejemplo.com"). */
    fun normalize(input: String): String =
        input.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore('/')
            .substringBefore('?')

    /** El host coincide con el dominio o es un subdominio suyo. */
    fun matches(host: String, domain: String): Boolean {
        val d = domain.trim().lowercase().removePrefix("www.")
        if (d.isEmpty()) return false
        val h = host.trim().lowercase().removePrefix("www.")
        return h == d || h.endsWith(".$d")
    }

    /**
     * Decide si un host puede abrirse según el modo y las listas del perfil.
     *  - whitelistMode = true: solo los dominios de [allowed] (y no bloqueados).
     *  - whitelistMode = false: todo salvo los de [blocked].
     */
    fun isAllowed(
        host: String,
        whitelistMode: Boolean,
        blocked: Set<String>,
        allowed: Set<String>
    ): Boolean {
        val isBlocked = blocked.any { matches(host, it) }
        return if (whitelistMode) {
            allowed.any { matches(host, it) } && !isBlocked
        } else {
            !isBlocked
        }
    }
}
