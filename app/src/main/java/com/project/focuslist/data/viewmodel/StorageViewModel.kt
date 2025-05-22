package com.project.focuslist.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.focuslist.data.repository.StorageRepository
import kotlinx.coroutines.launch

class StorageViewModel : ViewModel() {

    private val storageRepository = StorageRepository()

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> get() = _imageUrl

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> get() = _deleteStatus

    fun uploadFile(file: ByteArray, folderName : String, fileName: String) {
        viewModelScope.launch {
            val url = storageRepository.uploadFileToSupabase(file, folderName, fileName)
            if (url != null) {
                _imageUrl.postValue(url)
                _uploadStatus.postValue(true)
            } else {
                _uploadStatus.postValue(false)
            }
        }
    }

    fun deleteFile(filename: String, folderName: String) {
        viewModelScope.launch {
            val success = storageRepository.deleteFileOnSupabase(filename, folderName)
            _deleteStatus.postValue(success)
            _imageUrl.postValue(null)
        }
    }
}