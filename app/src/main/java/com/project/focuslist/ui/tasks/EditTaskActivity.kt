package com.project.focuslist.ui.tasks

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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.Timestamp
import com.project.focuslist.R
import com.project.focuslist.data.enumData.TaskPriority
import com.project.focuslist.data.model.TaskDraft
import com.project.focuslist.data.utils.UserViewModelFactory
import com.project.focuslist.data.viewmodel.StorageViewModel
import com.project.focuslist.data.viewmodel.TaskDraftViewModel
import com.project.focuslist.data.viewmodel.TaskViewModel
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.ActivityEditTaskBinding
import com.project.focuslist.databinding.DialogReminderBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding

    private val taskViewModel by viewModels<TaskViewModel>()
    private val userViewModel by viewModels<UserViewModel>(
        factoryProducer = { UserViewModelFactory(applicationContext) }
    )
    private val storageViewModel by viewModels<StorageViewModel>()
    private val taskDraftViewModel by viewModels<TaskDraftViewModel>()

    private var selectedDueDate: String? = null
    private var selectedDueHours: String? = null
    private var selectedDueTime: String? = null

    private var reminderDays: Int = 0
    private var reminderHours: Int = 0
    private var reminderMinutes: Int = 0
    private var formattedReminderTime: String? = null

    private var imageUri: Uri? = null
    private var taskId: String? = null
    private var taskDraftId: Int? = null

    private val priorityList = listOf(TaskPriority.LOW.name, TaskPriority.MID.name, TaskPriority.HIGH.name)
    private val priorityMap = mapOf(
        TaskPriority.LOW.name to 1,
        TaskPriority.MID.name to 2,
        TaskPriority.HIGH.name to 3
    )

    companion object {
        private const val TAG = "EditTaskActivity"
        const val TASK_DRAFT_ID = "task_draft_id"
        const val TASK_ID = "task_id"
        const val DEFAULT_INT = -1
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                Glide.with(this@EditTaskActivity).load(uri).into(binding.ivSelectedImage)

                imageUri = uri
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        taskId = intent.getStringExtra(TASK_ID)
        Log.d(TAG, "Task ID: $taskId")

        taskDraftId = intent.getIntExtra(TASK_DRAFT_ID, DEFAULT_INT)
        Log.d(TAG, "Task Draft ID: $taskDraftId")

        userViewModel.getUser()

        if (taskId != null) {
            taskViewModel.getTaskById(taskId!!)
        } else if (taskDraftId != -1) {
            lifecycleScope.launch {
                taskDraftViewModel.getTaskById(taskDraftId!!).observe(this@EditTaskActivity) { task ->
                    with(binding) {
                        tietActivity.setText(task.taskTitle)
                        tietDescription.setText(task.taskBody)
                        when (task.taskPriority) {
                            1 -> spinnerPriority.setText(TaskPriority.LOW.name, false)
                            2 -> spinnerPriority.setText(TaskPriority.MID.name, false)
                            3 -> spinnerPriority.setText(TaskPriority.HIGH.name, false)
                        }
                        tietDeadline.setText(task.taskDueDate)
                        Glide.with(this@EditTaskActivity).load(task.taskImageUrl).into(ivSelectedImage)

                        selectedDueDate = task.taskDueDate
                        selectedDueHours = task.taskDueHours
                        selectedDueTime = task.taskDueTime
                    }
                }
            }
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.set_reminder -> {
                        showReminder()
                        true
                    }
                    else -> false
                }
            }

            val spinnerAdapter = ArrayAdapter(this@EditTaskActivity, android.R.layout.simple_dropdown_item_1line, priorityList)
            spinnerPriority.setAdapter(spinnerAdapter)
            spinnerPriority.setOnItemClickListener { _, _, position, _ ->
                priorityList[position]
            }
            spinnerPriority.setDropDownBackgroundResource(R.drawable.dropdown_background)

            tietDeadline.setOnClickListener { showDatePicker() }
            
            ivSelectedImage.setOnClickListener {
                launcher.launch(
                    ImagePicker.with(this@EditTaskActivity)
                        .crop()
                        .galleryOnly()
                        .createIntent()
                )
            }

            btnSave.setOnClickListener {
                if (taskId != null) {
                    saveTask()
                } else if (taskDraftId != -1) {
                    saveToDraft()
                }
            }
        }
    }

    private fun showDatePicker() {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

        // Buat MaterialDatePicker
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Due Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateInMillis
            }

            val selectedYear = calendar.get(Calendar.YEAR)
            val selectedMonth = calendar.get(Calendar.MONTH)
            val selectedDay = calendar.get(Calendar.DAY_OF_MONTH)

            val dayFormatted = String.format(Locale.US, "%02d", selectedDay)
            val monthFormatted = String.format(Locale.US, "%02d", selectedMonth + 1)

            // Setelah tanggal dipilih, tampilkan time picker
            val currentTime = Calendar.getInstance()
            val timePicker = MaterialTimePicker.Builder()
                .setTitleText("Select Due Time")
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentTime.get(Calendar.HOUR_OF_DAY))
                .setMinute(currentTime.get(Calendar.MINUTE))
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val selectedHour = timePicker.hour
                val selectedMinute = timePicker.minute

                val hourFormatted = String.format(Locale.US, "%02d", selectedHour)
                val minuteFormatted = String.format(Locale.US, "%02d", selectedMinute)

                val finalTime = "$dayFormatted/$monthFormatted/$selectedYear $hourFormatted:$minuteFormatted"
                val finalDate = "$dayFormatted/$monthFormatted/$selectedYear"
                val finalHour = "$hourFormatted:$minuteFormatted"

                selectedDueTime = finalTime
                selectedDueDate = finalDate
                selectedDueHours = finalHour

                try {
                    val parsedTime = formatter.parse(finalTime)
                    binding.tietDeadline.setText(formatter.format(parsedTime!!))
                } catch (e: Exception) {
                    Log.e(TAG, "Date parse error: $e")
                    binding.tietDeadline.setText(finalTime)
                }

                Log.d(TAG, "Selected due date: $selectedDueDate")
                Log.d(TAG, "Selected due hours: $selectedDueHours")
                Log.d(TAG, "Selected due time: $selectedDueTime")
            }

            timePicker.show(supportFragmentManager, "timePicker")
        }

        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun showReminder() {
        val reminderBinding = DialogReminderBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(reminderBinding.root)
            .create()

        with(reminderBinding) {
            npDays.minValue = 0
            npDays.maxValue = 60
            npHours.minValue = 0
            npHours.maxValue = 23
            npMinutes.minValue = 0
            npMinutes.maxValue = 59

            npDays.wrapSelectorWheel = true
            npHours.wrapSelectorWheel = true
            npMinutes.wrapSelectorWheel = true

            // Inisialisasi nilai awal
            var selectedDays = npDays.value
            var selectedHours = npHours.value
            var selectedMinutes = npMinutes.value

            // Fungsi untuk update TextView
            fun updateReminderText() {
                tvReminder.text = getString(R.string.reminder_text, selectedDays, selectedHours, selectedMinutes)
            }

            // Set listener untuk masing-masing NumberPicker
            npDays.setOnValueChangedListener { _, _, newVal ->
                selectedDays = newVal
                updateReminderText()
            }

            npHours.setOnValueChangedListener { _, _, newVal ->
                selectedHours = newVal
                updateReminderText()
            }

            npMinutes.setOnValueChangedListener { _, _, newVal ->
                selectedMinutes = newVal
                updateReminderText()
            }

            // Set nilai awal
            updateReminderText()

            btnDone.setOnClickListener {
                reminderDays = selectedDays
                reminderHours = selectedHours
                reminderMinutes = selectedMinutes

                showToast("Reminder set for $reminderDays days, $reminderHours hours, and $reminderMinutes minutes earlier")
                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun saveTask() {
        with(binding) {
            val activity = tietActivity.text.toString().trim().ifEmpty { "Empty Title" }
            val description = tietDescription.text.toString().trim().ifEmpty { "Empty Description" }
            val selectedPriority = spinnerPriority.text.toString().trim()
            val oldTaskImageUrl = taskViewModel.taskImageUrl.value ?: ""

            val reminderDaysLong = reminderDays * 24 * 60 * 60 * 1000L
            val reminderHoursLong = reminderHours * 60 * 60 * 1000L
            val reminderMinutesLong = reminderMinutes * 60 * 1000L

            val reminderOffsetMillis: Long = reminderDaysLong + reminderHoursLong + reminderMinutesLong

            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            if (!selectedDueTime.isNullOrEmpty()) {
                val dueTime = try {
                    formatter.parse(selectedDueTime ?: "")?.time ?: return
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse taskDueDate: $selectedDueTime", e)
                    return
                }

                val reminderTimestamp = dueTime - reminderOffsetMillis
                Log.d(TAG, "Reminder timestamp: $reminderTimestamp")
                formattedReminderTime = formatter.format(Date(reminderTimestamp))
            }

            val priorityValue = priorityMap[selectedPriority] ?: 0

            progressBar.visibility = View.VISIBLE

            if (imageUri != null) {
                uploadImageToSupabase(imageUri!!) { imageUrl ->
                    taskViewModel.updateTask(
                        context = this@EditTaskActivity,
                        taskId = taskId!!,
                        taskTitle = activity,
                        taskBody = description,
                        taskPriority = priorityValue,
                        taskDueDate = selectedDueDate,
                        taskDueHours = selectedDueHours,
                        taskDueTime = selectedDueTime,
                        taskReminderTime = formattedReminderTime,
                        taskImageUrl = imageUrl,
                        reminderOffsetMillis = reminderOffsetMillis
                    )

                    deleteOldTaskImage(oldImageUrl = oldTaskImageUrl)
                }
            } else {
                taskViewModel.updateTask(
                    context = this@EditTaskActivity,
                    taskId = taskId!!,
                    taskTitle = activity,
                    taskBody = description,
                    taskPriority = priorityValue,
                    taskDueDate = selectedDueDate,
                    taskDueHours = selectedDueHours,
                    taskDueTime = selectedDueTime,
                    taskReminderTime = formattedReminderTime,
                    taskImageUrl = oldTaskImageUrl,
                    reminderOffsetMillis = reminderOffsetMillis
                )
            }
        }
    }

    private fun saveToDraft() {
        with(binding) {
            val activity = tietActivity.text.toString().trim().ifEmpty { "Empty Title" }
            val description = tietDescription.text.toString().trim().ifEmpty { "Empty Description" }
            val selectedPriority = spinnerPriority.text.toString().trim()
            val oldTaskImageUrl = taskViewModel.taskImageUrl.value ?: ""

            val userId = userViewModel.userId.value ?: ""

            val priorityValue = priorityMap[selectedPriority] ?: 0

            progressBar.visibility = View.VISIBLE

            if (imageUri != null) {
                uploadImageToSupabase(imageUri!!) { imageUrl ->
                    taskDraftViewModel.updateTask(
                        TaskDraft(
                            taskId = taskDraftId ?: 0,
                            userId = userId,
                            taskTitle = activity,
                            taskBody = description,
                            taskPriority = priorityValue,
                            taskDueDate = selectedDueDate,
                            taskDueHours = selectedDueHours,
                            taskDueTime = selectedDueTime,
                            taskImageUrl = imageUrl,
                            updatedAt = Timestamp.now().toString()
                        )
                    )
                    deleteOldTaskImage(oldImageUrl = oldTaskImageUrl)

                    showToast("Draft successfully saved")
                    Log.d(TAG, "$selectedDueDate $selectedDueHours $selectedDueTime")

                    finish()
                }
            } else {
                taskDraftViewModel.updateTask(
                    TaskDraft(
                        taskId = taskDraftId ?: 0,
                        userId = userId,
                        taskTitle = activity,
                        taskBody = description,
                        taskPriority = priorityValue,
                        taskDueDate = selectedDueDate,
                        taskDueHours = selectedDueHours,
                        taskDueTime = selectedDueTime,
                        taskImageUrl = oldTaskImageUrl,
                        updatedAt = Timestamp.now().toString()
                    )
                )

                Log.d(TAG, "$selectedDueDate $selectedDueHours $selectedDueTime")
                showToast("Draft successfully saved")

                finish()
            }
        }
    }

    private fun observeViewModels() {
        taskViewModel.apply {
            operationResult.observe(this@EditTaskActivity) { (success, message) ->
                binding.progressBar.visibility = View.GONE
                if (success) {
                    showToast("Task successfully updated")
                    finish()
                } else {
                    showToast(message ?: "Error occurred")
                }
            }

            taskTitle.observe(this@EditTaskActivity) { binding.tietActivity.setText(it) }
            taskBody.observe(this@EditTaskActivity) { binding.tietDescription.setText(it) }

            taskImageUrl.observe(this@EditTaskActivity) { imageUrl ->
                if (imageUrl != null) {
                    Glide.with(this@EditTaskActivity).load(imageUrl).into(binding.ivSelectedImage)
                }
            }

            taskDueDate.observe(this@EditTaskActivity) { dueDate ->
                selectedDueDate = dueDate
            }

            taskDueHours.observe(this@EditTaskActivity) { dueHours ->
                selectedDueHours = dueHours
            }

            taskDueTime.observe(this@EditTaskActivity) { dueTime ->
                binding.tietDeadline.setText(dueTime)
                selectedDueTime = dueTime
            }

            taskPriority.observe(this@EditTaskActivity) { priority ->
                when (priority) {
                    1 -> binding.spinnerPriority.setText(TaskPriority.LOW.name, false)
                    2 -> binding.spinnerPriority.setText(TaskPriority.MID.name, false)
                    3 -> binding.spinnerPriority.setText(TaskPriority.HIGH.name, false)
                }
            }
        }

        storageViewModel.uploadStatus.observe(this@EditTaskActivity) { success ->
            if (success) Log.d(TAG, "Image sucsessfully uploaded") else Log.d(TAG, "Failed to upload image")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun uploadImageToSupabase(uri: Uri, onSuccess: (String) -> Unit) {
        val inputStream = contentResolver.openInputStream(uri) ?: return showToast("Failed to read file")
        val fileName = "task_${System.currentTimeMillis()}.jpg"
        val byteArray = inputStream.use { it.readBytes() }

        storageViewModel.uploadFile(byteArray, "task_images", fileName)

        storageViewModel.imageUrl.observeOnce(this) { imageUrl ->
            binding.progressBar.visibility = View.GONE
            if (!imageUrl.isNullOrEmpty()) {
                onSuccess(imageUrl)
            } else {
                showToast("Failed to upload image")
            }
        }
    }

    private fun deleteOldTaskImage(oldImageUrl: String) {
        if (oldImageUrl.isNotEmpty()) {
            val fileName = oldImageUrl.substringAfterLast("/")
            storageViewModel.deleteFile(fileName, "task_images")
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