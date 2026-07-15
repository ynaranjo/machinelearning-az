package com.kidsguard.app.ui.launcher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kidsguard.app.databinding.ItemKidAppBinding
import com.kidsguard.app.model.AppInfo

class AppGridAdapter(
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.ViewHolder>() {

    private val items = mutableListOf<AppInfo>()

    fun submit(apps: List<AppInfo>) {
        items.clear()
        items.addAll(apps)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKidAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemKidAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInfo) {
            binding.ivIcon.setImageDrawable(app.icon)
            binding.tvLabel.text = app.label
            binding.root.setOnClickListener { onClick(app) }
        }
    }
}
