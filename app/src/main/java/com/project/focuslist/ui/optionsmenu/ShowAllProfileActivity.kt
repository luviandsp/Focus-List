package com.project.focuslist.ui.optionsmenu

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.focuslist.R
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.model.User
import com.project.focuslist.databinding.ActivityShowAllProfileBinding
import com.project.focuslist.ui.activity.DeleteProfileActivity
import com.project.focuslist.ui.activity.DetailTaskActivity
import com.project.focuslist.ui.activity.MainActivity
import com.project.focuslist.ui.adapter.ProfileAdapter
import com.project.focuslist.ui.adapter.TaskAdapter
import com.project.focuslist.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class ShowAllProfileActivity : AppCompatActivity(), ProfileAdapter.OnItemClickListener {

    private lateinit var binding: ActivityShowAllProfileBinding
    private val viewModel by viewModels<AuthViewModel>()
    private lateinit var profileAdapter: ProfileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShowAllProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        obseveProfileList()
    }

    private fun initViews() {
        with(binding) {

            profileAdapter = ProfileAdapter(mutableListOf()).apply {
                onItemClickListener = this@ShowAllProfileActivity
            }

            rvProfile.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@ShowAllProfileActivity)
                adapter = profileAdapter
            }

            ivBack.setOnClickListener {
                val intent = Intent(this@ShowAllProfileActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun obseveProfileList() {
        lifecycleScope.launch {
            viewModel.getAllUsers().observe(this@ShowAllProfileActivity) {
                profileAdapter.setProfiles(it)
            }
        }
    }

    override fun onItemClick(user: User) {
        val intent = Intent(this@ShowAllProfileActivity, DeleteProfileActivity::class.java)
        intent.putExtra(DeleteProfileActivity.INTENT_KEY_PROFILE_ID, user.userId)
        intent.putExtra(DeleteProfileActivity.INTENT_KEY, DeleteProfileActivity.DELETE_KEY)
        startActivity(intent)
    }
}