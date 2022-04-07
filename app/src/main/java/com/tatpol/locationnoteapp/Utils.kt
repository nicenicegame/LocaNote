package com.tatpol.locationnoteapp

import com.google.android.gms.maps.model.LatLng

fun LatLng.toFormattedString(): String {
    return "$latitude,$longitude"
}