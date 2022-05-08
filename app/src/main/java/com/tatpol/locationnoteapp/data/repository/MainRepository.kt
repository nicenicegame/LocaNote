package com.tatpol.locationnoteapp.data.repository

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.tatpol.locationnoteapp.data.model.DirectionsResult
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.presentation.NoteOrder

interface MainRepository {

    val user: LiveData<FirebaseUser?>

    fun getNotes(order: NoteOrder = NoteOrder.BY_TITLE): LiveData<Resource<List<Note>>>

    fun signInWithEmailProvider(email: String, password: String): Task<AuthResult>

    fun signInWithGoogleProvider(token: String): Task<AuthResult>

    fun signUp(email: String, password: String): Task<AuthResult>

    fun signOut()

    fun addNote(note: Note)

    fun deleteNote(note: Note)

    fun updateNote(note: Note)

    suspend fun getNoteRoute(
        fromLocation: LatLng,
        toLocation: LatLng
    ): Resource<List<DirectionsResult.Route>>
}