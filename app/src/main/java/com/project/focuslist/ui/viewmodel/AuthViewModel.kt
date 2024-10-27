package com.project.focuslist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.model.User
import com.project.focuslist.data.room.AppDatabase
import com.project.focuslist.data.room.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val userRepo: UserRepository

    init {
        val daoUser = AppDatabase.getDatabase(app).userDao
        userRepo = UserRepository(daoUser)
    }

    fun createUser(user: User) = viewModelScope.launch {
        userRepo.createUser(user)
    }

    fun deleteUser(user: User) = viewModelScope.launch {
        userRepo.deleteUser(user)
    }

    // Dalam AuthViewModel
    fun getUserById(userId: Int): LiveData<User> {
        val userData = MutableLiveData<User>()
        viewModelScope.launch {
            userData.postValue(userRepo.getUserById(userId)) // Gunakan postValue untuk mengupdate LiveData
        }
        return userData
    }


    fun updateUser(user: User) = viewModelScope.launch {
        userRepo.updateUser(user)
    }

    fun getProfileImage(userId: Int): LiveData<ByteArray?> {
        val imageData = MutableLiveData<ByteArray?>()
        viewModelScope.launch {
            imageData.value = userRepo.getProfileImage(userId)
        }
        return imageData
    }

    fun getUserIdByUsername(username: String): LiveData<Int> =
        userRepo.getUserIdByUsername(username)

    fun authenticateUser(username: String, password: String): LiveData<User?> =
        userRepo.getUserByUsernameAndPassword(username, password)

    fun getUserByUsername(username: String): LiveData<User?> =
        userRepo.getUserByUsername(username)

    fun updatePassword(userId: Int, newPassword: String) = viewModelScope.launch {
        userRepo.updatePassword(userId, newPassword)
    }


    fun getUserWithTasks(userId: Int) = userRepo.getUserWithTasks(userId)
}