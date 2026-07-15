package com.kidsguard.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.util.BlockEvaluator

/**
 * Bloqueo instantáneo: reacciona al evento TYPE_WINDOW_STATE_CHANGED en el
 * momento exacto en que una app pasa a primer plano, sin la ventana de ~1s
 * del sondeo. No lee contenido de pantalla (canRetrieveWindowContent=false).
 *
 * AppMonitorService sigue activo como respaldo y como contador de tiempo
 * de uso para los límites diarios y por app.
 */
class AppBlockerAccessibilityService : AccessibilityService() {

    private lateinit var prefs: PreferencesManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PreferencesManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return

        if (!::prefs.isInitialized) prefs = PreferencesManager(this)
        if (!prefs.childModeActive) return
        if (BlockEvaluator.isIgnored(this, packageName)) return

        val reason = BlockEvaluator.evaluate(this, prefs, packageName) ?: return
        BlockEvaluator.block(this, packageName, reason)
    }

    override fun onInterrupt() = Unit
}
