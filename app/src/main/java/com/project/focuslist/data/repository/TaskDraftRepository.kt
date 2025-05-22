package com.project.focuslist.data.repository

import androidx.paging.PagingSource
import com.google.firebase.auth.FirebaseAuth
import com.project.focuslist.data.model.TaskDraft
import com.project.focuslist.data.room.TaskDraftDao

class TaskDraftRepository(
    private val taskDraftDao: TaskDraftDao
) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid ?: ""

    suspend fun createTask(task: TaskDraft) {
        taskDraftDao.createTask(task)
    }

    suspend fun deleteTask(task: TaskDraft) {
        taskDraftDao.deleteTask(task)
    }

    suspend fun updateTask(task: TaskDraft) {
        taskDraftDao.updateTask(task)
    }

    fun getTaskList(): PagingSource<Int, TaskDraft> {
        return taskDraftDao.getTaskList(userId = userId)
    }

    suspend fun getTaskById(taskId: String): TaskDraft? {
        return taskDraftDao.getTaskById(taskId)
    }
}