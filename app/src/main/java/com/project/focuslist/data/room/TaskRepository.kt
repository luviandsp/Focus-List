package com.project.focuslist.data.room

import androidx.lifecycle.LiveData
import com.project.focuslist.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun getTaskList(): Flow<MutableList<Task>> = taskDao.getTaskList()

//    suspend fun getTasksForUser(userId: Int): Task? = taskDao.getTasksForUser(userId)

    suspend fun getTaskById(taskId: Int): Task? = taskDao.getTaskById(taskId)

    suspend fun createTask(task: Task) = taskDao.createTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
}