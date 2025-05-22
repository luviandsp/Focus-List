package com.project.focuslist.ui.activity

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
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
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.databinding.ActivityDetailTaskBinding

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaskBinding
    private val taskViewModel by viewModels<TaskViewModel>()
    private val storageViewModel by viewModels<StorageViewModel>()

    private var selectedDueDate: String? = null

    private var imageUri: Uri? = null
    private var taskId: String? = null

    companion object {
        private const val TAG = "DetailTaskActivity"
        const val TASK_ID = "task_id"
        const val EDIT_KEY = "EDIT"
        const val CREATE_KEY = "CREATE"
        const val INTENT_KEY = "EDIT_OR_CREATE"
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                Glide.with(this@DetailTaskActivity).load(uri).into(binding.ivImage)

                imageUri = uri
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        taskId = intent.getStringExtra(TASK_ID)
        Log.d(TAG, "Task ID: $taskId")

        if (taskId != null) {
            taskViewModel.getTaskById(taskId!!)
        } else {
            Log.d(TAG, "Task ID is null")
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            val isEdit = intent.getStringExtra(INTENT_KEY) == EDIT_KEY
            val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            tvTitle.text = if (isEdit) "Edit Task" else "Create Task"
            if (isEdit) {
                ivDelete.visibility = View.VISIBLE
            } else {
                ivDelete.visibility = View.GONE
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
                saveTask()
            }

            ivDelete.setOnClickListener {
                val oldTaskImageUrl = taskViewModel.taskImageUrl.value ?: ""

                if (taskId != null) {
                    taskViewModel.deleteTask(taskId!!)
                    deleteOldTaskImage(oldTaskImageUrl)
                    setResult(RESULT_OK)
                    finish()
                }
            }

            ivBack.setOnClickListener {
                finish()
            }

            ivImageInsert.setOnClickListener {
                launcher.launch(
                    ImagePicker.with(this@DetailTaskActivity)
                        .crop()
                        .galleryOnly()
                        .createIntent()
                )
            }

            ivCamera.setOnClickListener {
                launcher.launch(
                    ImagePicker.with(this@DetailTaskActivity)
                        .crop()
                        .cameraOnly()
                        .createIntent()
                )
            }
        }
    }

    private fun observeViewModels() {
        taskViewModel.apply {
            operationResult.observe(this@DetailTaskActivity) { (success, message) ->
                binding.progressBar.visibility = View.GONE
                if (success) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showToast(message ?: "Terjadi kesalahan")
                }
            }

            taskTitle.observe(this@DetailTaskActivity) { binding.tietTitle.setText(it) }
            taskBody.observe(this@DetailTaskActivity) { binding.tietBody.setText(it) }

            taskImageUrl.observe(this@DetailTaskActivity) { imageUrl ->
                if (imageUrl != null) {
                    Glide.with(this@DetailTaskActivity).load(imageUrl).into(binding.ivImage)
                }
            }

            taskDueDate.observe(this@DetailTaskActivity) { dueDate ->
                binding.tvSelectDate.text = dueDate
            }

            taskPriority.observe(this@DetailTaskActivity) { priority ->
                binding.spinnerPriority.setSelection(priority - 1)
            }
        }

        storageViewModel.uploadStatus.observe(this@DetailTaskActivity) { success ->
            if (success) showToast("Foto berhasil diunggah") else showToast("Gagal mengunggah foto")
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

    private fun saveTask() {
        val title = binding.tietTitle.text.toString().trim().ifEmpty { "Tanpa Judul" }
        val body = binding.tietBody.text.toString().trim().ifEmpty { "Tanpa Isi" }
        val dueDate = binding.tvSelectDate.text.toString()
        val selectedPriority = binding.spinnerPriority.selectedItem.toString()
        val oldTaskImageUrl = taskViewModel.taskImageUrl.value ?: ""

        val priorityMap = mapOf(
            "Rendah" to 1,
            "Sedang" to 2,
            "Tinggi" to 3
        )

        val priorityValue = priorityMap[selectedPriority] ?: 0

        if (imageUri != null) {
            uploadImageToSupabase(imageUri!!) { imageUrl ->
                if (taskId != null) {
                    taskViewModel.updateTask(
                        taskId = taskId!!,
                        taskTitle = title,
                        taskBody = body,
                        taskPriority = priorityValue,
                        taskDueDate = dueDate,
                        taskImageUrl = imageUrl
                    )
                    deleteOldTaskImage(oldImageUrl = oldTaskImageUrl)
                } else {
                    taskViewModel.createTask(
                        taskTitle = title,
                        taskBody = body,
                        taskPriority = priorityValue,
                        taskDueDate = dueDate,
                        taskImageUrl = imageUrl
                    )
                }
                setResult(RESULT_OK)
                finish()
            }
        } else {
            if (taskId != null) {
                taskViewModel.updateTask(
                    taskId = taskId!!,
                    taskTitle = title,
                    taskBody = body,
                    taskPriority = priorityValue,
                    taskDueDate = dueDate,
                    taskImageUrl = oldTaskImageUrl
                )
            } else {
                taskViewModel.createTask(
                    taskTitle = title,
                    taskBody = body,
                    taskPriority = priorityValue,
                    taskDueDate = dueDate,
                    taskImageUrl = oldTaskImageUrl
                )
            }
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun uploadImageToSupabase(uri: Uri, onSuccess: (String) -> Unit) {
        val inputStream = contentResolver.openInputStream(uri) ?: return showToast("Gagal membaca file")
        val fileName = "task_${System.currentTimeMillis()}.jpg"
        val byteArray = inputStream.use { it.readBytes() }

        storageViewModel.uploadFile(byteArray, "task_images", fileName)

        storageViewModel.imageUrl.observeOnce(this) { imageUrl ->
            binding.progressBar.visibility = View.GONE
            if (!imageUrl.isNullOrEmpty()) {
                onSuccess(imageUrl)
            } else {
                showToast("Gagal mengunggah gambar")
            }
        }
    }

    private fun deleteOldTaskImage(oldImageUrl: String) {
        if (oldImageUrl.isNotEmpty()) {
            val fileName = oldImageUrl.substringAfterLast("/")
            storageViewModel.deleteFile(fileName, "service_banner")
        }
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer(value)
                removeObserver(this)
            }
        })
    }
}
