package com.example.taller3.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MenuScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Menú Principal", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                navController.navigate("profile")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mi perfil")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate("map")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mapa en tiempo real")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo("menu") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}
