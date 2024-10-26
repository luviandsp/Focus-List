package com.project.focuslist.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.project.focuslist.R
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.ActivityDetailTaskBinding
import com.project.focuslist.ui.viewmodel.UserTaskViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaskBinding
    private val viewModel by viewModels<UserTaskViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        val isEdit = if (intent.getStringExtra(INTENT_KEY) == EDIT_KEY) true else false
        with (binding) {
            var taskData: Task? = null
            val taskId = intent.getIntExtra(INTENT_KEY_NOTE_ID, -1)
            if (isEdit) {
                viewModel.getTaskById(taskId)
                lifecycleScope.launch {
                    viewModel.stateGetTasks.collectLatest {
                        taskData = it
                        tietTitle.setText(taskData?.title)
                        tietBody.setText(taskData?.body)
                    }
                }
            }

            btnSave.setOnClickListener {
                val title = if (tietTitle.text.isNullOrEmpty()) "Tanpa Judul" else tietTitle.text.toString()
                val body = if (tietBody.text.isNullOrEmpty()) "Tanpa Isi" else tietBody.text.toString()
                viewModel.createTask(
                    Task(
                        taskData?.taskId ?: 0,
                        title,
                        body
                    )
                )
                finish()
            }

            ivDelete.setOnClickListener {
                taskData.apply {
                    viewModel.deleteTask(this!!)
                }
                finish()
            }
        }
    }


    companion object {
        const val EDIT_KEY = "EDIT"
        const val CREATE_KEY = "CREATE"
        const val INTENT_KEY = "EDIT_OR_CREATE"
        const val INTENT_KEY_NOTE_ID = "TASK_ID"
    }
}