package com.kidsguard.app.data.remote

import android.content.Context
import android.os.Build
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.service.AppMonitorService
import org.json.JSONArray
import org.json.JSONObject

/**
 * Orquesta la sincronización con el backend del adulto:
 *  1. Envía un snapshot (perfil activo, config, uso de hoy).
 *  2. Descarga y aplica los comandos remotos pendientes.
 *
 * Todas las operaciones son síncronas; se invocan desde el SyncWorker.
 */
object SyncManager {

    /** Empareja el dispositivo y guarda las credenciales. Devuelve error o null. */
    fun pair(context: Context, familyCode: String): String? {
        val prefs = PreferencesManager(context)
        val url = prefs.cloudBackendUrl
        if (url.isEmpty()) return "URL del servidor vacía"
        return try {
            val result = KidsGuardApi(url).pair(familyCode.trim(), deviceName())
            prefs.cloudDeviceId = result.deviceId
            prefs.cloudToken = result.token
            null
        } catch (e: Exception) {
            e.message ?: "Error de emparejamiento"
        }
    }

    /** Un ciclo completo de sincronización. Devuelve true si tuvo éxito. */
    fun syncOnce(context: Context): Boolean {
        val prefs = PreferencesManager(context)
        if (!prefs.cloudEnabled || !prefs.isPaired) return false
        val url = prefs.cloudBackendUrl
        if (url.isEmpty()) return false

        val api = KidsGuardApi(url)
        val deviceId = prefs.cloudDeviceId ?: return false
        val token = prefs.cloudToken ?: return false

        return try {
            api.pushSnapshot(deviceId, token, buildSnapshot(prefs))
            val commands = api.pullCommands(deviceId, token)
            commands.forEach { applyCommand(context, prefs, it) }
            prefs.cloudLastSync = System.currentTimeMillis()
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Estado que se envía al backend para que el adulto lo consulte. */
    private fun buildSnapshot(prefs: PreferencesManager): JSONObject {
        val profile = prefs.activeProfile
        val usage = JSONObject()
        prefs.usageMapToday().forEach { (pkg, seconds) -> usage.put(pkg, seconds) }

        return JSONObject()
            .put("childModeActive", prefs.childModeActive)
            .put(
                "activeProfile",
                JSONObject()
                    .put("id", profile.id)
                    .put("name", profile.name)
                    .put("emoji", profile.emoji)
            )
            .put("dailyLimitMinutes", prefs.dailyLimitMinutes)
            .put("extraMinutesToday", prefs.extraMinutesToday)
            .put("totalUsageSecondsToday", prefs.totalUsageSecondsToday())
            .put("allowedApps", JSONArray(prefs.allowedApps.toList()))
            .put("usageToday", usage)
            .put("reportedAt", System.currentTimeMillis())
    }

    /** Aplica un comando remoto al perfil activo del dispositivo. */
    private fun applyCommand(
        context: Context,
        prefs: PreferencesManager,
        command: RemoteCommand
    ) {
        when (command.type) {
            RemoteCommand.TYPE_GRANT_EXTRA_MINUTES -> {
                val minutes = command.payload.optInt("minutes", 0)
                if (minutes > 0) prefs.addExtraMinutesToday(minutes)
            }
            RemoteCommand.TYPE_SET_CHILD_MODE -> {
                val active = command.payload.optBoolean("active", prefs.childModeActive)
                prefs.childModeActive = active
                if (active) AppMonitorService.start(context)
                else AppMonitorService.stop(context)
            }
            RemoteCommand.TYPE_SET_ALLOWED_APPS -> {
                command.payload.optJSONArray("packages")?.let { array ->
                    prefs.allowedApps =
                        (0 until array.length()).map { array.getString(it) }.toSet()
                }
            }
            RemoteCommand.TYPE_SET_DAILY_LIMIT -> {
                if (command.payload.has("minutes")) {
                    prefs.dailyLimitMinutes = command.payload.getInt("minutes")
                }
            }
        }
    }

    private fun deviceName(): String =
        "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifEmpty { "Dispositivo Android" }
}
