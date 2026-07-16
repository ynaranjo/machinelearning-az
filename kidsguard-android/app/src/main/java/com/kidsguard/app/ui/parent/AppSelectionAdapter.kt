package com.kidsguard.app.ui.parent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kidsguard.app.databinding.ItemAppSelectBinding
import com.kidsguard.app.databinding.ItemCategoryHeaderBinding
import com.kidsguard.app.model.AppInfo

/** Lista de selección de apps agrupada por categorías del sistema. */
class AppSelectionAdapter(
    private val items: List<SelectionItem>,
    initiallyAllowed: Set<String>,
    private val onToggle: (packageName: String, allowed: Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    sealed class SelectionItem {
        data class Header(val title: String) : SelectionItem()
        data class App(val info: AppInfo) : SelectionItem()
    }

    private val allowed = initiallyAllowed.toMutableSet()

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SelectionItem.Header -> TYPE_HEADER
        is SelectionItem.App -> TYPE_APP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemCategoryHeaderBinding.inflate(inflater, parent, false))
        } else {
            AppViewHolder(ItemAppSelectBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SelectionItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is SelectionItem.App -> (holder as AppViewHolder).bind(item.info)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(
        private val binding: ItemCategoryHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(title: String) {
            binding.tvHeader.text = title
        }
    }

    inner class AppViewHolder(
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

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP = 1
    }
}
