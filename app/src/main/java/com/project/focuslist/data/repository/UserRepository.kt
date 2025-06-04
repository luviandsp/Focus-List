package com.project.focuslist.data.repository

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.project.focuslist.data.model.User
import com.project.focuslist.data.preferences.UserTempPreferences
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class UserRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "UserRepository"
    }

    private val userCollection = firestore.collection("users")
    private val taskCollection = firestore.collection("tasks")

    private fun User.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "userId" to userId,
            "email" to email,
            "username" to username,
            "profileImageUrl" to profileImageUrl,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    private fun Map<String, Any>.toUser(): User {
        return User(
            userId = this["userId"] as String,
            email = this["email"] as String,
            username = this["username"] as String,
            profileImageUrl = this["profileImageUrl"] as? String,
            createdAt = this["createdAt"] as Timestamp,
            updatedAt = this["updatedAt"] as Timestamp
        )
    }

    // Fungsi untuk membuat akun baru
    suspend fun registerAccountOnly(email: String, password: String): Pair<Boolean, String?> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.sendEmailVerification()
            Pair(true, "Please check your email to verify your account")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk mengirim ulang email verifikasi
    suspend fun resendVerificationEmail(): Pair<Boolean, String?> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.reload()

            if (user != null && user.isEmailVerified) {
                return Pair(false, "Your account is already verified")
            }

            user?.sendEmailVerification()?.await()
            Log.d("Auth", "Verification email sent to ${user?.email}")

            Pair(true, "Please check your email to verify your account")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk menyelesaikan registrasi akun
    suspend fun completeUserRegistration(context: Context): Pair<Boolean, String?> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.reload()
            user?.getIdToken(true)?.await()

            val refreshedUser = FirebaseAuth.getInstance().currentUser

            if (refreshedUser != null && refreshedUser.isEmailVerified) {
                val userDocRef = userCollection.document(refreshedUser.uid)
                val existingUser = userDocRef.get().await()

                if (existingUser.exists()) {
                    return Pair(true, null)
                }

                val prefs = UserTempPreferences(context)
                val tempUser = prefs.getTempUser() ?: return Pair(false, "User data not found")

                val userModelData = User(
                    userId = refreshedUser.uid,
                    email = refreshedUser.email ?: "",
                    username = tempUser.username,
                    profileImageUrl = null,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                ).toHashMap()

                userDocRef.set(userModelData).await()
                prefs.clearTempUser()

                return Pair(true, "Your account has been created")
            } else {
                return Pair(false, "Your account is not verified")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk login
    suspend fun login(email: String, password: String): Pair<Boolean, String?> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "User logged in with email: $email")
            Pair(true, null)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error logging in user: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk mendapatkan data pengguna dari Firestore
    suspend fun getUserData(): User? {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return null
            val userSnapshot = userCollection.document(userId).get().await()

            if (userSnapshot.exists()) {
                userSnapshot.toObject(User::class.java)
            } else {
                null
            }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error getting user data: ${e.message}")
            null
        }
    }

    // Fungsi untuk mengubah informasi pengguna
    suspend fun updateProfile(username: String, profileImageUrl: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User has not logged in")
            val userRef = userCollection.document(userId)

            userRef.update(
                mapOf(
                    "username" to username,
                    "profileImageUrl" to profileImageUrl,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            Log.d(TAG, "User profile updated with ID: $userId")
            Pair(true, "Profile successfully updated")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error updating user profile: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk mengubah password
    suspend fun forgotPassword(email: String): Pair<Boolean, String?> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to $email")
            Pair(true, "Please check your email to reset your password")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error sending password reset email: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk logout
    fun logout() {
        firebaseAuth.signOut()
        firestore.clearPersistence()
        if (firebaseAuth.currentUser == null) {
            Log.d(TAG, "User logged out")
        } else {
            Log.e(TAG, "Error logging out user")
        }
    }

    // Fungsi untuk menghapus akun
    suspend fun deleteAccountWithReauth(email: String, password: String, context: Context): Pair<Boolean, String?> {
        return try {
            val user = firebaseAuth.currentUser ?: return Pair(false, "User is not logged in")

            // Re-authenticate dulu
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()

            // Hapus data Firestore
            val userId = user.uid

            val tasksSnapshot = taskCollection.whereEqualTo("userId", userId).get().await()
            val taskIdsToDelete = mutableListOf<String>()

            for (doc in tasksSnapshot.documents) {
                val taskId = doc.id
                taskIdsToDelete.add(taskId)
            }

            for (doc in tasksSnapshot.documents) {
                doc.reference.delete().await()
            }

            for (taskId in taskIdsToDelete) {
                WorkManager.getInstance(context).cancelUniqueWork("reminder_$taskId")
                Log.d("UserRepository", "Cancelled notification for task: $taskId during account deletion")
            }

            // Hapus dokumen user
            userCollection.document(userId).delete().await()

            // Hapus akun Auth
            user.delete().await()

            Pair(true, "Your account has been deleted")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Pair(false, "Invalid credentials. Please check your email/password.")
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Pair(false, "Please login again before deleting your account")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk menangani error Firebase
    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("email address is badly formatted", ignoreCase = true) == true ->
                "Email address is badly formatted"
            e.message?.contains("password is invalid or the user does not have a password", ignoreCase = true) == true ->
                "Password is invalid or the user does not have a password"
            e.message?.contains("There is no user record corresponding to this identifier", ignoreCase = true) == true ->
                "Email has not been registered"
            e.message?.contains("The email address is already in use", ignoreCase = true) == true ->
                "Email address is already in use"
            e.message?.contains("Password should be at least 8 characters", ignoreCase = true) == true ->
                "Password should be at least 8 characters"
            e.message?.contains("The supplied auth credential is incorrect, malformed or has expired.", ignoreCase = true) == true ->
                "Email address or password is incorrect"
            e.message?.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.", ignoreCase = true) == true ->
                "Network error, please check your connection"
            e.message?.contains("PERMISSION_DENIED: Missing or insufficient permissions.", ignoreCase = true) == true ->
                "You don't have permission to access this feature"
            e.message?.contains("We have blocked all requests from this device due to unusual activity. Try again later.", ignoreCase = true) == true ->
                "Too many requests, please try again later"
            else -> e.message ?: "Unknown error occurred"
        }
    }
}