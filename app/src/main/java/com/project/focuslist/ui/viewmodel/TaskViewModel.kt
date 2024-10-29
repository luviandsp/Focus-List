package com.project.focuslist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

//    fun getTaskList(userId: Int): LiveData<MutableList<Task>> = taskRepo.getTaskList(userId)
//
//    fun getCompletedTasks(userId: Int): LiveData<MutableList<Task>> = taskRepo.getCompletedTasks(userId)
//
//    fun getInProgressTasks(userId: Int): LiveData<MutableList<Task>> = taskRepo.getInProgressTasks(userId)

    fun getTaskList(): LiveData<MutableList<Task>> = taskRepo.getTaskList()

    fun getCompletedTasks(): LiveData<MutableList<Task>> = taskRepo.getCompletedTasks()

    fun getInProgressTasks(): LiveData<MutableList<Task>> = taskRepo.getInProgressTasks()

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
