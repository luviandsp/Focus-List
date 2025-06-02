package com.project.focuslist.ui.others

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.project.focuslist.R
import com.project.focuslist.data.adapter.HorizontalTaskAdapter
import com.project.focuslist.data.adapter.VerticalTaskAdapter
import com.project.focuslist.data.enumData.TaskCategory
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.model.TaskWithUser
import com.project.focuslist.data.utils.UserViewModelFactory
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.ActivityMainBinding
import com.project.focuslist.ui.profile.ProfileActivity
import com.project.focuslist.ui.tasks.CreateTaskActivity
import com.project.focuslist.ui.tasks.DetailTaskActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val taskViewModel by viewModels<TaskViewModel>()
    private val userViewModel by viewModels<UserViewModel>(
        factoryProducer = { UserViewModelFactory(applicationContext) }
    )

    private lateinit var verticalTaskAdapter: VerticalTaskAdapter
    private lateinit var horizontalTaskAdapter: HorizontalTaskAdapter

    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd")
    private val today = LocalDate.now()

    private var currentReload: (() -> Unit)? = null
    private var currentLoadMore: (() -> Unit)? = null

    private var buttonType = TaskCategory.ALL_TASK.name

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showToast("Notification permission granted")
        } else {
            showToast("Notification permission denied")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        horizontalTaskAdapter = HorizontalTaskAdapter(
            onItemClickListener = { task -> readTask(task) }
        )

        checkAndRequestNotificationPermission()

        observeUserViewModel()
        setupTaskList()
        initViews()
        observeTaskViewModels()
    }

    private fun initViews() {
        with(binding) {
            val date = today.format(dateFormatter)
            val dayName = today.dayOfWeek.name

            tvDays.text = dayName
            tvDates.text = date

            llProfile.setOnClickListener {
                Intent(this@MainActivity, ProfileActivity::class.java).also {
                    startActivity(it)
                }
            }

            ivAddTask.setOnClickListener {
                Intent(this@MainActivity, CreateTaskActivity::class.java).also {
                    startActivity(it)
                }
            }

            cvCalendar.setOnClickListener {
                Intent(this@MainActivity, CalenderActivity::class.java).also {
                    startActivity(it)
                }
            }

            rvTodayTask.apply {
                layoutManager =
                    LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
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

            val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

            fun setActive(btn: MaterialButton) {
                btn.apply {
                    if (isDarkMode) {
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.blue))
                        setBackgroundColor(
                            ContextCompat.getColor(this@MainActivity, R.color.dark_grey)
                        )
                        setStrokeColorResource(R.color.blue)
                    } else {
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                        setBackgroundColor(
                            ContextCompat.getColor(this@MainActivity, R.color.blue)
                        )
                        setStrokeColorResource(R.color.blue)
                    }
                }
            }

            fun setInactive(btn: MaterialButton) {
                btn.apply {
                    if (isDarkMode) {
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                        setBackgroundColor(
                            ContextCompat.getColor(this@MainActivity, R.color.dark_grey)
                        )
                        setStrokeColorResource(R.color.white)
                    } else {
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black))
                        setBackgroundColor(
                            ContextCompat.getColor(this@MainActivity, R.color.white)
                        )
                        setStrokeColorResource(R.color.black)
                    }
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

        currentReload = { taskViewModel.getUserTask(resetPaging = true) }
        currentLoadMore = { taskViewModel.getUserTask() }
    }

    private fun showInProgressTask() {
        taskViewModel.getUserInProgressTask(resetPaging = true)

        currentReload = { taskViewModel.getUserInProgressTask(resetPaging = true) }
        currentLoadMore = { taskViewModel.getUserInProgressTask() }
    }

    private fun showCompletedTask() {
        taskViewModel.getUserCompletedTask(resetPaging = true)

        currentReload = { taskViewModel.getUserCompletedTask(resetPaging = true) }
        currentLoadMore = { taskViewModel.getUserCompletedTask() }
    }

    private fun setupTaskList() {
        with(binding) {
            verticalTaskAdapter = VerticalTaskAdapter(
                onItemClickListener = { task -> readTask(task) },
                onCheckBoxClickListener = { task, isChecked ->
                    taskViewModel.updateCompletionStatus(
                        taskId = task.taskId,
                        isCompleted = isChecked
                    )
                    currentReload?.invoke()
                }
            )

            binding.rvVerticalTask.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = verticalTaskAdapter
            }

            binding.rvVerticalTask.clearOnScrollListeners()
            binding.rvVerticalTask.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItem >= totalItemCount - 3 && taskViewModel.hasMoreData()) {
                        currentLoadMore?.invoke()
                    }
                }
            })
        }
    }

    private fun updateRecyclerView(
        tasks: List<TaskWithUser>,
        placeholderView: View,
        recyclerView: RecyclerView,
        adapter: ListAdapter<TaskWithUser, *>
    ) {
        if (tasks.isEmpty()) {
            placeholderView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholderView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.submitList(tasks)
            adapter.notifyDataSetChanged()
        }
    }

    private fun observeUserViewModel() {
        userViewModel.apply {
            userId.observe(this@MainActivity) { userId ->
                if (!userId.isNullOrEmpty()) {
                    Log.d(TAG, "User ID: $userId")
                }
            }

            userName.observe(this@MainActivity) { username ->
                if (!username.isNullOrEmpty()) {
                    binding.tvProfileName.text = username
                    Log.d(TAG, "User Name: $username")
                }
            }

            userImageUrl.observe(this@MainActivity) { imageUrl ->
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this@MainActivity)
                        .load(imageUrl)
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .circleCrop()
                        .into(binding.ivProfilePicture)
                    Log.d(TAG, "User Image: $imageUrl")
                }
            }

            authStatus.observe(this@MainActivity) { result ->
                if (result.first) {
                    Log.d(TAG, "Authentication status: Success")
                    // Lakukan tindakan setelah otentikasi berhasil jika perlu
                } else {
                    Log.d(TAG, "Authentication status: Failed - ${result.second}")
                    // Handle error otentikasi jika perlu
                }
            }
        }
    }

    private fun observeTaskViewModels() {
        taskViewModel.apply {
            todayTask.observe(this@MainActivity) { tasks ->
                Log.d(TAG, "Fetched Today Tasks: $tasks")
                updateRecyclerView(
                    tasks,
                    binding.tvPlaceholderToday,
                    binding.rvTodayTask,
                    horizontalTaskAdapter
                )
            }

            val commonObserver: (List<TaskWithUser>) -> Unit = { tasks ->
                Log.d(TAG, "Fetched Tasks: $tasks")
                updateRecyclerView(
                    tasks,
                    binding.ivTaskPlaceholder,
                    binding.rvVerticalTask,
                    verticalTaskAdapter
                )
            }

            allTask.observe(this@MainActivity, commonObserver)
            taskInProgress.observe(this@MainActivity, commonObserver)
            taskCompleted.observe(this@MainActivity, commonObserver)
        }
    }

    private fun readTask(task: Task) {
        Intent(this@MainActivity, DetailTaskActivity::class.java).apply {
            putExtra(DetailTaskActivity.Companion.TASK_ID, task.taskId)
            startActivity(this)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            showToast("Notification permission not needed for this device")
        }
    }

    override fun onResume() {
        super.onResume()

        userViewModel.getUser()

        taskViewModel.getTodayTask(resetPaging = true)
        Log.d(TAG, "Get Today Task")

        when (buttonType) {
            TaskCategory.ALL_TASK.name -> {
                changeButtonActive(TaskCategory.ALL_TASK.name)
                showAllTask()
                Log.d(TAG, "Get All Task")
            }
            TaskCategory.IN_PROGRESS.name -> {
                changeButtonActive(TaskCategory.IN_PROGRESS.name)
                showInProgressTask()
                Log.d(TAG, "Get In Progress Task")
            }
            TaskCategory.COMPLETED.name -> {
                changeButtonActive(TaskCategory.COMPLETED.name)
                showCompletedTask()
                Log.d(TAG, "Get Completed Task")
            }
        }
    }
}
