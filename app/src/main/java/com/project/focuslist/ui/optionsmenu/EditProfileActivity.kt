package com.project.focuslist.ui.optionsmenu

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.project.focuslist.R
import com.project.focuslist.data.model.User
import com.project.focuslist.databinding.ActivityEditProfileBinding
import com.project.focuslist.ui.activity.MainActivity
import com.project.focuslist.ui.viewmodel.AuthViewModel
import com.project.focuslist.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel by viewModels<AuthViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()
    private var loadedImage: ByteArray? = null
    private var isClicked = false

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
    }

    override fun onStart() {
        super.onStart()
        initViews()
    }

    private fun initViews() {
        with(binding) {
            lifecycleScope.launch {
                val username = loginViewModel.getProfileUsername()

                viewModel.getUserByUsername(username.toString()).observe(this@EditProfileActivity) { userData ->
                    userData?.let {
                        tietUsernameEdit.setText(it.username)
                        Glide.with(this@EditProfileActivity).load(it.profileImage?: R.drawable.baseline_account_circle_24).into(ivImageEdit)
                        loadedImage = it.profileImage
                    }
                }

                btnEdit.setOnClickListener {
                    lifecycleScope.launch {
                        val userData = viewModel.getUserByUsername(username.toString()).asFlow().firstOrNull()
                        userData?.let {
                            val updatedUsername = tietUsernameEdit.text.toString()
                            if (updatedUsername.isBlank()) {
                                Snackbar.make(root, "Username tidak boleh kosong.", Snackbar.LENGTH_SHORT).show()
                                return@let
                            }

                            val updatedUser = User(
                                userId = it.userId,
                                username = updatedUsername,
                                password = it.password,
                                profileImage = loadedImage
                            )

                            viewModel.updateUser(updatedUser)
                            loginViewModel.setProfileUsername(updatedUsername)
                            Snackbar.make(root, "Profil berhasil diperbarui.", Snackbar.LENGTH_SHORT).show()

                            val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } ?: run {
                            Snackbar.make(root, "User tidak ditemukan.", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            ivBack.setOnClickListener { finish() }
            ivImageEdit.setOnClickListener { openGallery() }
        }
    }


    private fun openGallery() {
        if (!isClicked) {
            isClicked = true
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Image"), 1)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            try {

                // Ambil ukuran file dalam satuan byte
                val inputStream: InputStream = imageUri?.let { contentResolver.openInputStream(it) }!!
                val fileSizeInBytes = inputStream.available()
                inputStream.close()

                // Konversikan ke MegaBytes
                val fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0)

                // Ambil bitmap serta periksa apakah perlu dirotasi
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap = rotateImageIfRequired(bitmap, imageUri)

                // Periksa ukuran file
                if (fileSizeInMB > 2.0) {

                    // Jika masih terlalu besar, lakukan kompresi
                    val outputStream = ByteArrayOutputStream()
                    var quality = 90 // Initial quality
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

                    // Turunkan kualitas hingga di bawah 2 MB
                    while (outputStream.toByteArray().size > 2 * 1024 * 1024) {
                        outputStream.reset()
                        quality -= 10
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    }

                    // Konversikan dalam bentuk ByteArray lalu tampilkan gambar hasil kompresi
                    loadedImage = outputStream.toByteArray()
                    binding.apply {
                        Glide.with(this@EditProfileActivity).load(loadedImage).into(ivImageEdit)
                    }
                } else {

                    // Gunakan gambar asli, ubah jadi ByteArray, lalu tampilkan
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    loadedImage = stream.toByteArray()
                    binding.apply {
                        Glide.with(this@EditProfileActivity).load(loadedImage).into(ivImageEdit)
                    }
                }
                isClicked = false
            } catch (e: IOException) {
                val snackbar = Snackbar.make(
                    binding.root,
                    "Gagal Mengambil Foto.",
                    Snackbar.LENGTH_SHORT
                )
                snackbar.show()
                isClicked = false
            }
        }
        isClicked = false
    }

    @Throws(IOException::class)
    private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap {
        val input: InputStream? = contentResolver.openInputStream(selectedImage)
        val ei = ExifInterface(input!!)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }
}
