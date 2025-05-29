package com.project.focuslist.data.adapter

import android.media.SoundPool
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.focuslist.R
import com.project.focuslist.data.enumData.TaskPriority
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.model.TaskWithUser
import com.project.focuslist.databinding.ItemVerticalTaskBinding

class VerticalTaskAdapter(
    private val onItemClickListener: (Task) -> Unit,
    private val onLongClickListener: (Task) -> Boolean,
    private val onCheckBoxClickListener: (Task, Boolean) -> Unit
) : ListAdapter<TaskWithUser, VerticalTaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    private var sp: SoundPool = SoundPool.Builder().setMaxStreams(10).build()
    private var soundIdBell: Int = 0
    private var spLoaded = false

    init {
        sp.setOnLoadCompleteListener { _, _, status ->
            spLoaded = (status == 0)
        }
    }

    inner class TaskViewHolder(private val binding: ItemVerticalTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(taskWithUser: TaskWithUser) {
            with(binding) {

                var isChecked = taskWithUser.task.isCompleted

                ivTaskCheck.setImageResource(
                    if (isChecked) {
                        if (taskWithUser.task.taskPriority == TaskPriority.MID.value) {
                            R.drawable.checkbox_outline_yellow
                        } else {
                            R.drawable.checkbox_outline
                        }
                    } else if (taskWithUser.task.taskPriority == TaskPriority.MID.value) {
                        R.drawable.checkbox_outline_blank_yellow
                    } else {
                        R.drawable.checkbox_outline_blank
                    }
                )

                llTaskCheck.setOnClickListener {
                    isChecked = !isChecked
                    ivTaskCheck.setImageResource(
                        if (isChecked) {
                            playSoundBell()
                            Toast.makeText(itemView.context, "Task Completed", Toast.LENGTH_SHORT).show()
                            if (taskWithUser.task.taskPriority == TaskPriority.MID.value) {
                                R.drawable.checkbox_outline_yellow
                            } else {
                                R.drawable.checkbox_outline
                            }
                        }
                        else if (taskWithUser.task.taskPriority == TaskPriority.MID.value) {
                            R.drawable.checkbox_outline_blank_yellow
                        }
                        else {
                            R.drawable.checkbox_outline_blank
                        }
                    )
                    onCheckBoxClickListener(taskWithUser.task, isChecked)
                }

                tvTitle.text = taskWithUser.task.taskTitle
                tvDesc.text = taskWithUser.task.taskBody

                if (taskWithUser.task.taskPriority == TaskPriority.MID.value) {
                    tvTitle.setTextColor(itemView.context.getColor(R.color.dark_yellow))
                    tvDesc.setTextColor(itemView.context.getColor(R.color.dark_yellow))
                    ivArrow.setColorFilter(itemView.context.getColor(R.color.dark_yellow))
                } else {
                    tvTitle.setTextColor(itemView.context.getColor(R.color.white))
                    tvDesc.setTextColor(itemView.context.getColor(R.color.white))
                    ivArrow.setColorFilter(itemView.context.getColor(R.color.white))
                }

                when (taskWithUser.task.taskPriority) {
                    TaskPriority.LOW.value -> cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.blue))
                    TaskPriority.MID.value -> cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.yellow))
                    TaskPriority.HIGH.value -> cvTasks.setCardBackgroundColor(itemView.context.getColor(R.color.red))
                }

                itemView.setOnClickListener { onItemClickListener(taskWithUser.task) }
                itemView.setOnLongClickListener { onLongClickListener(taskWithUser.task) }
            }
        }
    }

    fun playSoundBell() {
        if (spLoaded) sp.play(soundIdBell, 1f, 1f, 0, 0, 1f)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemVerticalTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        soundIdBell = sp.load(parent.context, R.raw.bell_sound, 1)
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

