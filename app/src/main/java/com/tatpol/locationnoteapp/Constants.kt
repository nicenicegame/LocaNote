package com.tatpol.locationnoteapp

import com.google.android.gms.maps.model.LatLng

object Constants {

    val BANGKOK_POSITION = LatLng(13.7563, 100.5018)

    const val NOTE_EVENT_REQUEST_KEY = "eventRequest"
    const val NOTE_EVENT_BUNDLE_KEY = "eventBundle"
    const val NOTES_COLLECTION_PATH = "notes"
    const val MIN_ZOOM = 10f
    const val MAX_ZOOM = 18f
    const val DEFAULT_ZOOM = 10f
}