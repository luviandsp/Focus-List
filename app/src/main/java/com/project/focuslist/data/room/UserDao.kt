package com.project.focuslist.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.focuslist.data.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user_table")
    fun getAllUsers(): LiveData<MutableList<User>>

    @Query("SELECT * FROM user_table WHERE user_id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

//    @Transaction
//    @Query("SELECT * FROM user_table WHERE user_id = :userId")
//    fun getUserWithTasks(userId: Int): LiveData<UserWithTasks?>

    @Query("SELECT * FROM user_table WHERE user_username = :username AND user_password = :password")
    fun getUserByUsernameAndPassword(username: String, password: String): LiveData<User?>

    @Query("SELECT * FROM user_table WHERE user_username = :username LIMIT 1")
    fun getUserByUsername(username: String): LiveData<User?>

    @Query("UPDATE user_table SET user_password = :newPassword WHERE user_id = :userId")
    suspend fun updatePassword(userId: Int, newPassword: String)

    @Query("SELECT user_profile_image FROM user_table WHERE user_id = :userId LIMIT 1")
    suspend fun getProfileImage(userId: Int): ByteArray?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createUser(user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}
