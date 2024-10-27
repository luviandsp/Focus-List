package com.project.focuslist.data.room

import androidx.lifecycle.LiveData
import com.project.focuslist.data.model.User
import com.project.focuslist.data.model.UserWithTasks
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getUserWithTasks(userId: Int): User? = userDao.getUserWithTasks(userId)

    fun getUserIdByUsername(username: String): LiveData<Int> = userDao.getUserIdByUsername(username)

    fun getUserByUsernameAndPassword(username: String, password: String): LiveData<User?> =
        userDao.getUserByUsernameAndPassword(username, password)

    suspend fun getUserById(userId: Int): User? = userDao.getUserById(userId)

    suspend fun getProfileImage(userId: Int): ByteArray? = userDao.getProfileImage(userId)

    fun getUserByUsername(username: String): LiveData<User?> = userDao.getUserByUsername(username)

    suspend fun updatePassword(userId: Int, newPassword: String) = userDao.updatePassword(userId, newPassword)

    suspend fun createUser(user: User) = userDao.createUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}

// TODO: Implementasi UserRepository