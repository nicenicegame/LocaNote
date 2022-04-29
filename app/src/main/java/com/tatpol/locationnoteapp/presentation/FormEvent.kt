package com.tatpol.locationnoteapp.presentation

sealed class FormEvent {

    object Empty : FormEvent()

    object Loading : FormEvent()

    data class Success(val message: String? = null) : FormEvent()

    data class Error(val message: String) : FormEvent()
}
