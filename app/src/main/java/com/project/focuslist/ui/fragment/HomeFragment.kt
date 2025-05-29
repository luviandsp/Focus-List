package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.project.focuslist.R
import com.project.focuslist.data.adapter.HorizontalTaskAdapter
import com.project.focuslist.data.adapter.VerticalTaskAdapter
import com.project.focuslist.data.enumData.TaskCategory
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.FragmentHomeBinding
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.activity.ReadTaskActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel by viewModels<TaskViewModel>()
    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var verticalTaskAdapter: VerticalTaskAdapter
    private lateinit var horizontalTaskAdapter: HorizontalTaskAdapter

    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd")
    private val today = LocalDate.now()

    private var buttonType = TaskCategory.ALL_TASK.name

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            val date = today.format(dateFormatter)
            val dayName = today.dayOfWeek.name

            tvDays.text = dayName
            tvDates.text = date

            setupTaskList(
                onReloadAfterCheck = { taskViewModel.getUserTask(resetPaging = true) },
                onLoadMore = { taskViewModel.getUserTask() }
            )

            horizontalTaskAdapter = HorizontalTaskAdapter(
                onItemClickListener = { task -> readTask(task) }
            )

            rvTodayTask.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = horizontalTaskAdapter
            }

            btnAllTask.setOnClickListener {
                showAllTask()
                changeButtonActive(TaskCategory.ALL_TASK.name)
            }

            btnInProgressTask.setOnClickListener {
                showInProgressTask()
                changeButtonActive(TaskCategory.IN_PROGRESS.name)
            }

            btnCompletedTask.setOnClickListener {
                showCompletedTask()
                changeButtonActive(TaskCategory.COMPLETED.name)
            }
        }
    }

    private fun changeButtonActive(type: String) {
        with(binding) {
            fun setActive(btn: MaterialButton) {
                btn.apply {
                    setTextColor(getColor(requireContext(), R.color.white))
                    setBackgroundColor(
                        getColor(requireContext(), R.color.black)
                    )
                }
            }

            fun setInactive(btn: MaterialButton) {
                btn.apply {
                    setTextColor(getColor(requireContext(), R.color.black))
                    setBackgroundColor(
                        getColor(requireContext(), R.color.white)
                    )
                }
            }

            when (type) {
                TaskCategory.ALL_TASK.name -> {
                    setActive(btnAllTask)
                    setInactive(btnInProgressTask)
                    setInactive(btnCompletedTask)

                    buttonType = TaskCategory.ALL_TASK.name
                }

                TaskCategory.IN_PROGRESS.name -> {
                    setInactive(btnAllTask)
                    setActive(btnInProgressTask)
                    setInactive(btnCompletedTask)

                    buttonType = TaskCategory.IN_PROGRESS.name
                }

                TaskCategory.COMPLETED.name -> {
                    setInactive(btnAllTask)
                    setInactive(btnInProgressTask)
                    setActive(btnCompletedTask)

                    buttonType = TaskCategory.COMPLETED.name
                }
            }
        }
    }

    private fun showAllTask() {
        taskViewModel.getUserTask(resetPaging = true)

        setupTaskList(
            onReloadAfterCheck = { taskViewModel.getUserTask(resetPaging = true) },
            onLoadMore = { taskViewModel.getUserTask() }
        )
    }

    private fun showInProgressTask() {
        taskViewModel.getUserInProgressTask(resetPaging = true)

        setupTaskList(
            onReloadAfterCheck = { taskViewModel.getUserInProgressTask(resetPaging = true) },
            onLoadMore = { taskViewModel.getUserInProgressTask() }
        )
    }

    private fun showCompletedTask() {
        taskViewModel.getUserCompletedTask(resetPaging = true)

        setupTaskList(
            onReloadAfterCheck = { taskViewModel.getUserCompletedTask(resetPaging = true) },
            onLoadMore = { taskViewModel.getUserCompletedTask() }
        )
    }

    private fun setupTaskList(
        onReloadAfterCheck: () -> Unit,
        onLoadMore: () -> Unit
    ) {
        with(binding) {
            verticalTaskAdapter = VerticalTaskAdapter(
                onItemClickListener = { task -> readTask(task) },
                onLongClickListener = { task -> detailTask(task); true },
                onCheckBoxClickListener = { task, isChecked ->
                    taskViewModel.updateCompletionStatus(
                        taskId = task.taskId,
                        isCompleted = isChecked
                    )
                    onReloadAfterCheck()
                }
            )

            rvVerticalTask.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = verticalTaskAdapter
            }

            rvVerticalTask.clearOnScrollListeners()
            rvVerticalTask.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItem >= totalItemCount - 3 && taskViewModel.hasMoreData()) {
                        onLoadMore()
                    }
                }
            })
        }
    }

    private fun observeViewModels() {
        userViewModel.apply {
            userName.observe(viewLifecycleOwner) { username ->
                binding.tvProfileName.text = getString(R.string.greeting_message, username)
            }

            userImageUrl.observe(viewLifecycleOwner) { imageUrl ->
                Glide.with(this@HomeFragment)
                    .load(imageUrl)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(binding.ivProfilePicture)
            }
        }

        taskViewModel.apply {
            todayTask.observe(viewLifecycleOwner) { tasks ->
                Log.d(TAG, "Fetched Today Tasks: $tasks")
                horizontalTaskAdapter.submitList(tasks)
                horizontalTaskAdapter.notifyDataSetChanged()
            }

            allTask.observe(viewLifecycleOwner) { tasks ->
                Log.d(TAG, "Fetched All Tasks: $tasks")
                verticalTaskAdapter.submitList(tasks)
                verticalTaskAdapter.notifyDataSetChanged()
            }

            taskInProgress.observe(viewLifecycleOwner) { tasks ->
                Log.d(TAG, "Fetched InProgress Tasks: $tasks")
                verticalTaskAdapter.submitList(tasks)
                verticalTaskAdapter.notifyDataSetChanged()
            }

            taskCompleted.observe(viewLifecycleOwner) { tasks ->
                Log.d(TAG, "Fetched Completed Tasks: $tasks")
                verticalTaskAdapter.submitList(tasks)
                verticalTaskAdapter.notifyDataSetChanged()
            }
        }
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

    override fun onResume() {
        super.onResume()

        taskViewModel.getTodayTask(resetPaging = true)

        when (buttonType) {
            TaskCategory.ALL_TASK.name -> {
                changeButtonActive(TaskCategory.ALL_TASK.name)
                showAllTask()
            }
            TaskCategory.IN_PROGRESS.name -> {
                changeButtonActive(TaskCategory.IN_PROGRESS.name)
                showInProgressTask()
            }
            TaskCategory.COMPLETED.name -> {
                changeButtonActive(TaskCategory.COMPLETED.name)
                showCompletedTask()
            }
        }

        Log.d(TAG, "onResume called")
        Log.d(TAG, "buttonType: $buttonType")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}