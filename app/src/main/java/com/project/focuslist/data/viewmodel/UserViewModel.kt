package com.project.focuslist.data.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.focuslist.data.preferences.AuthPreferences
import com.project.focuslist.data.preferences.UserAccountCache
import com.project.focuslist.data.preferences.UserTempPreferences
import com.project.focuslist.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(applicationContext: Context) : ViewModel() {

    private val userRepository = UserRepository()
    private val userAccountCache = UserAccountCache(applicationContext)
    private val authPreferences = AuthPreferences(applicationContext)
    private val userTempPreferences = UserTempPreferences(applicationContext)

    companion object {
        private const val TAG = "UserViewModel"
    }

    private val _authStatus = MutableLiveData<Pair<Boolean, String?>>()
    val authStatus: LiveData<Pair<Boolean, String?>> get() = _authStatus

    private val _authRegister = MutableLiveData<Pair<Boolean, String?>>()
    val authRegister: LiveData<Pair<Boolean, String?>> get() = _authRegister

    private val _authIsRegistered = MutableLiveData<Boolean>()
    val authIsRegistered: LiveData<Boolean> get() = _authIsRegistered

    private val _authLogin = MutableLiveData<Pair<Boolean, String?>>()
    val authLogin: LiveData<Pair<Boolean, String?>> get() = _authLogin

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _operationStatus = MutableLiveData<Pair<Boolean, String?>>()
    val operationStatus: LiveData<Pair<Boolean, String?>> get() = _operationStatus

    private val _operationUpdateStatus = MutableLiveData<Pair<Boolean, String?>>()
    val operationUpdateStatus: LiveData<Pair<Boolean, String?>> get() = _operationUpdateStatus

    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> get() = _userId

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> get() = _userEmail

    private val _userUsername = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userUsername

    private val _userImageUrl = MutableLiveData<String?>()
    val userImageUrl: LiveData<String?> get() = _userImageUrl

    init {
        loadUserCache()
    }

    private fun loadUserCache() {
        viewModelScope.launch {
            try {
                val cachedUserWithTimestamp = userAccountCache.getUserCache()

                if (cachedUserWithTimestamp.userCache != null) {
                    _userId.postValue(cachedUserWithTimestamp.userCache.userId)
                    _userUsername.postValue(cachedUserWithTimestamp.userCache.username)
                    _userImageUrl.postValue(cachedUserWithTimestamp.userCache.userProfileImage)
                    Log.d(TAG, "User data loaded from cache: ${cachedUserWithTimestamp.userCache}")
                } else {
                    Log.d(TAG, "No user data in cache")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user cache", e)
            }
        }
    }

    fun setTempUser(email: String, username: String) {
        viewModelScope.launch {
            userTempPreferences.saveTempUser(email, username)
            Log.d(TAG, "setTempUser: $email, $username")
        }
    }

    fun setRegistered(isRegistered: Boolean) {
        viewModelScope.launch {
            userTempPreferences.setRegistered(isRegistered)
            Log.d(TAG, "setRegistered: $isRegistered")
        }
    }

    fun getIsRegistered() {
        viewModelScope.launch {
            val isRegistered = userTempPreferences.isRegistered()

            _authIsRegistered.postValue(isRegistered)

            Log.d(TAG, "isRegistered: $isRegistered")
        }
    }

    fun setLoginStatus(isLoggedIn: Boolean) {
        viewModelScope.launch {
            authPreferences.setLoginStatus(isLoggedIn)
            Log.d(TAG, "setLoginStatus: $isLoggedIn")
        }
    }

    fun setEmail(email: String) {
        viewModelScope.launch {
            authPreferences.setEmail(email)
            Log.d(TAG, "setEmail: $email")
        }
    }

    fun getLoginStatus() {
        viewModelScope.launch {
            val loginStatus = authPreferences.getLoginStatus()

            _isLoggedIn.postValue(loginStatus)
            Log.d(TAG, "getLoginStatus: $loginStatus")
        }
    }

    fun getEmail() {
        viewModelScope.launch {
            val email = authPreferences.getEmail()

            _userEmail.postValue(email)
            Log.d(TAG, "getEmail: $email")
        }
    }

    fun registerAccountOnly(email: String, password: String, username: String) {
        viewModelScope.launch {
            val result = userRepository.registerAccountOnly(email, password)
            
            _authRegister.postValue(result)
            
            if (result.first) {
                setTempUser(
                    email = email,
                    username = username
                )
            }
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
            val cachedUserWithTimestamp = userAccountCache.getUserCache()
            val cachedUser = cachedUserWithTimestamp.userCache
            val cachedUpdatedAt = cachedUserWithTimestamp.updatedAt

            Log.d(TAG, "getUser: Cached data - $cachedUser, updatedAt - $cachedUpdatedAt")
            Log.d(TAG, "getUser: Fetching user data from repository")
            val userData = userRepository.getUserData()
            _authStatus.postValue(Pair(true, "User logged in"))


            if (userData != null) {
                val databaseUpdatedAt = userData.updatedAt.toDate().time
                Log.d(TAG, "getUser: Database data - ${userData.username}, updatedAt - $databaseUpdatedAt")

                if (cachedUser == null || cachedUpdatedAt == null || databaseUpdatedAt > cachedUpdatedAt) {
                    _userId.postValue(userData.userId)
                    _userUsername.postValue(userData.username)
                    _userImageUrl.postValue(userData.profileImageUrl)

                    saveUserCache(userData.userId, userData.username, userData.profileImageUrl.toString(), databaseUpdatedAt)
                    Log.d(TAG, "getUser: Data updated from database or cache is old")
                } else {
                    _userId.postValue(cachedUser.userId)
                    _userUsername.postValue(cachedUser.username)
                    _userImageUrl.postValue(cachedUser.userProfileImage)
                    Log.d(TAG, "getUser: Data is up to date")
                }
            } else {
                Log.d(TAG, "getUser: User data is null")
            }
        }
    }

    fun updateProfile(username: String, profileImageUrl: String) {
        viewModelScope.launch {
            val result = userRepository.updateProfile(
                username = username,
                profileImageUrl = profileImageUrl
            )

            _operationUpdateStatus.postValue(result)

            if (result.first) {
                _userUsername.postValue(username)
                _userImageUrl.postValue(profileImageUrl)

                val currentUserId = _userId.value
                if (currentUserId != null) {
                    // Setelah berhasil memperbarui profil, ambil data terbaru dari database
                    val freshUserData = userRepository.getUserData()
                    if (freshUserData != null) {
                        val databaseUpdatedAt = freshUserData.updatedAt.toDate().time
                        saveUserCache(currentUserId, username, profileImageUrl, databaseUpdatedAt)
                    } else {
                        // Handle jika gagal mengambil data terbaru setelah update
                        Log.w(TAG, "Failed to retrieve fresh user data after profile update to update cache's updatedAt")
                        saveUserCache(currentUserId, username, profileImageUrl, System.currentTimeMillis()) // Sebagai fallback, gunakan timestamp saat ini
                    }
                }
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            userRepository.logout()
            clearUserCache()

            _userId.postValue(null)
            _userUsername.postValue(null)
            _userImageUrl.postValue(null)
            _authStatus.postValue(Pair(false, "User logged out"))
        }
    }

    fun deleteAccount(
        email: String,
        password: String,
        context: Context
    ) {
        viewModelScope.launch {
            val result = userRepository.deleteAccountWithReauth(
                email = email,
                password = password,
                context = context
            )

            _operationStatus.postValue(result)

            if (result.first) {
                logoutUser()
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            val result = userRepository.forgotPassword(email)

            _authStatus.postValue(result)
        }
    }

    private fun saveUserCache(userId: String, username: String, userProfileImage: String, updatedAt: Long) {
        viewModelScope.launch {
            userAccountCache.saveUserCache(userId, username, userProfileImage, updatedAt)
            Log.d(TAG, "User data saved to cache: userId=$userId, username=$username, image=$userProfileImage")
        }
    }

    private fun clearUserCache() {
        viewModelScope.launch {
            userAccountCache.clearUserCache()
            Log.d(TAG, "User cache cleared")
        }
    }
}
