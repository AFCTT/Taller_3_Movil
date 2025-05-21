package com.example.taller3

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.taller3.ui.theme.Taller3Theme
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity(), OnMapsSdkInitializedCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        Log.d("FirebaseInit", "Firebase initialized: ${FirebaseApp.getInstance().name}")

        // Inicializar Google Maps SDK
        try {
            MapsInitializer.initialize(applicationContext, Renderer.LATEST, this)
        } catch (e: Exception) {
            Log.e("MapScreen", "Failed to initialize Google Maps SDK: ${e.message}")
        }

        // Configurar la interfaz de Compose solo después de la inicialización
        setContent {
            Taller3Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    AppNavigation(navController)
                }
            }
        }
    }

    override fun onMapsSdkInitialized(renderer: Renderer) {
        when (renderer) {
            Renderer.LATEST -> Log.d("MapScreen", "Using latest renderer")
            Renderer.LEGACY -> Log.d("MapScreen", "Using legacy renderer")
        }
    }
}