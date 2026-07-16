package com.kidsguard.app.ui.parent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kidsguard.app.databinding.ItemProfileBinding
import com.kidsguard.app.model.ChildProfile

class ProfileAdapter(
    private val profiles: List<ChildProfile>,
    private val activeId: Int,
    private val onSelect: (ChildProfile) -> Unit,
    private val onEdit: (ChildProfile) -> Unit,
    private val onDelete: (ChildProfile) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(profiles[position])

    override fun getItemCount(): Int = profiles.size

    inner class ViewHolder(
        private val binding: ItemProfileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: ChildProfile) {
            binding.tvEmoji.text = profile.emoji
            binding.tvName.text = profile.name
            binding.tvActive.visibility =
                if (profile.id == activeId) View.VISIBLE else View.GONE
            binding.root.setOnClickListener { onSelect(profile) }
            binding.btnEdit.setOnClickListener { onEdit(profile) }
            binding.btnDelete.setOnClickListener { onDelete(profile) }
        }
    }
}
