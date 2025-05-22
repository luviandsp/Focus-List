package com.project.focuslist.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "app")

class AuthPreferences(private val context: Context) {

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val EMAIL = stringPreferencesKey("email")
    }

    suspend fun setLoginStatus(isLoggedIn: Boolean) {
        context.datastore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun setEmail(email: String) {
        context.datastore.edit { preferences ->
            preferences[EMAIL] = email
        }
    }

    suspend fun getLoginStatus(): Boolean {
        return context.datastore.data.first()[IS_LOGGED_IN] == true
    }

    suspend fun getEmail(): String {
        return context.datastore.data.first()[EMAIL] ?: ""
    }

}