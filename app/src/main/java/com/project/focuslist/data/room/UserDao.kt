package com.project.focuslist.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.project.focuslist.data.model.User
import com.project.focuslist.data.model.UserWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table")
    fun getAllUser(): Flow<MutableList<User>>

    @Transaction
    @Query("SELECT * FROM user_table WHERE userId = :userId")
    fun getUserWithTasks(userId: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

// TODO: Implementasi UserDao