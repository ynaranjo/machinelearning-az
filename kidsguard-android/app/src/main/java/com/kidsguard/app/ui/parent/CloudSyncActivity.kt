package com.kidsguard.app.ui.parent

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kidsguard.app.R
import com.kidsguard.app.data.PreferencesManager
import com.kidsguard.app.data.remote.SyncManager
import com.kidsguard.app.databinding.ActivityCloudSyncBinding
import com.kidsguard.app.service.SyncWorker
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.Executors

/**
 * Control remoto en la nube (opcional): empareja este dispositivo con el
 * servidor del adulto para monitorizarlo y controlarlo a distancia.
 */
class CloudSyncActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCloudSyncBinding
    private lateinit var prefs: PreferencesManager
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCloudSyncBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        binding.swCloud.isChecked = prefs.cloudEnabled
        binding.etUrl.setText(prefs.cloudBackendUrl)

        binding.swCloud.setOnCheckedChangeListener { _, checked ->
            prefs.cloudEnabled = checked
            if (checked) SyncWorker.schedule(this) else SyncWorker.cancel(this)
            updateUi()
        }
        binding.btnPair.setOnClickListener { pair() }
        binding.btnUnpair.setOnClickListener {
            prefs.clearPairing()
            Toast.makeText(this, R.string.cloud_unpaired, Toast.LENGTH_SHORT).show()
            updateUi()
        }
        binding.btnSyncNow.setOnClickListener { syncNow() }

        updateUi()
    }

    private fun pair() {
        prefs.cloudBackendUrl = binding.etUrl.text.toString()
        val familyCode = binding.etFamilyCode.text.toString().trim()
        if (prefs.cloudBackendUrl.isEmpty() || familyCode.isEmpty()) {
            Toast.makeText(this, R.string.cloud_need_url_code, Toast.LENGTH_SHORT).show()
            return
        }
        setBusy(true)
        executor.execute {
            val error = SyncManager.pair(this, familyCode)
            mainHandler.post {
                setBusy(false)
                if (error == null) {
                    prefs.cloudEnabled = true
                    binding.swCloud.isChecked = true
                    SyncWorker.schedule(this)
                    Toast.makeText(this, R.string.cloud_paired, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this, getString(R.string.cloud_pair_error, error), Toast.LENGTH_LONG
                    ).show()
                }
                updateUi()
            }
        }
    }

    private fun syncNow() {
        setBusy(true)
        executor.execute {
            val ok = SyncManager.syncOnce(this)
            mainHandler.post {
                setBusy(false)
                Toast.makeText(
                    this,
                    if (ok) R.string.cloud_sync_ok else R.string.cloud_sync_error,
                    Toast.LENGTH_SHORT
                ).show()
                updateUi()
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.progress.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnPair.isEnabled = !busy
        binding.btnSyncNow.isEnabled = !busy
    }

    private fun updateUi() {
        val paired = prefs.isPaired
        binding.groupPair.visibility = if (paired) View.GONE else View.VISIBLE
        binding.groupPaired.visibility = if (paired) View.VISIBLE else View.GONE

        binding.tvStatus.text = getString(
            if (paired) R.string.cloud_status_paired else R.string.cloud_status_unpaired
        )
        val last = prefs.cloudLastSync
        binding.tvLastSync.text = if (last > 0) {
            getString(
                R.string.cloud_last_sync,
                DateFormat.getDateTimeInstance().format(Date(last))
            )
        } else {
            getString(R.string.cloud_never_synced)
        }
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}
