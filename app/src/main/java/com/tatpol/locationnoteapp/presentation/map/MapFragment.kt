package com.tatpol.locationnoteapp.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.tatpol.locationnoteapp.BuildConfig
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

    private lateinit var binding: FragmentMapBinding

    private lateinit var map: GoogleMap

    private lateinit var placesClient: PlacesClient

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationPermissionGranted = false

    private val viewModel: MapViewModel by viewModels()

    private val markers = mutableListOf<Marker>()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        updateLocationUi()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)
        binding.fabMyLocation.hide()
        binding.fabMyLocation.setOnClickListener {
            viewModel.lastKnownLocation?.let {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), 15f
                    )
                )
            }
        }
        initMap()
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false
        map.setMinZoomPreference(10f)
        map.setMaxZoomPreference(18f)
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))
        map.setOnMarkerClickListener(this)
        map.setOnInfoWindowLongClickListener(this)

        getLocationPermission()
        updateLocationUi()
        getDeviceLocation()
        subscribeUi()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))
        marker.showInfoWindow()
        return true
    }

    override fun onInfoWindowLongClick(marker: Marker) {
        marker.hideInfoWindow()
    }

    private fun initMap() {
        Places.initialize(requireActivity().applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(requireContext())

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) locationPermissionGranted = true
        else locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUi() {
        if (locationPermissionGranted) {
            map.isMyLocationEnabled = true
            binding.fabMyLocation.show()
        } else {
            binding.fabMyLocation.hide()
            map.isMyLocationEnabled = false
            getLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        if (!locationPermissionGranted) return
        if (viewModel.lastKnownLocation != null) return

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                viewModel.updateLastKnownLocation(location)

                if (viewModel.lastKnownLocation != null) {
                    val lastKnownLatLng = LatLng(
                        viewModel.lastKnownLocation!!.latitude,
                        viewModel.lastKnownLocation!!.longitude
                    )
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 10f))
                } else {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            Constants.BANGKOK_POSITION,
                            10f
                        )
                    )
                    binding.fabMyLocation.hide()
                }
            }
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
        viewModel.mapMode.observe(viewLifecycleOwner) { mapMode ->
            when (mapMode) {
                is MapMode.NormalMode -> {
                    binding.cvNavigation.visibility = View.GONE
                }
                is MapMode.NavigationMode -> {
                    binding.cvNavigation.visibility = View.VISIBLE
                }
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