package com.project.focuslist.ui.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import com.project.focuslist.data.preferences.datastore
import kotlinx.coroutines.flow.first

class LoginViewModel(app: Application): AndroidViewModel(app) {

    private val dataStore: DataStore<Preferences> = app.applicationContext.datastore

    suspend fun setLoginStatus(loginStatus: Int) {
        dataStore.edit {
            it[intPreferencesKey("loginStatus")] = loginStatus
        }
    }

    suspend fun getLoginStatus(): Int? {
        return dataStore.data.first()[intPreferencesKey("loginStatus")]
    }

    suspend fun setRememberedUsername(rememberedUsername: String) {
        dataStore.edit {
            it[stringPreferencesKey("rememberedUsername")] = rememberedUsername
        }
    }

    suspend fun getRememberedUsername(): String? {
        return dataStore.data.first()[stringPreferencesKey("rememberedUsername")]
    }

    suspend fun setRememberedPassword(rememberedPassword: String) {
        dataStore.edit {
            it[stringPreferencesKey("rememberedPassword")] = rememberedPassword
        }
    }

    suspend fun getRememberedPassword(): String? {
        return dataStore.data.first()[stringPreferencesKey("rememberedPassword")]
    }
}