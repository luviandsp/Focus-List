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
import com.project.focuslist.databinding.ActivityProfileBinding
import com.project.focuslist.ui.auth.AuthActivity
import com.project.focuslist.ui.tasks.DraftTaskActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val userViewModel by viewModels<UserViewModel>(
        factoryProducer = { UserViewModelFactory(applicationContext) }
    )

    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {

            toolbar.setNavigationOnClickListener { finish() }

            toolbar.menu.apply {
                findItem(R.id.activity_delete_profile).setOnMenuItemClickListener {
                    Intent(this@ProfileActivity, DeleteAccountActivity::class.java).apply {
                        startActivity(this)
                    }
                    true
                }

                findItem(R.id.activity_edit_profile).setOnMenuItemClickListener {
                    Intent(this@ProfileActivity, EditProfileActivity::class.java).apply {
                        startActivity(this)
                    }
                    true
                }
            }

            btnLogout.setOnClickListener {
                MaterialAlertDialogBuilder(this@ProfileActivity)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes") { _, _ ->
                        userViewModel.logoutUser()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            btnDraft.setOnClickListener {
                Intent(this@ProfileActivity, DraftTaskActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
    }

    private fun observeViewModels() {
        userViewModel.apply {
            userName.observe(this@ProfileActivity) { name ->
                if (name != null) {
                    binding.tvUsername.text = name
                } else {
                    binding.tvUsername.setText(R.string.user)
                }
            }

            userImageUrl.observe(this@ProfileActivity) { imageUrl ->
                Log.d(TAG, "Image URL: $imageUrl")
                Glide.with(this@ProfileActivity)
                    .load(imageUrl.takeUnless { it.isNullOrEmpty() } ?: R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(binding.ivProfileImage)
            }

            authStatus.observe(this@ProfileActivity) { result ->
                if (result.first == false) {
                    Toast.makeText(this@ProfileActivity, "Logout Success", Toast.LENGTH_SHORT).show()

                    userViewModel.setLoginStatus(false)
                    startActivity(Intent(this@ProfileActivity, AuthActivity::class.java).apply {
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })

                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        userViewModel.getUser()
    }
}