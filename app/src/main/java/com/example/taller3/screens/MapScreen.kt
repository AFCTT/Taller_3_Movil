package com.example.taller3.screens

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import android.util.Log

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }
    val cameraPositionState = rememberCameraPositionState()
    val dbRef = FirebaseDatabase.getInstance().getReference("users")
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var isTracking by remember { mutableStateOf(false) }
    val polylinePoints = remember { mutableStateListOf<LatLng>() }
    val otherUsers = remember { mutableStateMapOf<String, MutableList<LatLng>>() }

    // Definir locationCallback fuera del Switch para usarlo en removeLocationUpdates
    var locationCallback: LocationCallback? by remember { mutableStateOf(null) }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                }
            }
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    // Listener para otros usuarios
    LaunchedEffect(Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                otherUsers.clear()
                snapshot.children.forEach { userSnapshot ->
                    val uid = userSnapshot.key ?: return@forEach
                    if (uid != userId && otherUsers.size < 100) {
                        val isOnline = userSnapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                        if (isOnline) {
                            val latitude = userSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                            val longitude = userSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                            val points = otherUsers.getOrPut(uid) { mutableStateListOf() }
                            points.add(LatLng(latitude, longitude))
                        } else {
                            otherUsers.remove(uid)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MapScreen", "Database error: ${error.message}")
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {
            // Ruta y marcador del usuario
            if (polylinePoints.isNotEmpty()) {
                Polyline(polylinePoints)
                Marker(
                    state = MarkerState(position = polylinePoints.last()),
                    title = "Tú"
                )
            }

            // Marcadores y rutas de otros usuarios
            otherUsers.forEach { (uid, points) ->
                if (points.isNotEmpty()) {
                    Polyline(points)
                    Marker(
                        state = MarkerState(position = points.last()),
                        title = "Usuario $uid"
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Compartir ubicación")
            Switch(
                checked = isTracking,
                onCheckedChange = { checked ->
                    isTracking = checked
                    dbRef.child(userId).child("isOnline").setValue(checked)

                    if (checked) {
                        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000L)
                            .setMinUpdateIntervalMillis(2000L)
                            .build()

                        locationCallback = object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                val loc = result.lastLocation ?: return
                                val latLng = LatLng(loc.latitude, loc.longitude)
                                polylinePoints.add(latLng)
                                dbRef.child(userId).child("latitude").setValue(loc.latitude)
                                dbRef.child(userId).child("longitude").setValue(loc.longitude)
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                            }
                        }

                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback!!,
                            context.mainLooper
                        )
                    } else {
                        locationCallback?.let { callback ->
                            fusedLocationClient.removeLocationUpdates(callback)
                        }
                        polylinePoints.clear()
                        dbRef.child(userId).child("latitude").setValue(0.0)
                        dbRef.child(userId).child("longitude").setValue(0.0)
                    }
                }
            )
        }
    }
}

fun createCustomMarker(color: Int): Bitmap {
    val size = 50
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply { this.color = color }
    canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2).toFloat(), paint)
    return bitmap
}