package com.kidsguard.app.ui.pin

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityPinBinding
import com.kidsguard.app.ui.parent.ParentDashboardActivity

/**
 * Pide el PIN de adulto (o huella/rostro, si está habilitado) y, si es
 * correcto, abre el panel de control. Ofrece recuperación mediante la
 * pregunta de seguridad si el PIN se olvidó.
 */
class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        binding.btnOk.setOnClickListener { verify() }

        if (prefs.isSecurityQuestionSet) {
            binding.btnForgot.visibility = View.VISIBLE
            binding.btnForgot.setOnClickListener { showRecoveryDialog() }
        }

        if (prefs.biometricEnabled) {
            maybeShowBiometricPrompt()
        }
    }

    private fun verify() {
        val pin = binding.etPin.text.toString()
        if (prefs.checkPin(pin)) {
            openDashboard()
        } else {
            binding.tvError.text = getString(R.string.pin_error_wrong)
            binding.tvError.visibility = View.VISIBLE
            binding.etPin.text.clear()
        }
    }

    private fun openDashboard() {
        startActivity(Intent(this, ParentDashboardActivity::class.java))
        finish()
    }

    private fun maybeShowBiometricPrompt() {
        val canAuthenticate = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) return

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    openDashboard()
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setNegativeButtonText(getString(R.string.biometric_use_pin))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()
        )
    }

    private fun showRecoveryDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = getString(R.string.security_answer_hint)
        }
        val container = FrameLayout(this).apply {
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
            addView(input)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.recovery_title)
            .setMessage(prefs.securityQuestion)
            .setView(container)
            .setPositiveButton(R.string.ok) { _, _ ->
                if (prefs.checkSecurityAnswer(input.text.toString())) {
                    startActivity(Intent(this, PinSetupActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, R.string.security_answer_wrong, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
