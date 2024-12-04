package com.project.focuslist.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.project.focuslist.R
import com.project.focuslist.databinding.FragmentLoginBinding
import com.project.focuslist.ui.activity.MainActivity
import com.project.focuslist.ui.viewmodel.AuthViewModel
import com.project.focuslist.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel by viewModels<LoginViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            if (loginViewModel.getLoginStatus() == 1) {
                navigateToMainActivity()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initViews()
    }

    private fun initViews() {
        with(binding) {
            lifecycleScope.launch {
                if (!loginViewModel.getRememberedUsername()
                        .isNullOrEmpty() && !loginViewModel.getRememberedPassword().isNullOrEmpty()
                ) {
                    tietUsername.setText(loginViewModel.getRememberedUsername())
                    tietPassword.setText(loginViewModel.getRememberedPassword())
                    cbRemember.isChecked = true
                }
            }

            btnLogin.setOnClickListener {
                val username = tietUsername.text.toString()
                val password = tietPassword.text.toString()

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity, "Username dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                authViewModel.authenticateUser(username, password).observe(viewLifecycleOwner) { user ->
                    if (user != null) {
                        lifecycleScope.launch {
                            loginViewModel.setLoginStatus(1)

                            loginViewModel.setRememberedUsername(if (cbRemember.isChecked) username else "")
                            loginViewModel.setRememberedPassword(if (cbRemember.isChecked) password else "")

                            loginViewModel.setProfileUsername(username)

                            navigateToMainActivity()
                        }
                    } else {
                        Toast.makeText(activity, "Username atau Password salah", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            tvForgot.setOnClickListener {
                view?.findNavController()?.navigate(R.id.login_to_forgot)
            }

            tvRegister.setOnClickListener {
                view?.findNavController()?.navigate(R.id.login_to_register)
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
