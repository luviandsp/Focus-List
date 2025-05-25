package com.project.focuslist.data.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.project.focuslist.data.model.TaskWithUser
import com.project.focuslist.data.notification.NotificationWorker
import com.project.focuslist.data.repository.TaskRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskViewModel: ViewModel() {

    private val taskRepository = TaskRepository()

    companion object {
        private const val TAG = "TaskViewModel"
    }

    private val _allTask = MutableLiveData<List<TaskWithUser>>()
    val allTask: LiveData<List<TaskWithUser>> = _allTask

    private val _taskInProgress = MutableLiveData<List<TaskWithUser>>()
    val taskInProgress: LiveData<List<TaskWithUser>> = _taskInProgress

    private val _taskCompleted = MutableLiveData<List<TaskWithUser>>()
    val taskCompleted: LiveData<List<TaskWithUser>> = _taskCompleted

    private val _taskByDate = MutableLiveData<List<TaskWithUser>>()
    val taskByDate: LiveData<List<TaskWithUser>> = _taskByDate

    private val _taskTitle = MutableLiveData<String>()
    val taskTitle: LiveData<String> = _taskTitle

    private val _taskBody = MutableLiveData<String>()
    val taskBody: LiveData<String> = _taskBody

    private val _taskPriority = MutableLiveData<Int>()
    val taskPriority: LiveData<Int> = _taskPriority

    private val _taskDueDate = MutableLiveData<String?>()
    val taskDueDate: LiveData<String?> = _taskDueDate

    private val _taskDueHours = MutableLiveData<String?>()
    val taskDueHours: LiveData<String?> = _taskDueHours

    private val _taskDueTime = MutableLiveData<String?>()
    val taskDueTime: LiveData<String?> = _taskDueTime

    private val _taskImageUrl = MutableLiveData<String?>()
    val taskImageUrl: LiveData<String?> = _taskImageUrl

    private val _isCompleted = MutableLiveData<Boolean>()
    val isCompleted: LiveData<Boolean> = _isCompleted

    private val _operationResult = MutableLiveData<Pair<Boolean, String?>>()
    val operationResult: LiveData<Pair<Boolean, String?>> get() = _operationResult

    private var currentList = mutableListOf<TaskWithUser>()

    fun hasMoreData(): Boolean {
        return !taskRepository.isLastPage()
    }

    fun createTask(
        context: Context,
        taskTitle: String,
        taskBody: String,
        taskPriority: Int,
        taskDueDate: String?,
        taskDueHours: String?,
        taskDueTime: String?,
        taskImageUrl: String?,
        reminderOffsetMillis: Long?
        ) {
        viewModelScope.launch {
            val result = taskRepository.createTask(
                taskTitle = taskTitle,
                taskBody = taskBody,
                taskPriority = taskPriority,
                taskDueDate = taskDueDate,
                taskDueHours = taskDueHours,
                taskDueTime = taskDueTime,
                taskImageUrl = taskImageUrl
            )

            _operationResult.postValue(result)

            if (result.first) {
                _taskTitle.postValue(taskTitle)
                _taskBody.postValue(taskBody)
                _taskPriority.postValue(taskPriority)
                _taskDueDate.postValue(taskDueDate)
                _taskDueHours.postValue(taskDueHours)
                _taskDueTime.postValue(taskDueTime)
                _taskImageUrl.postValue(taskImageUrl)
                _isCompleted.postValue(false)

                scheduleNotification(
                    context = context,
                    taskTitle = taskTitle,
                    taskDueDate = taskDueTime,
                    reminderOffsetMillis = reminderOffsetMillis
                )

                Log.d(TAG, "createTask: Task $taskTitle created")
            }
        }
    }

    fun getUserTask(resetPaging: Boolean = false) {
        viewModelScope.launch {
            val tasks = taskRepository.getUserTask(resetPaging)

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(tasks)
            _allTask.postValue(currentList)

            Log.d(TAG, "getUserTask: Current list size: ${currentList.size}")
        }
    }

    fun getUserCompletedTask(resetPaging: Boolean = false) {
        viewModelScope.launch {
            val tasks = taskRepository.getUserCompletedTask(resetPaging)

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(tasks)
            _taskCompleted.postValue(currentList)

            Log.d(TAG, "getUserCompletedTask: Current list size: ${currentList.size}")
        }
    }

    fun getUserInProgressTask(resetPaging: Boolean = false) {
        viewModelScope.launch {
            val tasks = taskRepository.getUserInProgressTask(resetPaging)

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(tasks)
            _taskInProgress.postValue(currentList)

            Log.d(TAG, "getUserInProgressTask: Current list size: ${currentList.size}")
        }
    }

    fun getUserTaskByDate(date: String, resetPaging: Boolean = false) {
        viewModelScope.launch {
            val tasks = taskRepository.getUserTaskByDate(
                date = date,
                resetPaging = resetPaging
            )

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(tasks)
            _taskByDate.postValue(currentList)

            Log.d(TAG, "getUserTaskByDate: Current list size: ${currentList.size}")
        }
    }

    fun getTaskById(taskId: String) {
        viewModelScope.launch {
            val tasks = taskRepository.getTaskById(taskId)

            if (tasks != null) {
                _taskTitle.postValue(tasks.task.taskTitle)
                _taskBody.postValue(tasks.task.taskBody)
                _taskPriority.postValue(tasks.task.taskPriority)
                _taskDueDate.postValue(tasks.task.taskDueDate)
                _taskDueHours.postValue(tasks.task.taskDueHours)
                _taskDueTime.postValue(tasks.task.taskDueTime)
                _taskImageUrl.postValue(tasks.task.taskImageUrl)
            }
        }
    }

    fun updateTask(
        context: Context,
        taskId: String,
        taskTitle: String,
        taskBody: String,
        taskPriority: Int,
        taskDueDate: String?,
        taskDueHours: String?,
        taskDueTime: String?,
        taskImageUrl: String?,
        reminderOffsetMillis: Long?
    ) {
        viewModelScope.launch {
            val result = taskRepository.updateTask(
                taskId = taskId,
                taskTitle = taskTitle,
                taskBody = taskBody,
                taskPriority = taskPriority,
                taskDueDate = taskDueDate,
                taskDueHours = taskDueHours,
                taskDueTime = taskDueTime,
                taskImageUrl = taskImageUrl
            )

            _operationResult.postValue(result)

            if (result.first) {
                _taskTitle.postValue(taskTitle)
                _taskBody.postValue(taskBody)
                _taskPriority.postValue(taskPriority)
                _taskDueDate.postValue(taskDueDate)
                _taskDueHours.postValue(taskDueHours)
                _taskDueTime.postValue(taskDueTime)
                _taskImageUrl.postValue(taskImageUrl)
                _isCompleted.postValue(false)

                scheduleNotification(
                    context = context,
                    taskTitle = taskTitle,
                    taskDueDate = taskDueTime,
                    reminderOffsetMillis = reminderOffsetMillis
                )

                Log.d(TAG, "updateTask: Task $taskTitle updated")
            }
        }
    }

    fun updateCompletionStatus(
        taskId: String,
        isCompleted: Boolean,
    ) {
        viewModelScope.launch {
            val result = taskRepository.updateCompletionStatus(
                taskId = taskId,
                isCompleted = isCompleted
            )

            _operationResult.postValue(result)

            if (result.first) {
                _isCompleted.postValue(isCompleted)
            }
        }
    }

    fun deleteTask(
        taskId: String
    ) {
        viewModelScope.launch {
            val result = taskRepository.deleteTask(
                taskId = taskId
            )

            _operationResult.postValue(result)
        }
    }

    private fun scheduleNotification(context: Context, taskTitle: String, taskDueDate: String?, reminderOffsetMillis: Long?) {
        taskDueDate?.matches(Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"))?.let {
            if (!it) {
                Log.e(TAG, "Invalid taskDueDate format: $taskDueDate")
                return
            }
        }

        if (taskDueDate == null) return

        Log.d(TAG, "Scheduling notification for task: $taskTitle")

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val dueDate = try {
            formatter.parse(taskDueDate)?.time ?: return
        } catch (e: Exception) {
            return
        }

        Log.d(TAG, "Task due date: $dueDate")

        val reminderTime = reminderOffsetMillis ?: 0

        val delay = dueDate - System.currentTimeMillis() - reminderTime
        Log.d(TAG, "Delay: $delay")
        if (delay <= 0) return

        val data = workDataOf(
            "title" to "Reminder",
            "message" to "Task: $taskTitle is due soon!"
        )

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

}
