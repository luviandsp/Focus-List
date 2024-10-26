package com.project.focuslist.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.focuslist.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_table")
    fun getTaskList(): Flow<MutableList<Task>>

//    @Query("SELECT * FROM task_table WHERE userOwnedId=:userId")
//    suspend fun getTasksForUser(userId: Int): Task?

    @Query("SELECT * FROM task_table WHERE taskId=:taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}