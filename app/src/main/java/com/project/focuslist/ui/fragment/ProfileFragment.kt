package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.project.focuslist.R
import com.project.focuslist.data.model.User
import com.project.focuslist.databinding.FragmentProfileBinding
import com.project.focuslist.ui.activity.AuthActivity
import com.project.focuslist.ui.activity.EditProfileActivity
import com.project.focuslist.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val loginViewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        with(binding) {
            var userData: User? = null

            lifecycleScope.launch {
                Glide.with(this@ProfileFragment).load(userData?.profileImage?: R.drawable.baseline_account_circle_24).into(ivProfileImage)
                tvUsername.text = loginViewModel.getProfileUsername()
            }

            btnLogout.setOnClickListener {
                lifecycleScope.launch {

                    loginViewModel.setLoginStatus(0)
                    val intent = Intent(activity, AuthActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            fabEditProfile.setOnClickListener {
                val intent = Intent(activity, EditProfileActivity::class.java)
                intent.putExtra(EditProfileActivity.INTENT_KEY, EditProfileActivity.CREATE_KEY)
                startActivity(intent)
            }
        }
    }
}
