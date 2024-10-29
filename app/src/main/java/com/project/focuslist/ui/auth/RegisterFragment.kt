package com.project.focuslist.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.project.focuslist.R
import com.project.focuslist.data.model.User
import com.project.focuslist.databinding.FragmentRegisterBinding
import com.project.focuslist.ui.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
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
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        with(binding) {
            btnRegister.setOnClickListener {
                val username = tietUsername.text.toString()
                val password = tietPassword.text.toString()
                val confirmPassword = tietConfirmPassword.text.toString()

                // Validate input fields
                if (username.isEmpty()) {
                    tietUsername.error = "Masukkan Username"
                    return@setOnClickListener
                }
                if (password.isEmpty()) {
                    tietPassword.error = "Masukkan Password"
                    return@setOnClickListener
                }
                if (confirmPassword.isEmpty()) {
                    tietConfirmPassword.error = "Masukkan Konfirmasi Password"
                    return@setOnClickListener
                }
                if (password != confirmPassword) {
                    tietConfirmPassword.error = "Password tidak sama"
                    return@setOnClickListener
                }

                // Register user
                val newUser = User(userId = 0, username = username, password = password)
                viewModel.createUser(newUser)

                // Navigate to login
                view?.findNavController()?.navigate(R.id.register_to_login)
            }

            tvLogin.setOnClickListener {
                view?.findNavController()?.navigate(R.id.register_to_login)
            }
        }
    }
}
