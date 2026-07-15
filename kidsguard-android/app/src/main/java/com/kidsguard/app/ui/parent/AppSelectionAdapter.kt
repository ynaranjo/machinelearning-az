package com.kidsguard.app.ui.parent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kidsguard.app.databinding.ItemAppSelectBinding
import com.kidsguard.app.model.AppInfo

class AppSelectionAdapter(
    private val apps: List<AppInfo>,
    initiallyAllowed: Set<String>,
    private val onToggle: (packageName: String, allowed: Boolean) -> Unit
) : RecyclerView.Adapter<AppSelectionAdapter.ViewHolder>() {

    private val allowed = initiallyAllowed.toMutableSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(apps[position])

    override fun getItemCount(): Int = apps.size

    inner class ViewHolder(
        private val binding: ItemAppSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInfo) {
            binding.ivIcon.setImageDrawable(app.icon)
            binding.tvLabel.text = app.label
            binding.cbAllowed.setOnCheckedChangeListener(null)
            binding.cbAllowed.isChecked = app.packageName in allowed
            binding.cbAllowed.setOnCheckedChangeListener { _, checked ->
                if (checked) allowed.add(app.packageName) else allowed.remove(app.packageName)
                onToggle(app.packageName, checked)
            }
            binding.root.setOnClickListener { binding.cbAllowed.toggle() }
        }
    }
}
