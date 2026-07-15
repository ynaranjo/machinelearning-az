package com.kidsguard.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Almacén central de configuración: PIN, apps permitidas, límites y uso diario.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    // ---------- PIN ----------

    val isPinSet: Boolean
        get() = prefs.getString(KEY_PIN_HASH, null) != null

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        prefs.edit()
            .putString(KEY_PIN_SALT, saltB64)
            .putString(KEY_PIN_HASH, hash(pin, saltB64))
            .apply()
    }

    fun checkPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        return prefs.getString(KEY_PIN_HASH, null) == hash(pin, salt)
    }

    private fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((salt + pin).toByteArray())
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    // ---------- Apps permitidas ----------

    var allowedApps: Set<String>
        get() = prefs.getStringSet(KEY_ALLOWED_APPS, emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_ALLOWED_APPS, value.toSet()).apply()

    // ---------- Modo niños ----------

    var childModeActive: Boolean
        get() = prefs.getBoolean(KEY_CHILD_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_CHILD_MODE, value).apply()

    // ---------- Límite diario ----------

    /** Minutos por día. -1 = sin límite. */
    var dailyLimitMinutes: Int
        get() = prefs.getInt(KEY_DAILY_LIMIT, -1)
        set(value) = prefs.edit().putInt(KEY_DAILY_LIMIT, value).apply()

    // ---------- Hora de dormir ----------

    var bedtimeEnabled: Boolean
        get() = prefs.getBoolean(KEY_BEDTIME_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BEDTIME_ENABLED, value).apply()

    /** Minutos desde medianoche. */
    var bedtimeStartMinutes: Int
        get() = prefs.getInt(KEY_BEDTIME_START, 21 * 60)
        set(value) = prefs.edit().putInt(KEY_BEDTIME_START, value).apply()

    var bedtimeEndMinutes: Int
        get() = prefs.getInt(KEY_BEDTIME_END, 7 * 60)
        set(value) = prefs.edit().putInt(KEY_BEDTIME_END, value).apply()

    // ---------- Límites por app ----------

    /** Mapa paquete -> minutos por día. */
    fun appLimits(): Map<String, Int> {
        val json = JSONObject(prefs.getString(KEY_APP_LIMITS, "{}") ?: "{}")
        val map = mutableMapOf<String, Int>()
        json.keys().forEach { key -> map[key] = json.getInt(key) }
        return map
    }

    fun appLimitFor(packageName: String): Int? = appLimits()[packageName]

    /** minutes null o <= 0 elimina el límite. */
    fun setAppLimit(packageName: String, minutes: Int?) {
        val json = JSONObject(prefs.getString(KEY_APP_LIMITS, "{}") ?: "{}")
        if (minutes == null || minutes <= 0) {
            json.remove(packageName)
        } else {
            json.put(packageName, minutes)
        }
        prefs.edit().putString(KEY_APP_LIMITS, json.toString()).apply()
    }

    // ---------- Uso diario ----------

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun usageJson(): JSONObject {
        val storedDate = prefs.getString(KEY_USAGE_DATE, null)
        return if (storedDate == today()) {
            JSONObject(prefs.getString(KEY_USAGE_MAP, "{}") ?: "{}")
        } else {
            JSONObject()
        }
    }

    fun addUsageSeconds(packageName: String, seconds: Int) {
        val json = usageJson()
        json.put(packageName, json.optInt(packageName, 0) + seconds)
        prefs.edit()
            .putString(KEY_USAGE_DATE, today())
            .putString(KEY_USAGE_MAP, json.toString())
            .apply()
    }

    fun usageSecondsFor(packageName: String): Int =
        usageJson().optInt(packageName, 0)

    fun totalUsageSecondsToday(): Int {
        val json = usageJson()
        var total = 0
        json.keys().forEach { key -> total += json.getInt(key) }
        return total
    }

    fun usageMapToday(): Map<String, Int> {
        val json = usageJson()
        val map = mutableMapOf<String, Int>()
        json.keys().forEach { key -> map[key] = json.getInt(key) }
        return map
    }

    companion object {
        private const val FILE_NAME = "kidsguard_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_ALLOWED_APPS = "allowed_apps"
        private const val KEY_CHILD_MODE = "child_mode_active"
        private const val KEY_DAILY_LIMIT = "daily_limit_minutes"
        private const val KEY_BEDTIME_ENABLED = "bedtime_enabled"
        private const val KEY_BEDTIME_START = "bedtime_start_minutes"
        private const val KEY_BEDTIME_END = "bedtime_end_minutes"
        private const val KEY_APP_LIMITS = "app_limits_json"
        private const val KEY_USAGE_DATE = "usage_date"
        private const val KEY_USAGE_MAP = "usage_map_json"
    }
}
