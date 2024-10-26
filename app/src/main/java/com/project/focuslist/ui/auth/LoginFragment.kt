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
import com.project.focuslist.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            if (viewModel.getLoginStatus() == 1) {
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        initViews()
    }

    private fun initViews() {
        with(binding) {
            lifecycleScope.launch {
                if (!viewModel.getRememberedUsername()
                        .isNullOrEmpty() && !viewModel.getRememberedPassword().isNullOrEmpty()
                ) {
                    tietUsername.setText(viewModel.getRememberedUsername())
                    tietPassword.setText(viewModel.getRememberedPassword())
                    cbRemember.isChecked = true
                }
            }

            btnLogin.setOnClickListener {
                val username = tietUsername.text.toString()
                val password = tietPassword.text.toString()
                if (username == "ksmandroid" && password == "androinter") {

                    lifecycleScope.launch {

                        viewModel.setLoginStatus(1)

                        viewModel.setRememberedUsername(
                            if (cbRemember.isChecked) username else ""
                        )
                        viewModel.setRememberedPassword(
                            if (cbRemember.isChecked) password else ""
                        )

                        val intent = Intent(activity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                } else {
                    Toast.makeText(
                        activity,
                        "Username: ksmandroid; Password: androinter",
                        Toast.LENGTH_SHORT
                    ).show()
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
}