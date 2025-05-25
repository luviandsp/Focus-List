package com.project.focuslist.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.project.focuslist.data.model.TaskDraft

const val OLD_VERSION = 1
const val NEW_VERSION = 2

@Database(
    entities = [TaskDraft::class],
    version = 2
)
abstract class AppDatabase: RoomDatabase() {

    abstract val taskDraftDao: TaskDraftDao

    companion object {
        private const val DATABASE_NAME = "focuslist_database"

        @Volatile
        private var databaseInstance: AppDatabase? = null

        private val MIGRATION_OLD_TO_NEW = object : Migration(OLD_VERSION, NEW_VERSION) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add migration code if needed
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `task_draft_table` (
                    `task_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `user_id` TEXT NOT NULL,
                    `task_title` TEXT NOT NULL,
                    `task_body` TEXT NOT NULL,
                    `task_due_date` TEXT,
                    `task_due_time` TEXT,
                    `task_due_hours` TEXT,
                    `task_image_url` TEXT,
                    `task_is_completed` INTEGER NOT NULL,
                    `task_priority` INTEGER NOT NULL,
                    `created_at` TEXT NOT NULL,
                    `updated_at` TEXT NOT NULL
                    )
                    """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            synchronized(this) {
                return databaseInstance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_OLD_TO_NEW)
                    .build().also { instance ->
                        databaseInstance = instance
                    }
            }
        }
    }
}
