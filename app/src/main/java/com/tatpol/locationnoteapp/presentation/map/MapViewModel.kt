package com.tatpol.locationnoteapp.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    val notes = notesRepository.notesFlow.asLiveData()
}