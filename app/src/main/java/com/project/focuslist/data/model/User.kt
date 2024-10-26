package com.project.focuslist.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int,
    val username: String,
    val password: String
) : Parcelable

// TODO: Implementasi User Table