package com.kidsguard.app.ui.pin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityPinBinding
import com.kidsguard.app.ui.parent.ParentDashboardActivity

/**
 * Pide el PIN de adulto y, si es correcto, abre el panel de control.
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
    }

    private fun verify() {
        val pin = binding.etPin.text.toString()
        if (prefs.checkPin(pin)) {
            startActivity(Intent(this, ParentDashboardActivity::class.java))
            finish()
        } else {
            binding.tvError.text = getString(R.string.pin_error_wrong)
            binding.tvError.visibility = View.VISIBLE
            binding.etPin.text.clear()
        }
    }
}
