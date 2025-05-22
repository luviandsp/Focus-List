package com.project.focuslist.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.focuslist.data.model.TaskWithUser
import com.project.focuslist.data.repository.TaskRepository
import kotlinx.coroutines.launch

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
        taskTitle: String,
        taskBody: String,
        taskPriority: Int,
        taskDueDate: String?,
        taskImageUrl: String?
        ) {
        viewModelScope.launch {
            val result = taskRepository.createTask(
                taskTitle = taskTitle,
                taskBody = taskBody,
                taskPriority = taskPriority,
                taskDueDate = taskDueDate,
                taskImageUrl = taskImageUrl
            )

            _operationResult.postValue(result)

            if (result.first) {
                _taskTitle.postValue(taskTitle)
                _taskBody.postValue(taskBody)
                _taskPriority.postValue(taskPriority)
                _taskDueDate.postValue(taskDueDate)
                _taskImageUrl.postValue(taskImageUrl)
                _isCompleted.postValue(false)
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
                _taskImageUrl.postValue(tasks.task.taskImageUrl)
            }
        }
    }

    fun updateTask(
        taskId: String,
        taskTitle: String,
        taskBody: String,
        taskPriority: Int,
        taskDueDate: String?,
        taskImageUrl: String?
    ) {
        viewModelScope.launch {
            val result = taskRepository.updateTask(
                taskId = taskId,
                taskTitle = taskTitle,
                taskBody = taskBody,
                taskPriority = taskPriority,
                taskDueDate = taskDueDate,
                taskImageUrl = taskImageUrl
            )

            _operationResult.postValue(result)

            if (result.first) {
                _taskTitle.postValue(taskTitle)
                _taskBody.postValue(taskBody)
                _taskPriority.postValue(taskPriority)
                _taskDueDate.postValue(taskDueDate)
                _taskImageUrl.postValue(taskImageUrl)
                _isCompleted.postValue(false)
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
}
