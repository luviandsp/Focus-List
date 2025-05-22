package com.project.focuslist.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.project.focuslist.R
import com.project.focuslist.data.preferences.AuthPreferences
import com.project.focuslist.databinding.ActivityAuthBinding
import com.project.focuslist.ui.activity.MainActivity
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var authPreferences: AuthPreferences
    private var loginStatus: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()

        super.onCreate(savedInstanceState)

        authPreferences = AuthPreferences(this)
        var keepSplashOnScreen = true
        splash.setKeepOnScreenCondition { keepSplashOnScreen }

        lifecycleScope.launch {
            loginStatus = authPreferences.getLoginStatus()

            if (loginStatus == true) {
                keepSplashOnScreen = false

                Intent(this@AuthActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(this)
                }

            } else {
                keepSplashOnScreen = false
                setContentView()
            }
        }
    }

    private fun setContentView() {
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}