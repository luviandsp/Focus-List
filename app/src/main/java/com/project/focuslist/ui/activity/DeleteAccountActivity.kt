package com.project.focuslist.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.project.focuslist.R
import com.project.focuslist.data.preferences.AuthPreferences
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.ActivityDeleteAccountBinding
import com.project.focuslist.ui.auth.AuthActivity
import kotlinx.coroutines.launch

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var authPreferences: AuthPreferences

    companion object {
        private const val TAG = "DeleteAccountActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authPreferences = AuthPreferences(this)
        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {

            toolbar.setNavigationOnClickListener { finish() }

            btnDelete.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@DeleteAccountActivity)

                builder
                    .setTitle(R.string.delete_account)
                    .setMessage(R.string.delete_message)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        userViewModel.deleteAccount()
                    }
                    .setNegativeButton(R.string.no) { dialog, _ ->
                        dialog.cancel()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun observeViewModel() {
        userViewModel.userName.observe(this) { username ->
            binding.tvUsername.text = username
        }

        userViewModel.userImageUrl.observe(this) { imageUrl ->
            Glide.with(this)
                .load(imageUrl.takeUnless { it.isNullOrEmpty() } ?: R.drawable.baseline_account_circle_24)
                .circleCrop()
                .into(binding.ivProfileImage)
        }

        userViewModel.authStatus.observe(this@DeleteAccountActivity) { status ->
            if (status.first) {
                logoutUser()
            }

            Toast.makeText(this, status.second, Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            userViewModel.logoutUser()
            startActivity(Intent(this@DeleteAccountActivity, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}