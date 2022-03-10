package com.tatpol.locationnoteapp.presentation.note_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    val notes = notesRepository.notesFlow.asLiveData()
}