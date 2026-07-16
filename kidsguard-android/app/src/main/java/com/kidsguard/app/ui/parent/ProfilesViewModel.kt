package com.kidsguard.app.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.model.ChildProfile

/**
 * ViewModel de la gestión de perfiles (MVVM). La Activity solo pinta la
 * lista y delega el CRUD y la activación del perfil.
 */
class ProfilesViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PreferencesManager(app)

    fun profiles(): List<ChildProfile> = prefs.profiles()

    val activeProfileId: Int
        get() = prefs.activeProfileId

    fun activate(id: Int) {
        prefs.activeProfileId = id
    }

    fun addProfile(name: String, emoji: String) {
        prefs.addProfile(name, emoji)
    }

    fun updateProfile(profile: ChildProfile) {
        prefs.updateProfile(profile)
    }

    /** Elimina un perfil. Devuelve false si es el último (no permitido). */
    fun deleteProfile(id: Int): Boolean = prefs.deleteProfile(id)
}
