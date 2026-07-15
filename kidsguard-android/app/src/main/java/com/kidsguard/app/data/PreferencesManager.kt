package com.kidsguard.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Almacén central de configuración: PIN, apps permitidas, límites y uso diario.
 *
 * Los datos se guardan en EncryptedSharedPreferences (Jetpack Security,
 * AES-256). Si el cifrado no está disponible en el dispositivo se usa el
 * almacén plano como último recurso. Los datos de una instalación anterior
 * sin cifrar se migran automáticamente la primera vez.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = obtainPrefs(context.applicationContext)

    // ---------- PIN ----------

    val isPinSet: Boolean
        get() = prefs.getString(KEY_PIN_HASH, null) != null

    fun setPin(pin: String) {
        val saltB64 = newSalt()
        prefs.edit()
            .putString(KEY_PIN_SALT, saltB64)
            .putString(KEY_PIN_HASH, hash(pin, saltB64))
            .apply()
    }

    fun checkPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        return prefs.getString(KEY_PIN_HASH, null) == hash(pin, salt)
    }

    // ---------- Recuperación de PIN (pregunta de seguridad) ----------

    val isSecurityQuestionSet: Boolean
        get() = prefs.getString(KEY_SEC_QUESTION, null) != null

    val securityQuestion: String?
        get() = prefs.getString(KEY_SEC_QUESTION, null)

    fun setSecurityQuestion(question: String, answer: String) {
        val saltB64 = newSalt()
        prefs.edit()
            .putString(KEY_SEC_QUESTION, question)
            .putString(KEY_SEC_ANSWER_SALT, saltB64)
            .putString(KEY_SEC_ANSWER_HASH, hash(normalizeAnswer(answer), saltB64))
            .apply()
    }

    fun checkSecurityAnswer(answer: String): Boolean {
        val salt = prefs.getString(KEY_SEC_ANSWER_SALT, null) ?: return false
        return prefs.getString(KEY_SEC_ANSWER_HASH, null) ==
            hash(normalizeAnswer(answer), salt)
    }

    private fun normalizeAnswer(answer: String): String = answer.trim().lowercase()

    // ---------- Desbloqueo biométrico ----------

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()

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

    // ---------- Hash ----------

    private fun newSalt(): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    private fun hash(value: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((salt + value).toByteArray())
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    companion object {
        private const val LEGACY_FILE_NAME = "kidsguard_prefs"
        private const val ENCRYPTED_FILE_NAME = "kidsguard_secure_prefs"

        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_SEC_QUESTION = "sec_question"
        private const val KEY_SEC_ANSWER_HASH = "sec_answer_hash"
        private const val KEY_SEC_ANSWER_SALT = "sec_answer_salt"
        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val KEY_ALLOWED_APPS = "allowed_apps"
        private const val KEY_CHILD_MODE = "child_mode_active"
        private const val KEY_DAILY_LIMIT = "daily_limit_minutes"
        private const val KEY_BEDTIME_ENABLED = "bedtime_enabled"
        private const val KEY_BEDTIME_START = "bedtime_start_minutes"
        private const val KEY_BEDTIME_END = "bedtime_end_minutes"
        private const val KEY_APP_LIMITS = "app_limits_json"
        private const val KEY_USAGE_DATE = "usage_date"
        private const val KEY_USAGE_MAP = "usage_map_json"

        @Volatile
        private var cachedPrefs: SharedPreferences? = null

        /** El almacén cifrado se abre una sola vez por proceso (es costoso). */
        private fun obtainPrefs(context: Context): SharedPreferences =
            cachedPrefs ?: synchronized(this) {
                cachedPrefs ?: createPrefs(context).also { cachedPrefs = it }
            }

        private fun createPrefs(context: Context): SharedPreferences = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).also { migrateLegacyPrefs(context, it) }
        } catch (_: Exception) {
            context.getSharedPreferences(LEGACY_FILE_NAME, Context.MODE_PRIVATE)
        }

        private fun migrateLegacyPrefs(context: Context, encrypted: SharedPreferences) {
            val legacy = context.getSharedPreferences(LEGACY_FILE_NAME, Context.MODE_PRIVATE)
            if (legacy.all.isEmpty()) return
            val editor = encrypted.edit()
            for ((key, value) in legacy.all) {
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is String -> editor.putString(key, value)
                    is Set<*> -> editor.putStringSet(
                        key, value.filterIsInstance<String>().toSet()
                    )
                }
            }
            editor.apply()
            legacy.edit().clear().apply()
        }
    }
}
