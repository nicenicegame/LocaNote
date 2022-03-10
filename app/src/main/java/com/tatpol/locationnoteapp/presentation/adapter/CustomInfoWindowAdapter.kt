package com.tatpol.locationnoteapp.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.tatpol.locationnoteapp.databinding.CustomInfoWindowBinding

class CustomInfoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {

    private val binding = CustomInfoWindowBinding.inflate(LayoutInflater.from(context))

    override fun getInfoContents(marker: Marker): View {
        binding.tvTitle.text = marker.title
        binding.tvSnippet.text = marker.snippet
        return binding.root
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}