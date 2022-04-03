package com.tatpol.locationnoteapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.tatpol.locationnoteapp.Constants.NOTES_COLLECTION_PATH
import com.tatpol.locationnoteapp.data.api.DirectionsService
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
class NotesRepositoryImpl(
    db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val directionsService: DirectionsService
) : NotesRepository {

    private val notesCollection = db.collection(NOTES_COLLECTION_PATH)

    private val userFlow: Flow<FirebaseUser?>
        get() = callbackFlow {
            val authStateListener = FirebaseAuth.AuthStateListener {
                trySend(it.currentUser)
            }

            auth.addAuthStateListener(authStateListener)
            awaitClose { auth.removeAuthStateListener(authStateListener) }
        }

    override val user: LiveData<FirebaseUser?> = userFlow.asLiveData()

    override val notes: LiveData<Resource<List<Note>>>
        get() = callbackFlow {
            trySend(Resource.Loading)

            val subscription = notesCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "An error occurred"))
                    return@addSnapshotListener
                }
                val notes = mutableListOf<Note>()
                for (document in snapshot?.documents!!) {
                    var note = document.toObject(Note::class.java)!!
                    note = note.copy(id = document.id)
                    notes.add(note)
                }
                trySend(Resource.Success(notes))
            }

            awaitClose { subscription.remove() }
        }.asLiveData()

    override fun addNote(note: Note) {
        notesCollection.add(note.copy(userId = user.value?.uid))
    }

    override fun deleteNote(note: Note) {
        note.id?.let {
            notesCollection.document(note.id).delete()
        }
    }

    override fun updateNote(note: Note) {
        note.id?.let {
            notesCollection.document(note.id).set(note)
        }
    }

    override suspend fun getNoteRoute(note: Note) {
        return directionsService.getDirections()
    }
}