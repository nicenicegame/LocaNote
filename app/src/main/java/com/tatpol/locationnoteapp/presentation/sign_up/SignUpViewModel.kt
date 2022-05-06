package com.tatpol.locationnoteapp.presentation.sign_up

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.tatpol.locationnoteapp.data.repository.MainRepository
import com.tatpol.locationnoteapp.presentation.FormEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    val user = mainRepository.user

    private var _formEvent = MutableLiveData<FormEvent>(FormEvent.Empty)
    val formEvent: LiveData<FormEvent> get() = _formEvent

    fun signUpWithEmailProvider(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _formEvent.value = FormEvent.Error("Form is not fully filled")
        } else if (password != confirmPassword) {
            _formEvent.value = FormEvent.Error("Password does not match")
        } else {
            _formEvent.value = FormEvent.Loading
            mainRepository.signUp(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        autoSignIn(email, password)
                    } else {
                        _formEvent.value = FormEvent.Error(
                            task.exception?.localizedMessage ?: "Authentication Failed"
                        )
                    }
                }
        }
    }

    private fun autoSignIn(email: String, password: String) {
        mainRepository.signInWithEmailProvider(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _formEvent.value = FormEvent.Success()
                } else {
                    _formEvent.value =
                        FormEvent.Error(
                            task.exception?.localizedMessage ?: "Authentication failed"
                        )
                }
            }
    }
}