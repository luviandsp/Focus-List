package com.project.focuslist.data.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.focuslist.data.model.TaskDraft

@Dao
interface TaskDraftDao {

    @Query("SELECT * FROM task_draft_table WHERE user_id = :userId ORDER BY task_priority DESC, task_due_date ASC")
    fun getTaskList(userId: String): PagingSource<Int, TaskDraft>

    @Query("SELECT * FROM task_draft_table WHERE task_id = :taskId")
    suspend fun getTaskById(taskId: String): TaskDraft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createTask(task: TaskDraft)

    @Delete
    suspend fun deleteTask(task: TaskDraft)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTask(task: TaskDraft)
}
