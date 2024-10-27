package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.FragmentTaskDoneBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.adapter.TaskAdapter
import com.project.focuslist.ui.viewmodel.TaskViewModel

class TaskDoneFragment : Fragment(), TaskAdapter.OnItemClickListener {

    private lateinit var binding: FragmentTaskDoneBinding
    private val viewModel by viewModels<TaskViewModel>()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTaskDoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeCompletedTasks()
    }

    private fun initViews() {
        with (binding) {
            taskAdapter = TaskAdapter(mutableListOf()).apply {
                onItemClickListener = this@TaskDoneFragment
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
        val intent = Intent(activity, DetailTaskActivity::class.java)
        intent.putExtra(DetailTaskActivity.INTENT_KEY_TASK_ID, task.taskId)
        intent.putExtra(DetailTaskActivity.INTENT_KEY, DetailTaskActivity.EDIT_KEY)
        startActivity(intent)
    }

    private fun updateTaskListVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.ivTaskDoneList.visibility = View.VISIBLE
            binding.rvTaskDone.visibility = View.GONE
        } else {
            binding.ivTaskDoneList.visibility = View.GONE
            binding.rvTaskDone.visibility = View.VISIBLE
        }
    }
}