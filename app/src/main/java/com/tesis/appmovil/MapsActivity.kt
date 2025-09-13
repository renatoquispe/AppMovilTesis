package com.tesis.appmovil

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    // Lanzador para pedir permisos en tiempo de ejecución
    private val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val granted =
                result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                enableMyLocation()
                moveToCurrentLocation(16f)
            } else {
                showLimaFallback()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Si ya hay permiso, activar capa "Mi ubicación" y centrar
        if (hasLocationPermission()) {
            enableMyLocation()
            moveToCurrentLocation(16f)
        } else {
            // Pedir permisos
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission") // Ya comprobamos permiso antes de llamar
    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            mMap.isMyLocationEnabled = true
        }
    }

    /** Centra la cámara en la ubicación actual si es posible */
    @SuppressLint("MissingPermission") // Ya comprobamos permiso antes de llamar
    private fun moveToCurrentLocation(zoom: Float = 16f) {
        if (!hasLocationPermission()) return

        // 1) Intenta usar la última ubicación cacheada
        fusedLocationClient.lastLocation.addOnSuccessListener { last ->
            if (last != null) {
                val here = LatLng(last.latitude, last.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(here, zoom))
            } else {
                // 2) Si es null, pide una lectura puntual de alta precisión
                val cts = CancellationTokenSource()
                fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            val here = LatLng(loc.latitude, loc.longitude)
                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(here, zoom)
                            )
                        } else {
                            showLimaFallback()
                        }
                    }
                    .addOnFailureListener {
                        showLimaFallback()
                    }
            }
        }.addOnFailureListener {
            showLimaFallback()
        }
    }

    private fun showLimaFallback() {
        val lima = LatLng(-12.0464, -77.0428)
        mMap.addMarker(MarkerOptions().position(lima).title("Lima, Perú"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 14f))
    }
}
