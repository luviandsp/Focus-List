package com.project.focuslist.ui.fragment

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
import com.project.focuslist.data.adapter.TaskAdapter
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.databinding.FragmentAllTaskBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.activity.ReadTaskActivity

class AllTaskFragment : Fragment() {

    private var _binding: FragmentAllTaskBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel by viewModels<TaskViewModel>()
    private lateinit var taskAdapter: TaskAdapter

    companion object {
        private const val TAG = "AllTaskFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllTaskBinding.inflate(inflater, container, false)
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

                    taskViewModel.getUserTask(resetPaging = true)
                }
            )

            rvAllTask.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = taskAdapter
            }

//            rvAllTask.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    val totalItemCount = layoutManager.itemCount
//                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
//
//                    // Cek apakah masih ada data sebelum request lagi
//                    if (lastVisibleItem >= totalItemCount - 3 && taskViewModel.hasMoreData()) {
//                        taskViewModel.getUserTask()
//                    }
//                }
//            })
        }
    }

    private fun observeViewModels() {
        taskViewModel.allTask.observe(viewLifecycleOwner) { tasks ->
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
            startActivity(this)
        }
    }

    private fun updateTaskListVisibility(isEmpty: Boolean) {
        binding.ivAllTaskList.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvAllTask.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        taskViewModel.getUserTask(resetPaging = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
