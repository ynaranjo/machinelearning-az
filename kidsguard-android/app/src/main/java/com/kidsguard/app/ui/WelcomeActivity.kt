package com.kidsguard.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.databinding.ActivityWelcomeBinding
import com.kidsguard.app.ui.pin.PinSetupActivity

/**
 * Primera pantalla del asistente de configuración: explica qué hace
 * KidsGuard y los pasos que vienen (PIN → permisos → apps permitidas).
 */
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            startActivity(
                Intent(this, PinSetupActivity::class.java)
                    .putExtra(PinSetupActivity.EXTRA_FIRST_RUN, true)
            )
            finish()
        }
    }
}
