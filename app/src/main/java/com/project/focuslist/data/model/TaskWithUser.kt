package com.project.focuslist.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class TaskWithUser(
    val task: Task = Task(),
    val user: User = User()
)
