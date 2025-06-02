package com.project.focuslist.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.focuslist.R
import com.project.focuslist.data.utils.UserViewModelFactory
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.ActivityDeleteAccountBinding
import com.project.focuslist.databinding.DialogDeleteAccountBinding
import com.project.focuslist.ui.auth.AuthActivity

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    private val userViewModel by viewModels<UserViewModel>(
        factoryProducer = { UserViewModelFactory(applicationContext) }
    )

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

        userViewModel.getUser()

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {

            toolbar.setNavigationOnClickListener { finish() }

            btnDelete.setOnClickListener {
                showDeleteConfirmationDialog()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        val deleteBinding = DialogDeleteAccountBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(deleteBinding.root)
            .create()

        with(deleteBinding) {
            btnDelete.setOnClickListener {
                val email = tietEmail.text.toString().trim()
                val password = tietPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this@DeleteAccountActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                userViewModel.deleteAccount(email, password)
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun observeViewModel() {
        userViewModel.apply {
            userName.observe(this@DeleteAccountActivity) { username ->
                binding.tvUsername.text = username
            }

            userImageUrl.observe(this@DeleteAccountActivity) { imageUrl ->
                Glide.with(this@DeleteAccountActivity)
                    .load(imageUrl.takeUnless { it.isNullOrEmpty() } ?: R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(binding.ivProfileImage)
            }

            operationStatus.observe(this@DeleteAccountActivity) { result ->
                if (result.first) {
                    Toast.makeText(this@DeleteAccountActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                    userViewModel.setLoginStatus(false)
                    startActivity(Intent(this@DeleteAccountActivity, AuthActivity::class.java).apply {
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    Toast.makeText(this@DeleteAccountActivity, "Failed to delete account: ${result.second}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Failed to delete account: ${result.second}")
                }
            }
        }
    }
}