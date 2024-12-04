package com.project.focuslist.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "task_table",
//    foreignKeys = [
//        ForeignKey(
//            entity = User::class,
//            parentColumns = ["user_id"],
//            childColumns = ["task_user_id"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    val taskId: Int = 0,
    @ColumnInfo(name = "task_title")
    val title: String,
    @ColumnInfo(name = "task_body")
    val body: String,
    @ColumnInfo(name = "task_is_completed")
    var isCompleted: Boolean = false,
    @ColumnInfo(name = "task_priority")
    val priority: Int = 0,
    @ColumnInfo(name = "task_due_date")
    val dueDate: String? = null,
    @ColumnInfo(name = "task_image")
    val taskImage: ByteArray? = null,
//    @ColumnInfo(name = "task_user_id", index = true)
//    val taskUserId: Int // Foreign key referencing "user_id" in the User table
) : Parcelable