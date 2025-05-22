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
import com.project.focuslist.data.preferences.AuthPreferences
import com.project.focuslist.databinding.FragmentLoginBinding
import com.project.focuslist.ui.activity.MainActivity
import com.project.focuslist.data.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authPreferences: AuthPreferences
    private val userViewModel by viewModels<UserViewModel>()

    private var email: String = ""
    private var password: String = ""

    companion object {
        private const val TAG = "LoginFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authPreferences = AuthPreferences(requireContext())

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {
            lifecycleScope.launch {
                if (authPreferences.getEmail().isNotEmpty()) {
                    email = authPreferences.getEmail()

                    tietEmail.setText(email)
                    cbRemember.isChecked = true
                }
            }

            btnLogin.setOnClickListener {
                email = tietEmail.text.toString().trim()
                password = tietPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                userViewModel.loginUser(email, password)
            }

            tvForgot.setOnClickListener {
                view?.findNavController()?.navigate(R.id.login_to_forgot)
            }

            tvRegister.setOnClickListener {
                view?.findNavController()?.navigate(R.id.login_to_register)
            }
        }
    }

    private fun observeViewModel() {
        userViewModel.authLogin.observe(viewLifecycleOwner) { result ->
            if (result.first) {
                userViewModel.completeUserRegistration(requireContext())
            } else {
                Toast.makeText(requireContext(), result.second ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.authStatus.observe(viewLifecycleOwner) { result ->
            if (result.first) {
                lifecycleScope.launch {
                    if (binding.cbRemember.isChecked) {
                        authPreferences.setEmail(email)
                    }

                    authPreferences.setLoginStatus(true)
                    Toast.makeText(requireContext(), result.second ?: "Berhasil login", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
            } else {
                Toast.makeText(requireContext(), result.second ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
