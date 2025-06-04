package com.project.focuslist.ui.profile

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
import com.project.focuslist.data.utils.UserViewModelFactory
import com.project.focuslist.data.viewmodel.StorageViewModel
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private val userViewModel by viewModels<UserViewModel>(
        factoryProducer = { UserViewModelFactory(applicationContext) }
    )
    private val storageViewModel by viewModels<StorageViewModel>()

    private var imageUri: Uri? = null
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
                val oldImageUrl = userViewModel.userImageUrl.value ?: ""
                Log.d(TAG, "Old Image URL: $oldImageUrl")
                Log.d(TAG, "Current Image URI: $imageUri")

                if (username.isEmpty()) {
                    showToast("Username cannot be empty")
                    return@setOnClickListener
                }

                if (imageUri != null) {
                    if (!oldImageUrl.isEmpty()) {
                        // Update new profile picture
                        uploadImageToSupabase(imageUri!!) { imageUrl ->
                            deleteOldProfilePicture(oldImageUrl)
                            userViewModel.updateProfile(username, imageUrl)
                        }
                    } else {
                        // Upload new profile picture
                        uploadImageToSupabase(imageUri!!) { imageUrl ->
                            userViewModel.updateProfile(username, imageUrl)
                        }
                    }
                } else {
                    userViewModel.updateProfile(username, oldImageUrl)
                }

                binding.progressBar.visibility = View.VISIBLE
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
            operationUpdateStatus.observe(this@EditProfileActivity) { (success, message) ->
                binding.progressBar.visibility = View.GONE

                if (success) {
                    showToast("Profile successfully updated")
                    Log.d(TAG, "Profile successfully updated")
                    finish()
                } else {
                    showToast("Error Occured")
                    Log.e(TAG, "Error Occured: $message")
                }
            }

            userImageUrl.observe(this@EditProfileActivity) { oldImageUrl ->
                Glide.with(this@EditProfileActivity)
                    .load(oldImageUrl)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(binding.ivImageEdit)
            }

            userName.observe(this@EditProfileActivity) { binding.tietUsernameEdit.setText(it) }
        }

        storageViewModel.apply {
            uploadStatus.observe(this@EditProfileActivity) { status ->
                if (!status) {
                    Log.d(TAG, "Failed to upload image")
                    showToast("Failed to upload image")
                } else {
                    Log.d(TAG, "Image uploaded successfully")
                }
            }

            deleteStatus.observe(this@EditProfileActivity) { status ->
                if (status) {
                    Log.d(TAG, "Image deleted successfully")
                }
            }
        }
    }

    private fun uploadImageToSupabase(uri: Uri, onSuccess: (String) -> Unit) {
        val inputStream = contentResolver.openInputStream(uri) ?: return showToast("Failed to read file")
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val byteArray = inputStream.use { it.readBytes() }

        storageViewModel.uploadFile(byteArray, "profile_pictures", fileName)

        storageViewModel.imageUrl.observeOnce(this) { imageUrl ->
            binding.progressBar.visibility = View.GONE
            if (!imageUrl.isNullOrEmpty()) {
                onSuccess(imageUrl)
            } else {
                showToast("Failed to upload image")
            }
        }
    }

    private fun deleteOldProfilePicture(oldImageUrl: String) {
        if (oldImageUrl.isNotEmpty()) {
            val fileName = oldImageUrl.substringAfterLast("/")
            storageViewModel.deleteFile(fileName, "profile_pictures")
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