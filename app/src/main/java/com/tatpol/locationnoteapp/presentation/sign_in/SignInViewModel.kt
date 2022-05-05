package com.tatpol.locationnoteapp.presentation.sign_in

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tatpol.locationnoteapp.data.repository.NotesRepository
import com.tatpol.locationnoteapp.presentation.FormEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    val user = notesRepository.user

    private var _formEvent = MutableLiveData<FormEvent>(FormEvent.Empty)
    val formEvent: LiveData<FormEvent> get() = _formEvent

    fun signInWithEmailProvider(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _formEvent.value = FormEvent.Error("Form is not fully filled")
        } else {
            _formEvent.value = FormEvent.Loading
            notesRepository.signInWithEmailProvider(email, password)
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

    fun signInWithGoogleProvider(token: String) {
        _formEvent.value = FormEvent.Loading
        notesRepository.signInWithGoogleProvider(token)
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