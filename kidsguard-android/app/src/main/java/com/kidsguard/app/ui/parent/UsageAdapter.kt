package com.kidsguard.app.ui.parent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kidsguard.app.R
import com.kidsguard.app.databinding.ItemUsageBinding
import com.kidsguard.app.model.AppInfo
import com.kidsguard.app.util.TimeRules

class UsageAdapter(
    private val apps: List<AppInfo>,
    private val usageSeconds: Map<String, Int>,
    private val limits: Map<String, Int>,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<UsageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUsageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(apps[position])

    override fun getItemCount(): Int = apps.size

    inner class ViewHolder(
        private val binding: ItemUsageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInfo) {
            val context = binding.root.context
            binding.ivIcon.setImageDrawable(app.icon)
            binding.tvLabel.text = app.label
            binding.tvTime.text =
                TimeRules.formatDuration(usageSeconds[app.packageName] ?: 0)
            val limit = limits[app.packageName]
            binding.tvLimit.text = if (limit != null) {
                context.getString(R.string.limit_fmt, limit)
            } else {
                context.getString(R.string.no_limit)
            }
            binding.root.setOnClickListener { onClick(app) }
        }
    }
}
