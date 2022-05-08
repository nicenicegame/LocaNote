package com.tatpol.locationnoteapp.presentation.note_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.repository.MainRepository
import com.tatpol.locationnoteapp.presentation.NoteOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    private var _noteOrder = MutableLiveData(NoteOrder.BY_TITLE)

    val notes = Transformations.switchMap(_noteOrder) { order ->
        mainRepository.getNotes(order)
    }

    fun changeNoteOrder(order: NoteOrder) {
        _noteOrder.value = order
    }

    fun deleteNote(note: Note) {
        mainRepository.deleteNote(note)
    }

    fun signOut() {
        mainRepository.signOut()
    }
}