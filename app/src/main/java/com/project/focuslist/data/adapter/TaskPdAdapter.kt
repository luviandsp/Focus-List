package com.project.focuslist.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.project.focuslist.R
import com.project.focuslist.data.enumData.TaskPriority
import com.project.focuslist.data.model.TaskDraft
import com.project.focuslist.databinding.TaskDraftItemBinding

class TaskPdAdapter(
    private val onItemClickListener: (TaskDraft) -> Unit,
    private val onLongClickListener: (TaskDraft) -> Boolean,
) : PagingDataAdapter<TaskDraft, TaskPdAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    inner class TaskViewHolder(private val binding: TaskDraftItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskDraft?) {
            if (task == null) return

            with(binding) {

                tvTaskName.text = task.taskTitle
                tvTaskBody.text = task.taskBody
                tvDueDate.text = task.taskDueTime

                when (task.taskPriority) {
                    TaskPriority.LOW.value -> constraintLayout.setBackgroundResource(R.drawable.background_shape_1)
                    TaskPriority.MEDIUM.value -> constraintLayout.setBackgroundResource(R.drawable.background_shape_2)
                    TaskPriority.HIGH.value -> constraintLayout.setBackgroundResource(R.drawable.background_shape_3)
                }

                itemView.setOnClickListener { onItemClickListener(task) }
                itemView.setOnLongClickListener { onLongClickListener(task) }
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
        val binding = TaskDraftItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
