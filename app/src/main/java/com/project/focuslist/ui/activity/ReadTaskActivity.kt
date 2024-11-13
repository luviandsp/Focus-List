package com.project.focuslist.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.project.focuslist.R
import com.project.focuslist.databinding.ActivityReadTaskBinding
import com.project.focuslist.ui.viewmodel.TaskViewModel

class ReadTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadTaskBinding
    private val viewModel by viewModels<TaskViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReadTaskBinding.inflate(layoutInflater)
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

        val taskId = intent.getIntExtra(INTENT_KEY_TASK_ID, 0)
        with(binding) {
            ivBack.setOnClickListener {
                finish()
            }

            viewModel.getTaskById(taskId).observe(this@ReadTaskActivity) {
                tvTitle.text = it.title
                tvDescription.text = it.body
                Glide.with(this@ReadTaskActivity).load(it.taskImage).into(ivImage)
            }
        }
    }

    companion object {
        const val INTENT_KEY_TASK_ID = "task_id"
    }
}