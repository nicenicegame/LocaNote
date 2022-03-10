package com.tatpol.locationnoteapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tatpol.locationnoteapp.Constants
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(ExperimentalCoroutinesApi::class)
class NotesRepositoryImpl(
    db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NotesRepository {

    private val notesCollection = db.collection(Constants.NOTES_COLLECTION_PATH)

    override val notesFlow: Flow<Resource<List<Note>>>
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
        }

    override fun addNote(note: Note) {
        notesCollection.add(note)
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

    override fun getRouteNote(note: Note) {
        TODO("Not yet implemented")
    }
}