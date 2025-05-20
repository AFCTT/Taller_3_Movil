package com.example.taller3.screens

import android.Manifest
import android.annotation.SuppressLint
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

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

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    cameraPositionState.position = CameraPositionState(
                        position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                    ).position
                }
            }
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {
            if (polylinePoints.isNotEmpty()) {
                Polyline(points = polylinePoints)
                Marker(
                    state = MarkerState(position = polylinePoints.last()),
                    title = "Tú"
                )
            }

            listOf(
                LatLng(4.715, -74.068),
                LatLng(4.716, -74.067),
                LatLng(4.717, -74.066)
            ).forEachIndexed { index, latLng ->
                Marker(
                    state = MarkerState(position = latLng),
                    title = "Usuario ${index + 1}"
                )
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


                        val locationCallback = object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                val loc = result.lastLocation ?: return
                                val latLng = LatLng(loc.latitude, loc.longitude)
                                polylinePoints.add(latLng)
                                dbRef.child(userId).child("lat").setValue(loc.latitude)
                                dbRef.child(userId).child("lng").setValue(loc.longitude)
                            }
                        }

                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            context.mainLooper
                        )
                    } else {
                        polylinePoints.clear()
                    }
                }
            )
        }
    }
}