package com.kidsguard.app.ui.block

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityBlockedBinding
import com.kidsguard.app.util.BlockReason

/**
 * Pantalla a pantalla completa que se muestra encima de una app bloqueada.
 * Si el bloqueo es por límite de tiempo, un adulto puede conceder minutos
 * extra introduciendo su PIN, sin salir del modo niños.
 */
class BlockedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockedBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        val reason = intent.getStringExtra(EXTRA_REASON)
            ?.let { runCatching { BlockReason.valueOf(it) }.getOrNull() }
            ?: BlockReason.NOT_ALLOWED

        val (emoji, title, message) = when (reason) {
            BlockReason.NOT_ALLOWED -> Triple(
                "🚫", R.string.blocked_not_allowed_title, R.string.blocked_not_allowed_msg
            )
            BlockReason.DAILY_LIMIT -> Triple(
                "⏰", R.string.blocked_daily_title, R.string.blocked_daily_msg
            )
            BlockReason.APP_LIMIT -> Triple(
                "⏳", R.string.blocked_app_limit_title, R.string.blocked_app_limit_msg
            )
            BlockReason.BEDTIME -> Triple(
                "🌙", R.string.blocked_bedtime_title, R.string.blocked_bedtime_msg
            )
        }
        binding.tvEmoji.text = emoji
        binding.tvBlockTitle.text = getString(title)
        binding.tvBlockMsg.text = getString(message)

        binding.btnHome.setOnClickListener { goHome() }

        // Solo tiene sentido conceder tiempo extra en bloqueos por límite.
        val extendable =
            reason == BlockReason.DAILY_LIMIT || reason == BlockReason.APP_LIMIT
        binding.btnMoreTime.visibility = if (extendable) View.VISIBLE else View.GONE
        binding.btnMoreTime.setOnClickListener { showExtensionDialog() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })
    }

    private fun showExtensionDialog() {
        val pinInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = getString(R.string.pin_hint)
        }
        val options = intArrayOf(15, 30, 60)
        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            options.forEachIndexed { index, minutes ->
                addView(RadioButton(context).apply {
                    id = index
                    text = getString(R.string.minutes_fmt, minutes)
                })
            }
            check(0)
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
            addView(pinInput)
            addView(radioGroup)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.extension_title)
            .setView(container)
            .setPositiveButton(R.string.ok) { _, _ ->
                if (!prefs.checkPin(pinInput.text.toString())) {
                    Toast.makeText(this, R.string.pin_error_wrong, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val minutes = options[radioGroup.checkedRadioButtonId.coerceIn(0, 2)]
                prefs.addExtraMinutesToday(minutes)
                Toast.makeText(
                    this,
                    getString(R.string.extension_granted, minutes),
                    Toast.LENGTH_SHORT
                ).show()
                // La app deja de estar bloqueada: volver a donde estaba el niño.
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    companion object {
        const val EXTRA_REASON = "extra_reason"
        const val EXTRA_PACKAGE = "extra_package"
    }
}
