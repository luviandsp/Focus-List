package com.project.focuslist.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.project.focuslist.R
import com.project.focuslist.databinding.FragmentForgotPasswordBinding
import com.project.focuslist.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private val viewModel by viewModels<AuthViewModel>()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            view?.findNavController()?.popBackStack(R.id.login_fragment, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        with (binding) {
            btnChangePassword.setOnClickListener {
                val username = tietUsername.text.toString()
                val newPassword = tietNewPassword.text.toString()
                val confirmPassword = tietConfirmPassword.text.toString()

                if (username.isEmpty()) {
                    tietUsername.error = "Masukkan username"
                    return@setOnClickListener
                }

                if (newPassword.isEmpty()) {
                    tietNewPassword.error = "Masukkan kata sandi baru"
                    return@setOnClickListener
                }

                if (newPassword != confirmPassword) {
                    tietConfirmPassword.error = "Kata sandi tidak sama"
                    return@setOnClickListener
                }

                viewModel.getUserByUsername(username).observe(viewLifecycleOwner) { user ->
                    if (user != null) {
                        // Update kata sandi jika user ditemukan
                        lifecycleScope.launch {
                            viewModel.updatePassword(user.userId, newPassword)
                            Toast.makeText(activity, "Kata sandi berhasil diubah", Toast.LENGTH_SHORT).show()
                            view?.findNavController()?.navigate(R.id.forgot_to_login)
                        }
                    } else {
                        Toast.makeText(activity, "Username tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}