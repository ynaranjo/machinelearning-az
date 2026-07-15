package com.kidsguard.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.ui.pin.PinActivity
import com.kidsguard.app.ui.pin.PinSetupActivity

/**
 * Punto de entrada del adulto (icono de la app).
 * Primera vez: crear PIN. Después: pedir PIN para entrar al panel.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferencesManager(this)
        val next = if (prefs.isPinSet) {
            Intent(this, PinActivity::class.java)
        } else {
            Intent(this, PinSetupActivity::class.java)
                .putExtra(PinSetupActivity.EXTRA_FIRST_RUN, true)
        }
        startActivity(next)
        finish()
    }
}
