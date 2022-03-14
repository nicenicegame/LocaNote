package com.tatpol.locationnoteapp.presentation

import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.*
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.repository.NotesRepository
import com.tatpol.locationnoteapp.presentation.create_edit.FormMode
import com.tatpol.locationnoteapp.presentation.map.MapMode
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MapNoteViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
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
            return@map addresses[0].getAddressLine(0)
        }
    }

    private var _mapMode = MutableLiveData<MapMode>(MapMode.NormalMode)
    val mapMode: LiveData<MapMode> get() = _mapMode

    private var _formMode = MutableLiveData<FormMode>(FormMode.CreateMode)
    val formMode: LiveData<FormMode> get() = _formMode

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
}