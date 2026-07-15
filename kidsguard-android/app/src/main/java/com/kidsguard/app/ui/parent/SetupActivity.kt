package com.kidsguard.app.ui.parent

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.R
import com.kidsguard.app.databinding.ActivitySetupBinding
import com.kidsguard.app.util.Permissions

/**
 * Guía al adulto para conceder los permisos especiales que necesita
 * KidsGuard: datos de uso, superposición, notificaciones, administrador
 * de dispositivo y launcher predeterminado.
 */
class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private var firstRun = false

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            refreshStatuses()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firstRun = intent.getBooleanExtra(EXTRA_FIRST_RUN, false)

        binding.btnUsage.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.btnOverlay.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
        binding.btnNotif.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                )
            }
        }
        binding.btnAdmin.setOnClickListener {
            startActivity(
                Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        Permissions.adminComponent(this)
                    )
                    .putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getString(R.string.admin_explanation)
                    )
            )
        }
        binding.btnLauncher.setOnClickListener { requestHomeRole() }

        binding.btnDone.setOnClickListener {
            if (firstRun) {
                startActivity(Intent(this, ParentDashboardActivity::class.java))
            }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatuses()
    }

    private fun requestHomeRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
    }

    private fun refreshStatuses() {
        binding.tvStatusUsage.text = status(Permissions.hasUsageAccess(this))
        binding.tvStatusAccessibility.text = status(Permissions.hasAccessibility(this))
        binding.tvStatusOverlay.text = status(Permissions.hasOverlay(this))
        binding.tvStatusNotif.text = status(Permissions.hasNotifications(this))
        binding.tvStatusAdmin.text = status(Permissions.isDeviceAdmin(this))
        binding.tvStatusLauncher.text = status(Permissions.isDefaultLauncher(this))
    }

    private fun status(granted: Boolean): String = if (granted) "✅" else "❌"

    companion object {
        const val EXTRA_FIRST_RUN = "extra_first_run"
    }
}
