package com.kidsguard.app.data.remote

import org.json.JSONObject

/**
 * Comando remoto emitido por el adulto desde el panel web y aplicado en
 * el dispositivo del niño en la próxima sincronización.
 */
data class RemoteCommand(
    val id: String,
    val type: String,
    val payload: JSONObject
) {
    companion object {
        /** Conceder minutos extra hoy al perfil activo. payload: {minutes}. */
        const val TYPE_GRANT_EXTRA_MINUTES = "grant_extra_minutes"

        /** Activar/desactivar el modo niños. payload: {active}. */
        const val TYPE_SET_CHILD_MODE = "set_child_mode"

        /** Cambiar la lista de apps permitidas del perfil activo. payload: {packages:[...]}. */
        const val TYPE_SET_ALLOWED_APPS = "set_allowed_apps"

        /** Cambiar el límite diario del perfil activo. payload: {minutes}. */
        const val TYPE_SET_DAILY_LIMIT = "set_daily_limit"
    }
}
