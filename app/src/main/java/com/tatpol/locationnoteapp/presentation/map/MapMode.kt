package com.tatpol.locationnoteapp.presentation.map

import com.google.android.gms.maps.model.LatLng

sealed class MapMode {

    object NormalMode : MapMode()

    data class NavigationMode(val position: LatLng) : MapMode()

}
