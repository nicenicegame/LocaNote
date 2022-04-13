package com.tatpol.locationnoteapp.presentation.sign_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tatpol.locationnoteapp.databinding.FragmentSignInBinding
import com.tatpol.locationnoteapp.presentation.AuthFormEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by viewModels()

    private lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnSignIn.setOnClickListener {
                val email = binding.etEmail.editText?.text.toString()
                val password = binding.etPassword.editText?.text.toString()
                viewModel.signInWithEmailProvider(email, password)
            }
            tvGotoSignUp.setOnClickListener { navigateToSignUpScreen() }
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.formEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AuthFormEvent.Success -> {

                }
                is AuthFormEvent.Error -> {

                }
                is AuthFormEvent.Empty -> {

                }
                is AuthFormEvent.Loading -> {

                }
            }
        }
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                navigateToHome()
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(
            SignInFragmentDirections.actionSignInFragmentToMapNoteViewPagerFragment()
        )
    }

    private fun navigateToSignUpScreen() {
        findNavController().navigate(
            SignInFragmentDirections.actionSignInFragmentToSignUpFragment()
        )
    }
}