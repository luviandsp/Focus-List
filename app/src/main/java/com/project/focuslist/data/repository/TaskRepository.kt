package com.project.focuslist.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.model.TaskWithUser
import com.project.focuslist.data.model.User
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

class TaskRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "ServiceRepository"
        private const val PAGE_SIZE = 10
    }

    private var lastDocument: DocumentSnapshot? = null
    private var isLastPage = false

    private val taskCollection = firestore.collection("tasks")
    private val userCollection = firestore.collection("users")

    private fun Task.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "taskId" to taskId,
            "userId" to userId,
            "taskTitle" to taskTitle,
            "taskBody" to taskBody,
            "taskPriority" to taskPriority,
            "taskDueDate" to taskDueDate,
            "taskDueHours" to taskDueHours,
            "taskDueTime" to taskDueTime,
            "taskReminderTime" to taskReminderTime,
            "taskImageUrl" to taskImageUrl,
            "isCompleted" to isCompleted,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    private fun Map<String, Any>.toTask(): Task {
        return Task(
            taskId = this["taskId"] as? String ?: "",
            userId = this["userId"] as? String ?: "",
            taskTitle = this["taskTitle"] as? String ?: "",
            taskBody = this["taskBody"] as? String ?: "",
            taskPriority = (this["taskPriority"] as? Number)?.toInt() ?: 0,
            taskDueDate = this["taskDueDate"] as? String,
            taskDueHours = this["taskDueHours"] as? String,
            taskDueTime = this["taskDueTime"] as? String,
            taskReminderTime = this["taskReminderTime"] as? String,
            taskImageUrl = this["taskImageUrl"] as? String,
            isCompleted = this["isCompleted"] as? Boolean == true,
            createdAt = this["createdAt"] as Timestamp,
            updatedAt = this["updatedAt"] as Timestamp
        )
    }

    suspend fun createTask(
        taskTitle: String,
        taskBody: String,
        taskPriority: Int,
        taskDueDate: String?,
        taskDueHours: String?,
        taskDueTime: String?,
        taskReminderTime: String?,
        taskImageUrl: String?
    ): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User has not logged in")
            val taskId = taskCollection.document().id // Auto-generate ID

            val taskModelData = Task(
                taskId = taskId,
                userId = userId,
                taskTitle = taskTitle,
                taskBody = taskBody,
                taskPriority = taskPriority,
                taskDueDate = taskDueDate,
                taskDueHours = taskDueHours,
                taskDueTime = taskDueTime,
                taskReminderTime = taskReminderTime,
                taskImageUrl = taskImageUrl,
                isCompleted = false,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ).toHashMap()

            taskCollection.document(taskId).set(taskModelData).await()
            Log.d(TAG, "Task created with ID: $taskId")
            Pair(true, taskId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error creating task: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun updateTask(
        taskId: String,
        taskTitle: String,
        taskBody: String,
        taskPriority: Int,
        taskDueDate: String?,
        taskDueHours: String?,
        taskDueTime: String?,
        taskReminderTime: String?,
        taskImageUrl: String?
    ) : Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User has not logged in")
            val taskRef = taskCollection.document(taskId)
            val taskSnapshot = taskRef.get().await()

            if (taskSnapshot.exists() && taskSnapshot.getString("userId") == userId) {
                taskRef.update(
                    mapOf(
                        "taskTitle" to taskTitle,
                        "taskBody" to taskBody,
                        "taskPriority" to taskPriority,
                        "taskDueDate" to taskDueDate,
                        "taskDueHours" to taskDueHours,
                        "taskDueTime" to taskDueTime,
                        "taskReminderTime" to taskReminderTime,
                        "taskImageUrl" to taskImageUrl,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
                Log.d(TAG, "Task updated with ID: $taskId")

                Pair(true, "Task successfully updated")
            } else {
                Log.e(TAG, "Task not found or not owned by the user")
                Pair(false, "Task not found or not owned by the user")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun updateCompletionStatus(
        taskId: String,
        isCompleted: Boolean
    ): Pair<Boolean, String?> {
        return try {
            val taskRef = taskCollection.document(taskId)

            taskRef.update(
                mapOf(
                    "isCompleted" to isCompleted,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            Log.d(TAG, "Task completion status updated with ID: $taskId")
            Pair(true, "Status successfully updated")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun deleteTask(
        taskId: String
    ) : Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User has not logged in")
            val taskRef = taskCollection.document(taskId)
            val taskSnapshot = taskRef.get().await()

            if (taskSnapshot.exists() && taskSnapshot.getString("userId") == userId) {
                taskRef.delete().await()
            } else {
                return Pair(false, "You don't have permission to delete this task")
            }

            Log.d(TAG, "Task deleted with ID: $taskId")
            Pair(true, "Task successfully deleted")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error deleting task: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun getTaskById(taskId: String): TaskWithUser? {
        return try {
            val taskSnapshot = taskCollection.document(taskId).get().await()
            val task = taskSnapshot.toObject(Task::class.java) ?: return null

            val userSnapshot = userCollection.document(task.userId).get().await()
            val user = userSnapshot.toObject(User::class.java) ?: return null

            TaskWithUser(task, user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching task by ID: ${e.message}")
            null
        }
    }

    suspend fun getUserTodayTask(resetPaging: Boolean = false): List<TaskWithUser> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            val userId = firebaseAuth.currentUser?.uid ?: return emptyList()
            val currentDate = getCurrentDate()

            if (isLastPage) return emptyList()

            var query: Query = taskCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("taskDueDate", currentDate)
                .orderBy("taskPriority", Query.Direction.DESCENDING)
                .orderBy("taskDueTime", Query.Direction.ASCENDING)
                .limit(PAGE_SIZE.toLong())

            // Jika ada lastDocument, gunakan startAfter untuk melanjutkan paging
            lastDocument?.let {
                query = query.startAfter(it)
            }

            // Ambil data tugas milik user
            val taskSnapshot = query.get().await()

            // Jika tidak ada data, langsung set isLastPage = true
            if (taskSnapshot.isEmpty) {
                isLastPage = true
                return emptyList()
            }

            val task = taskSnapshot.documents.mapNotNull { it.data?.toTask() }

            // Simpan dokumen terakhir untuk paginasi berikutnya
            lastDocument = taskSnapshot.documents.lastOrNull()

            // Cek apakah ini halaman terakhir
            if (taskSnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            // Ambil data user yang sedang login
            val userSnapshot = userCollection
                .document(userId)
                .get()
                .await()

            val userData = userSnapshot.toObject(User::class.java)

            if (userData == null) {
                Log.e(TAG, "User data not found for userId: $userId")
                return emptyList()
            }

            // Gabungkan tugas dengan data pengguna
            task.map { service -> TaskWithUser(service, userData) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user tasks: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserTask(resetPaging: Boolean = false): List<TaskWithUser> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            val userId = firebaseAuth.currentUser?.uid ?: return emptyList()

            if (isLastPage) return emptyList()

            var query: Query = taskCollection
                .whereEqualTo("userId", userId)
                .orderBy("taskPriority", Query.Direction.DESCENDING)
                .orderBy("taskDueTime", Query.Direction.ASCENDING)
                .limit(PAGE_SIZE.toLong())

            // Jika ada lastDocument, gunakan startAfter untuk melanjutkan paging
            lastDocument?.let {
                query = query.startAfter(it)
            }

            // Ambil data layanan milik user
            val taskSnapshot = query.get().await()
            val task = taskSnapshot.documents.mapNotNull { it.data?.toTask() }

            // Simpan dokumen terakhir untuk paginasi berikutnya
            lastDocument = taskSnapshot.documents.lastOrNull()

            // Cek apakah ini halaman terakhir
            if (taskSnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            // Ambil data user yang sedang login
            val userSnapshot = userCollection
                .document(userId)
                .get()
                .await()

            val userData = userSnapshot.toObject(User::class.java)

            if (userData == null) {
                Log.e(TAG, "User data not found for userId: $userId")
                return emptyList()
            }

            // Gabungkan tugas dengan data pengguna
            task.map { service -> TaskWithUser(service, userData) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user tasks: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserCompletedTask(resetPaging: Boolean = false): List<TaskWithUser> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            val userId = firebaseAuth.currentUser?.uid ?: return emptyList()

            if (isLastPage) return emptyList()

            var query: Query = taskCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", true)
                .orderBy("taskPriority", Query.Direction.DESCENDING)
                .orderBy("taskDueTime", Query.Direction.ASCENDING)
                .limit(PAGE_SIZE.toLong())

            // Jika ada lastDocument, gunakan startAfter untuk melanjutkan paging
            lastDocument?.let {
                query = query.startAfter(it)
            }

            // Ambil data tugas milik user
            val taskSnapshot = query.get().await()

            // Jika tidak ada data, langsung set isLastPage = true
            if (taskSnapshot.isEmpty) {
                isLastPage = true
                return emptyList()
            }

            val task = taskSnapshot.documents.mapNotNull { it.data?.toTask() }

            // Simpan dokumen terakhir untuk paginasi berikutnya
            lastDocument = taskSnapshot.documents.lastOrNull()

            // Cek apakah ini halaman terakhir
            if (taskSnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            // Ambil data user yang sedang login
            val userSnapshot = userCollection
                .document(userId)
                .get()
                .await()

            val userData = userSnapshot.toObject(User::class.java)

            if (userData == null) {
                Log.e(TAG, "User data not found for userId: $userId")
                return emptyList()
            }

            // Gabungkan tugas dengan data pengguna
            task.map { service -> TaskWithUser(service, userData) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user tasks: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserInProgressTask(resetPaging: Boolean = false): List<TaskWithUser> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            val userId = firebaseAuth.currentUser?.uid ?: return emptyList()

            if (isLastPage) return emptyList()

            var query: Query = taskCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", false)
                .orderBy("taskPriority", Query.Direction.DESCENDING)
                .orderBy("taskDueTime", Query.Direction.ASCENDING)
                .limit(PAGE_SIZE.toLong())

            // Jika ada lastDocument, gunakan startAfter untuk melanjutkan paging
            lastDocument?.let {
                query = query.startAfter(it)
            }

            // Ambil data tugas milik user
            val taskSnapshot = query.get().await()

            // Jika tidak ada data, langsung set isLastPage = true
            if (taskSnapshot.isEmpty) {
                isLastPage = true
                return emptyList()
            }

            val task = taskSnapshot.documents.mapNotNull { it.data?.toTask() }

            // Simpan dokumen terakhir untuk paginasi berikutnya
            lastDocument = taskSnapshot.documents.lastOrNull()

            // Cek apakah ini halaman terakhir
            if (taskSnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            // Ambil data user yang sedang login
            val userSnapshot = userCollection
                .document(userId)
                .get()
                .await()

            val userData = userSnapshot.toObject(User::class.java)

            if (userData == null) {
                Log.e(TAG, "User data not found for userId: $userId")
                return emptyList()
            }

            // Gabungkan tugas dengan data pengguna
            task.map { service -> TaskWithUser(service, userData) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user tasks: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserTaskByDate(resetPaging: Boolean = false, date: String): List<TaskWithUser> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            val userId = firebaseAuth.currentUser?.uid ?: return emptyList()

            if (isLastPage) return emptyList()

            var query: Query = taskCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("taskDueDate", date)
                .orderBy("taskPriority", Query.Direction.DESCENDING)
                .orderBy("taskDueTime", Query.Direction.ASCENDING)
                .limit(PAGE_SIZE.toLong())

            // Jika ada lastDocument, gunakan startAfter untuk melanjutkan paging
            lastDocument?.let {
                query = query.startAfter(it)
            }

            // Ambil data tugas milik user
            val taskSnapshot = query.get().await()

            // Jika tidak ada data, langsung set isLastPage = true
            if (taskSnapshot.isEmpty) {
                isLastPage = true
                return emptyList()
            }

            val task = taskSnapshot.documents.mapNotNull { it.data?.toTask() }

            // Simpan dokumen terakhir untuk paginasi berikutnya
            lastDocument = taskSnapshot.documents.lastOrNull()

            // Cek apakah ini halaman terakhir
            if (taskSnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            // Ambil data user yang sedang login
            val userSnapshot = userCollection
                .document(userId)
                .get()
                .await()

            val userData = userSnapshot.toObject(User::class.java)

            if (userData == null) {
                Log.e(TAG, "User data not found for userId: $userId")
                return emptyList()
            }

            // Gabungkan tugas dengan data pengguna
            task.map { service -> TaskWithUser(service, userData) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user tasks: ${e.message}")
            emptyList()
        }
    }

    fun isLastPage(): Boolean {
        return isLastPage
    }

    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter)
    }

    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.", ignoreCase = true) == true ->
                "Network error, please check your internet connection"
            else -> e.message ?: "Unknown error occurred"
        }
    }
}