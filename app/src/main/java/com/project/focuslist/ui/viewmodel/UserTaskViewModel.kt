package com.project.focuslist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.model.User
import com.project.focuslist.data.model.UserWithTasks
import com.project.focuslist.data.room.AppDatabase
import com.project.focuslist.data.room.TaskRepository
import com.project.focuslist.data.room.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserTaskViewModel(app: Application): AndroidViewModel(app) {

    private val taskRepo: TaskRepository
    val stateGetTasks = MutableStateFlow<Task?>(null)

    init {
        val daoTask = AppDatabase.getDatabase(app).taskDao
        taskRepo = TaskRepository(daoTask)
    }

    fun getTaskList(): Flow<MutableList<Task>> = taskRepo.getTaskList()

    fun createTask(task: Task) = viewModelScope.launch {
        taskRepo.createTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepo.deleteTask(task)
    }

    fun getTaskById(taskId: Int) = viewModelScope.launch {
        stateGetTasks.value = taskRepo.getTaskById(taskId)
    }

}