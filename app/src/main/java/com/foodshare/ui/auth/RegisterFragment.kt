package com.foodshare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.foodshare.R
import com.foodshare.databinding.FragmentRegisterBinding
import com.foodshare.ui.MainActivity
import com.foodshare.util.Resource
import com.foodshare.util.gone
import com.foodshare.util.toast
import com.foodshare.util.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeRegisterState()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val displayName = binding.etDisplayName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validateInput(displayName, email, password, confirmPassword)) {
                viewModel.register(email, password, displayName)
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun validateInput(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (displayName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            requireContext().toast(getString(R.string.error_empty_fields))
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            requireContext().toast(getString(R.string.error_invalid_email))
            return false
        }
        if (password.length < 6) {
            requireContext().toast(getString(R.string.error_password_short))
            return false
        }
        if (password != confirmPassword) {
            requireContext().toast(getString(R.string.error_password_mismatch))
            return false
        }
        return true
    }

    private fun observeRegisterState() {
        viewModel.registerState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.btnRegister.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    navigateToMain()
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.btnRegister.isEnabled = true
                    requireContext().toast(result.message ?: getString(R.string.error_register))
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
