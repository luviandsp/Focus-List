package com.project.focuslist.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Keep
@IgnoreExtraProperties
@Parcelize
data class Task(
    @PropertyName("taskId") val taskId: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("taskTitle") val taskTitle: String = "",
    @PropertyName("taskBody") val taskBody: String = "",
    @PropertyName("taskPriority") val taskPriority: Int = 0,
    @PropertyName("taskDueDate") val taskDueDate: String? = null,
    @PropertyName("taskDueHours") val taskDueHours: String? = null,
    @PropertyName("taskDueTime") val taskDueTime: String? = null,
    @PropertyName("taskReminderTime") val taskReminderTime: String? = null,
    @PropertyName("taskImageUrl") val taskImageUrl: String? = null,
    @PropertyName("isCompleted") val isCompleted: Boolean = false,
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("updatedAt") val updatedAt: Timestamp = Timestamp.now()
) : Parcelable