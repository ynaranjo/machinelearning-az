package com.kidsguard.app.ui.parent

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.data.UsageHistoryRepository
import com.kidsguard.app.databinding.ActivityWeeklyReportBinding
import com.kidsguard.app.databinding.ItemDayUsageBinding
import com.kidsguard.app.databinding.ItemTopAppBinding
import com.kidsguard.app.util.TimeRules
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Informe de los últimos 7 días del perfil activo: tiempo total por día
 * (barras) y top de apps más usadas de la semana.
 */
class WeeklyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyReportBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)
    }

    override fun onResume() {
        super.onResume()
        UsageHistoryRepository.weeklyReport(this, prefs.activeProfileId) { report ->
            if (!isFinishing && !isDestroyed) render(report)
        }
    }

    private fun render(report: UsageHistoryRepository.WeeklyReport) {
        binding.llDays.removeAllViews()
        binding.llTopApps.removeAllViews()

        val total = report.days.sumOf { it.totalSeconds }
        binding.tvEmpty.visibility = if (total == 0) View.VISIBLE else View.GONE

        val maxSeconds = report.days.maxOf { it.totalSeconds }.coerceAtLeast(1)
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayFormat = SimpleDateFormat("EEE d", Locale.getDefault())

        report.days.forEach { day ->
            val item = ItemDayUsageBinding.inflate(layoutInflater, binding.llDays, false)
            item.tvDay.text = runCatching {
                dayFormat.format(parser.parse(day.date)!!)
            }.getOrDefault(day.date)
            item.pbDay.max = maxSeconds
            item.pbDay.progress = day.totalSeconds
            item.tvDuration.text = TimeRules.formatDuration(day.totalSeconds)
            binding.llDays.addView(item.root)
        }

        binding.tvTopTitle.visibility =
            if (report.topApps.isEmpty()) View.GONE else View.VISIBLE
        val pm = packageManager
        report.topApps.forEach { (packageName, seconds) ->
            val item = ItemTopAppBinding.inflate(layoutInflater, binding.llTopApps, false)
            val appInfo = runCatching { pm.getApplicationInfo(packageName, 0) }.getOrNull()
            if (appInfo != null) {
                item.ivIcon.setImageDrawable(pm.getApplicationIcon(appInfo))
                item.tvLabel.text = pm.getApplicationLabel(appInfo)
            } else {
                // App desinstalada desde entonces: mostrar el paquete.
                item.tvLabel.text = packageName
            }
            item.tvTime.text = TimeRules.formatDuration(seconds)
            binding.llTopApps.addView(item.root)
        }
    }
}
