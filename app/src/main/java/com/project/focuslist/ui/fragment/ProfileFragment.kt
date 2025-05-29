package com.project.focuslist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.project.focuslist.data.preferences.AuthPreferences
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.FragmentProfileBinding
import com.project.focuslist.ui.activity.DeleteProfileActivity
import com.project.focuslist.ui.activity.DraftTaskActivity
import com.project.focuslist.ui.auth.AuthActivity
import com.project.focuslist.ui.activity.EditProfileActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var authPreferences: AuthPreferences

    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authPreferences = AuthPreferences(requireContext())

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            btnLogout.setOnClickListener {
                logoutUser()
            }

            btnDraft.setOnClickListener {
                Intent(requireContext(), DraftTaskActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
    }

    private fun observeViewModels() {
        userViewModel.apply {
            userName.observe(viewLifecycleOwner) { name ->
                if (name != null) {
                    binding.tvUsername.text = name
                } else {
                    binding.tvUsername.setText(R.string.user)
                }
            }

            userImageUrl.observe(viewLifecycleOwner) { imageUrl ->
                Log.d(TAG, "Image URL: $imageUrl")
                Glide.with(this@ProfileFragment)
                    .load(imageUrl.takeUnless { it.isNullOrEmpty() } ?: R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(binding.ivProfileImage)
            }

            authStatus.observe(viewLifecycleOwner) { result ->
                if (result.first == false) {
                    lifecycleScope.launch {
                        authPreferences.setLoginStatus(false)
                        startActivity(Intent(requireActivity(), AuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                }
            }
        }
    }

    private fun logoutUser() {
        userViewModel.logoutUser()
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
                R.id.activity_delete_profile -> DeleteProfileActivity::class.java
                R.id.activity_edit_profile -> EditProfileActivity::class.java
                else -> null
            }
        )

        startActivity(intent)
        return true
    }

    override fun onResume() {
        super.onResume()
        userViewModel.getUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
