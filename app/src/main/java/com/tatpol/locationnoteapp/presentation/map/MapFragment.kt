package com.tatpol.locationnoteapp.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.tatpol.locationnoteapp.Constants.BANGKOK_POSITION
import com.tatpol.locationnoteapp.Constants.DEFAULT_ZOOM
import com.tatpol.locationnoteapp.Constants.MAX_ZOOM
import com.tatpol.locationnoteapp.Constants.MID_ZOOM
import com.tatpol.locationnoteapp.Constants.MIN_ZOOM
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_BUNDLE_KEY
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_REQUEST_KEY
import com.tatpol.locationnoteapp.R
import com.tatpol.locationnoteapp.data.model.DirectionsResult
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

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationPermissionGranted = false

    private val viewModel: MapNoteViewModel by activityViewModels()

    private val markers = mutableListOf<Marker>()

    private var polyLine: Polyline? = null

    private var notes: List<Note> = emptyList()

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
                viewModel.displayNoteRoute(result.note)
            }
        }

        binding.fabMyLocation.hide()
    }

    @SuppressLint("PotentialBehaviorOverride")
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
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, MID_ZOOM))
        marker.showInfoWindow()
        return true
    }

    override fun onInfoWindowLongClick(marker: Marker) {
        val note = notes.find {
            it.lat == marker.position.latitude && it.lng == marker.position.longitude
        }
        viewModel.setMapMode(MapMode.RoutingMode(note!!))
        viewModel.displayNoteRoute(note)
        marker.hideInfoWindow()
    }

    private fun initMap() {
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
                    notes = result.data
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> Unit
            }
        }
        viewModel.mapMode.observe(viewLifecycleOwner) { mapMode ->
            when (mapMode) {
                is MapMode.NormalMode -> {
                    binding.apply {
                        cvNavigation.visibility = View.GONE
                        fabMyLocation.setImageResource(R.drawable.ic_my_location)
                        fabMyLocation.setOnClickListener { moveCameraToCurrentLocation() }
                    }
                }
                is MapMode.RoutingMode -> {
                    binding.apply {
                        cvNavigation.visibility = View.VISIBLE
                        fabMyLocation.setImageResource(R.drawable.ic_close)
                        fabMyLocation.setOnClickListener {
                            viewModel.setMapMode(MapMode.NormalMode)
                            polyLine?.remove()
                        }
                        tvNoteTitle.text = mapMode.note.title
                        tvNoteAddress.text = mapMode.note.address
                    }
                }
            }
        }
        viewModel.routes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    val routes = result.data
                    drawPolyline(routes)
                }
                is Resource.Error -> {

                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun moveCameraToCurrentLocation() {
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

    private fun drawPolyline(routes: List<DirectionsResult.Route>) {
        if (!routes.isNullOrEmpty()) {
            polyLine?.remove()
            val polylineOptions = PolylineOptions()
            // get first route
            val route = routes[0]
            val path = PolyUtil.decode(route.overviewPolyline.points)
            polylineOptions.color(Color.BLUE)
            polylineOptions.width(8f)
            polylineOptions.addAll(path)
            polyLine = map.addPolyline(polylineOptions)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(path[0], MID_ZOOM))
        }
    }
}