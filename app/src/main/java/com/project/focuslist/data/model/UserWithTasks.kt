package com.project.focuslist.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithTasks(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userOwnedId"
    )
    val tasks: List<Task>
)

// TODO: Implementasi User Table dengan Task Table