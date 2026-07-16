package com.kidsguard.app.ui.parent

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidsguard.app.databinding.ActivityAppSelectionBinding

/**
 * Lista de todas las apps instaladas, agrupadas por categoría, con
 * casillas para elegir cuáles puede usar el perfil activo.
 *
 * El agrupado y la persistencia viven en AppSelectionViewModel (MVVM).
 */
class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private val viewModel: AppSelectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = AppSelectionAdapter(viewModel.buildItems(), viewModel.allowedApps) {
                packageName, allowed ->
            viewModel.setAllowed(packageName, allowed)
        }
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter
    }
}
