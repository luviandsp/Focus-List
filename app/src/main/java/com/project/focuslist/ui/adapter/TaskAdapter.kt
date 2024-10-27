package com.project.focuslist.ui.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.focuslist.R
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.TaskItemBinding

class TaskAdapter(private var taskList: MutableList<Task>): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    lateinit var onItemClickListener: OnItemClickListener
    lateinit var onCheckBoxClickListener: (Task, Boolean) -> Unit

    inner class TaskViewHolder(private val binding: TaskItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            with (binding) {
                tvTaskName.text = task.title
                tvTaskBody.text = task.body
                tvDueDate.text = task.dueDate
                cbTaskCheck.isChecked = task.isCompleted

                itemView.setOnClickListener { onItemClickListener.onItemClick(task) }

                cbTaskCheck.setOnCheckedChangeListener { _, isChecked ->
                    onCheckBoxClickListener(task, isChecked)
                }

                when (task.priority) {
                    1 -> constraintLayout.setBackgroundResource(R.drawable.background_shape_1)
                    2 -> constraintLayout.setBackgroundResource(R.drawable.background_shape_2)
                    3 -> constraintLayout.setBackgroundResource(R.drawable.background_shape_3)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTasks(newTaskList: MutableList<Task>) {
        this.taskList = newTaskList
        notifyDataSetChanged()
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