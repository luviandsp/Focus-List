package com.project.focuslist.data.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.focuslist.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _authStatus = MutableLiveData<Pair<Boolean, String?>>()
    val authStatus: LiveData<Pair<Boolean, String?>> get() = _authStatus

    private val _authRegister = MutableLiveData<Pair<Boolean, String?>>()
    val authRegister: LiveData<Pair<Boolean, String?>> get() = _authRegister

    private val _authLogin = MutableLiveData<Pair<Boolean, String?>>()
    val authLogin: LiveData<Pair<Boolean, String?>> get() = _authLogin

    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> get() = _userId

    private val _userUsername = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userUsername

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> get() = _userEmail

    private val _userImageUrl = MutableLiveData<String?>()
    val userImageUrl: LiveData<String?> get() = _userImageUrl

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        getUser()
    }

    fun registerAccountOnly(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.registerAccountOnly(email, password)
            _authRegister.postValue(result)
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val result = userRepository.resendVerificationEmail()
            _authStatus.postValue(result)
        }
    }

    fun completeUserRegistration(context: Context) {
        viewModelScope.launch {
            val result = userRepository.completeUserRegistration(context)
            _authStatus.postValue(result)
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.login(email, password)
            _authLogin.postValue(result)
        }
    }

    fun getUser() {
        viewModelScope.launch {
            val userData = userRepository.getUserData()

            if (userData != null) {
                _userId.postValue(userData.userId)
                _userEmail.postValue(userData.email)
                _userUsername.postValue(userData.username)
                _userImageUrl.postValue(userData.profileImageUrl)
            } else {
                _errorMessage.postValue("Data user tidak ditemukan")
            }
        }
    }

    fun getUserId() {
        viewModelScope.launch {
            val userId = userRepository.getUserId()
            _userId.postValue(userId)
        }
    }

    fun updateProfile(username: String, profileImageUrl: String) {
        viewModelScope.launch {
            val result = userRepository.updateProfile(
                username = username,
                profileImageUrl = profileImageUrl
            )

            _authStatus.postValue(result)

            if (result.first) {
                _userUsername.postValue(username)
                _userImageUrl.postValue(profileImageUrl)
                _errorMessage.postValue(null)
            }
        }
    }

    fun updateProfileImageUrl(imageUrl: String) {
        viewModelScope.launch {
            val result = userRepository.updateProfileImageUrl(imageUrl)
            if (result.first) {
                _userImageUrl.postValue(imageUrl)
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            userRepository.logout()
            _userId.postValue(null)
            _userUsername.postValue(null)
            _userEmail.postValue(null)
            _userImageUrl.postValue(null)
            _errorMessage.postValue(null)
            _authStatus.postValue(Pair(false, "User logged out"))
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val result = userRepository.deleteAccount()
            _authStatus.postValue(result)
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            userRepository.forgotPassword(email)
            _authStatus.postValue(Pair(true, "Silakan cek email Anda untuk reset password"))
        }
    }
}
