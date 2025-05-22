package com.project.focuslist.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "task_draft_table")
data class TaskDraft(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    val taskId: Int = 0,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
    @ColumnInfo(name = "task_title")
    val taskTitle: String = "",
    @ColumnInfo(name = "task_body")
    val taskBody: String = "",
    @ColumnInfo(name = "task_is_completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "task_priority")
    val taskPriority: Int = 0,
    @ColumnInfo(name = "task_due_date")
    val taskDueDate: String? = null,
    @ColumnInfo(name = "task_image_url")
    val taskImageUrl: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String = "",
    @ColumnInfo(name = "updated_at")
    val updatedAt: String = ""
) : Parcelable
