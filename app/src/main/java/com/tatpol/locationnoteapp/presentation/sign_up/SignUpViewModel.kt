package com.tatpol.locationnoteapp.presentation.sign_up

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.tatpol.locationnoteapp.data.repository.NotesRepository
import com.tatpol.locationnoteapp.presentation.FormEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    notesRepository: NotesRepository
) : ViewModel() {

    val user = notesRepository.user

    private var _formEvent = MutableLiveData<FormEvent>(FormEvent.Empty)
    val formEvent: LiveData<FormEvent> get() = _formEvent

    fun signUpWithEmailProvider(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _formEvent.value = FormEvent.Error("Password does not match.")
        } else {
            _formEvent.value = FormEvent.Loading
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        successAuthentication()
                    } else {
                        _formEvent.value = FormEvent.Error("Authentication failed.")
                    }
                }
        }
    }

    private fun successAuthentication() {
        _formEvent.value = FormEvent.Success()
    }
}