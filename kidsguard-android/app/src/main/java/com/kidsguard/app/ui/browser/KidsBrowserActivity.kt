package com.kidsguard.app.ui.browser

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityKidsBrowserBinding
import com.kidsguard.app.util.WebFilter

/**
 * Navegador infantil: WebView con filtrado de dominios (lista negra o
 * blanca según el perfil), SafeSearch forzado en los buscadores y modo
 * restringido de YouTube vía cabecera.
 */
class KidsBrowserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKidsBrowserBinding
    private lateinit var prefs: PreferencesManager

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKidsBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        binding.webView.webViewClient = FilteringWebViewClient()

        binding.etUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                navigate(binding.etUrl.text.toString())
                true
            } else {
                false
            }
        }
        binding.btnGo.setOnClickListener { navigate(binding.etUrl.text.toString()) }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) binding.webView.goBack() else finish()
            }
        })

        navigate(HOME_URL)
    }

    /** Convierte lo tecleado en URL o en búsqueda de Google. */
    private fun navigate(input: String) {
        val text = input.trim()
        if (text.isEmpty()) return
        val url = when {
            text.startsWith("http://") || text.startsWith("https://") -> text
            text.contains(" ") || !text.contains(".") ->
                "https://www.google.com/search?q=" + Uri.encode(text)
            else -> "https://$text"
        }
        loadFiltered(url)
    }

    private fun loadFiltered(url: String) {
        if (!WebFilter.isAllowed(prefs, url)) {
            showBlocked(url)
            return
        }
        hideBlocked()
        val safeUrl = WebFilter.withSafeSearch(url)
        if (WebFilter.isYouTube(safeUrl)) {
            binding.webView.loadUrl(safeUrl, mapOf("YouTube-Restrict" to "Strict"))
        } else {
            binding.webView.loadUrl(safeUrl)
        }
    }

    private fun showBlocked(url: String) {
        binding.llBlocked.visibility = View.VISIBLE
        binding.webView.visibility = View.GONE
        val host = runCatching { Uri.parse(url).host }.getOrNull() ?: url
        binding.tvBlockedHost.text = getString(R.string.web_blocked_msg, host)
    }

    private fun hideBlocked() {
        binding.llBlocked.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }

    private inner class FilteringWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString() ?: return false
            if (!WebFilter.isAllowed(prefs, url)) {
                showBlocked(url)
                return true
            }
            val safeUrl = WebFilter.withSafeSearch(url)
            if (safeUrl != url || WebFilter.isYouTube(safeUrl)) {
                loadFiltered(safeUrl)
                return true
            }
            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            url?.let { binding.etUrl.setText(it) }
        }
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val HOME_URL = "https://www.google.com"
    }
}
