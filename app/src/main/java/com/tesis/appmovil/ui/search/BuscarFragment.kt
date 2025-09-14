package com.tesis.appmovil.ui.search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.tesis.appmovil.R

class BuscarFragment : Fragment(), OnMapReadyCallback {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<MaterialCardView>
    private var gMap: GoogleMap? = null

    // Permisos (moderno)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        enableMyLocation(granted)
        if (granted) moveToLastLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_buscar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindBottomSheet(view)
        setupMap()
        checkLocationPermissionOrRequest()
    }

    // --- Bottom Sheet ---
    private fun bindBottomSheet(root: View) {
        val coordinator = root as? CoordinatorLayout
            ?: throw IllegalStateException("El root de fragment_buscar.xml debe ser CoordinatorLayout")

        val sheet = coordinator.findViewById<MaterialCardView>(R.id.bottom_sheet)
            ?: throw IllegalStateException("No se encontró @id/bottom_sheet dentro del CoordinatorLayout")

        bottomSheetBehavior = BottomSheetBehavior.from(sheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    // --- Google Map ---
    private fun setupMap() {
        val fm = childFragmentManager
        val tag = "map_child_fragment"
        val existing = fm.findFragmentByTag(tag) as? SupportMapFragment

        val mapFrag = existing ?: SupportMapFragment.newInstance().also {
            fm.beginTransaction()
                .replace(R.id.map_container, it, tag)
                .commitNow()
        }
        mapFrag.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map
        // Estilo básico (opcional)
        gMap?.uiSettings?.isMyLocationButtonEnabled = false
        gMap?.uiSettings?.isMapToolbarEnabled = false

        val hasFine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        enableMyLocation(hasFine || hasCoarse)
        if (hasFine || hasCoarse) moveToLastLocation()
        else requestLocationPermission()
    }

    private fun checkLocationPermissionOrRequest() {
        val hasFine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!(hasFine || hasCoarse)) {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun enableMyLocation(enabled: Boolean) {
        val map = gMap ?: return
        try {
            map.isMyLocationEnabled = enabled
        } catch (_: SecurityException) {
            // Si no hay permiso, Google Maps lanza SecurityException. Lo ignoramos y esperamos al launcher.
        }
    }

    private fun moveToLastLocation() {
        // Puedes reemplazar por el FusedLocationProvider si quieres la última ubicación real
        val defaultLatLng = LatLng(-12.0464, -77.0428) // Lima (ejemplo)
        gMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 14f))
    }
}
