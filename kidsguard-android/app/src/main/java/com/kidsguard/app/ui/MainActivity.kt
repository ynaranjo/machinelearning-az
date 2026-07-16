package com.kidsguard.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.ui.pin.PinActivity

/**
 * Punto de entrada del adulto (icono de la app).
 * Primera vez: asistente de bienvenida. Después: pedir PIN para el panel.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferencesManager(this)
        val next = if (prefs.isPinSet) {
            Intent(this, PinActivity::class.java)
        } else {
            Intent(this, WelcomeActivity::class.java)
        }
        startActivity(next)
        finish()
    }
}
