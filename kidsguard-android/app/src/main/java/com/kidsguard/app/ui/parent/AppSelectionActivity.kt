package com.kidsguard.app.ui.parent

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidsguard.app.R
import com.kidsguard.app.data.AppRepository
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityAppSelectionBinding
import com.kidsguard.app.ui.parent.AppSelectionAdapter.SelectionItem

/**
 * Lista de todas las apps instaladas, agrupadas por categoría, con
 * casillas para elegir cuáles puede usar el perfil activo.
 */
class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        val otherLabel = getString(R.string.category_other)
        val groups = AppRepository.getLaunchableApps(this)
            .groupBy { it.category ?: otherLabel }
        val sortedCategories = groups.keys
            .sortedWith(compareBy({ it == otherLabel }, { it }))

        val items = buildList {
            sortedCategories.forEach { category ->
                add(SelectionItem.Header(category))
                groups.getValue(category).forEach { add(SelectionItem.App(it)) }
            }
        }

        val adapter = AppSelectionAdapter(items, prefs.allowedApps) { packageName, allowed ->
            val current = prefs.allowedApps.toMutableSet()
            if (allowed) current.add(packageName) else current.remove(packageName)
            prefs.allowedApps = current
        }
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter
    }
}
