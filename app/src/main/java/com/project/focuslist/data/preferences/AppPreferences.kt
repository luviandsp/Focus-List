package com.project.focuslist.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "app")