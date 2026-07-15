package com.kidsguard.app.ui.parent

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.databinding.ActivityTimeLimitsBinding
import com.kidsguard.app.util.TimeRules

/**
 * Configura el límite diario de uso y el horario de dormir.
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
