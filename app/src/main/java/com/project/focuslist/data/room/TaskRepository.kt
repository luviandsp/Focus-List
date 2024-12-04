package com.project.focuslist.data.room

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import com.project.focuslist.data.model.Task

class TaskRepository(private val taskDao: TaskDao) {

//    fun getTaskList(userId: Int): LiveData<MutableList<Task>> = taskDao.getTaskList(userId)
//
//    fun getCompletedTasks(userId: Int): LiveData<MutableList<Task>> = taskDao.getCompletedTasks(userId)
//
//    fun getInProgressTasks(userId: Int): LiveData<MutableList<Task>> = taskDao.getInProgressTasks(userId)

    fun getTaskListPaged(): PagingSource<Int, Task> = taskDao.getTaskListPaged()

    fun getCompletedTasksPaged(): PagingSource<Int, Task> = taskDao.getCompletedTasksPaged()

    fun getInProgressTasksPaged(): PagingSource<Int, Task> = taskDao.getInProgressTasksPaged()

//    fun getTaskList(): LiveData<MutableList<Task>> = taskDao.getTaskList()
//
//    fun getCompletedTasks(): LiveData<MutableList<Task>> = taskDao.getCompletedTasks()
//
//    fun getInProgressTasks(): LiveData<MutableList<Task>> = taskDao.getInProgressTasks()

    fun getTaskListByDate(date: String): LiveData<MutableList<Task>> = taskDao.getTaskListByDate(date)

    suspend fun getTaskById(taskId: Int): Task? = taskDao.getTaskById(taskId)

    suspend fun createTask(task: Task) = taskDao.createTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
}