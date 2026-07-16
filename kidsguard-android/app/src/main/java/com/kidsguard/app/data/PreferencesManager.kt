package com.kidsguard.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kidsguard.app.model.ChildProfile
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Almacén central de configuración: PIN, perfiles de hijos, apps permitidas,
 * límites y uso diario.
 *
 * Los datos se guardan en EncryptedSharedPreferences (Jetpack Security,
 * AES-256), con migración automática desde el almacén plano anterior.
 *
 * Desde v1.2 la configuración de protección (apps permitidas, límites,
 * horarios y uso) es POR PERFIL: las claves llevan el sufijo del perfil
 * activo. El PIN, la biometría y el estado del modo niños son globales.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = obtainPrefs(context.applicationContext)

    init {
        ensureProfileSetup()
    }

    // ---------- PIN (global) ----------

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

    // ---------- Recuperación de PIN (global) ----------

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

    // ---------- Desbloqueo biométrico (global) ----------

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()

    // ---------- Modo niños (global) ----------

    var childModeActive: Boolean
        get() = prefs.getBoolean(KEY_CHILD_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_CHILD_MODE, value).apply()

    // ---------- Perfiles de hijos ----------

    var activeProfileId: Int
        get() = prefs.getInt(KEY_ACTIVE_PROFILE, 1)
        set(value) = prefs.edit().putInt(KEY_ACTIVE_PROFILE, value).apply()

    val activeProfile: ChildProfile
        get() {
            val list = profiles()
            return list.firstOrNull { it.id == activeProfileId }
                ?: list.firstOrNull()
                ?: DEFAULT_PROFILE
        }

    fun profiles(): List<ChildProfile> {
        val array = JSONArray(prefs.getString(KEY_PROFILES, "[]") ?: "[]")
        return (0 until array.length()).map { i ->
            val json = array.getJSONObject(i)
            ChildProfile(
                id = json.getInt("id"),
                name = json.getString("name"),
                emoji = json.optString("emoji", DEFAULT_PROFILE.emoji)
            )
        }
    }

    fun addProfile(name: String, emoji: String): ChildProfile {
        val list = profiles()
        val id = (list.maxOfOrNull { it.id } ?: 0) + 1
        val profile = ChildProfile(id, name, emoji.ifBlank { DEFAULT_PROFILE.emoji })
        saveProfiles(list + profile)
        return profile
    }

    fun updateProfile(profile: ChildProfile) {
        saveProfiles(profiles().map { if (it.id == profile.id) profile else it })
    }

    /** No permite borrar el último perfil. Limpia su configuración. */
    fun deleteProfile(id: Int): Boolean {
        val list = profiles()
        if (list.size <= 1) return false
        saveProfiles(list.filter { it.id != id })

        val editor = prefs.edit()
        PROFILE_SCOPED_KEYS.forEach { base -> editor.remove(base + PROFILE_SUFFIX + id) }
        editor.apply()

        if (activeProfileId == id) {
            activeProfileId = profiles().first().id
        }
        return true
    }

    private fun saveProfiles(list: List<ChildProfile>) {
        val array = JSONArray()
        list.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("emoji", it.emoji)
            )
        }
        prefs.edit().putString(KEY_PROFILES, array.toString()).apply()
    }

    /** Clave con el sufijo del perfil activo. */
    private fun pk(base: String): String = base + PROFILE_SUFFIX + activeProfileId

    /**
     * Primera ejecución tras actualizar (o instalación nueva): crea el
     * perfil 1 y migra la configuración global anterior a sus claves.
     */
    private fun ensureProfileSetup() {
        if (prefs.getString(KEY_PROFILES, null) != null) return

        val editor = prefs.edit()
            .putString(
                KEY_PROFILES,
                JSONArray().put(
                    JSONObject()
                        .put("id", DEFAULT_PROFILE.id)
                        .put("name", DEFAULT_PROFILE.name)
                        .put("emoji", DEFAULT_PROFILE.emoji)
                ).toString()
            )
            .putInt(KEY_ACTIVE_PROFILE, DEFAULT_PROFILE.id)

        val suffix = PROFILE_SUFFIX + DEFAULT_PROFILE.id
        prefs.getStringSet(KEY_ALLOWED_APPS, null)?.let {
            editor.putStringSet(KEY_ALLOWED_APPS + suffix, it).remove(KEY_ALLOWED_APPS)
        }
        if (prefs.contains(KEY_DAILY_LIMIT)) {
            editor.putInt(KEY_DAILY_LIMIT + suffix, prefs.getInt(KEY_DAILY_LIMIT, -1))
                .remove(KEY_DAILY_LIMIT)
        }
        if (prefs.contains(KEY_BEDTIME_ENABLED)) {
            editor.putBoolean(
                KEY_BEDTIME_ENABLED + suffix, prefs.getBoolean(KEY_BEDTIME_ENABLED, false)
            ).remove(KEY_BEDTIME_ENABLED)
        }
        if (prefs.contains(KEY_BEDTIME_START)) {
            editor.putInt(KEY_BEDTIME_START + suffix, prefs.getInt(KEY_BEDTIME_START, 21 * 60))
                .remove(KEY_BEDTIME_START)
        }
        if (prefs.contains(KEY_BEDTIME_END)) {
            editor.putInt(KEY_BEDTIME_END + suffix, prefs.getInt(KEY_BEDTIME_END, 7 * 60))
                .remove(KEY_BEDTIME_END)
        }
        prefs.getString(KEY_APP_LIMITS, null)?.let {
            editor.putString(KEY_APP_LIMITS + suffix, it).remove(KEY_APP_LIMITS)
        }
        prefs.getString(KEY_USAGE_DATE, null)?.let {
            editor.putString(KEY_USAGE_DATE + suffix, it).remove(KEY_USAGE_DATE)
        }
        prefs.getString(KEY_USAGE_MAP, null)?.let {
            editor.putString(KEY_USAGE_MAP + suffix, it).remove(KEY_USAGE_MAP)
        }
        editor.apply()
    }

    // ---------- Apps permitidas (por perfil) ----------

    var allowedApps: Set<String>
        get() = prefs.getStringSet(pk(KEY_ALLOWED_APPS), emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(pk(KEY_ALLOWED_APPS), value.toSet()).apply()

    // ---------- Límite diario (por perfil) ----------

    /** Minutos por día. -1 = sin límite. */
    var dailyLimitMinutes: Int
        get() = prefs.getInt(pk(KEY_DAILY_LIMIT), -1)
        set(value) = prefs.edit().putInt(pk(KEY_DAILY_LIMIT), value).apply()

    // ---------- Hora de dormir (por perfil) ----------

    var bedtimeEnabled: Boolean
        get() = prefs.getBoolean(pk(KEY_BEDTIME_ENABLED), false)
        set(value) = prefs.edit().putBoolean(pk(KEY_BEDTIME_ENABLED), value).apply()

    /** Minutos desde medianoche. */
    var bedtimeStartMinutes: Int
        get() = prefs.getInt(pk(KEY_BEDTIME_START), 21 * 60)
        set(value) = prefs.edit().putInt(pk(KEY_BEDTIME_START), value).apply()

    var bedtimeEndMinutes: Int
        get() = prefs.getInt(pk(KEY_BEDTIME_END), 7 * 60)
        set(value) = prefs.edit().putInt(pk(KEY_BEDTIME_END), value).apply()

    // ---------- Extensión de tiempo (por perfil) ----------

    /** Minutos extra concedidos hoy por un adulto (se suman a los límites). */
    val extraMinutesToday: Int
        get() = if (prefs.getString(pk(KEY_EXTENSION_DATE), null) == today()) {
            prefs.getInt(pk(KEY_EXTENSION_MINUTES), 0)
        } else {
            0
        }

    fun addExtraMinutesToday(minutes: Int) {
        prefs.edit()
            .putString(pk(KEY_EXTENSION_DATE), today())
            .putInt(pk(KEY_EXTENSION_MINUTES), extraMinutesToday + minutes)
            .apply()
    }

    // ---------- Límites por app (por perfil) ----------

    /** Mapa paquete -> minutos por día. */
    fun appLimits(): Map<String, Int> {
        val json = JSONObject(prefs.getString(pk(KEY_APP_LIMITS), "{}") ?: "{}")
        val map = mutableMapOf<String, Int>()
        json.keys().forEach { key -> map[key] = json.getInt(key) }
        return map
    }

    fun appLimitFor(packageName: String): Int? = appLimits()[packageName]

    /** minutes null o <= 0 elimina el límite. */
    fun setAppLimit(packageName: String, minutes: Int?) {
        val json = JSONObject(prefs.getString(pk(KEY_APP_LIMITS), "{}") ?: "{}")
        if (minutes == null || minutes <= 0) {
            json.remove(packageName)
        } else {
            json.put(packageName, minutes)
        }
        prefs.edit().putString(pk(KEY_APP_LIMITS), json.toString()).apply()
    }

    // ---------- Uso diario (por perfil) ----------

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun usageJson(): JSONObject {
        val storedDate = prefs.getString(pk(KEY_USAGE_DATE), null)
        return if (storedDate == today()) {
            JSONObject(prefs.getString(pk(KEY_USAGE_MAP), "{}") ?: "{}")
        } else {
            JSONObject()
        }
    }

    fun addUsageSeconds(packageName: String, seconds: Int) {
        val json = usageJson()
        json.put(packageName, json.optInt(packageName, 0) + seconds)
        prefs.edit()
            .putString(pk(KEY_USAGE_DATE), today())
            .putString(pk(KEY_USAGE_MAP), json.toString())
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

        private val DEFAULT_PROFILE = ChildProfile(1, "Mi peque", "🧒")
        private const val PROFILE_SUFFIX = "_p"

        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_SEC_QUESTION = "sec_question"
        private const val KEY_SEC_ANSWER_HASH = "sec_answer_hash"
        private const val KEY_SEC_ANSWER_SALT = "sec_answer_salt"
        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val KEY_CHILD_MODE = "child_mode_active"
        private const val KEY_PROFILES = "profiles_json"
        private const val KEY_ACTIVE_PROFILE = "active_profile_id"
        private const val KEY_ALLOWED_APPS = "allowed_apps"
        private const val KEY_DAILY_LIMIT = "daily_limit_minutes"
        private const val KEY_BEDTIME_ENABLED = "bedtime_enabled"
        private const val KEY_BEDTIME_START = "bedtime_start_minutes"
        private const val KEY_BEDTIME_END = "bedtime_end_minutes"
        private const val KEY_APP_LIMITS = "app_limits_json"
        private const val KEY_USAGE_DATE = "usage_date"
        private const val KEY_USAGE_MAP = "usage_map_json"
        private const val KEY_EXTENSION_DATE = "extension_date"
        private const val KEY_EXTENSION_MINUTES = "extension_minutes"

        /** Claves que existen una vez por perfil. */
        private val PROFILE_SCOPED_KEYS = listOf(
            KEY_ALLOWED_APPS, KEY_DAILY_LIMIT,
            KEY_BEDTIME_ENABLED, KEY_BEDTIME_START, KEY_BEDTIME_END,
            KEY_APP_LIMITS, KEY_USAGE_DATE, KEY_USAGE_MAP,
            KEY_EXTENSION_DATE, KEY_EXTENSION_MINUTES
        )

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
