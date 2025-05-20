package com.example.taller3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentMarker: Marker? = null
    private var polyline: Polyline? = null
    private val polylinePoints = mutableListOf<LatLng>()
    private var isTracking = false

    private val dbRef = FirebaseDatabase.getInstance().getReference("users")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Switch>(R.id.locationSwitch).setOnCheckedChangeListener { _, isChecked ->
            isTracking = isChecked
            if (isChecked) {
                startTracking()
                dbRef.child(userId).child("isOnline").setValue(true)
            } else {
                stopTracking()
                dbRef.child(userId).child("isOnline").setValue(false)
                polyline?.remove()
                currentMarker?.remove()
                polylinePoints.clear()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
    }

    private fun startTracking() {
        val locationRequest = LocationRequest.create().apply {
            interval = 4000
            fastestInterval = 2000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!isTracking) return
                val location = result.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)

                // Mover marcador
                currentMarker?.remove()
                currentMarker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Tú")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )

                // Mover cámara
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                // Agregar al recorrido
                polylinePoints.add(latLng)
                polyline?.remove()
                polyline = map.addPolyline(
                    PolylineOptions()
                        .addAll(polylinePoints)
                        .width(8f)
                        .color(0xFF1E88E5.toInt())
                )

                // Guardar en Firebase
                dbRef.child(userId).child("lat").setValue(location.latitude)
                dbRef.child(userId).child("lng").setValue(location.longitude)
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }
        map.isMyLocationEnabled = true
    }
}
