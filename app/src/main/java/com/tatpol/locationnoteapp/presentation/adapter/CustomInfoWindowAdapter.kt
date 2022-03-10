package com.tatpol.locationnoteapp.presentation.adapter

import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.tatpol.locationnoteapp.presentation.views.InfoWindowView

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        val infoWindow = InfoWindowView(context)
        infoWindow.setInfoWindowContent(marker.title, marker.snippet)
        return infoWindow
    }
}