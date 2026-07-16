package com.kidsguard.app.ui.parent

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidsguard.app.R
import com.kidsguard.app.databinding.ActivityWebFilterBinding

/**
 * Configura el navegador infantil del perfil activo: activarlo, elegir el
 * modo (lista negra o blanca) y gestionar la lista de dominios.
 *
 * La lógica de estado vive en WebFilterViewModel (patrón MVVM); esta
 * Activity solo pinta y delega.
 */
class WebFilterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebFilterBinding
    private val viewModel: WebFilterViewModel by viewModels()
    private lateinit var adapter: DomainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swBrowser.isChecked = viewModel.browserEnabled
        binding.swBrowser.setOnCheckedChangeListener { _, checked ->
            viewModel.browserEnabled = checked
            updateEnabledState()
        }

        binding.swWhitelist.isChecked = viewModel.whitelistMode
        binding.swWhitelist.setOnCheckedChangeListener { _, checked ->
            viewModel.whitelistMode = checked
            refresh()
        }

        adapter = DomainAdapter { domain ->
            viewModel.removeDomain(domain)
            refresh()
        }
        binding.rvDomains.layoutManager = LinearLayoutManager(this)
        binding.rvDomains.adapter = adapter

        binding.btnAdd.setOnClickListener { showAddDialog() }

        updateEnabledState()
        refresh()
    }

    private fun updateEnabledState() {
        binding.groupRules.visibility =
            if (viewModel.browserEnabled) View.VISIBLE else View.GONE
    }

    private fun refresh() {
        val whitelist = viewModel.whitelistMode
        binding.tvListTitle.text = getString(
            if (whitelist) R.string.web_allowed_list else R.string.web_blocked_list
        )
        binding.tvListHint.text = getString(
            if (whitelist) R.string.web_whitelist_hint else R.string.web_blacklist_hint
        )
        adapter.submit(viewModel.currentDomains())
    }

    private fun showAddDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            hint = getString(R.string.web_domain_hint)
        }
        val container = FrameLayout(this).apply {
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
            addView(input)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.web_add_domain)
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                if (viewModel.addDomain(input.text.toString())) refresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
