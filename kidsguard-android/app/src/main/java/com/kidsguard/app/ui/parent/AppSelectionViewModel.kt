package com.kidsguard.app.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kidsguard.app.R
import com.kidsguard.app.data.AppRepository
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.ui.parent.AppSelectionAdapter.SelectionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel de la selección de apps (MVVM + corrutinas): agrupa las apps
 * instaladas por categoría (fuera del hilo principal) y persiste la lista
 * blanca del perfil activo.
 */
class AppSelectionViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PreferencesManager(app)

    val allowedApps: Set<String>
        get() = prefs.allowedApps

    private val _items = MutableStateFlow<List<SelectionItem>?>(null)
    val items: StateFlow<List<SelectionItem>?> = _items.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _items.value = withContext(Dispatchers.Default) { buildItems() }
        }
    }

    private fun buildItems(): List<SelectionItem> {
        val otherLabel = getApplication<Application>().getString(R.string.category_other)
        val groups = AppRepository.getLaunchableApps(getApplication())
            .groupBy { it.category ?: otherLabel }
        val sortedCategories = groups.keys
            .sortedWith(compareBy({ it == otherLabel }, { it }))

        return buildList {
            sortedCategories.forEach { category ->
                add(SelectionItem.Header(category))
                groups.getValue(category).forEach { add(SelectionItem.App(it)) }
            }
        }
    }

    fun setAllowed(packageName: String, allowed: Boolean) {
        val current = prefs.allowedApps.toMutableSet()
        if (allowed) current.add(packageName) else current.remove(packageName)
        prefs.allowedApps = current
    }
}
