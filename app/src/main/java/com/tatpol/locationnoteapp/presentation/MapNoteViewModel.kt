package com.tatpol.locationnoteapp.presentation

import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.tatpol.locationnoteapp.data.model.DirectionsResult
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.data.repository.NotesRepository
import com.tatpol.locationnoteapp.presentation.create_edit.FormMode
import com.tatpol.locationnoteapp.presentation.map.MapMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MapNoteViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    application: Application
) : AndroidViewModel(application) {

    private val geocoder = Geocoder(application, Locale.getDefault())

    val notes = notesRepository.notes

    val user = notesRepository.user

    private var _lastKnownLocation = MutableLiveData<Location?>(null)
    val lastKnownLocation: LiveData<Location?> get() = _lastKnownLocation

    val currentAddress = Transformations.map(_lastKnownLocation) { location ->
        location?.let {
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            if (addresses.size == 0) return@map null
            return@map addresses[0].getAddressLine(0)
        }
    }

    private var _mapMode = MutableLiveData<MapMode>(MapMode.NormalMode)
    val mapMode: LiveData<MapMode> get() = _mapMode

    private var _formMode = MutableLiveData<FormMode>(FormMode.CreateMode)
    val formMode: LiveData<FormMode> get() = _formMode

    private var _routes = MutableLiveData<Resource<List<DirectionsResult.Route>>>()
    val routes: LiveData<Resource<List<DirectionsResult.Route>>> get() = _routes

    fun updateLastKnownLocation(location: Location) {
        _lastKnownLocation.value = location
    }

    fun setMapMode(mode: MapMode) {
        _mapMode.value = mode
    }

    fun setFormMode(mode: FormMode) {
        _formMode.value = mode
    }

    fun submitForm(title: String, description: String) {
        if (currentAddress.value != null && _lastKnownLocation.value != null) {
            when (_formMode.value) {
                is FormMode.CreateMode -> {
                    notesRepository.addNote(
                        Note(
                            title = title,
                            description = description,
                            lat = _lastKnownLocation.value?.latitude!!,
                            lng = _lastKnownLocation.value?.longitude!!,
                            address = currentAddress.value!!,
                        )
                    )
                }
                is FormMode.EditMode -> {
                    notesRepository.updateNote(
                        (_formMode.value as FormMode.EditMode).note.copy(
                            title = title,
                            description = description
                        )
                    )
                }
                else -> Unit
            }
        }
    }

    fun displayNoteRoute(note: Note) {
        viewModelScope.launch {
            _routes.value = notesRepository.getNoteRoute(
                LatLng(_lastKnownLocation.value?.latitude!!, _lastKnownLocation.value?.longitude!!),
                LatLng(note.lat, note.lng)
            )
        }
    }
}