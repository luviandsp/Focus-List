package com.project.focuslist.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.TaskItemBinding

class TaskAdapter(private val taskList: MutableList<Task>): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    lateinit var onItemClickListener: OnItemClickListener

    inner class TaskViewHolder(private val binding: TaskItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            with (binding) {
                tvTaskName.text = task.title
                tvTaskBody.text = task.body
                cbTaskCheck.isChecked = task.completed
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int { return taskList.size }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])
        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(taskList[position]) }
    }
}