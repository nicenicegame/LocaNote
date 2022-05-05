package com.tatpol.locationnoteapp.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.PolyUtil
import com.tatpol.locationnoteapp.Constants.BANGKOK_POSITION
import com.tatpol.locationnoteapp.Constants.DEFAULT_ZOOM
import com.tatpol.locationnoteapp.Constants.MAX_ZOOM
import com.tatpol.locationnoteapp.Constants.MID_ZOOM
import com.tatpol.locationnoteapp.Constants.MIN_ZOOM
import com.tatpol.locationnoteapp.R
import com.tatpol.locationnoteapp.data.model.DirectionsResult
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.databinding.FragmentMapBinding
import com.tatpol.locationnoteapp.presentation.MapNoteViewModel
import com.tatpol.locationnoteapp.presentation.adapter.CustomInfoWindowAdapter
import dagger.hilt.android.AndroidEntryPoint

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("MissingPermission", "PotentialBehaviorOverride")
@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowLongClickListener {

    private lateinit var binding: FragmentMapBinding

    private lateinit var map: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private lateinit var locationRequest: LocationRequest

    private var locationPermissionGranted = false

    private var shouldCheckForLocationPermission = false

    private val viewModel: MapNoteViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val markers = mutableListOf<Marker>()

    private var polyLine: Polyline? = null

    private var notes: List<Note> = emptyList()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val accessCoarseLocationGranted =
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        val accessFineLocationGranted =
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)

        locationPermissionGranted = accessCoarseLocationGranted && accessFineLocationGranted
        updateLocationUi()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)

        initMap()
        binding.fabMyLocation.hide()

        return binding.root
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
        marker.hideInfoWindow()
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation.let {
                    viewModel.updateLastKnownLocation(it)
                }
            }
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            updateLocationUi()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun updateLocationUi() {
        if (locationPermissionGranted) {
            map.isMyLocationEnabled = true
            binding.fabMyLocation.show()
            getDeviceLocation()
            startLocationUpdates()
        } else {
            binding.fabMyLocation.hide()
            map.isMyLocationEnabled = false
            Snackbar.make(
                binding.root,
                getString(R.string.location_need_granted),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Manage") {
                    shouldCheckForLocationPermission = true

                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    val intent = Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = uri
                    }
                    startActivity(intent)
                }
                .show()
        }
    }

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
                        viewModel.displayNoteRoute(mapMode.note)
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
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_SHORT).show()
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
            polylineOptions.width(10f)
            polylineOptions.addAll(path)
            polyLine = map.addPolyline(polylineOptions)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(path[0], MID_ZOOM))
        }
    }

    override fun onResume() {
        super.onResume()
        if (shouldCheckForLocationPermission) {
            shouldCheckForLocationPermission = false
            getLocationPermission()
        }
        if (locationPermissionGranted) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}