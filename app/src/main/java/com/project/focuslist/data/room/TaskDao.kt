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
//    @Query("SELECT * FROM task_table WHERE task_user_id = :userId ORDER BY task_priority DESC, task_due_date ASC")
//    fun getTaskList(userId: Int): LiveData<MutableList<Task>>
//
//    @Query("SELECT * FROM task_table WHERE task_user_id = :userId AND task_is_completed = 1 ORDER BY task_due_date ASC")
//    fun getCompletedTasks(userId: Int): LiveData<MutableList<Task>>
//
//    @Query("SELECT * FROM task_table WHERE task_user_id = :userId AND task_is_completed = 0 ORDER BY task_due_date ASC")
//    fun getInProgressTasks(userId: Int): LiveData<MutableList<Task>>

    @Query("SELECT * FROM task_table ORDER BY task_priority DESC, task_due_date ASC")
    fun getTaskList(): LiveData<MutableList<Task>>

    @Query("SELECT * FROM task_table WHERE task_is_completed = 1 ORDER BY task_priority DESC, task_due_date ASC")
    fun getCompletedTasks(): LiveData<MutableList<Task>>

    @Query("SELECT * FROM task_table WHERE task_is_completed = 0 ORDER BY task_priority DESC, task_due_date ASC")
    fun getInProgressTasks(): LiveData<MutableList<Task>>

    @Query("SELECT * FROM task_table WHERE task_id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTask(task: Task)
}
