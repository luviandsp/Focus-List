package com.project.focuslist.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "task_table"
//    foreignKeys = [ForeignKey(
//        entity = User::class,
//        parentColumns = arrayOf("userId"),
//        childColumns = arrayOf("userOwnedId"),
//        onDelete = ForeignKey.CASCADE
//    )],
//    indices = [Index(value = ["userOwnedId"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val taskId: Int,
    val title: String,
    val body: String,
    val completed: Boolean = false,
    val dueDate: Long = 0L,
    val priority: Int = 0,
    val favorite: Boolean = false,
    val labels: String? = null,
    val color: String = "#ffffff", // Default color white
    val createdAt: Long = System.currentTimeMillis()
//    val userOwnedId: Int
) : Parcelable
