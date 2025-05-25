package com.project.focuslist.ui.fragment

import android.content.Context
import android.content.Intent
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
import com.project.focuslist.data.adapter.TaskAdapter
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.databinding.FragmentTaskDoneBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.activity.ReadTaskActivity

class TaskCompletedFragment : Fragment() {

    private var _binding: FragmentTaskDoneBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel by viewModels<TaskViewModel>()
    private lateinit var taskAdapter: TaskAdapter

    companion object {
        private const val TAG = "TaskCompletedFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModels()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        with(binding) {
            taskAdapter = TaskAdapter(
                onItemClickListener = { task -> readTask(task) },
                onLongClickListener = { task -> detailTask(task); true },
                onCheckBoxClickListener = { task, isChecked ->
                    taskViewModel.updateCompletionStatus(
                        taskId = task.taskId,
                        isCompleted = isChecked
                    )

                    taskViewModel.getUserCompletedTask(resetPaging = true)
                }
            )

            rvTaskDone.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = taskAdapter
            }

            rvTaskDone.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    // Cek apakah masih ada data sebelum request lagi
                    if (lastVisibleItem >= totalItemCount - 3 && taskViewModel.hasMoreData()) {
                        taskViewModel.getUserCompletedTask()
                    }
                }
            })
        }
    }

    private fun observeViewModels() {
        taskViewModel.taskCompleted.observe(viewLifecycleOwner) { tasks ->
            Log.d(TAG, "Fetched Tasks: $tasks")
            taskAdapter.submitList(tasks)
            taskAdapter.notifyDataSetChanged()

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
        binding.ivTaskDoneList.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTaskDone.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: Fragment attached")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment created")
    }

    override fun onResume() {
        super.onResume()
        taskViewModel.getUserCompletedTask(resetPaging = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
