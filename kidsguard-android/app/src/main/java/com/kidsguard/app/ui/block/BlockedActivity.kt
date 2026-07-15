package com.kidsguard.app.ui.block

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.R
import com.kidsguard.app.databinding.ActivityBlockedBinding
import com.kidsguard.app.util.BlockReason

/**
 * Pantalla a pantalla completa que se muestra encima de una app bloqueada.
 */
class BlockedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })
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
