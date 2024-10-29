package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.FragmentAllTaskBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.adapter.TaskAdapter
import com.project.focuslist.ui.viewmodel.AuthViewModel
import com.project.focuslist.ui.viewmodel.LoginViewModel
import com.project.focuslist.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

class AllTaskFragment : Fragment(), TaskAdapter.OnItemClickListener {

    private lateinit var binding: FragmentAllTaskBinding
    private val viewModel by viewModels<TaskViewModel>()
    private val userViewModel by viewModels<AuthViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAllTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeTaskList()
    }

    private fun initViews() {
        with (binding) {
            taskAdapter = TaskAdapter(mutableListOf()).apply {
                onItemClickListener = this@AllTaskFragment
                onCheckBoxClickListener = { task, isChecked ->
                    viewModel.toggleTaskCompletion(task, isChecked)
                }
            }

            rvAllTask.apply {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                adapter = taskAdapter
            }
        }
    }

    private fun observeTaskList() {
        lifecycleScope.launch {
            viewModel.getTaskList().observe(viewLifecycleOwner) { taskList ->
                taskAdapter.setTasks(taskList)
                updateTaskListVisibility(taskList.isEmpty())
            }
        }
    }

    override fun onItemClick(task: Task) {
        val intent = Intent(activity, DetailTaskActivity::class.java)
        intent.putExtra(DetailTaskActivity.INTENT_KEY_TASK_ID, task.taskId)
        intent.putExtra(DetailTaskActivity.INTENT_KEY, DetailTaskActivity.EDIT_KEY)
        startActivity(intent)
    }

    private fun updateTaskListVisibility(isEmpty: Boolean) {
        binding.ivAllTaskList.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvAllTask.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
