package com.project.focuslist.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.focuslist.R
import com.project.focuslist.data.model.Task
import com.project.focuslist.databinding.ActivityDetailTaskBinding
import com.project.focuslist.ui.viewmodel.TaskViewModel

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaskBinding
    private val viewModel by viewModels<TaskViewModel>()
    private var selectedDueDate: String? = null
    private var taskData: Task? = null // Store task data for later use

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

        initViews()
    }

    private fun initViews() {
        with(binding) {
            val isEdit = intent.getStringExtra(INTENT_KEY) == EDIT_KEY
            val taskId = intent.getIntExtra(INTENT_KEY_TASK_ID, -1)

            if (isEdit) {
                // Observe the LiveData for task details
                viewModel.getTaskById(taskId).observe(this@DetailTaskActivity) { task ->
                    task?.let {
                        taskData = it
                        tietTitle.setText(it.title)
                        tietBody.setText(it.body)
                        spinnerPriority.setSelection(it.priority -1) // Assuming priority is an index
                        tvDate.text = it.dueDate ?: "Select Date"
                    }
                }
            }

            val priorities = arrayOf("Rendah", "Sedang", "Tinggi")
            val adapter = ArrayAdapter(this@DetailTaskActivity, android.R.layout.simple_spinner_item, priorities)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPriority.adapter = adapter

            btnDatePicker.setOnClickListener {
                showDatePicker()
            }

            btnSave.setOnClickListener {
                saveTask(isEdit, taskId)
            }

            ivDelete.setOnClickListener {
                if (isEdit) {
                    viewModel.deleteTask(Task(taskId, "", "", false))
                    finish()
                }
            }

            ivBack.setOnClickListener {
                val intent = Intent(this@DetailTaskActivity, MainActivity::class.java)
                startActivity(intent)
            }

            tvTitle.text = if (isEdit) "Edit Task" else "Create Task"
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedDueDate = "$dayOfMonth/${month + 1}/$year"
            binding.tvDate.text = selectedDueDate // Tampilkan tanggal yang dipilih
        }, 2024, 0, 1) // Set default date (tahun, bulan, hari)

        datePickerDialog.show()
    }

    private fun saveTask(isEdit: Boolean, taskId: Int) {
        val title = binding.tietTitle.text.toString().ifEmpty { "Tanpa Judul" }
        val body = binding.tietBody.text.toString().ifEmpty { "Tanpa Isi" }
        val dueDate = selectedDueDate ?: binding.tvDate.text.toString().ifEmpty { "" }
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
                        dueDate
                    )
                )
            }
        } else {
            viewModel.createTask(Task(0, title, body, false, priorityValue, dueDate))
        }
        finish()
    }

    companion object {
        const val EDIT_KEY = "EDIT"
        const val CREATE_KEY = "CREATE"
        const val INTENT_KEY = "EDIT_OR_CREATE"
        const val INTENT_KEY_TASK_ID = "TASK_ID"
    }
}
