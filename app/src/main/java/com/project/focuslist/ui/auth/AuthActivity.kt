package com.project.focuslist.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.focuslist.R
import com.project.focuslist.data.utils.UserViewModelFactory
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.ActivityAuthBinding
import com.project.focuslist.ui.others.MainActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val userViewModel by viewModels<UserViewModel>(
        factoryProducer = { UserViewModelFactory(applicationContext) }
    )

    companion object {
        private const val TAG = "AuthActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var keepSplashOnScreen = true
        splash.setKeepOnScreenCondition { keepSplashOnScreen }

        userViewModel.getLoginStatus()

        userViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                keepSplashOnScreen = false
                Log.d(TAG, "User is logged in")

                if (!isFinishing && !isDestroyed) {
                    Intent(this@AuthActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(this)
                        finish()
                    }
                }
            } else {
                keepSplashOnScreen = false
                Log.d(TAG, "User is not logged in")
            }
        }
    }
}