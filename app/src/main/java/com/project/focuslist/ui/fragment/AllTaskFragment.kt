package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.FragmentAllTaskBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.activity.ReadTaskActivity
import com.project.focuslist.ui.adapter.LoadingStateAdapter
import com.project.focuslist.ui.adapter.TaskAdapter
import com.project.focuslist.ui.adapter.TaskPdAdapter
import com.project.focuslist.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AllTaskFragment : Fragment(), TaskAdapter.OnItemClickListener, TaskAdapter.OnItemLongClickListener {

    private lateinit var binding: FragmentAllTaskBinding
    private val viewModel by viewModels<TaskViewModel>()
    private lateinit var taskAdapter: TaskPdAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observePagingData()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskPdAdapter().apply {
            onItemClickListener = { task ->
                val intent = Intent(requireContext(), ReadTaskActivity::class.java)
                intent.putExtra(ReadTaskActivity.INTENT_KEY_TASK_ID, task.taskId)
                startActivity(intent)
            }

            onLongClickListener = { task ->
                val intent = Intent(requireContext(), DetailTaskActivity::class.java)
                intent.putExtra(DetailTaskActivity.INTENT_KEY_TASK_ID, task.taskId)
                intent.putExtra(DetailTaskActivity.INTENT_KEY, DetailTaskActivity.EDIT_KEY)

                startActivity(intent)
                true
            }

            onCheckBoxClickListener = { task, isChecked ->
                viewModel.toggleTaskCompletion(task, isChecked)
            }
        }

        binding.rvAllTask.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { taskAdapter.retry() }
            )
        }
    }

    private fun observePagingData() {
        lifecycleScope.launch {
            viewModel.pagedTasksList.collectLatest { pagingData ->
                taskAdapter.submitData(lifecycle, pagingData)
                observeLoadState()
            }
        }
    }

    private fun observeLoadState() {
        taskAdapter.addLoadStateListener { loadState ->
            val isEmptyList = loadState.refresh is LoadState.NotLoading && taskAdapter.itemCount == 0

            updateTaskListVisibility(isEmptyList)

            if (loadState.refresh is LoadState.Error) {
                val errorState = loadState.refresh as LoadState.Error
                showError(errorState.error.localizedMessage ?: "Error occurred")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


//    override fun onStart() {
//        super.onStart()
//
//        initViews()
//        observeTaskList()
//    }
//
//    private fun initViews() {
//        with (binding) {
//            taskAdapter = TaskAdapter(mutableListOf()).apply {
//                onItemClickListener = this@AllTaskFragment
//                onLongClickListener = this@AllTaskFragment
//                onCheckBoxClickListener = { task, isChecked ->
//                    viewModel.toggleTaskCompletion(task, isChecked)
//                }
//            }
//
//            rvAllTask.apply {
//                layoutManager = LinearLayoutManager(context)
//                setHasFixedSize(true)
//                adapter = taskAdapter
//            }
//        }
//    }
//
//    private fun observeTaskList() {
//        viewModel.getTaskList().observe(viewLifecycleOwner) { taskList ->
//            taskAdapter.setTasks(taskList)
//            updateTaskListVisibility(taskList.isEmpty())
//        }
//    }

    override fun onItemClick(task: Task) {
        val intent = Intent(activity, ReadTaskActivity::class.java)
        intent.putExtra(ReadTaskActivity.INTENT_KEY_TASK_ID, task.taskId)
        startActivity(intent)
    }

    override fun onItemLongClick(task: Task): Boolean {
        val intent = Intent(activity, DetailTaskActivity::class.java)
        intent.putExtra(DetailTaskActivity.INTENT_KEY_TASK_ID, task.taskId)
        intent.putExtra(DetailTaskActivity.INTENT_KEY, DetailTaskActivity.EDIT_KEY)
        startActivity(intent)
        return true
    }

    private fun updateTaskListVisibility(isEmpty: Boolean) {
        binding.ivAllTaskList.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvAllTask.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
