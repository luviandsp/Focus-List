package com.project.focuslist.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.project.focuslist.R
import com.project.focuslist.data.model.User
import com.project.focuslist.databinding.ActivityDeleteProfileBinding
import com.project.focuslist.ui.optionsmenu.ShowAllProfileActivity
import com.project.focuslist.ui.viewmodel.AuthViewModel
import com.project.focuslist.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DeleteProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteProfileBinding
    private val viewModel by viewModels<AuthViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()
    private var loadedImage: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDeleteProfileBinding.inflate(layoutInflater)
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
            val profileId = intent.getIntExtra(INTENT_KEY_PROFILE_ID, -1)

            viewModel.getUserById(profileId).observe(this@DeleteProfileActivity) { user ->
                user?.let {
                    tvUsername.text = it.username
                    Glide.with(this@DeleteProfileActivity).load(it.profileImage ?: R.drawable.baseline_account_circle_24).into(ivProfileImage)
                    loadedImage = it.profileImage
                }
            }

            ivBack.setOnClickListener {
                val intent = Intent(this@DeleteProfileActivity, ShowAllProfileActivity::class.java)
                startActivity(intent)
            }

            btnDelete.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@DeleteProfileActivity)
                builder
                    .setTitle(R.string.delete_profile)
                    .setMessage(R.string.delete_message)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        viewModel.deleteUser(User(profileId, "", ""))
                        finish()

                        lifecycleScope.launch {
                            loginViewModel.setLoginStatus(0)
                            loginViewModel.setProfileUsername("")
                            loginViewModel.setRememberedUsername("")
                            loginViewModel.setRememberedPassword("")
                            val intent = Intent(this@DeleteProfileActivity, AuthActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                    .setNegativeButton(R.string.no) { dialog, _ ->
                        dialog.cancel()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }

    companion object {
        const val DELETE_KEY = "DELETE"
        const val INTENT_KEY = "DELETE_OR_NOT"
        const val INTENT_KEY_PROFILE_ID = "TASK_ID"
    }
}