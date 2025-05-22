package com.project.focuslist.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.project.focuslist.data.model.TaskDraft
import com.project.focuslist.data.repository.TaskDraftRepository
import com.project.focuslist.data.room.AppDatabase
import kotlinx.coroutines.launch

class TaskDraftViewModel(app: Application): AndroidViewModel(app) {

    private val taskRepo: TaskDraftRepository

    init {
        val daoTask = AppDatabase.getDatabase(app).taskDraftDao
        taskRepo = TaskDraftRepository(daoTask)
    }

    val pagedTasksList = Pager(
        config = PagingConfig(
            pageSize = 10
        ),
        pagingSourceFactory = { taskRepo.getTaskList() }
    ).flow.cachedIn(viewModelScope)

    fun createTask(task: TaskDraft) = viewModelScope.launch {
        taskRepo.createTask(task)
    }

    fun deleteTask(task: TaskDraft) = viewModelScope.launch {
        taskRepo.deleteTask(task)
    }

    fun getTaskById(taskId: String): LiveData<TaskDraft> {
        val taskData = MutableLiveData<TaskDraft>()

        viewModelScope.launch {
            taskData.value = taskRepo.getTaskById(taskId)
        }
        return taskData
    }

    fun updateTask(task: TaskDraft) = viewModelScope.launch {
        taskRepo.updateTask(task)
    }
}
