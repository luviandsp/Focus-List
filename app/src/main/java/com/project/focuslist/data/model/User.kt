package com.project.focuslist.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Int = 0,
    @ColumnInfo(name = "user_username")
    val username: String,
    @ColumnInfo(name = "user_password")
    var password: String,
    @ColumnInfo(name = "user_profile_image")
    val profileImage: ByteArray? = null
) : Parcelable
