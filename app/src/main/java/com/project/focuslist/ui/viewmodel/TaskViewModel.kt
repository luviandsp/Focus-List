package com.project.focuslist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.room.AppDatabase
import com.project.focuslist.data.room.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(app: Application): AndroidViewModel(app) {

    private val taskRepo: TaskRepository

    init {
        val daoTask = AppDatabase.getDatabase(app).taskDao
        taskRepo = TaskRepository(daoTask)
    }

    val pagedTasksList = Pager(
        config = PagingConfig(
            pageSize = 10
        ),
        pagingSourceFactory = { taskRepo.getTaskListPaged() }
    ).flow.cachedIn(viewModelScope)

    val pagedTasksInProgress = Pager(
        config = PagingConfig(
            pageSize = 10
        ),
        pagingSourceFactory = { taskRepo.getInProgressTasksPaged() }
    ).flow.cachedIn(viewModelScope)

    val pagedTasksCompleted = Pager(
        config = PagingConfig(
            pageSize = 10
        ),
        pagingSourceFactory = { taskRepo.getCompletedTasksPaged() }
    ).flow.cachedIn(viewModelScope)

//    fun getTaskList(): LiveData<MutableList<Task>> = taskRepo.getTaskList()

    // Fungsi untuk mendapatkan tugas yang sudah selesai
//    fun getCompletedTasks(): LiveData<MutableList<Task>> = taskRepo.getCompletedTasks()

    // Fungsi untuk mendapatkan tugas yang sedang berjalan
//    fun getInProgressTasks(): LiveData<MutableList<Task>> = taskRepo.getInProgressTasks()

    fun getTaskListByDate(date: String): LiveData<MutableList<Task>> = taskRepo.getTaskListByDate(date)

    fun createTask(task: Task) = viewModelScope.launch {
        taskRepo.createTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepo.deleteTask(task)
    }

    fun getTaskById(taskId: Int): LiveData<Task> {
        val taskData = MutableLiveData<Task>()
        viewModelScope.launch {
            taskData.value = taskRepo.getTaskById(taskId)
        }
        return taskData
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepo.updateTask(task)
    }

    fun toggleTaskCompletion(task: Task, isCompleted: Boolean) = viewModelScope.launch {
        task.isCompleted = isCompleted
        taskRepo.updateTask(task)
    }
}
