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

    // Menggunakan Paging 3 untuk mendapatkan data tugas
//    fun getTaskList(): Flow<PagingData<Task>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = 20,
//                enablePlaceholders = false
//            ),
//            pagingSourceFactory = { FocusListPagingSource(taskRepo) }
//        ).flow.cachedIn(viewModelScope)
//    }

    fun getTaskList(): LiveData<MutableList<Task>> = taskRepo.getTaskList()

    // Fungsi untuk mendapatkan tugas yang sudah selesai
    fun getCompletedTasks(): LiveData<MutableList<Task>> = taskRepo.getCompletedTasks()

    // Fungsi untuk mendapatkan tugas yang sedang berjalan
    fun getInProgressTasks(): LiveData<MutableList<Task>> = taskRepo.getInProgressTasks()

    // Fungsi untuk membuat tugas baru
    fun createTask(task: Task) = viewModelScope.launch {
        taskRepo.createTask(task)
    }

    // Fungsi untuk menghapus tugas
    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepo.deleteTask(task)
    }

    // Fungsi untuk mendapatkan tugas berdasarkan ID
    fun getTaskById(taskId: Int): LiveData<Task> {
        val taskData = MutableLiveData<Task>()
        viewModelScope.launch {
            taskData.value = taskRepo.getTaskById(taskId)
        }
        return taskData
    }

    // Fungsi untuk memperbarui tugas
    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepo.updateTask(task)
    }

    // Fungsi untuk toggle status penyelesaian tugas
    fun toggleTaskCompletion(task: Task, isCompleted: Boolean) = viewModelScope.launch {
        task.isCompleted = isCompleted
        taskRepo.updateTask(task)
    }
}
