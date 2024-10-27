package com.project.focuslist.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.focuslist.data.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_table")
    fun getTaskList(): LiveData<MutableList<Task>>

    @Query("SELECT * FROM task_table WHERE isCompleted = 1")
    fun getCompletedTasks(): LiveData<MutableList<Task>>

    @Query("SELECT * FROM task_table WHERE isCompleted = 0")
    fun getInProgressTasks(): LiveData<MutableList<Task>>

    @Query("SELECT * FROM task_table WHERE taskId=:taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTask(task: Task)
}