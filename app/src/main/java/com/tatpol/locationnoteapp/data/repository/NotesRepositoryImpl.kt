package com.tatpol.locationnoteapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.tatpol.locationnoteapp.Constants.NOTES_COLLECTION_PATH
import com.tatpol.locationnoteapp.data.api.DirectionsService
import com.tatpol.locationnoteapp.data.model.DirectionsResult
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.toFormattedString
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

    private val userFlow: Flow<FirebaseUser?>
        get() = callbackFlow {
            val authStateListener = FirebaseAuth.AuthStateListener {
                trySend(it.currentUser)
            }

            auth.addAuthStateListener(authStateListener)
            awaitClose { auth.removeAuthStateListener(authStateListener) }
        }

    override val user: LiveData<FirebaseUser?> = userFlow.asLiveData()

    private val notesCollection = db.collection(NOTES_COLLECTION_PATH)

    override val notes: LiveData<Resource<List<Note>>>
        get() = callbackFlow {
            trySend(Resource.Loading)

            val subscription = notesCollection.whereEqualTo("userId", user.value?.uid)
                .addSnapshotListener { snapshot, error ->
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

    override fun signInWithEmailProvider(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    override fun signInWithGoogleProvider(token: String): Task<AuthResult> {
        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
        return auth.signInWithCredential(firebaseCredential)
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

    override suspend fun getNoteRoute(
        fromLocation: LatLng,
        toLocation: LatLng
    ): Resource<List<DirectionsResult.Route>> {
        return try {
            val result = directionsService.getDirections(
                hashMapOf(
                    "origin" to fromLocation.toFormattedString(),
                    "destination" to toLocation.toFormattedString()
                )
            )
            Resource.Success(result.routes)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }
}