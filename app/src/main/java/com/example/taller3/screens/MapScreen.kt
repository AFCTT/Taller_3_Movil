package com.example.taller3.screens

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.*
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL

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
    var followUser by remember { mutableStateOf(true) }
    val polylinePoints = remember { mutableStateListOf<LatLng>() }
    val otherUsers = remember { mutableStateMapOf<String, MutableList<LatLng>>() }
    val userPhotos = remember { mutableStateMapOf<String, BitmapDescriptor>() }
    val userNames = remember { mutableStateMapOf<String, String>() }

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

    LaunchedEffect(Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                otherUsers.clear()
                snapshot.children.forEach { userSnapshot ->
                    val uid = userSnapshot.key ?: return@forEach
                    if (uid != userId) {
                        val isOnline = userSnapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                        val photoUrl = userSnapshot.child("photoUrl").getValue(String::class.java)
                        val name = userSnapshot.child("name").getValue(String::class.java) ?: "Usuario"
                        userNames[uid] = name

                        if (isOnline) {
                            val latitude = userSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                            val longitude = userSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                            val points = otherUsers.getOrPut(uid) { mutableStateListOf() }
                            points.add(LatLng(latitude, longitude))

                            if (!userPhotos.containsKey(uid) && !photoUrl.isNullOrEmpty()) {
                                snapshot.child(uid).ref.child("photoUrl").ref.get().addOnSuccessListener {
                                    val bmp = runBlocking { getBitmapFromUrl(photoUrl) }
                                    bmp?.let {
                                        val scaled = Bitmap.createScaledBitmap(it, 130, 130, false)
                                        val circular = getCircularBitmap(scaled)
                                        userPhotos[uid] = BitmapDescriptorFactory.fromBitmap(circular)
                                    }
                                }
                            }
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
            if (polylinePoints.isNotEmpty()) {
                Polyline(points = polylinePoints, width = 8f)
                Marker(state = MarkerState(position = polylinePoints.last()), title = "Tú")
            }

            otherUsers.forEach { (uid, points) ->
                val name = userNames[uid] ?: "Usuario"
                if (points.size > 1) {
                    for (i in 1 until points.size) {
                        val speed = calculateSpeed(points[i - 1], points[i])
                        Polyline(
                            points = listOf(points[i - 1], points[i]),
                            color = getColorForSpeed(speed),
                            width = 10f
                        )
                    }
                }

                val lastPoint = points.lastOrNull() ?: return@forEach
                val photo = userPhotos[uid] ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                Marker(
                    state = MarkerState(position = lastPoint),
                    title = name,
                    icon = photo
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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
                                    if (followUser) {
                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                                    }
                                }
                            }
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, context.mainLooper)
                        } else {
                            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
                            polylinePoints.clear()
                            dbRef.child(userId).child("latitude").setValue(0.0)
                            dbRef.child(userId).child("longitude").setValue(0.0)
                        }
                    }
                )
            }

            Column {
                Text("Seguir mi ubicación")
                Switch(
                    checked = followUser,
                    onCheckedChange = { followUser = it }
                )
            }
        }
    }
}

fun getColorForSpeed(speed: Float): Color {
    return when {
        speed < 5 -> Color.Blue
        speed < 15 -> Color.Green
        else -> Color.Red
    }
}

fun calculateSpeed(start: LatLng, end: LatLng): Float {
    val results = FloatArray(1)
    Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
    return results[0] / 4
}

suspend fun getBitmapFromUrl(url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connect()
            val input = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            null
        }
    }
}

fun getCircularBitmap(bitmap: Bitmap): Bitmap {
    val size = minOf(bitmap.width, bitmap.height)
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint().apply { isAntiAlias = true }
    val rect = Rect(0, 0, size, size)
    val rectF = RectF(rect)
    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawOval(rectF, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, null, rect, paint)
    return output
}
