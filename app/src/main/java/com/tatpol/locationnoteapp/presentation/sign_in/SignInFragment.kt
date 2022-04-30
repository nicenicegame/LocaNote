package com.tatpol.locationnoteapp.presentation.sign_in

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.tatpol.locationnoteapp.BuildConfig
import com.tatpol.locationnoteapp.databinding.FragmentSignInBinding
import com.tatpol.locationnoteapp.presentation.FormEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by viewModels()

    private lateinit var binding: FragmentSignInBinding

    private lateinit var oneTapClient: SignInClient

    private val googleSignInRequest = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Snackbar.make(binding.root, "Couldn't start One Tap UI", Snackbar.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            when {
                idToken != null -> {
                    viewModel.signInWithGoogleProvider(idToken)
                }
                else -> {
                    Snackbar.make(
                        binding.root,
                        "No ID token!", Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: ApiException) {
            e.localizedMessage?.let {
                Snackbar.make(
                    binding.root,
                    it, Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

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

        oneTapClient = Identity.getSignInClient(requireContext())

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        binding.apply {
            btnSignIn.setOnClickListener {
                val email = binding.etEmail.editText?.text.toString()
                val password = binding.etPassword.editText?.text.toString()
                viewModel.signInWithEmailProvider(email, password)
            }
            btnSignInWithGoogle.setOnClickListener {
                signInWithGoogleProvider(signInRequest)
            }
            tvGotoSignUp.setOnClickListener { navigateToSignUpScreen() }
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
                navigateToHome()
            }
        }
    }

    private fun signInWithGoogleProvider(signInRequest: BeginSignInRequest) {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
                googleSignInRequest.launch(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                e.localizedMessage?.let {
                    Snackbar.make(
                        binding.root,
                        it, Snackbar.LENGTH_SHORT
                    ).show()
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