package com.kidsguard.app.ui.launcher

import android.content.Intent
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
import com.kidsguard.app.ui.pin.PinActivity
import com.kidsguard.app.ui.pin.PinSetupActivity
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
            packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
        }
        binding.rvApps.layoutManager = GridLayoutManager(this, 4)
        binding.rvApps.adapter = adapter

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
        }
    }

    private fun refresh() {
        val apps = AppRepository.getAllowedApps(this, prefs.allowedApps)
        adapter.submit(apps)
        binding.tvEmpty.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE

        val dailyLimit = prefs.dailyLimitMinutes
        if (dailyLimit >= 0) {
            val remaining = (dailyLimit * 60 - prefs.totalUsageSecondsToday()).coerceAtLeast(0)
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
