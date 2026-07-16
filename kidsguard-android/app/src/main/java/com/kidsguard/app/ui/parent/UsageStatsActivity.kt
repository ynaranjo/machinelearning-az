package com.kidsguard.app.ui.parent

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidsguard.app.R
import com.kidsguard.app.databinding.ActivityUsageBinding
import com.kidsguard.app.model.AppInfo
import com.kidsguard.app.util.TimeRules

/**
 * Muestra el tiempo de uso de hoy por app. Al tocar una app se puede fijar
 * su límite diario en minutos. El estado vive en UsageStatsViewModel (MVVM).
 */
class UsageStatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsageBinding
    private val viewModel: UsageStatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvUsage.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val data = viewModel.load()
        binding.tvTotal.text = getString(
            R.string.usage_total_fmt,
            TimeRules.formatDuration(data.totalSeconds)
        )
        binding.rvUsage.adapter =
            UsageAdapter(data.apps, data.usageSeconds, data.limits) { app ->
                showLimitDialog(app)
            }
    }

    private fun showLimitDialog(app: AppInfo) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = getString(R.string.set_limit_hint)
            viewModel.appLimitFor(app.packageName)?.let { setText(it.toString()) }
        }
        val container = FrameLayout(this).apply {
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
            addView(input)
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.set_limit_title, app.label))
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.setAppLimit(app.packageName, input.text.toString().toIntOrNull())
                refresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
