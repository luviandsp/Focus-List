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
import com.project.focuslist.databinding.TaskItemBinding

class TaskAdapter(
    private val onItemClickListener: (Task) -> Unit,
    private val onLongClickListener: (Task) -> Boolean,
    private val onCheckBoxClickListener: (Task, Boolean) -> Unit
) : ListAdapter<TaskWithUser, TaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    private var sp: SoundPool = SoundPool.Builder().setMaxStreams(10).build()
    private var soundIdBell: Int = 0
    private var spLoaded = false

    init {
        sp.setOnLoadCompleteListener { _, _, status ->
            spLoaded = (status == 0)
        }
    }

    inner class TaskViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(taskWithUser: TaskWithUser) {
            with(binding) {
                var isChecked = taskWithUser.task.isCompleted
                ivTaskCheck.setImageResource(
                    if (isChecked) R.drawable.baseline_check_box_24
                    else R.drawable.baseline_check_box_outline_blank_24
                )

                llTaskCheck.setOnClickListener {
                    isChecked = !isChecked
                    ivTaskCheck.setImageResource(
                        if (isChecked) {
                            playSoundBell()
                            Toast.makeText(itemView.context, "Task Completed", Toast.LENGTH_SHORT).show()
                            R.drawable.baseline_check_box_24
                        }
                        else R.drawable.baseline_check_box_outline_blank_24
                    )
                    onCheckBoxClickListener(taskWithUser.task, isChecked)
                }

                tvTaskName.text = taskWithUser.task.taskTitle
                tvTaskBody.text = taskWithUser.task.taskBody
                tvDueDate.text = taskWithUser.task.taskDueDate

                when (taskWithUser.task.taskPriority) {
                    TaskPriority.LOW.value -> constraintLayout.setBackgroundResource(R.drawable.background_shape_1)
                    TaskPriority.MEDIUM.value -> constraintLayout.setBackgroundResource(R.drawable.background_shape_2)
                    TaskPriority.HIGH.value -> constraintLayout.setBackgroundResource(R.drawable.background_shape_3)
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
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

