package com.tatpol.locationnoteapp.presentation.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.tatpol.locationnoteapp.Constants
import com.tatpol.locationnoteapp.R
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.databinding.FragmentMapBinding
import com.tatpol.locationnoteapp.presentation.adapter.CustomInfoWindowAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowLongClickListener {

    private lateinit var map: GoogleMap

    private val viewModel: MapViewModel by viewModels()

    private val markers = mutableListOf<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMapBinding.inflate(inflater)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMinZoomPreference(10f)
        map.setMaxZoomPreference(18f)
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.BANGKOK_POSITION, 10f))
        map.setOnMarkerClickListener(this)
        map.setOnInfoWindowLongClickListener(this)

        subscribeUi()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 18f))
        marker.showInfoWindow()
        return true
    }

    override fun onInfoWindowLongClick(marker: Marker) {
        marker.hideInfoWindow()
    }

    private fun subscribeUi() {
        viewModel.notes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    updateMarkers(result.data)
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun updateMarkers(notes: List<Note>) {
        markers.forEach { it.remove() }
        notes.forEach { note ->
            val position = LatLng(note.lat, note.lng)
            val markerOptions = MarkerOptions()
                .position(position)
                .title(note.title)
                .snippet(getString(R.string.long_click_action))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            val newMarker = map.addMarker(markerOptions)
            markers.add(newMarker!!)
        }
    }
}