package com.project.focuslist.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.project.focuslist.R
import com.project.focuslist.databinding.ActivityMainBinding
import com.project.focuslist.ui.optionsmenu.EditProfileActivity
import com.project.focuslist.ui.optionsmenu.ShowAllProfileActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        initViews()
    }

    private fun initViews() {
        with(binding) {
            val navHostFragment = supportFragmentManager.findFragmentById(fcvMain.id) as NavHostFragment
            bnvMain.setupWithNavController(navHostFragment.navController)
        }
    }
}
