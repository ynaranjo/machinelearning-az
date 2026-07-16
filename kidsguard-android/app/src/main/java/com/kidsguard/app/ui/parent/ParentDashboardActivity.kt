package com.kidsguard.app.ui.parent

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityParentDashboardBinding
import com.kidsguard.app.service.AppMonitorService
import com.kidsguard.app.service.ServiceWatchdogWorker
import com.kidsguard.app.ui.pin.PinSetupActivity
import com.kidsguard.app.util.DeviceOwnerManager
import com.kidsguard.app.util.Permissions

/**
 * Panel de control del adulto: activar el modo niños y acceder a
 * apps permitidas, límites, uso y permisos.
 */
class ParentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentDashboardBinding
    private lateinit var prefs: PreferencesManager
    private var updatingSwitch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        binding.swChildMode.setOnCheckedChangeListener { _, checked ->
            if (updatingSwitch) return@setOnCheckedChangeListener
            if (checked) enableChildMode() else disableChildMode()
        }

        setupBiometricSwitch()

        binding.btnApps.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }
        binding.btnLimits.setOnClickListener {
            startActivity(Intent(this, TimeLimitsActivity::class.java))
        }
        binding.btnWebFilter.setOnClickListener {
            startActivity(Intent(this, WebFilterActivity::class.java))
        }
        binding.btnUsage.setOnClickListener {
            startActivity(Intent(this, UsageStatsActivity::class.java))
        }
        binding.btnWeekly.setOnClickListener {
            startActivity(Intent(this, WeeklyReportActivity::class.java))
        }
        binding.btnProfiles.setOnClickListener {
            startActivity(Intent(this, ProfilesActivity::class.java))
        }
        binding.btnCloud.setOnClickListener {
            startActivity(Intent(this, CloudSyncActivity::class.java))
        }
        binding.btnSetup.setOnClickListener {
            startActivity(Intent(this, SetupActivity::class.java))
        }
        binding.btnChangePin.setOnClickListener {
            startActivity(Intent(this, PinSetupActivity::class.java))
        }
        binding.btnLauncherSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        updatingSwitch = true
        binding.swChildMode.isChecked = prefs.childModeActive
        updatingSwitch = false

        val profile = prefs.activeProfile
        binding.tvActiveProfile.text =
            getString(R.string.active_profile_fmt, "${profile.emoji} ${profile.name}")

        binding.tvModeHint.text = if (Permissions.hasAccessibility(this)) {
            getString(R.string.child_mode_desc)
        } else {
            getString(R.string.child_mode_desc) + "\n" +
                getString(R.string.accessibility_recommend)
        }
    }

    private fun setupBiometricSwitch() {
        val available = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
        binding.rowBiometric.visibility = if (available) View.VISIBLE else View.GONE
        binding.swBiometric.isChecked = prefs.biometricEnabled
        binding.swBiometric.setOnCheckedChangeListener { _, checked ->
            prefs.biometricEnabled = checked
        }
    }

    private fun enableChildMode() {
        if (!Permissions.hasUsageAccess(this) || !Permissions.hasOverlay(this)) {
            updatingSwitch = true
            binding.swChildMode.isChecked = false
            updatingSwitch = false
            AlertDialog.Builder(this)
                .setTitle(R.string.missing_permissions_title)
                .setMessage(R.string.missing_permissions_msg)
                .setPositiveButton(R.string.ok) { _, _ ->
                    startActivity(Intent(this, SetupActivity::class.java))
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
            return
        }

        prefs.childModeActive = true
        AppMonitorService.start(this)
        ServiceWatchdogWorker.schedule(this)
        DeviceOwnerManager.applyChildModePolicies(this, prefs)
        Toast.makeText(this, R.string.child_mode_on, Toast.LENGTH_SHORT).show()

        // Con device owner el launcher queda fijado por política: no hace falta pedirlo.
        if (!Permissions.isDefaultLauncher(this) && !DeviceOwnerManager.isDeviceOwner(this)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.set_launcher_title)
                .setMessage(R.string.set_launcher_msg)
                .setPositiveButton(R.string.ok) { _, _ ->
                    startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun disableChildMode() {
        prefs.childModeActive = false
        AppMonitorService.stop(this)
        ServiceWatchdogWorker.cancel(this)
        DeviceOwnerManager.clearChildModePolicies(this)
        runCatching { stopLockTask() }
        Toast.makeText(this, R.string.child_mode_off, Toast.LENGTH_SHORT).show()
    }
}
