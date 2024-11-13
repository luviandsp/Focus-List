package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.FragmentTaskDoneBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.activity.ReadTaskActivity
import com.project.focuslist.ui.adapter.TaskAdapter
import com.project.focuslist.ui.viewmodel.AuthViewModel
import com.project.focuslist.ui.viewmodel.LoginViewModel
import com.project.focuslist.ui.viewmodel.TaskViewModel

class TaskDoneFragment : Fragment(), TaskAdapter.OnItemClickListener,
    TaskAdapter.OnItemLongClickListener {

    private lateinit var binding: FragmentTaskDoneBinding
    private val viewModel by viewModels<TaskViewModel>()
    private val userViewModel by viewModels<AuthViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTaskDoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initViews()
        observeCompletedTasks()
    }

    private fun initViews() {
        with (binding) {
            taskAdapter = TaskAdapter(mutableListOf()).apply {
                onItemClickListener = this@TaskDoneFragment
                onLongClickListener = this@TaskDoneFragment
                onCheckBoxClickListener = { task, isChecked ->
                    viewModel.toggleTaskCompletion(task, isChecked)
                }
            }

            rvTaskDone.apply {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                adapter = taskAdapter
            }
        }
    }

    private fun observeCompletedTasks() {
        viewModel.getCompletedTasks().observe(viewLifecycleOwner) { taskList ->
            taskAdapter.setTasks(taskList)
            updateTaskListVisibility(taskList.isEmpty())
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
        binding.ivTaskDoneList.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTaskDone.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
