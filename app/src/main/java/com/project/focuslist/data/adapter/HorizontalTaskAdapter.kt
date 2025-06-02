package com.project.focuslist.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.focuslist.R
import com.project.focuslist.data.enumData.TaskPriority
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.model.TaskWithUser
import com.project.focuslist.databinding.ItemHorizontalTaskBinding

class HorizontalTaskAdapter(
    private val onItemClickListener: (Task) -> Unit,
) : ListAdapter<TaskWithUser, HorizontalTaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    inner class TaskViewHolder(private val binding: ItemHorizontalTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(taskWithUser: TaskWithUser) {
            with(binding) {
                tvTitle.text = taskWithUser.task.taskTitle
                tvDesc.text = taskWithUser.task.taskBody
                tvTimes.text = taskWithUser.task.taskDueHours

                if (taskWithUser.task.taskPriority == TaskPriority.MID.value) {
                    tvTitle.setTextColor(itemView.context.getColor(R.color.dark_yellow))
                    tvDesc.setTextColor(itemView.context.getColor(R.color.dark_yellow))
                    tvTimes.setTextColor(itemView.context.getColor(R.color.dark_yellow))
                } else {
                    tvTitle.setTextColor(itemView.context.getColor(R.color.white))
                    tvDesc.setTextColor(itemView.context.getColor(R.color.white))
                    tvTimes.setTextColor(itemView.context.getColor(R.color.white))
                }

                when (taskWithUser.task.taskPriority) {
                    TaskPriority.LOW.value -> {
                        cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.blue))
                        tvPriority.text = TaskPriority.LOW.name
                        cvPriority.visibility = ViewGroup.VISIBLE
                    }
                    TaskPriority.MID.value -> {
                        cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.yellow))
                        tvPriority.text = TaskPriority.MID.name
                        cvPriority.visibility = ViewGroup.VISIBLE
                    }
                    TaskPriority.HIGH.value -> {
                        cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.red))
                        tvPriority.text = TaskPriority.HIGH.name
                        cvPriority.visibility = ViewGroup.VISIBLE
                    }
                    else -> {
                        cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.blue))
                        cvPriority.visibility = ViewGroup.GONE
                    }
                }

                itemView.setOnClickListener { onItemClickListener(taskWithUser.task) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemHorizontalTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        if (task != null) {
            holder.bind(task)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TaskWithUser>() {
            override fun areItemsTheSame(oldItem: TaskWithUser, newItem: TaskWithUser): Boolean {
                return oldItem.task.taskId == newItem.task.taskId
            }

            override fun areContentsTheSame(oldItem: TaskWithUser, newItem: TaskWithUser): Boolean {
                return oldItem == newItem
            }
        }
    }
}

