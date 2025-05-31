package com.project.focuslist.ui.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.project.focuslist.R
import com.project.focuslist.data.viewmodel.StorageViewModel
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private val userViewModel by viewModels<UserViewModel>()
    private val storageViewModel by viewModels<StorageViewModel>()

    private var imageUri: Uri? = null
    private var oldImageUrl: String? = null
    private var isPhotoDeleted: Boolean = false

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivImageEdit)

                imageUri = uri
                isPhotoDeleted = false
            }
        }
    }

    companion object {
        const val RESULT_CODE = 110
        const val TAG = "EditProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            btnEdit.setOnClickListener {
                val username = tietUsernameEdit.text.toString().trim()
                oldImageUrl = userViewModel.userImageUrl.value ?: ""

                if (username.isEmpty()) {
                    showToast("Username Tidak Boleh Kosong")
                    return@setOnClickListener
                }

                if (imageUri != null) {
                    if (!oldImageUrl.isNullOrEmpty()) {
                        // Update Foto Profil Baru
                        userViewModel.updateProfile(username, "")

                        uploadImageToSupabase(imageUri!!) { imageUrl ->
                            userViewModel.updateProfile(username, imageUrl)
                            deleteProfilePhoto(oldImageUrl)
                        }
                    } else {
                        // Upload Foto Profil Baru
                        uploadImageToSupabase(imageUri!!) { imageUrl ->
                            userViewModel.updateProfile(username, imageUrl)
                        }
                    }
                } else {
                    userViewModel.updateProfile(username, oldImageUrl ?: "")
                }
            }

            toolbar.setNavigationOnClickListener { finish() }

            ivImageEdit.setOnClickListener {
                launcher.launch(
                    ImagePicker.Companion.with(this@EditProfileActivity)
                        .crop()
                        .cropOval()
                        .galleryOnly()
                        .createIntent()
                )
            }
        }
    }

    private fun observeViewModels() {
        userViewModel.apply {
            operationStatus.observe(this@EditProfileActivity) { (success, message) ->
                binding.progressBar.visibility = View.GONE
                if (success) {
                    showToast(message ?: "Profile berhasil diperbarui")
                    Log.d(TAG, "Profile berhasil diperbarui")

                    setResult(RESULT_CODE)
                    finish()
                } else {
                    showToast(message ?: "Terjadi kesalahan")
                    Log.e(TAG, "Terjadi kesalahan: $message")
                }
            }

            userImageUrl.observe(this@EditProfileActivity) { oldImageUrl ->
                updateProfilePicture(oldImageUrl)
                Log.d(TAG, "Old Image URL: $oldImageUrl")
            }

            userName.observe(this@EditProfileActivity) { binding.tietUsernameEdit.setText(it) }
        }

        storageViewModel.apply {
            uploadStatus.observe(this@EditProfileActivity) { if (it) showToast("Foto profil berhasil diperbarui") }
            deleteStatus.observe(this@EditProfileActivity) { status ->
                if (status) {
                    showToast("Foto profil berhasil dihapus")
                    isPhotoDeleted = true

                    setResult(RESULT_CODE)
                    finish()
                }
            }

            imageUrl.observeOnce(this@EditProfileActivity) { newImageUrl ->
                if (!newImageUrl.isNullOrEmpty()) {
                    userViewModel.updateProfileImageUrl(newImageUrl)
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateProfilePicture(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(R.drawable.baseline_account_circle_24)
                .circleCrop()
                .into(binding.ivImageEdit)
        } else {
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .into(binding.ivImageEdit)
        }
    }

    private fun uploadImageToSupabase(uri: Uri, onSuccess: (String) -> Unit) {
        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            showToast("Gagal membaca file")
            return
        }

        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        binding.progressBar.visibility = View.VISIBLE

        val byteArray = inputStream.use { it.readBytes() }

        storageViewModel.uploadFile(byteArray, "profile_pictures", fileName)

        saveToFirestore(onSuccess)
    }

    private fun deleteProfilePhoto(currentImageUrl : String? = null) {
        if (!currentImageUrl.isNullOrEmpty()) {
            val fileName = currentImageUrl.substringAfterLast("/")
            Log.d(TAG, "File Name: $fileName")
            storageViewModel.deleteFile(fileName, "profile_pictures")
        } else {
            showToast("Foto profil tidak ditemukan")
        }
    }

    private fun saveToFirestore(onSuccess: (String) -> Unit) {
        storageViewModel.imageUrl.observeOnce(this) { imageUrl ->
            if (!imageUrl.isNullOrEmpty()) {
                userViewModel.updateProfileImageUrl(imageUrl)
                binding.progressBar.visibility = View.GONE
                onSuccess(imageUrl)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer(value)
                removeObserver(this)
            }
        })
    }
}