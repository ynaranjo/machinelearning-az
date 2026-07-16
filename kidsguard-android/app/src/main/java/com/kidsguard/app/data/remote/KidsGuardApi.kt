package com.kidsguard.app.data.remote

import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente REST mínimo (sin dependencias externas) para el backend de
 * KidsGuard. Todas las llamadas son síncronas y deben ejecutarse fuera
 * del hilo principal.
 */
class KidsGuardApi(private val baseUrl: String) {

    data class PairResult(val deviceId: String, val token: String)

    /** Empareja este dispositivo con una familia usando el código del adulto. */
    fun pair(familyCode: String, deviceName: String): PairResult {
        val body = JSONObject()
            .put("familyCode", familyCode)
            .put("deviceName", deviceName)
        val response = post("/api/pair", body, token = null)
        return PairResult(
            deviceId = response.getString("deviceId"),
            token = response.getString("token")
        )
    }

    /** Envía el estado actual (configuración + uso) del dispositivo. */
    fun pushSnapshot(deviceId: String, token: String, snapshot: JSONObject) {
        post("/api/devices/$deviceId/snapshot", snapshot, token)
    }

    /**
     * Descarga los comandos remotos pendientes emitidos por el adulto
     * y los marca como recibidos.
     */
    fun pullCommands(deviceId: String, token: String): List<RemoteCommand> {
        val response = get("/api/devices/$deviceId/commands", token)
        val array = response.optJSONArray("commands") ?: return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            val obj = array.optJSONObject(i) ?: return@mapNotNull null
            RemoteCommand(
                id = obj.optString("id"),
                type = obj.optString("type"),
                payload = obj.optJSONObject("payload") ?: JSONObject()
            )
        }
    }

    // ---------- HTTP ----------

    private fun post(path: String, body: JSONObject, token: String?): JSONObject =
        request("POST", path, body, token)

    private fun get(path: String, token: String?): JSONObject =
        request("GET", path, null, token)

    private fun request(
        method: String,
        path: String,
        body: JSONObject?,
        token: String?
    ): JSONObject {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            token?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }
        return try {
            if (body != null) {
                connection.outputStream.use { it.write(body.toString().toByteArray()) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            if (code !in 200..299) {
                throw ApiException(code, text)
            }
            if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    class ApiException(val code: Int, message: String) : Exception("HTTP $code: $message")

    companion object {
        private const val TIMEOUT_MS = 15_000
    }
}
