package com.kidsguard.app.ui.parent

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityWebFilterBinding
import com.kidsguard.app.util.WebFilter

/**
 * Configura el navegador infantil del perfil activo: activarlo, elegir el
 * modo (lista negra o blanca) y gestionar la lista de dominios.
 */
class WebFilterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebFilterBinding
    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: DomainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        binding.swBrowser.isChecked = prefs.browserEnabled
        binding.swBrowser.setOnCheckedChangeListener { _, checked ->
            prefs.browserEnabled = checked
            updateEnabledState()
        }

        binding.swWhitelist.isChecked = prefs.webWhitelistMode
        binding.swWhitelist.setOnCheckedChangeListener { _, checked ->
            prefs.webWhitelistMode = checked
            refresh()
        }

        adapter = DomainAdapter { domain -> removeDomain(domain) }
        binding.rvDomains.layoutManager = LinearLayoutManager(this)
        binding.rvDomains.adapter = adapter

        binding.btnAdd.setOnClickListener { showAddDialog() }

        updateEnabledState()
        refresh()
    }

    private fun updateEnabledState() {
        val enabled = prefs.browserEnabled
        binding.groupRules.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun refresh() {
        val whitelist = prefs.webWhitelistMode
        binding.tvListTitle.text = getString(
            if (whitelist) R.string.web_allowed_list else R.string.web_blocked_list
        )
        binding.tvListHint.text = getString(
            if (whitelist) R.string.web_whitelist_hint else R.string.web_blacklist_hint
        )
        adapter.submit(currentDomains().sorted())
    }

    private fun currentDomains(): Set<String> =
        if (prefs.webWhitelistMode) prefs.allowedDomains else prefs.blockedDomains

    private fun saveDomains(domains: Set<String>) {
        if (prefs.webWhitelistMode) prefs.allowedDomains = domains
        else prefs.blockedDomains = domains
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
                val domain = WebFilter.normalizeDomain(input.text.toString())
                if (domain.isNotEmpty()) {
                    saveDomains(currentDomains() + domain)
                    refresh()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun removeDomain(domain: String) {
        saveDomains(currentDomains() - domain)
        refresh()
    }
}
