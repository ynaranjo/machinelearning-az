package com.kidsguard.app.ui.parent

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityTimeLimitsBinding
import com.kidsguard.app.databinding.ItemCategoryLimitBinding
import com.kidsguard.app.util.AppCategories
import com.kidsguard.app.util.TimeRules

/**
 * Configura el límite diario de uso, el horario de dormir y los
 * límites por categoría de apps.
 */
class TimeLimitsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeLimitsBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeLimitsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        setupDailyLimit()
        setupBedtime()
        setupCategoryLimits()
    }

    private fun setupCategoryLimits() {
        binding.llCategories.removeAllViews()
        val limits = prefs.categoryLimits()
        AppCategories.ALL.forEach { category ->
            val row = ItemCategoryLimitBinding.inflate(
                layoutInflater, binding.llCategories, false
            )
            row.tvCatLabel.text = AppCategories.label(this, category)
            row.tvCatLimit.text = limits[category]
                ?.let { getString(R.string.limit_fmt, it) }
                ?: getString(R.string.no_limit)
            row.root.setOnClickListener { showCategoryLimitDialog(category) }
            binding.llCategories.addView(row.root)
        }
    }

    private fun showCategoryLimitDialog(category: Int) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = getString(R.string.set_limit_hint)
            prefs.categoryLimitFor(category)?.let { setText(it.toString()) }
        }
        val container = FrameLayout(this).apply {
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
            addView(input)
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.set_limit_title, AppCategories.label(this, category)))
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                prefs.setCategoryLimit(category, input.text.toString().toIntOrNull())
                setupCategoryLimits()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupDailyLimit() {
        val limit = prefs.dailyLimitMinutes
        binding.swDaily.isChecked = limit >= 0
        binding.sbDaily.progress = if (limit >= 0) limit else DEFAULT_DAILY_MINUTES
        updateDailyLabel()

        binding.swDaily.setOnCheckedChangeListener { _, checked ->
            prefs.dailyLimitMinutes = if (checked) {
                binding.sbDaily.progress.coerceAtLeast(MIN_DAILY_MINUTES)
            } else {
                -1
            }
            updateDailyLabel()
        }

        binding.sbDaily.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && progress < MIN_DAILY_MINUTES) {
                    seekBar.progress = MIN_DAILY_MINUTES
                    return
                }
                updateDailyLabel()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (binding.swDaily.isChecked) {
                    prefs.dailyLimitMinutes = seekBar.progress.coerceAtLeast(MIN_DAILY_MINUTES)
                }
            }
        })
    }

    private fun updateDailyLabel() {
        binding.tvDailyValue.text = getString(R.string.minutes_fmt, binding.sbDaily.progress)
    }

    private fun setupBedtime() {
        binding.swBedtime.isChecked = prefs.bedtimeEnabled
        updateBedtimeButtons()

        binding.swBedtime.setOnCheckedChangeListener { _, checked ->
            prefs.bedtimeEnabled = checked
        }

        binding.btnBedStart.setOnClickListener {
            pickTime(prefs.bedtimeStartMinutes) { minutes ->
                prefs.bedtimeStartMinutes = minutes
                updateBedtimeButtons()
            }
        }
        binding.btnBedEnd.setOnClickListener {
            pickTime(prefs.bedtimeEndMinutes) { minutes ->
                prefs.bedtimeEndMinutes = minutes
                updateBedtimeButtons()
            }
        }
    }

    private fun updateBedtimeButtons() {
        binding.btnBedStart.text = getString(R.string.bedtime_from) + " " +
            TimeRules.formatTimeOfDay(prefs.bedtimeStartMinutes)
        binding.btnBedEnd.text = getString(R.string.bedtime_to) + " " +
            TimeRules.formatTimeOfDay(prefs.bedtimeEndMinutes)
    }

    private fun pickTime(currentMinutes: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(
            this,
            { _, hour, minute -> onPicked(hour * 60 + minute) },
            currentMinutes / 60,
            currentMinutes % 60,
            true
        ).show()
    }

    companion object {
        private const val DEFAULT_DAILY_MINUTES = 120
        private const val MIN_DAILY_MINUTES = 15
    }
}
