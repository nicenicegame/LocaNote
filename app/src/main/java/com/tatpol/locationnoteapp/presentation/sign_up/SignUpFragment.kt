package com.tatpol.locationnoteapp.presentation.sign_up

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.tatpol.locationnoteapp.databinding.FragmentSignUpBinding
import com.tatpol.locationnoteapp.presentation.FormEvent
import com.tatpol.locationnoteapp.presentation.sign_in.SignInFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by viewModels()

    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnSignUp.setOnClickListener {
                val email = binding.etEmail.editText?.text.toString()
                val password = binding.etPassword.editText?.text.toString()
                val confirmPassword = binding.etConfirmPassword.editText?.text.toString()
                viewModel.signUpWithEmailProvider(email, password, confirmPassword)
            }
            tvGotoSignIn.setOnClickListener { navigateToSignUpScreen() }
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.formEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is FormEvent.Success -> {

                }
                is FormEvent.Error -> {
                    Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                }
                is FormEvent.Empty -> {

                }
                is FormEvent.Loading -> {

                }
            }
        }
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                navigateToMapScreen()
            }
        }
    }

    private fun navigateToMapScreen() {
        findNavController().navigate(
            SignInFragmentDirections.actionSignInFragmentToMapFragment()
        )
    }

    private fun navigateToSignUpScreen() {
        findNavController().navigate(
            SignUpFragmentDirections.actionSignUpFragmentToSignInFragment()
        )
    }
}