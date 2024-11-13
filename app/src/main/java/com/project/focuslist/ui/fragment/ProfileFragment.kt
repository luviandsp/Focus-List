package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.project.focuslist.R
import com.project.focuslist.databinding.FragmentProfileBinding
import com.project.focuslist.ui.activity.AuthActivity
import com.project.focuslist.ui.optionsmenu.EditProfileActivity
import com.project.focuslist.ui.optionsmenu.ShowAllProfileActivity
import com.project.focuslist.ui.viewmodel.AuthViewModel
import com.project.focuslist.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val loginViewModel by viewModels<LoginViewModel>()
    private val userViewModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initViews()
    }

    private fun initViews() {
        with(binding) {

            lifecycleScope.launch {
                val username = loginViewModel.getProfileUsername()

                userViewModel.getUserByUsername(username.toString()).observe(viewLifecycleOwner) { user ->
                    Glide.with(this@ProfileFragment).load(user?.profileImage?: R.drawable.baseline_account_circle_24).into(ivProfileImage)
                    tvUsername.text = user?.username
                }
            }

            btnLogout.setOnClickListener {
                lifecycleScope.launch {
                    loginViewModel.setLoginStatus(0)
                    val intent = Intent(activity, AuthActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(
            requireContext(),
            when (item.itemId) {
                R.id.activity_show_all_profile -> ShowAllProfileActivity::class.java
                R.id.activity_edit_profile -> EditProfileActivity::class.java
                else -> null
            }
        )

        startActivity(intent)
        return true
    }
}
