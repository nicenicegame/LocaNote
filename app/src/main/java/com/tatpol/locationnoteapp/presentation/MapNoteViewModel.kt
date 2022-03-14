package com.tatpol.locationnoteapp.presentation

import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.*
import com.tatpol.locationnoteapp.Constants
import com.tatpol.locationnoteapp.data.repository.NotesRepository
import com.tatpol.locationnoteapp.presentation.create_edit.FormMode
import com.tatpol.locationnoteapp.presentation.map.MapMode
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

private const val TAG = "MapNoteViewModel"

@HiltViewModel
class MapNoteViewModel @Inject constructor(
    notesRepository: NotesRepository,
    application: Application
) : AndroidViewModel(application) {

    private val geocoder = Geocoder(application, Locale.getDefault())

    val notes = notesRepository.notesFlow.asLiveData()

    private var _lastKnownLocation = MutableLiveData<Location?>(null)
    val lastKnownLocation: LiveData<Location?> get() = _lastKnownLocation

    val currentAddress = Transformations.map(_lastKnownLocation) { location ->
        location?.let {
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            if (addresses.size == 0) return@map null
            val returnedAddress = addresses[0]
            var fullAddress = ""
            for (line in 0..returnedAddress.maxAddressLineIndex) {
                fullAddress += returnedAddress.getAddressLine(line) + "\n"
            }
            return@map fullAddress
        }
    }

    private var _mapMode = MutableLiveData<MapMode>(MapMode.NormalMode)
    val mapMode: LiveData<MapMode> get() = _mapMode

    private var _formMode = MutableLiveData<FormMode>(FormMode.CreateMode)
    val formMode: LiveData<FormMode> get()= _formMode

    fun updateLastKnownLocation(location: Location) {
        _lastKnownLocation.value = location
    }

    fun setMapMode(mode: MapMode) {
        _mapMode.value = mode
    }

    fun setFormMode(mode: FormMode) {
        _formMode.value = mode
    }
}