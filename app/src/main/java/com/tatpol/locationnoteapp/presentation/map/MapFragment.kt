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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
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
import com.tatpol.locationnoteapp.Constants.BANGKOK_POSITION
import com.tatpol.locationnoteapp.Constants.DEFAULT_ZOOM
import com.tatpol.locationnoteapp.Constants.MAX_ZOOM
import com.tatpol.locationnoteapp.Constants.MIN_ZOOM
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_BUNDLE_KEY
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_REQUEST_KEY
import com.tatpol.locationnoteapp.R
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.databinding.FragmentMapBinding
import com.tatpol.locationnoteapp.presentation.EventType
import com.tatpol.locationnoteapp.presentation.MapNoteViewModel
import com.tatpol.locationnoteapp.presentation.NoteEvent
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

    private val viewModel: MapNoteViewModel by activityViewModels()

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()

        setFragmentResultListener(NOTE_EVENT_REQUEST_KEY) { _, bundle ->
            val result = bundle.get(NOTE_EVENT_BUNDLE_KEY) as NoteEvent
            if (result.type == EventType.SHOW_NOTE_ROUTE) {
                viewModel.setMapMode(MapMode.RoutingMode(result.note))
            }
        }

        binding.fabMyLocation.hide()
        binding.fabMyLocation.setOnClickListener {
            viewModel.lastKnownLocation.value?.let {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), DEFAULT_ZOOM
                    )
                )
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false
        map.setMinZoomPreference(MIN_ZOOM)
        map.setMaxZoomPreference(MAX_ZOOM)
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))
        map.setOnMarkerClickListener(this)
        map.setOnInfoWindowLongClickListener(this)

        getLocationPermission()
        updateLocationUi()
        getDeviceLocation()
        subscribeUi()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, DEFAULT_ZOOM))
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
        if (viewModel.lastKnownLocation.value != null) return

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                viewModel.updateLastKnownLocation(location)

                if (viewModel.lastKnownLocation.value != null) {
                    val lastKnownLatLng = LatLng(
                        viewModel.lastKnownLocation.value!!.latitude,
                        viewModel.lastKnownLocation.value!!.longitude
                    )
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, DEFAULT_ZOOM))
                } else {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            BANGKOK_POSITION,
                            DEFAULT_ZOOM
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
                    binding.cvNavigation.apply {
                        visibility = View.GONE
                    }
                }
                is MapMode.RoutingMode -> {
                    binding.apply {
                        cvNavigation.visibility = View.VISIBLE
                        tvNoteTitle.text = mapMode.note.title
                        tvNoteAddress.text = mapMode.note.address
                    }
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