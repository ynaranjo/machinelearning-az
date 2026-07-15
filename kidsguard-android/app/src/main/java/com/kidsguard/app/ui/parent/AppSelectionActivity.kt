package com.kidsguard.app.ui.parent

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidsguard.app.data.AppRepository
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityAppSelectionBinding

/**
 * Lista de todas las apps instaladas con casillas para elegir
 * cuáles puede usar el niño.
 */
class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        val apps = AppRepository.getLaunchableApps(this)
        val adapter = AppSelectionAdapter(apps, prefs.allowedApps) { packageName, allowed ->
            val current = prefs.allowedApps.toMutableSet()
            if (allowed) current.add(packageName) else current.remove(packageName)
            prefs.allowedApps = current
        }
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter
    }
}
