package com.project.focuslist.ui.activity

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.focuslist.data.adapter.VerticalTaskAdapter
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.databinding.ActivityCalenderBinding
import com.project.focuslist.ui.tasks.DetailTaskActivity
import java.util.Locale

class CalenderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalenderBinding

    private val taskViewModel by viewModels<TaskViewModel>()
    private lateinit var verticalTaskAdapter: VerticalTaskAdapter

    private var calendar: Calendar = Calendar.getInstance()
    private var dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var formattedDate: String = dateFormat.format(calendar.time)

    companion object {
        private const val TAG = "CalenderActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalenderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with (binding) {
            toolbar.setNavigationOnClickListener { finish() }

            calenderView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                calendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                formattedDate = dateFormat.format(calendar.time)
                Log.d(TAG, "Formatted Date: $formattedDate")

                taskViewModel.getUserTaskByDate(date = formattedDate, resetPaging = true)
            }

            verticalTaskAdapter = VerticalTaskAdapter(
                onItemClickListener = { task -> readTask(task) },
                onCheckBoxClickListener = { task, isChecked ->
                    taskViewModel.updateCompletionStatus(
                        taskId = task.taskId,
                        isCompleted = isChecked
                    )

                    taskViewModel.getUserTaskByDate(date = formattedDate, resetPaging = true)
                }
            )

            rvTask.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = verticalTaskAdapter
            }

            rvTask.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    // Cek apakah masih ada data sebelum request lagi
                    if (lastVisibleItem >= totalItemCount - 3 && taskViewModel.hasMoreData()) {
                        taskViewModel.getUserTaskByDate(date = formattedDate)
                    }
                }
            })
        }
    }

    private fun observeViewModels() {
        taskViewModel.taskByDate.observe(this@CalenderActivity) { tasks ->
            Log.d(TAG, "Fetched Tasks: $tasks")
            verticalTaskAdapter.submitList(tasks)
            verticalTaskAdapter.notifyDataSetChanged()

            if (tasks.isNullOrEmpty()) {
                updateTaskListVisibility(true)
            } else {
                updateTaskListVisibility(false)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@CalenderActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun readTask(task: Task) {
        Intent(this@CalenderActivity, DetailTaskActivity::class.java).apply {
            putExtra(DetailTaskActivity.TASK_ID, task.taskId)
            startActivity(this)
        }
    }

    private fun updateTaskListVisibility(isEmpty: Boolean) {
        binding.ivTaskList.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTask.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        taskViewModel.getUserTaskByDate(date = formattedDate, resetPaging = true)
    }
}