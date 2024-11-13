package com.project.focuslist.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.focuslist.R
import com.project.focuslist.data.model.User
import com.project.focuslist.databinding.ProfileItemBinding
import com.project.focuslist.ui.adapter.TaskAdapter.OnItemClickListener

class ProfileAdapter(private var profileList: MutableList<User>): RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    lateinit var onItemClickListener: OnItemClickListener

    inner class ProfileViewHolder(private val binding: ProfileItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            with(binding) {
                tvUsername.text = user.username
                Glide.with(binding.root).load(user.profileImage?: R.drawable.baseline_account_circle_24).into(ivProfileImage)

                itemView.setOnClickListener { onItemClickListener.onItemClick(user) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ProfileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileViewHolder(binding)
    }

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }

    override fun getItemCount(): Int { return profileList.size }

    @SuppressLint("NotifyDataSetChanged")
    fun setProfiles(newProfileList: MutableList<User>) {
        this.profileList = newProfileList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profileList[position])
    }
}