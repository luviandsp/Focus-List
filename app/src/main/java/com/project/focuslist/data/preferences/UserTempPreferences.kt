package com.project.focuslist.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.project.focuslist.data.model.TempUser
import kotlinx.coroutines.flow.first

private val Context.datastore by preferencesDataStore(name = "user_account_temp_preferences")

class UserTempPreferences(private val context: Context) {

    companion object {
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val REGISTERED_KEY = booleanPreferencesKey("registered")
        const val TAG = "UserAccountTempPreferences"
    }

    suspend fun setRegistered(isRegistered: Boolean) {
        context.datastore.edit { preferences ->
            preferences[REGISTERED_KEY] = isRegistered
        }
    }

    suspend fun isRegistered(): Boolean {
        val preferences = context.datastore.data.first()
        return preferences[REGISTERED_KEY] == true
    }

    suspend fun saveTempUser(email: String, username: String) {
        context.datastore.edit { preferences ->
            preferences[EMAIL_KEY] = email
            preferences[USERNAME_KEY] = username
        }

        Log.d(TAG, "email=$email, username=$username")
    }

    suspend fun getTempUser(): TempUser? {
        val preferences = context.datastore.data.first()
        val email = preferences[EMAIL_KEY]
        val username = preferences[USERNAME_KEY]

        Log.d(TAG, "email=$email, username=$username,")

        return if (email!= null && username != null) {
            TempUser(email, username)
        } else null
    }

    suspend fun clearTempUser() {
        context.datastore.edit { it.clear() }
    }
}