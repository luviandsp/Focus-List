package com.project.focuslist.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.project.focuslist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        initViews()
    }

    private fun initViews() {
        with(binding) {

            fabAdd.setOnClickListener {
                val intent = Intent(this@MainActivity, DetailTaskActivity::class.java).apply {
                    putExtra(DetailTaskActivity.INTENT_KEY, DetailTaskActivity.CREATE_KEY)
                }

                startActivity(intent)
            }

            val navHostFragment = supportFragmentManager.findFragmentById(fcvMain.id) as NavHostFragment
            bnvMain.setupWithNavController(navHostFragment.navController)
        }
    }
}
