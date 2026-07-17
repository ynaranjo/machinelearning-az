package com.kidsguard.app.ui.parent

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidsguard.app.databinding.ActivityAppSelectionBinding
import com.kidsguard.app.ui.parent.AppSelectionAdapter.SelectionItem
import kotlinx.coroutines.launch

/**
 * Lista de todas las apps instaladas, agrupadas por categoría, con
 * casillas para elegir cuáles puede usar el perfil activo.
 *
 * El agrupado (fuera del hilo principal) y la persistencia viven en
 * AppSelectionViewModel (MVVM); la Activity observa el StateFlow.
 */
class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private val viewModel: AppSelectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvApps.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { items -> if (items != null) render(items) }
            }
        }
    }

    private fun render(items: List<SelectionItem>) {
        binding.rvApps.adapter =
            AppSelectionAdapter(items, viewModel.allowedApps) { packageName, allowed ->
                viewModel.setAllowed(packageName, allowed)
            }
    }
}
