package com.project.focuslist.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.project.focuslist.data.model.User
import com.project.focuslist.data.preferences.UserAccountPreferences
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class UserRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "UserRepository"
    }

    private val userCollection = firestore.collection("users")

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
            Pair(true, "Silakan verifikasi email Anda sebelum login.")
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
                return Pair(false, "Email sudah diverifikasi")
            }

            user?.sendEmailVerification()?.await()
            Log.d("Auth", "Verification email sent to ${user?.email}")

            Pair(true, "Silakan cek email Anda untuk verifikasi akun Anda.")
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

                val prefs = UserAccountPreferences(context)
                val tempUser = prefs.getTempUser() ?: return Pair(false, "Data user tidak ditemukan")

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

                return Pair(true, "Akun berhasil dibuat sepenuhnya!")
            } else {
                return Pair(false, "Akun belum terverifikasi")
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

    fun getUserId(): String? {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return null
            userId
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error getting user ID: ${e.message}")
            null
        }
    }

    // Fungsi untuk mengubah informasi pengguna
    suspend fun updateProfile(username: String, profileImageUrl: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = userCollection.document(userId)

            userRef.update(
                mapOf(
                    "username" to username,
                    "profileImageUrl" to profileImageUrl,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            Log.d(TAG, "User profile updated with ID: $userId")
            Pair(true, "Profil berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error updating user profile: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk mengubah foto profil
    suspend fun updateProfileImageUrl(imageUrl: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = userCollection.document(userId)

            userRef.update(
                mapOf(
                    "profileImageUrl" to imageUrl,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            Log.d(TAG, "User profile image URL updated with ID: $userId")
            Pair(true, "URL foto profil berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            val errorMessage = getErrorMessage(e)
            Log.e(TAG, "Error updating profile image URL: $errorMessage")

            Pair(false, errorMessage)
        }
    }

    // Fungsi untuk mengubah password
    suspend fun forgotPassword(email: String): Pair<Boolean, String?> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to $email")
            Pair(true, "Silakan cek email Anda untuk reset password")
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
    suspend fun deleteAccount(): Pair<Boolean, String?> {
        return try {
            firebaseAuth.currentUser?.delete()?.await()
            Pair(true, "Akun berhasil dihapus")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (e is FirebaseAuthRecentLoginRequiredException) {
                return Pair(false, "Harap login ulang untuk menghapus akun")
            }
            return Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk menangani error Firebase
    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("email address is badly formatted", ignoreCase = true) == true ->
                "Format email tidak valid"
            e.message?.contains("password is invalid or the user does not have a password", ignoreCase = true) == true ->
                "Password salah atau akun tidak memiliki password"
            e.message?.contains("There is no user record corresponding to this identifier", ignoreCase = true) == true ->
                "Email belum terdaftar"
            e.message?.contains("The email address is already in use", ignoreCase = true) == true ->
                "Email sudah digunakan"
            e.message?.contains("Password should be at least 8 characters", ignoreCase = true) == true ->
                "Password minimal harus 8 karakter"
            e.message?.contains("The supplied auth credential is incorrect, malformed or has expired.", ignoreCase = true) == true ->
                "Email atau password salah"
            e.message?.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.", ignoreCase = true) == true ->
                "Terjadi kesalahan jaringan, coba lagi nanti"
            e.message?.contains("PERMISSION_DENIED: Missing or insufficient permissions.", ignoreCase = true) == true ->
                "Anda tidak memiliki izin untuk melakukan ini"
            e.message?.contains("We have blocked all requests from this device due to unusual activity. Try again later.", ignoreCase = true) == true ->
                "Terlalu banyak permintaan, coba lagi nanti"
            else -> e.message ?: "Terjadi kesalahan, coba lagi nanti"
        }
    }
}