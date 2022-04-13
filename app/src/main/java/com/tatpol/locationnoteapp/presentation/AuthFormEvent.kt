package com.tatpol.locationnoteapp.presentation

sealed class AuthFormEvent {

    object Empty : AuthFormEvent()

    object Loading : AuthFormEvent()

    object Success : AuthFormEvent()

    data class Error(val message: String) : AuthFormEvent()
}
