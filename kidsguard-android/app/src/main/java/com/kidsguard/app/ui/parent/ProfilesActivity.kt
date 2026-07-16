package com.kidsguard.app.ui.parent

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityProfilesBinding
import com.kidsguard.app.model.ChildProfile

/**
 * Gestión de perfiles de hijos: cada perfil tiene sus propias apps
 * permitidas, límites y horarios. Tocar un perfil lo activa.
 */
class ProfilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilesBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        binding.rvProfiles.layoutManager = LinearLayoutManager(this)
        binding.btnAdd.setOnClickListener { showEditDialog(null) }
        refresh()
    }

    private fun refresh() {
        binding.rvProfiles.adapter = ProfileAdapter(
            profiles = prefs.profiles(),
            activeId = prefs.activeProfileId,
            onSelect = { profile ->
                prefs.activeProfileId = profile.id
                refresh()
            },
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
    }

    private fun showEditDialog(profile: ChildProfile?) {
        val nameInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            hint = getString(R.string.profile_name_hint)
            profile?.let { setText(it.name) }
        }
        val emojiInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = getString(R.string.profile_emoji_hint)
            profile?.let { setText(it.emoji) }
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
            addView(nameInput)
            addView(emojiInput)
        }
        AlertDialog.Builder(this)
            .setTitle(if (profile == null) R.string.add_profile else R.string.edit_profile)
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isEmpty()) return@setPositiveButton
                val emoji = emojiInput.text.toString().trim()
                if (profile == null) {
                    prefs.addProfile(name, emoji)
                } else {
                    prefs.updateProfile(
                        profile.copy(name = name, emoji = emoji.ifBlank { profile.emoji })
                    )
                }
                refresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmDelete(profile: ChildProfile) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_profile)
            .setMessage(getString(R.string.delete_profile_confirm, profile.name))
            .setPositiveButton(R.string.ok) { _, _ ->
                if (!prefs.deleteProfile(profile.id)) {
                    Toast.makeText(this, R.string.cannot_delete_last, Toast.LENGTH_SHORT).show()
                }
                refresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
