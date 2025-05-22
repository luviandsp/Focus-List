package com.project.focuslist.data.repository

import com.project.focuslist.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class StorageRepository {
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Storage) {
            transferTimeout = 120.seconds
        }
    }

    suspend fun uploadFileToSupabase(file: ByteArray, folderName : String, fileName: String): String? {
        return try {
            val storage = supabase.storage.from("focuslist")
            val filePath = "$folderName/$fileName"

            withContext(Dispatchers.IO) {
                storage.upload(filePath, file) {
                    upsert = false
                }
            }

            val fileUrl = storage.publicUrl(filePath)
            println("File uploaded: $fileUrl")
            fileUrl
        } catch (e: Exception) {
            println("Upload failed: ${e.message}")
            null
        }
    }

    suspend fun deleteFileOnSupabase(filename: String, folderName: String): Boolean {
        return try {
            val filePath = "$folderName/$filename"

            val storage = supabase.storage.from("focuslist")
            storage.delete(filePath)
            println("File deleted successfully")
            true
        } catch (e: Exception) {
            println("Delete failed: ${e.message}")
            false
        }
    }
}