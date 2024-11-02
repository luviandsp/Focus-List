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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.project.focuslist.R
import com.project.focuslist.data.model.User
import com.project.focuslist.databinding.ActivityEditProfileBinding
import com.project.focuslist.ui.activity.MainActivity
import com.project.focuslist.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

//TODO: Belum diimplementasikan
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel by viewModels<AuthViewModel>()
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

        initViews()
    }

    private fun initViews() {
        with (binding) {
            val isEdit = intent.getStringExtra(INTENT_KEY) == EDIT_KEY
            val userId = intent.getIntExtra(INTENT_KEY_USER_ID, -1)

            if (isEdit) {
                lifecycleScope.launch {
                    viewModel.getUserById(userId).observe(this@EditProfileActivity) { userData ->
                        userData?.let {
                            binding.tietUsernameEdit.setText(it.username)
                            Glide.with(this@EditProfileActivity).load(it.profileImage).into(binding.ivImageEdit)
                            loadedImage = it.profileImage
                        }
                    }
                }
            }

            btnEdit.setOnClickListener {
                lifecycleScope.launch {
                    val username = tietUsernameEdit.text.toString()

                    // Ambil data pengguna
                    val userData = viewModel.getUserById(userId).value
                    userData?.let {
                        // Perbarui objek User dengan data terbaru
                        val updatedUser = User(
                            userId,
                            username,
                            password = it.password,
                            loadedImage // Sertakan gambar profil jika ada
                        )

                        // Perbarui pengguna di database
                        viewModel.updateUser(updatedUser)

                        // Tutup aktivitas setelah pembaruan
                        finish()
                    } ?: run {
                        // Tindakan jika userData tidak ditemukan, jika perlu
                        Snackbar.make(binding.root, "User tidak ditemukan.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            ivBack.setOnClickListener {
                val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
                startActivity(intent)
            }

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

    companion object {
        const val EDIT_KEY = "EDIT"
        const val CREATE_KEY = "CREATE"
        const val INTENT_KEY = "EDIT_OR_CREATE"
        const val INTENT_KEY_USER_ID = "USER_ID"
    }
}
