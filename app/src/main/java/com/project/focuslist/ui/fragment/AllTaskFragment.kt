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
import com.project.focuslist.ui.viewmodel.UserTaskViewModel
import kotlinx.coroutines.launch

class AllTaskFragment : Fragment(), TaskAdapter.OnItemClickListener {

    private lateinit var binding: FragmentAllTaskBinding
    private val viewModel by viewModels<UserTaskViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        with (binding) {
            lifecycleScope.launch {
                viewModel.getTaskList().collect { taskList ->
                    val adapter = TaskAdapter(taskList)
                    rvAllTask.setHasFixedSize(true)
                    rvAllTask.layoutManager = LinearLayoutManager(this@AllTaskFragment.context)
                    adapter.onItemClickListener = this@AllTaskFragment
                    rvAllTask.adapter = adapter

                    if (taskList.isEmpty()) {
                        ivAllTaskList.visibility = View.VISIBLE
                        rvAllTask.visibility = View.GONE
                    } else {
                        ivAllTaskList.visibility = View.GONE
                        rvAllTask.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onItemClick(task: Task) {
        val intent = Intent(activity, DetailTaskActivity::class.java)
        intent.putExtra(DetailTaskActivity.INTENT_KEY_NOTE_ID, task.taskId)
        intent.putExtra(DetailTaskActivity.INTENT_KEY, DetailTaskActivity.EDIT_KEY)
        startActivity(intent)
    }
}