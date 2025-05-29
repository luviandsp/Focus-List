package com.project.focuslist.ui.fragment

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.focuslist.data.adapter.VerticalTaskAdapter
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.databinding.FragmentCalenderBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.activity.ReadTaskActivity
import java.util.Locale


class CalendarFragment : Fragment() {

    private var _binding: FragmentCalenderBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel by viewModels<TaskViewModel>()
    private lateinit var verticalTaskAdapter: VerticalTaskAdapter

    private var calendar: Calendar = Calendar.getInstance()
    private var dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var formattedDate: String = dateFormat.format(calendar.time)

    companion object {
        private const val TAG = "CalendarFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with (binding) {

            binding.calenderView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                calendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                formattedDate = dateFormat.format(calendar.time)
                Log.d(TAG, "Formatted Date: $formattedDate")

                taskViewModel.getUserTaskByDate(date = formattedDate, resetPaging = true)
            }

            verticalTaskAdapter = VerticalTaskAdapter(
                onItemClickListener = { task -> readTask(task) },
                onLongClickListener = { task -> detailTask(task); true },
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
        taskViewModel.taskByDate.observe(viewLifecycleOwner) { tasks ->
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun readTask(task: Task) {
        Intent(requireContext(), ReadTaskActivity::class.java).apply {
            putExtra(ReadTaskActivity.TASK_ID, task.taskId)
            startActivity(this)
        }
    }

    private fun detailTask(task: Task) {
        Intent(requireContext(), DetailTaskActivity::class.java).apply {
            putExtra(DetailTaskActivity.TASK_ID, task.taskId)
            putExtra(DetailTaskActivity.INTENT_KEY, DetailTaskActivity.EDIT_KEY)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}