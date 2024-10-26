package com.project.focuslist.data.room

import androidx.lifecycle.LiveData
import com.project.focuslist.data.model.User
import com.project.focuslist.data.model.UserWithTasks
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getAllUser(): Flow<MutableList<User>> = userDao.getAllUser()

    fun getUserWithTasks(userId: Int): User? = userDao.getUserWithTasks(userId)

    suspend fun createUser(user: User) = userDao.createUser(user)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}

// TODO: Implementasi UserRepository