package com.tatpol.locationnoteapp.presentation.sign_in

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.tatpol.locationnoteapp.data.repository.NotesRepository
import com.tatpol.locationnoteapp.presentation.AuthFormEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    notesRepository: NotesRepository
) : ViewModel() {

    val user = notesRepository.user

    private var _formEvent = MutableLiveData<AuthFormEvent>(AuthFormEvent.Empty)
    val formEvent: LiveData<AuthFormEvent> get() = _formEvent

    fun signInWithEmailProvider(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _formEvent.value = AuthFormEvent.Success
                } else {
                    _formEvent.value = AuthFormEvent.Error("Authentication failed.")
                }
            }
    }
}