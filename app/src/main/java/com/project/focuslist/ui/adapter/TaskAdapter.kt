package com.project.focuslist.ui.adapter


import android.annotation.SuppressLint
import android.media.SoundPool
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.project.focuslist.R
import com.project.focuslist.data.enumData.TaskPriority
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.TaskItemBinding

class TaskAdapter(private var taskList: MutableList<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    lateinit var onItemClickListener: OnItemClickListener
    lateinit var onCheckBoxClickListener: (Task, Boolean) -> Unit
    private var sp: SoundPool = SoundPool.Builder().setMaxStreams(10).build()
    private var soundIdBell: Int = 0
    private var spLoaded = false

    init {
        sp.setOnLoadCompleteListener { _, _, status ->
            spLoaded = (status == 0)
        }
    }

    inner class TaskViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            with(binding) {
                var isChecked = task.isCompleted
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
                    onCheckBoxClickListener(task, isChecked)
                }

                tvTaskName.text = task.title
                tvTaskBody.text = task.body
                tvDueDate.text = task.dueDate

                when (task.priority) {
                    TaskPriority.LOW.value -> constraintLayout.setBackgroundResource(R.drawable.background_shape_1)
                    TaskPriority.MEDIUM.value -> constraintLayout.setBackgroundResource(R.drawable.background_shape_2)
                    TaskPriority.HIGH.value -> constraintLayout.setBackgroundResource(R.drawable.background_shape_3)
                }

                itemView.setOnClickListener { onItemClickListener.onItemClick(task) }
            }
        }
    }

    fun playSoundBell() {
        if (spLoaded) sp.play(soundIdBell, 1f, 1f, 0, 0, 1f)
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
        soundIdBell = sp.load(parent.context, R.raw.bell_sound, 1)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int = taskList.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])
    }
}

