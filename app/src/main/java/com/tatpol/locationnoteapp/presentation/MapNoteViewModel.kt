package com.tatpol.locationnoteapp.presentation

import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.tatpol.locationnoteapp.data.model.DirectionsResult
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.data.repository.MainRepository
import com.tatpol.locationnoteapp.presentation.create_edit.FormMode
import com.tatpol.locationnoteapp.presentation.map.MapMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MapNoteViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    application: Application
) : AndroidViewModel(application) {

    private val geocoder = Geocoder(application.applicationContext, Locale.getDefault())

    val notes = mainRepository.getNotes()

    val user = mainRepository.user

    private var _lastKnownLocation = MutableLiveData<Location?>(null)
    val lastKnownLocation: LiveData<Location?> get() = _lastKnownLocation

    val currentAddress = Transformations.switchMap(_lastKnownLocation) { location ->
        liveData {
            location?.let {
                emit(getAddressFromLocation(it.latitude, it.longitude))
            }
        }
    }

    private var _mapMode = MutableLiveData<MapMode>(MapMode.NormalMode)
    val mapMode: LiveData<MapMode> get() = _mapMode

    private var _formMode = MutableLiveData<FormMode>(FormMode.CreateMode)
    val formMode: LiveData<FormMode> get() = _formMode

    private var _routes = MutableLiveData<Resource<List<DirectionsResult.Route>>>()
    val routes: LiveData<Resource<List<DirectionsResult.Route>>> get() = _routes

    private var _createEditFormEvent = MutableLiveData<FormEvent>(FormEvent.Empty)
    val createEditFormEvent: LiveData<FormEvent> get() = _createEditFormEvent

    fun updateLastKnownLocation(location: Location) {
        _lastKnownLocation.value = location
    }

    fun setMapMode(mode: MapMode) {
        _mapMode.value = mode
    }

    fun setFormMode(mode: FormMode) {
        _formMode.value = mode
    }

    private suspend fun getAddressFromLocation(lat: Double, lng: Double) =
        withContext(Dispatchers.IO) {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (addresses.size == 0) return@withContext null
            return@withContext addresses[0].getAddressLine(0)
        }

    fun submitForm(title: String, description: String) {
        if (currentAddress.value != null && _lastKnownLocation.value != null) {
            if (title.isBlank() || description.isBlank()) {
                _createEditFormEvent.value =
                    FormEvent.Error("Note title and description must not be empty")
                return
            }
            when (_formMode.value) {
                is FormMode.CreateMode -> {
                    mainRepository.addNote(
                        Note(
                            title = title,
                            description = description,
                            lat = _lastKnownLocation.value?.latitude!!,
                            lng = _lastKnownLocation.value?.longitude!!,
                            address = currentAddress.value!!,
                        )
                    )
                    _createEditFormEvent.value = FormEvent.Success("New note created successfully")
                }
                is FormMode.EditMode -> {
                    mainRepository.updateNote(
                        (_formMode.value as FormMode.EditMode).note.copy(
                            title = title,
                            description = description
                        )
                    )
                    _createEditFormEvent.value = FormEvent.Success("Edited note successfully")
                }
                else -> Unit
            }
        } else {
            _createEditFormEvent.value = FormEvent.Error("Cannot submit. Please try again later.")
        }
    }

    fun displayNoteRoute(note: Note) {
        viewModelScope.launch {
            _routes.value = mainRepository.getNoteRoute(
                LatLng(_lastKnownLocation.value?.latitude!!, _lastKnownLocation.value?.longitude!!),
                LatLng(note.lat, note.lng)
            )
        }
    }
}