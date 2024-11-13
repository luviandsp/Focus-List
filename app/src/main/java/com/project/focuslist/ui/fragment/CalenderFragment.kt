package com.project.focuslist.ui.fragment

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.FragmentCalenderBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.activity.ReadTaskActivity
import com.project.focuslist.ui.adapter.TaskAdapter
import com.project.focuslist.ui.viewmodel.TaskViewModel
import java.util.Locale


class CalenderFragment : Fragment(), TaskAdapter.OnItemClickListener,
    TaskAdapter.OnItemLongClickListener {

    private lateinit var binding: FragmentCalenderBinding
    private val viewModel by viewModels<TaskViewModel>()
    private lateinit var taskAdapter: TaskAdapter
    private var calendar: Calendar = Calendar.getInstance()
    private var dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var formattedDate: String = dateFormat.format(calendar.time)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initViews()
        observeTaskList()
    }

    private fun initViews() {
        with (binding) {

            taskAdapter = TaskAdapter(mutableListOf()).apply {
                onItemClickListener = this@CalenderFragment
                onLongClickListener = this@CalenderFragment
                onCheckBoxClickListener = { task, isChecked ->
                    viewModel.toggleTaskCompletion(task, isChecked)
                }
            }

            rvTask.apply {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                adapter = taskAdapter
            }

            viewModel.getTaskListByDate(formattedDate).observe(viewLifecycleOwner) { taskList ->
                taskAdapter.setTasks(taskList)
                updateTaskListVisibility(taskList.isEmpty())
            }
        }
    }

    private fun observeTaskList() {
        binding.calenderView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formattedDate = dateFormat.format(calendar.time)

            viewModel.getTaskListByDate(formattedDate).observe(viewLifecycleOwner) { taskList ->
                taskAdapter.setTasks(taskList)
                updateTaskListVisibility(taskList.isEmpty())
            }
        }
    }

    override fun onItemClick(task: Task) {
        val intent = Intent(activity, ReadTaskActivity::class.java)
        intent.putExtra(ReadTaskActivity.INTENT_KEY_TASK_ID, task.taskId)
        startActivity(intent)
    }

    // Mengimplementasikan method dari OnItemLongClickListener
    override fun onItemLongClick(task: Task): Boolean {
        val intent = Intent(activity, DetailTaskActivity::class.java)
        intent.putExtra(DetailTaskActivity.INTENT_KEY_TASK_ID, task.taskId)
        intent.putExtra(DetailTaskActivity.INTENT_KEY, DetailTaskActivity.EDIT_KEY)
        startActivity(intent)
        return true
    }

    private fun updateTaskListVisibility(isEmpty: Boolean) {
        binding.ivTaskList.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTask.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}