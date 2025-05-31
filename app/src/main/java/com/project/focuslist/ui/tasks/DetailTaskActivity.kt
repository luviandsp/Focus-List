package com.project.focuslist.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.project.focuslist.R
import com.project.focuslist.data.enumData.TaskPriority
import com.project.focuslist.data.model.TaskDraft
import com.project.focuslist.data.viewmodel.StorageViewModel
import com.project.focuslist.data.viewmodel.TaskDraftViewModel
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.databinding.ActivityDetailTaskBinding

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaskBinding
    private val taskViewModel by viewModels<TaskViewModel>()
    private val storageViewModel by viewModels<StorageViewModel>()
    private val taskDraftViewModel by viewModels<TaskDraftViewModel>()

    private var taskId: String? = null
    private var taskDraftId: Int? = null

    private var taskTitle: String? = null
    private var taskBody: String? = null
    private var taskPriority: Int? = null
    private var taskDueDate: String? = null
    private var taskDueHours: String? = null
    private var taskDueTime: String? = null
    private var ImageUrl: String? = null
    private var reminderOffsetMillis: Long? = null

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

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete_task -> {
                        if (taskId != null) {
                            deleteTaskFromDB()
                        } else if (taskDraftId != -1) {
                            deleteTaskFromDraft()
                            Toast.makeText(this@DetailTaskActivity, "Task Draft successfully deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        true
                    }
                    else -> false
                }
            }

            if (taskDraftId != -1) {
                btnUploadTask.setOnClickListener {
                    uploadTask()
                }

                btnEdit.apply {
                    setBackgroundColor(getColor(R.color.white))
                    setTextColor(getColor(R.color.blue))
                    strokeColor = getColorStateList(R.color.blue)
                }
            } else {
                btnUploadTask.visibility = View.GONE
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

    private fun uploadTask() {
        taskViewModel.createTask(
            context = this@DetailTaskActivity,
            taskTitle = taskTitle ?: "",
            taskBody = taskBody ?: "",
            taskPriority = taskPriority ?: 0,
            taskDueDate = taskDueDate ?: "",
            taskDueHours = taskDueHours ?: "",
            taskDueTime = taskDueTime ?: "",
            taskImageUrl = ImageUrl ?: "",
            reminderOffsetMillis = reminderOffsetMillis ?: 0
        )

        deleteTaskFromDraft()
        Toast.makeText(this, "Task uploaded", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteTaskFromDraft() {
        if (taskDraftId != -1) {
            taskDraftViewModel.deleteTask(TaskDraft(taskId = taskDraftId!!))
            deleteOldTaskImage(ImageUrl ?: "")
            Log.d(TAG, "Task draft deleted")
        }
    }

    private fun deleteTaskFromDB() {
        if (taskId != null) {
            taskViewModel.deleteTask(taskId!!)
            Log.d(TAG, "Task deleted")
        }
    }

    private fun observeViewModels() {
        taskViewModel.apply {
            operationDeleteResult.observe(this@DetailTaskActivity) {
                if (it.first) {
                    Toast.makeText(this@DetailTaskActivity, it.second, Toast.LENGTH_SHORT).show()
                    deleteOldTaskImage(ImageUrl ?: "")
                }
            }

            taskTitle.observe(this@DetailTaskActivity) {
                binding.toolbar.title = it
            }

            taskBody.observe(this@DetailTaskActivity) {
                binding.tvDescription.text = it
            }

            taskDueTime.observe(this@DetailTaskActivity) {
                binding.tvDeadline.text = it
            }

            taskPriority.observe(this@DetailTaskActivity) {
                when (it) {
                    1 -> binding.tvPriority.text = TaskPriority.LOW.name
                    2 -> binding.tvPriority.text = TaskPriority.MID.name
                    3 -> binding.tvPriority.text = TaskPriority.HIGH.name
                }
            }

            taskImageUrl.observe(this@DetailTaskActivity) { imageUrl ->
                if (imageUrl.isNullOrEmpty()) {
                    binding.flImageContainer.visibility = View.GONE
                } else {
                    binding.flImageContainer.visibility = View.VISIBLE

                    Glide.with(this@DetailTaskActivity)
                        .load(imageUrl)
                        .into(binding.ivTaskImage)
                }
            }
        }

        storageViewModel.deleteStatus.observe(this@DetailTaskActivity) { success ->
            if (success) {
                Log.d(TAG, "Task image deleted")
                finish()
            }
        }
    }

    private fun deleteOldTaskImage(oldImageUrl: String) {
        if (oldImageUrl.isNotEmpty()) {
            val fileName = oldImageUrl.substringAfterLast("/")
            storageViewModel.deleteFile(fileName, "task_images")
        }
    }

    override fun onResume() {
        super.onResume()

        if (taskId == null && taskDraftId == -1) {
            finish()
        } else if (taskId != null) {
            taskViewModel.getTaskById(taskId!!)
        } else {
            taskDraftViewModel.getTaskById(taskDraftId!!).observe(this) {
                taskTitle = it.taskTitle
                taskBody = it.taskBody
                taskPriority = it.taskPriority
                taskDueDate = it.taskDueDate
                taskDueHours = it.taskDueHours
                taskDueTime = it.taskDueTime
                ImageUrl = it.taskImageUrl

                binding.toolbar.title = it.taskTitle
                binding.tvDescription.text = it.taskBody
                binding.tvDeadline.text = it.taskDueTime
                binding.tvPriority.text = when (it.taskPriority) {
                    1 -> TaskPriority.LOW.name
                    2 -> TaskPriority.MID.name
                    3 -> TaskPriority.HIGH.name
                    else -> ""
                }
            }
        }
    }
}