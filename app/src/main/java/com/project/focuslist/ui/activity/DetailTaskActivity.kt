package com.project.focuslist.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.project.focuslist.R
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.ActivityDetailTaskBinding
import com.project.focuslist.ui.viewmodel.TaskViewModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaskBinding
    private val viewModel by viewModels<TaskViewModel>()
    private var loadedImage: ByteArray? = null
    private var isClicked = false
    private var selectedDueDate: String? = null
    private var taskData: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailTaskBinding.inflate(layoutInflater)
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
            val isEdit = intent.getStringExtra(INTENT_KEY) == EDIT_KEY
            val taskId = intent.getIntExtra(INTENT_KEY_TASK_ID, -1)
            val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            if (isEdit) {
                viewModel.getTaskById(taskId).observe(this@DetailTaskActivity) { task ->
                    task?.let {
                        taskData = it
                        tietTitle.setText(it.title)
                        tietBody.setText(it.body)
                        spinnerPriority.setSelection(it.priority -1)
                        tvSelectDate.text = it.dueDate
                        if (task.taskImage != null) {
                            Glide.with(this@DetailTaskActivity).load(task.taskImage).into(ivImage)
                        } else {
                            ivImage.visibility = View.GONE
                        }
                        loadedImage = it.taskImage
                    }
                }
            }

            val itemLayout = if (isDarkMode) R.layout.spinner_item_dark else R.layout.spinner_item
            val priorities = arrayOf("Rendah", "Sedang", "Tinggi")
            val adapter = ArrayAdapter(this@DetailTaskActivity, itemLayout, priorities)
            adapter.setDropDownViewResource(itemLayout)
            spinnerPriority.adapter = adapter

            btnDatePicker.setOnClickListener {
                showDatePicker()
            }

            btnSave.setOnClickListener {
                saveTask(isEdit, taskId)
            }

            ivDelete.setOnClickListener {
                if (isEdit) {
                    viewModel.deleteTask(Task(taskId, "", "", false, 0, null, null))
                    finish()
                }
            }

            ivBack.setOnClickListener {
                val intent = Intent(this@DetailTaskActivity, MainActivity::class.java)
                startActivity(intent)
            }

            tvTitle.text = if (isEdit) "Edit Task" else "Create Task"
            ivImageInsert.setOnClickListener { openGallery() }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            val day = String.format("%02d", selectedDayOfMonth)
            val month = String.format("%02d", selectedMonth + 1)
            selectedDueDate = "$day/$month/$selectedYear"
            binding.tvSelectDate.text = selectedDueDate
        }, year, month, dayOfMonth)

        datePickerDialog.show()
    }

    private fun saveTask(isEdit: Boolean, taskId: Int) {
        val title = binding.tietTitle.text.toString().ifEmpty { "Tanpa Judul" }
        val body = binding.tietBody.text.toString().ifEmpty { "Tanpa Isi" }
        val dueDate = binding.tvSelectDate.text.toString()
        val selectedPriority = binding.spinnerPriority.selectedItem.toString()

        val priorityMap = mapOf(
            "Rendah" to 1,
            "Sedang" to 2,
            "Tinggi" to 3
        )

        val priorityValue = priorityMap[selectedPriority] ?: 0

        if (isEdit) {
            taskData?.let {
                viewModel.updateTask(
                    Task(
                        taskId,
                        title,
                        body,
                        it.isCompleted,
                        priorityValue,
                        dueDate,
                        loadedImage
                    )
                )
            }
        } else {
            viewModel.createTask(Task(0, title, body, false, priorityValue, dueDate, loadedImage))
        }
        finish()
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
                        Glide.with(this@DetailTaskActivity).load(loadedImage).into(ivImage)
                    }
                } else {

                    // Gunakan gambar asli, ubah jadi ByteArray, lalu tampilkan
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    loadedImage = stream.toByteArray()
                    binding.apply {
                        Glide.with(this@DetailTaskActivity).load(loadedImage).into(ivImage)
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
        const val INTENT_KEY_TASK_ID = "TASK_ID"
    }
}
