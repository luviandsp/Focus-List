package com.project.focuslist.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.focuslist.data.adapter.LoadingStateAdapter
import com.project.focuslist.data.adapter.TaskPdAdapter
import com.project.focuslist.data.model.TaskDraft
import com.project.focuslist.data.viewmodel.TaskDraftViewModel
import com.project.focuslist.databinding.ActivityDraftTaskBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DraftTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDraftTaskBinding

    private val taskDraftViewModel by viewModels<TaskDraftViewModel>()
    private lateinit var taskDraftAdapter: TaskPdAdapter

    companion object {
        private const val TAG = "DraftTaskActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDraftTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }

            taskDraftAdapter = TaskPdAdapter(
                onItemClickListener = { task -> readTask(task) }
            )

            taskDraftAdapter.addLoadStateListener { loadStates ->
                val isListEmpty = loadStates.refresh is LoadState.NotLoading && taskDraftAdapter.itemCount == 0

                Log.d(TAG, "LoadState: $loadStates")
                Log.d(TAG, "ItemCount: ${taskDraftAdapter.itemCount}")

                updateTaskListVisibility(isListEmpty)
            }

            rvDraftTask.apply {
                adapter = taskDraftAdapter.withLoadStateHeaderAndFooter(
                    header = LoadingStateAdapter { taskDraftAdapter.retry() },
                    footer = LoadingStateAdapter { taskDraftAdapter.retry() }
                )
                layoutManager = LinearLayoutManager(this@DraftTaskActivity)
            }
        }
    }

    private fun loadDraftTasks() {
        lifecycleScope.launch {
            taskDraftViewModel.pagedTasksList.collectLatest { tasks ->
                taskDraftAdapter.submitData(tasks)
                Log.d(TAG, "Fetched Tasks: $tasks")
                taskDraftAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun readTask(task: TaskDraft) {
        Intent(this, DetailTaskActivity::class.java).apply {
            putExtra(DetailTaskActivity.TASK_DRAFT_ID, task.taskId)
            startActivity(this)
        }
    }

    private fun updateTaskListVisibility(isEmpty: Boolean) {
        binding.ivPlaceholderTask.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvDraftTask.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        loadDraftTasks()
    }
}