package com.tatpol.locationnoteapp.presentation.map

import com.tatpol.locationnoteapp.data.model.Note

sealed class MapMode {

    object NormalMode : MapMode()

    data class NavigationMode(val note: Note) : MapMode()
}
