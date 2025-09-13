package com.tesis.appmovil

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.Fragment
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.tesis.appmovil.R
import com.google.android.gms.tasks.CancellationTokenSource

class BuscarFragment : Fragment(R.layout.fragment_buscar), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inserta el MapFragment en el contenedor del layout del fragmento
        val fm = childFragmentManager
        var mapFrag = fm.findFragmentByTag("mapFrag") as SupportMapFragment?
        if (mapFrag == null) {
            mapFrag = SupportMapFragment.newInstance()
            fm.beginTransaction()
                .replace(R.id.map_container, mapFrag, "mapFrag")
                .commit()
        }
        mapFrag.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        moveToLimaFallback() // vista por defecto

        if (hasLocationPermission()) {
            enableMyLocation()
            moveToCurrentLocation(16f)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableMyLocation()
                moveToCurrentLocation(16f)
            } else {
                moveToLimaFallback()
            }
        }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!hasLocationPermission()) return
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation(zoom: Float) {
        if (!hasLocationPermission()) return

        // 1) usa última ubicación cacheada
        fused.lastLocation.addOnSuccessListener { last ->
            if (last != null) {
                val here = LatLng(last.latitude, last.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(here, zoom))
            } else {
                // 2) si no hay cache, pide una lectura puntual de alta precisión
                val req = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()
                val cts = CancellationTokenSource()
                fused.getCurrentLocation(req, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            val here = LatLng(loc.latitude, loc.longitude)
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(here, zoom))
                        } else {
                            moveToLimaFallback()
                        }
                    }
                    .addOnFailureListener { moveToLimaFallback() }
            }
        }.addOnFailureListener { moveToLimaFallback() }
    }

    private fun moveToLimaFallback() {
        val lima = LatLng(-12.0464, -77.0428)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 11f))
    }
}