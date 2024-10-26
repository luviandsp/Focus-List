package com.project.focuslist.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.model.User

@Database(
    entities = [User::class, Task::class],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {

    abstract val taskDao: TaskDao

    companion object {
        private const val databaseName = "focuslist_database"

        @Volatile
        private var databaseInstance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            synchronized(this) {
                return databaseInstance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    databaseName
                ).build().also {
                    databaseInstance = it
                }
            }
        }
    }
}