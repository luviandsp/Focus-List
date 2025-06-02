package com.project.focuslist.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.project.focuslist.data.model.UserCache
import com.project.focuslist.data.model.UserCacheWithTimestamp
import kotlinx.coroutines.flow.first

private val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "user_account_cache")

class UserAccountCache(private val context: Context) {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("userId")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USER_PROFILE_IMAGE_KEY = stringPreferencesKey("userProfileImage")
        private val UPDATED_AT_KEY = longPreferencesKey("updatedAt")
        private const val TAG = "UserAccountCache"
    }

    suspend fun saveUserCache(userId: String, username: String, userProfileImage: String, updatedAt: Long) {
        context.datastore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[USER_PROFILE_IMAGE_KEY] = userProfileImage
            preferences[UPDATED_AT_KEY] = updatedAt
        }

        Log.d(TAG, "userId=$userId, username=$username, userProfileImage=$userProfileImage, updatedAt=$updatedAt")
    }

    suspend fun getUserCache(): UserCacheWithTimestamp {
        val preferences = context.datastore.data.first()
        val userId = preferences[USER_ID_KEY]
        val username = preferences[USERNAME_KEY]
        val userProfileImage = preferences[USER_PROFILE_IMAGE_KEY]
        val updatedAt = preferences[UPDATED_AT_KEY]

        Log.d(TAG, "userId=$userId, username=$username, userProfileImage=$userProfileImage, updatedAt=$updatedAt")

        val userCache = if (userId != null && username != null && userProfileImage != null) {
            UserCache(userId, username, userProfileImage)
        } else null

        return UserCacheWithTimestamp(userCache, updatedAt)
    }

    suspend fun clearUserCache() {
        context.datastore.edit { it.clear() }
    }
}