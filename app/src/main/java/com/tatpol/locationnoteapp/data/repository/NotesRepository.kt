package com.tatpol.locationnoteapp.data.repository

import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import kotlinx.coroutines.flow.Flow

interface NotesRepository {

    val notesFlow: Flow<Resource<List<Note>>>

    fun addNote(note: Note)

    fun deleteNote(note: Note)

    fun updateNote(note: Note)

    fun getRouteNote(note: Note)
}