package com.example.taller3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser ?: return
    val userId = user.uid
    val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var newPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dbRef.get().addOnSuccessListener {
            name = it.child("name").getValue(String::class.java) ?: ""
            phone = it.child("phone").getValue(String::class.java) ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Perfil", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Correo: $email")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (name.isNotEmpty() && phone.isNotEmpty()) {
                dbRef.updateChildren(mapOf("name" to name, "phone" to phone))
                    .addOnSuccessListener {
                        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }) {
            Text("Actualizar")
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Nueva contraseña") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (newPassword.length >= 6) {
                auth.currentUser?.updatePassword(newPassword)?.addOnSuccessListener {
                    Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                }?.addOnFailureListener {
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Cambiar contraseña")
        }
    }
}
