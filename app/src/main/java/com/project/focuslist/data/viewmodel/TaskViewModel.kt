package com.project.focuslist.data.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
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

    private val _todayTask = MutableLiveData<List<TaskWithUser>>()
    val todayTask: LiveData<List<TaskWithUser>> = _todayTask

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

    private val _taskReminderTime = MutableLiveData<String?>()
    val taskReminderTime: LiveData<String?> = _taskReminderTime

    private val _taskImageUrl = MutableLiveData<String?>()
    val taskImageUrl: LiveData<String?> = _taskImageUrl

    private val _isCompleted = MutableLiveData<Boolean>()
    val isCompleted: LiveData<Boolean> = _isCompleted

    private val _operationResult = MutableLiveData<Pair<Boolean, String?>>()
    val operationResult: LiveData<Pair<Boolean, String?>> get() = _operationResult

    private val _operationDeleteResult = MutableLiveData<Pair<Boolean, String?>>()
    val operationDeleteResult: LiveData<Pair<Boolean, String?>> get() = _operationDeleteResult

    private var currentVerticalList = mutableListOf<TaskWithUser>()
    private var currentHorizontalList = mutableListOf<TaskWithUser>()

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
        taskReminderTime: String?,
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
                taskReminderTime = taskReminderTime,
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
                _taskReminderTime.postValue(taskReminderTime)
                _taskImageUrl.postValue(taskImageUrl)
                _isCompleted.postValue(false)

                scheduleNotification(
                    context = context,
                    taskId = result.second!!,
                    taskTitle = taskTitle,
                    taskDueTime = taskDueTime,
                    reminderOffsetMillis = reminderOffsetMillis
                )

                Log.d(TAG, "createTask: Task $taskTitle created")
            }
        }
    }

    fun getTodayTask(resetPaging: Boolean = false) {
        viewModelScope.launch {
            val tasks = taskRepository.getUserTodayTask(resetPaging)

            if (resetPaging) {
                currentHorizontalList.clear()
            }

            currentHorizontalList.addAll(tasks)
            _todayTask.postValue(currentHorizontalList)

            Log.d(TAG, "getTodayTask: Current list size: ${currentHorizontalList.size}")
        }
    }

    fun getUserTask(resetPaging: Boolean = false) {
        viewModelScope.launch {
            val tasks = taskRepository.getUserTask(resetPaging)

            if (resetPaging) {
                currentVerticalList.clear()
            }

            currentVerticalList.addAll(tasks)
            _allTask.postValue(currentVerticalList)

            Log.d(TAG, "getUserTask: Current list size: ${currentVerticalList.size}")
        }
    }

    fun getUserCompletedTask(resetPaging: Boolean = false) {
        viewModelScope.launch {
            val tasks = taskRepository.getUserCompletedTask(resetPaging)

            if (resetPaging) {
                currentVerticalList.clear()
            }

            currentVerticalList.addAll(tasks)
            _taskCompleted.postValue(currentVerticalList)

            Log.d(TAG, "getUserCompletedTask: Current list size: ${currentVerticalList.size}")
        }
    }

    fun getUserInProgressTask(resetPaging: Boolean = false) {
        viewModelScope.launch {
            val tasks = taskRepository.getUserInProgressTask(resetPaging)

            if (resetPaging) {
                currentVerticalList.clear()
            }

            currentVerticalList.addAll(tasks)
            _taskInProgress.postValue(currentVerticalList)

            Log.d(TAG, "getUserInProgressTask: Current list size: ${currentVerticalList.size}")
        }
    }

    fun getUserTaskByDate(date: String, resetPaging: Boolean = false) {
        viewModelScope.launch {
            val tasks = taskRepository.getUserTaskByDate(
                date = date,
                resetPaging = resetPaging
            )

            if (resetPaging) {
                currentVerticalList.clear()
            }

            currentVerticalList.addAll(tasks)
            _taskByDate.postValue(currentVerticalList)

            Log.d(TAG, "getUserTaskByDate: Current list size: ${currentVerticalList.size}")
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
                _taskReminderTime.postValue(tasks.task.taskReminderTime)
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
        taskReminderTime: String?,
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
                taskReminderTime = taskReminderTime,
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
                _taskReminderTime.postValue(taskReminderTime)
                _taskImageUrl.postValue(taskImageUrl)
                _isCompleted.postValue(false)

                scheduleNotification(
                    context = context,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    taskDueTime = taskDueTime,
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
        taskId: String,
        context: Context
    ) {
        viewModelScope.launch {
            val result = taskRepository.deleteTask(
                taskId = taskId
            )

            _operationDeleteResult.postValue(result)

            if (result.first) {
                cancelNotification(context, taskId)
            }
        }
    }

    private fun scheduleNotification(context: Context, taskId: String, taskTitle: String, taskDueTime: String?, reminderOffsetMillis: Long?) {
        if (taskDueTime == null || !taskDueTime.matches(Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"))) {
            Log.e(TAG, "Invalid or null taskDueDate format: $taskDueTime")
            return
        }

        Log.d(TAG, "Task ID for notification: $taskId")
        Log.d(TAG, "Scheduling notification for task: $taskTitle")

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val dueTime = try {
            formatter.parse(taskDueTime)?.time ?: return
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse taskDueDate: $taskDueTime", e)
            return
        }

        Log.d(TAG, "Task due date (ms): $dueTime")

        val reminderTime = reminderOffsetMillis ?: 0L
        val delay = dueTime - System.currentTimeMillis()
        val finalDelay = delay - reminderTime

        Log.d(TAG, "Calculated delay (ms): $delay")
        Log.d(TAG, "Final delay (ms): $finalDelay")

        if (delay <= 0) return

        val data = workDataOf(
            "title" to "Reminder",
            "message" to "Task: $taskTitle is due soon!"
        )

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(finalDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reminder_$taskId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun cancelNotification(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$taskId")
        Log.d(TAG, "Notification for task $taskId cancelled")
    }
}
