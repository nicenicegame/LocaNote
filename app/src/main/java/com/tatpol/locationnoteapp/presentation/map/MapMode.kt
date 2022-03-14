package com.tatpol.locationnoteapp.presentation.map

import com.tatpol.locationnoteapp.data.model.Note

sealed class MapMode {

    object NormalMode : MapMode()

    data class RoutingMode(val note: Note) : MapMode()
}
