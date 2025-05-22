package com.project.focuslist.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class Task(
    @PropertyName("taskId") val taskId: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("taskTitle") val taskTitle: String = "",
    @PropertyName("taskBody") val taskBody: String = "",
    @PropertyName("taskPriority") val taskPriority: Int = 0,
    @PropertyName("taskDueDate") val taskDueDate: String? = null,
    @PropertyName("taskImageUrl") val taskImageUrl: String? = null,
    @PropertyName("isCompleted") val isCompleted: Boolean = false,
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("updatedAt") val updatedAt: Timestamp = Timestamp.now()
)