package com.project.focuslist.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.project.focuslist.R
import com.project.focuslist.data.utils.UserViewModelFactory
import com.project.focuslist.data.viewmodel.UserViewModel
import com.project.focuslist.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val userViewModel by viewModels<UserViewModel>(
        factoryProducer = { UserViewModelFactory(requireContext()) }
    )

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


        userViewModel.getIsRegistered()

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        with(binding) {
            btnRegister.setOnClickListener {
                val email = tietEmail.text.toString().trim()
                val username = tietUsername.text.toString().trim()
                val password = tietPassword.text.toString().trim()
                val confirmPassword = tietConfirmPassword.text.toString().trim()

                if (!validateInputs(email, username, password, confirmPassword)) {
                    return@setOnClickListener
                }

                userViewModel.registerAccountOnly(email, password, username)
            }

            tvResendVerification.setOnClickListener {
                userViewModel.resendVerificationEmail()
            }

            tvLogin.setOnClickListener {
                navigateToLogin()
            }

            tietPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updatePasswordValidation(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun validateInputs(email: String, userName: String, password: String, confirmPassword: String) : Boolean {
        if (email.isEmpty() || userName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(activity, "All fields are required!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 8) {
            Toast.makeText(activity, "Password must have at least 8 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isUpperCase() }) {
            Toast.makeText(activity, "Password must have at least 1 uppercase character", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isLowerCase() }) {
            Toast.makeText(activity, "Password must have at least 1 lowercase character", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isDigit() }) {
            Toast.makeText(activity, "Password must have at least 1 number", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(activity, "Password and confirm password do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updatePasswordValidation(password: String) {
        with(binding) {
            iconLength.setImageResource(if (password.length >= 8) R.drawable.check_circle else R.drawable.cross_circle)
            iconUppercase.setImageResource(if (password.any { it.isUpperCase() }) R.drawable.check_circle else R.drawable.cross_circle)
            iconLowercase.setImageResource(if (password.any { it.isLowerCase() }) R.drawable.check_circle else R.drawable.cross_circle)
            iconNumber.setImageResource(if (password.any { it.isDigit() }) R.drawable.check_circle else R.drawable.cross_circle)
        }
    }

    private fun observeViewModel() {
        userViewModel.apply {
            authRegister.observe(viewLifecycleOwner) { result ->
                if (result.first) {
                    Toast.makeText(requireContext(), result.second ?: "Registration success, please check your email", Toast.LENGTH_SHORT).show()

                    userViewModel.setRegistered(true)

                    binding.tvResendVerification.visibility = View.VISIBLE
                } else {
                    Toast.makeText(activity, result.second, Toast.LENGTH_SHORT).show()
                }
            }

            authIsRegistered.observe(viewLifecycleOwner) { isRegistered ->
                if (isRegistered) {
                    binding.tvResendVerification.visibility = View.VISIBLE
                } else {
                    binding.tvResendVerification.visibility = View.GONE
                }
            }

            authStatus.observe(viewLifecycleOwner) { result ->
                Toast.makeText(activity, result.second, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLogin() {
        view?.findNavController()?.navigate(R.id.register_to_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
