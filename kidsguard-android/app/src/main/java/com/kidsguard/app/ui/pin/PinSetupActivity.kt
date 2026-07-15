package com.kidsguard.app.ui.pin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityPinSetupBinding
import com.kidsguard.app.ui.parent.SetupActivity

/**
 * Crea o cambia el PIN de adulto, junto con la pregunta de seguridad para
 * recuperarlo. En la primera ejecución, al terminar lleva a la pantalla de
 * permisos del sistema.
 */
class PinSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinSetupBinding
    private lateinit var prefs: PreferencesManager
    private var firstRun = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)
        firstRun = intent.getBooleanExtra(EXTRA_FIRST_RUN, false)

        if (!firstRun) {
            binding.tvTitle.text = getString(R.string.pin_change_title)
            // Al cambiar el PIN la pregunta existente se conserva si se deja vacío.
            prefs.securityQuestion?.let { binding.etQuestion.setText(it) }
        }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val pin = binding.etPin.text.toString()
        val confirm = binding.etPinConfirm.text.toString()
        val question = binding.etQuestion.text.toString().trim()
        val answer = binding.etAnswer.text.toString()

        val error = when {
            pin.length < 4 -> getString(R.string.pin_error_short)
            pin != confirm -> getString(R.string.pin_error_mismatch)
            firstRun && (question.isEmpty() || answer.isBlank()) ->
                getString(R.string.error_need_question)
            else -> null
        }
        if (error != null) {
            binding.tvError.text = error
            binding.tvError.visibility = View.VISIBLE
            return
        }

        prefs.setPin(pin)
        if (question.isNotEmpty() && answer.isNotBlank()) {
            prefs.setSecurityQuestion(question, answer)
        }
        Toast.makeText(this, R.string.pin_saved, Toast.LENGTH_SHORT).show()
        if (firstRun) {
            startActivity(
                Intent(this, SetupActivity::class.java)
                    .putExtra(SetupActivity.EXTRA_FIRST_RUN, true)
            )
        }
        finish()
    }

    companion object {
        const val EXTRA_FIRST_RUN = "extra_first_run"
    }
}
