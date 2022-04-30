package com.tatpol.locationnoteapp

import com.google.android.gms.maps.model.LatLng

object Constants {

    val BANGKOK_POSITION = LatLng(13.7563, 100.5018)

    const val NOTE_EVENT_REQUEST_KEY = "event_request_key"
    const val NOTE_EVENT_BUNDLE_KEY = "event_bundle_key"
    const val SNACKBAR_REQUEST_KEY = "snackbar_request_key"
    const val OPEN_SETTINGS_REQUEST_KEY = "open_settings_request_key"
    const val NOTES_COLLECTION_PATH = "notes"
    const val MIN_ZOOM = 10f
    const val MAX_ZOOM = 18f
    const val DEFAULT_ZOOM = 10f
    const val MID_ZOOM = 12f
}