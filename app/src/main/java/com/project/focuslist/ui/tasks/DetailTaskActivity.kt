package com.project.focuslist.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.project.focuslist.R
import com.project.focuslist.data.enumData.TaskPriority
import com.project.focuslist.data.viewmodel.TaskDraftViewModel
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.databinding.ActivityDetailTaskBinding

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaskBinding
    private val taskViewModel by viewModels<TaskViewModel>()
    private val taskDraftViewModel by viewModels<TaskDraftViewModel>()

    private var taskId: String? = null
    private var taskDraftId: Int? = null

    companion object {
        private const val TAG = "DetailTaskActivity"
        const val TASK_ID = "task_id"
        const val TASK_DRAFT_ID = "task_draft_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        taskId = intent.getStringExtra(TASK_ID)
        taskDraftId = intent.getIntExtra(TASK_DRAFT_ID, -1)
        Log.d(TAG, "Task ID: $taskId")
        Log.d(TAG, "Task Draft ID: $taskDraftId")

        if (taskId == null && taskDraftId == -1) {
            finish()
        } else if (taskId != null) {
            taskViewModel.getTaskById(taskId!!)
        } else {
            taskDraftViewModel.getTaskById(taskDraftId!!).observe(this) {
                with(binding) {
                    toolbar.title = it.taskTitle
                    tvDescription.text = it.taskBody
                    tvDeadline.text = it.taskDueTime
                    when (it.taskPriority) {
                        1 -> tvPriority.text = TaskPriority.LOW.name
                        2 -> tvPriority.text = TaskPriority.MID.name
                        3 -> tvPriority.text = TaskPriority.HIGH.name
                    }
                    Glide.with(this@DetailTaskActivity)
                        .load(it.taskImageUrl)
                        .placeholder(R.drawable.baseline_add_photo_alternate_24)
                        .into(ivTaskImage)
                }
            }
        }

        initViews()

        if (taskId != null) {
            observeViewModels()
        }
    }

    private fun initViews() {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                finish()
            }

            btnEdit.setOnClickListener {
                val intent = Intent(this@DetailTaskActivity, EditTaskActivity::class.java).apply {
                    putExtra(EditTaskActivity.TASK_ID, taskId)
                    putExtra(EditTaskActivity.TASK_DRAFT_ID, taskDraftId)
                }
                startActivity(intent)
            }
        }
    }

    private fun observeViewModels() {
        taskViewModel.taskTitle.observe(this) {
            binding.toolbar.title = it
        }

        taskViewModel.taskBody.observe(this) {
            binding.tvDescription.text = it
        }

        taskViewModel.taskDueTime.observe(this) {
            binding.tvDeadline.text = it
        }

        taskViewModel.taskPriority.observe(this) {
            when (it) {
                1 -> binding.tvPriority.text = TaskPriority.LOW.name
                2 -> binding.tvPriority.text = TaskPriority.MID.name
                3 -> binding.tvPriority.text = TaskPriority.HIGH.name
            }
        }

        taskViewModel.taskImageUrl.observe(this) {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.baseline_add_photo_alternate_24)
                .into(binding.ivTaskImage)
        }
    }
}