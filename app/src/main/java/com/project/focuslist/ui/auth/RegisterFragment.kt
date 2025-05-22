package com.project.focuslist.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.project.focuslist.R
import com.project.focuslist.data.preferences.UserAccountPreferences
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var userAccountPreferences: UserAccountPreferences

    private var isRegistered : Boolean = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            view?.findNavController()?.popBackStack(R.id.login_fragment, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userAccountPreferences = UserAccountPreferences(requireContext())

        lifecycleScope.launch {
            isRegistered = userAccountPreferences.isRegistered()
            initViews()
            observeViewModel()
        }
    }

    private fun initViews() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        with(binding) {
            btnRegister.setOnClickListener {
                val email = tietEmail.text.toString().trim()
                val username = tietUsername.text.toString().trim()
                val password = tietPassword.text.toString().trim()
                val confirmPassword = tietConfirmPassword.text.toString().trim()

                if (!validateInputs(email, password, confirmPassword, username)) {
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    userAccountPreferences.saveTempUser(email, username)
                    userViewModel.registerAccountOnly(email, password)
                }
            }

            if (isRegistered) {
                tvResendVerification.visibility = View.VISIBLE
            } else {
                tvResendVerification.visibility = View.GONE
            }

            tvResendVerification.setOnClickListener {
                userViewModel.resendVerificationEmail()
            }

            tvLogin.setOnClickListener {
                navigateToLogin()
            }
        }
    }

    private fun observeViewModel() {
        userViewModel.authRegister.observe(viewLifecycleOwner) { result ->
            if (result.first) {
                Toast.makeText(activity, "Registrasi berhasil, silahkan cek email untuk verifikasi", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    userAccountPreferences.setRegistered(true)
                }

                binding.tvResendVerification.visibility = View.VISIBLE
            } else {
                Toast.makeText(activity, result.second, Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.authStatus.observe(viewLifecycleOwner) { result ->
            Toast.makeText(activity, result.second, Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String, username: String): Boolean {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(activity, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 8) {
            Toast.makeText(activity, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isUpperCase() }) {
            Toast.makeText(activity, "Password harus mengandung huruf kapital", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isLowerCase() }) {
            Toast.makeText(activity, "Password harus mengandung huruf kecil", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isDigit() }) {
            Toast.makeText(activity, "Password harus mengandung angka", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(activity, "Password tidak sama", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun navigateToLogin() {
        view?.findNavController()?.navigate(R.id.register_to_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
