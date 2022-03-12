package com.tatpol.locationnoteapp.presentation.map

import android.location.Location
import androidx.lifecycle.*
import com.tatpol.locationnoteapp.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    val notes = notesRepository.notesFlow.asLiveData()

    private var _lastKnownLocation: Location? = null
    val lastKnownLocation get() = _lastKnownLocation

    private var _mapMode = MutableLiveData<MapMode>()
    val mapMode: LiveData<MapMode> get() = _mapMode

    init {
        _mapMode.value = MapMode.NormalMode
    }

    fun updateLastKnownLocation(location: Location) {
        _lastKnownLocation = location
    }
}