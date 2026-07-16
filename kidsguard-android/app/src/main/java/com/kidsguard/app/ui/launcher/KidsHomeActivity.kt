package com.kidsguard.app.ui.launcher

import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.kidsguard.app.R
import com.kidsguard.app.data.AppRepository
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityKidsHomeBinding
import com.kidsguard.app.service.AppMonitorService
import com.kidsguard.app.ui.browser.KidsBrowserActivity
import com.kidsguard.app.ui.pin.PinActivity
import com.kidsguard.app.ui.pin.PinSetupActivity
import com.kidsguard.app.util.DeviceOwnerManager
import com.kidsguard.app.util.LimitChecker
import com.kidsguard.app.util.TimeRules

/**
 * Pantalla de inicio infantil: solo muestra las apps permitidas.
 * Actúa como launcher (categoría HOME), por lo que se abre al encender
 * el dispositivo y al pulsar el botón de inicio.
 */
class KidsHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKidsHomeBinding
    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: AppGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKidsHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        adapter = AppGridAdapter { app ->
            packageManager.getLaunchIntentForPackage(app.packageName)?.let { launchApp(it) }
        }
        // Columnas según el ancho de pantalla: ~4 en móvil, más en tablet.
        val spanCount = (resources.configuration.screenWidthDp / 96).coerceIn(3, 8)
        binding.rvApps.layoutManager = GridLayoutManager(this, spanCount)
        binding.rvApps.adapter = adapter

        binding.btnBrowser.setOnClickListener {
            startActivity(Intent(this, KidsBrowserActivity::class.java))
        }

        binding.btnParent.setOnClickListener {
            val next = if (prefs.isPinSet) {
                Intent(this, PinActivity::class.java)
            } else {
                Intent(this, PinSetupActivity::class.java)
                    .putExtra(PinSetupActivity.EXTRA_FIRST_RUN, true)
            }
            startActivity(next)
        }

        // El botón atrás no hace nada: es la pantalla de inicio.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })

        hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        refresh()
        if (prefs.childModeActive) {
            AppMonitorService.start(this)
            DeviceOwnerManager.refreshLockTaskPackages(this, prefs)
            maybeStartLockTask()
        } else {
            maybeStopLockTask()
        }
    }

    /** En un app permitida el LockTask se hereda; el lanzamiento explícito es para P+. */
    private fun launchApp(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            DeviceOwnerManager.isDeviceOwner(this) && prefs.childModeActive
        ) {
            val options = ActivityOptions.makeBasic().apply { setLockTaskEnabled(true) }
            runCatching { startActivity(intent, options.toBundle()) }
                .onFailure { startActivity(intent) }
        } else {
            startActivity(intent)
        }
    }

    /** Con device owner, encierra la sesión en las apps permitidas (kiosco). */
    private fun maybeStartLockTask() {
        if (!DeviceOwnerManager.isDeviceOwner(this)) return
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
            runCatching { startLockTask() }
        }
    }

    private fun maybeStopLockTask() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
            runCatching { stopLockTask() }
        }
    }

    private fun refresh() {
        val profile = prefs.activeProfile
        binding.tvProfile.text = "${profile.emoji} ${profile.name}"

        binding.btnBrowser.visibility = if (prefs.browserEnabled) View.VISIBLE else View.GONE

        val apps = AppRepository.getAllowedApps(this, prefs.allowedApps)
        adapter.submit(apps)
        binding.tvEmpty.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE

        val dailyLimit = prefs.dailyLimitMinutes
        if (dailyLimit >= 0) {
            val remaining = LimitChecker.remainingSeconds(
                prefs.totalUsageSecondsToday(), dailyLimit, prefs.extraMinutesToday
            )
            binding.tvTimeLeft.text =
                getString(R.string.time_left_fmt, TimeRules.formatDuration(remaining))
            binding.tvTimeLeft.visibility = View.VISIBLE
        } else {
            binding.tvTimeLeft.visibility = View.INVISIBLE
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
