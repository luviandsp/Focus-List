package com.project.focuslist.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.project.focuslist.data.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user_table WHERE userId=:userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT userId FROM user_table WHERE username = :username LIMIT 1")
    fun getUserIdByUsername(username: String): LiveData<Int>

    @Transaction
    @Query("SELECT * FROM user_table WHERE userId = :userId")
    fun getUserWithTasks(userId: Int): User?

    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password")
    fun getUserByUsernameAndPassword(username: String, password: String): LiveData<User?>

    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    fun getUserByUsername(username: String): LiveData<User?>

    @Query("UPDATE user_table SET password = :newPassword WHERE userId = :userId")
    suspend fun updatePassword(userId: Int, newPassword: String)

    @Query("SELECT profileImage FROM user_table WHERE userId = :userId LIMIT 1")
    suspend fun getProfileImage(userId: Int): ByteArray?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createUser(user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}