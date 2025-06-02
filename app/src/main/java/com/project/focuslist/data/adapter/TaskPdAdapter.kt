package com.project.focuslist.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.project.focuslist.R
import com.project.focuslist.data.enumData.TaskPriority
import com.project.focuslist.data.model.TaskDraft
import com.project.focuslist.databinding.ItemDraftTaskBinding

class TaskPdAdapter(
    private val onItemClickListener: (TaskDraft) -> Unit,
) : PagingDataAdapter<TaskDraft, TaskPdAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    inner class TaskViewHolder(private val binding: ItemDraftTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskDraft?) {
            if (task == null) return

            with(binding) {

                tvTitle.text = task.taskTitle
                tvDesc.text = task.taskBody

                if (task.taskPriority == TaskPriority.MID.value) {
                    tvTitle.setTextColor(itemView.context.getColor(R.color.dark_yellow))
                    tvDesc.setTextColor(itemView.context.getColor(R.color.dark_yellow))
                    ivArrow.setColorFilter(itemView.context.getColor(R.color.dark_yellow))
                } else {
                    tvTitle.setTextColor(itemView.context.getColor(R.color.white))
                    tvDesc.setTextColor(itemView.context.getColor(R.color.white))
                    ivArrow.setColorFilter(itemView.context.getColor(R.color.white))
                }

                when (task.taskPriority) {
                    TaskPriority.LOW.value -> cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.blue))
                    TaskPriority.MID.value -> cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.yellow))
                    TaskPriority.HIGH.value -> cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.red))
                    else -> cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.blue))
                }

                itemView.setOnClickListener { onItemClickListener(task) }
            }
        }
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        if (task != null) {
            holder.bind(task)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemDraftTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TaskDraft>() {
            override fun areItemsTheSame(oldItem: TaskDraft, newItem: TaskDraft): Boolean {
                return oldItem.taskId == newItem.taskId
            }

            override fun areContentsTheSame(oldItem: TaskDraft, newItem: TaskDraft): Boolean {
                return oldItem == newItem
            }
        }
    }
}
