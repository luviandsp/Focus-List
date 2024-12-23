package com.project.focuslist.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.model.User

const val OLD_VERSION = 1
const val NEW_VERSION = 1

@Database(
    entities = [User::class, Task::class],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {

    abstract val taskDao: TaskDao
    abstract val userDao: UserDao

    companion object {
        private const val databaseName = "focuslist_database"

        @Volatile
        private var databaseInstance: AppDatabase? = null

        private val MIGRATION_OLD_TO_NEW = object : Migration(OLD_VERSION, NEW_VERSION) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add migration code if needed
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            synchronized(this) {
                return databaseInstance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    databaseName
                )
                    .addMigrations(MIGRATION_OLD_TO_NEW)
                    .build().also {
                        databaseInstance = it
                    }
            }
        }
    }
}
