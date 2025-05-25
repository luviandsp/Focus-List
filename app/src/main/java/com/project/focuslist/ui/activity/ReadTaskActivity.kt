package com.project.focuslist.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.project.focuslist.R
import com.project.focuslist.data.viewmodel.TaskDraftViewModel
import com.project.focuslist.databinding.ActivityReadTaskBinding
import com.project.focuslist.data.viewmodel.TaskViewModel

class ReadTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadTaskBinding
    private val taskViewModel by viewModels<TaskViewModel>()
    private val taskDraftViewModel by viewModels<TaskDraftViewModel>()

    private var taskId: String? = null
    private var taskDraftId: Int? = null

    companion object {
        private const val TAG = "ReadTaskActivity"
        const val TASK_ID = "task_id"
        const val TASK_DRAFT_ID = "task_draft_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReadTaskBinding.inflate(layoutInflater)
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
                    tvTitle.text = it.taskTitle
                    tvDescription.text = it.taskBody
                    Glide.with(this@ReadTaskActivity)
                        .load(it.taskImageUrl)
                        .placeholder(R.drawable.baseline_add_photo_alternate_24)
                        .into(ivImage)
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
            ivBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun observeViewModels() {
        taskViewModel.taskTitle.observe(this) {
            binding.tvTitle.text = it
        }

        taskViewModel.taskBody.observe(this) {
            binding.tvDescription.text = it
        }

        taskViewModel.taskImageUrl.observe(this) {
            if (it != null) {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.baseline_add_photo_alternate_24)
                    .into(binding.ivImage)
            } else {
                binding.ivImage.setImageResource(R.drawable.baseline_add_photo_alternate_24)
            }
        }
    }
}