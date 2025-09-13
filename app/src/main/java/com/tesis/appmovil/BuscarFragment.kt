package com.tesis.appmovil.ui.search

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import com.tesis.appmovil.R

class BuscarFragment : Fragment(R.layout.fragment_buscar), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var markerIdToPlace: Map<String, BeautyPlace> = emptyMap()

    // ====== Datos de ejemplo (3 locales de belleza en Lima) ======
    data class BeautyPlace(
        val id: String,
        val name: String,
        val address: String,
        val phone: String,
        val rating: Double,
        val reviews: Int,
        val hours: String,
        val latLng: LatLng,
        val services: List<String>
    )

    private val samplePlaces = listOf(
        BeautyPlace(
            id = "LUNA",
            name = "Luna Belleza & Spa",
            address = "Av. José Larco 450, Miraflores",
            phone = "+51 999 111 222",
            rating = 4.8,
            reviews = 120,
            hours = "Lun-Dom: 9:00 - 20:00",
            latLng = LatLng(-12.1211, -77.0308),
            services = listOf("Corte", "Color", "Peinado", "Spa facial", "Uñas")
        ),
        BeautyPlace(
            id = "GLOW",
            name = "Glow Nails Studio",
            address = "C. Los Conquistadores 500, San Isidro",
            phone = "+51 988 777 666",
            rating = 4.6,
            reviews = 95,
            hours = "Mar-Dom: 10:00 - 19:00",
            latLng = LatLng(-12.0987, -77.0365),
            services = listOf("Manicure", "Pedicure", "Semipermanente", "Diseños")
        ),
        BeautyPlace(
            id = "ANDINA",
            name = "Andina Hair Lounge",
            address = "Jockey Plaza, Monterrico, Surco",
            phone = "+51 987 654 321",
            rating = 4.7,
            reviews = 80,
            hours = "Lun-Sáb: 9:00 - 21:00; Dom: 10:00 - 18:00",
            latLng = LatLng(-12.0875, -76.9770),
            services = listOf("Corte", "Alisado", "Tratamientos", "Maquillaje")
        )
    )
    // ============================================================

    // Launcher para permiso (si niega, avisamos)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) enableMyLocation()
        else Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindBottomSheet(view)

        // Aseguramos que haya un SupportMapFragment en el contenedor
        val fm = childFragmentManager
        val existing = fm.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        val mapFrag = existing ?: SupportMapFragment.newInstance().also {
            fm.beginTransaction().replace(R.id.mapContainer, it).commitNow()
        }
        mapFrag.getMapAsync(this)

        // Barra de búsqueda
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)
        val btnSearch = view.findViewById<ImageButton>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            val q = etSearch.text?.toString().orEmpty()
            searchAndFocus(q)
        }
        etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAndFocus(v.text?.toString().orEmpty())
                true
            } else false
        }

        // Botón Mi ubicación (seguro)
        view.findViewById<View>(R.id.btnMyLocation)?.setOnClickListener {
            enableMyLocationAndCenter()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Centro de Lima por defecto
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-12.0464, -77.0428), 12f))

        // Habilitar "Mi ubicación" si ya hay permisos
        enableMyLocation()

        // Pintar los marcadores de ejemplo
        samplePlaces.forEach { place ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(place.latLng)
                    .title(place.name)
                    .snippet(place.address)
            )
            marker?.tag = place.id
        }
        markerIdToPlace = samplePlaces.associateBy { it.id }

        // Click en marcador -> abrir BottomSheet
        map.setOnMarkerClickListener { marker ->
            handleMarkerClick(marker)
            true
        }
    }

    private fun enableMyLocation() {
        if (::map.isInitialized && hasLocationPermission()) {
            try {
                map.isMyLocationEnabled = true
            } catch (_: SecurityException) {
                // En caso extremo de falta de permiso en runtime, ignoramos
            }
        }
    }

    @SuppressLint("MissingPermission") // Ya verificamos permiso adentro
    private fun enableMyLocationAndCenter() {
        if (!::map.isInitialized) {
            Toast.makeText(requireContext(), "El mapa aún no está listo", Toast.LENGTH_SHORT).show()
            return
        }
        if (!hasLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        // Activar capa de ubicación
        try {
            map.isMyLocationEnabled = true
        } catch (se: SecurityException) {
            Toast.makeText(requireContext(), "Sin permiso de ubicación", Toast.LENGTH_SHORT).show()
            return
        }

        // Ir a última ubicación conocida; si no hay, Lima
        val fused = LocationServices.getFusedLocationProviderClient(requireContext())
        fused.lastLocation
            .addOnSuccessListener { loc ->
                val target = if (loc != null) LatLng(loc.latitude, loc.longitude)
                else LatLng(-12.0464, -77.0428)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 13f))
            }
            .addOnFailureListener {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-12.0464, -77.0428), 13f))
            }
    }

    // ====== BottomSheet ======
    private fun bindBottomSheet(root: View) {
        bottomSheetBehavior = BottomSheetBehavior.from(root.findViewById(R.id.bottomSheet))
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        root.findViewById<View>(R.id.btnClose).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun handleMarkerClick(marker: Marker) {
        val tag = marker.tag as? String ?: return
        val place = markerIdToPlace[tag] ?: return
        showPlaceBottomSheet(place)
    }

    private fun showPlaceBottomSheet(place: BeautyPlace) {
        val sheet = requireView().findViewById<View>(R.id.bottomSheet)
        sheet.findViewById<TextView>(R.id.txtName).text = place.name
        sheet.findViewById<TextView>(R.id.txtRating).text = "★ ${place.rating} (${place.reviews} reseñas)"
        sheet.findViewById<TextView>(R.id.txtAddress).text = place.address
        sheet.findViewById<TextView>(R.id.txtHours).text = place.hours
        sheet.findViewById<TextView>(R.id.txtServices).text = "Servicios: " + place.services.joinToString(", ")

        // Llamar
        sheet.findViewById<View>(R.id.btnCall).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.phone}"))
            startActivity(intent)
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    // =========================

    // ====== Búsqueda simple entre los 3 locales ======
    private fun norm(s: String): String =
        java.text.Normalizer.normalize(s.lowercase(), java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

    private fun searchAndFocus(query: String) {
        if (query.isBlank()) {
            Toast.makeText(requireContext(), "Escribe algo para buscar", Toast.LENGTH_SHORT).show()
            return
        }

        val qn = norm(query)
        val matches = samplePlaces.filter { p ->
            norm(p.name).contains(qn) || norm(p.address).contains(qn)
        }

        if (matches.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontró en los ejemplos", Toast.LENGTH_SHORT).show()
            return
        }

        if (matches.size == 1) {
            val p = matches.first()
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(p.latLng, 16f))
            showPlaceBottomSheet(p)
        } else {
            val builder = LatLngBounds.Builder()
            matches.forEach { builder.include(it.latLng) }
            val bounds = builder.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            showPlaceBottomSheet(matches.first())
        }
    }
    // ================================================
}
