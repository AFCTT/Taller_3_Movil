package com.example.taller3

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // nombre de tu XML

        auth = FirebaseAuth.getInstance()

        val nameField = findViewById<EditText>(R.id.editName)
        val emailField = findViewById<EditText>(R.id.editEmail)
        val passwordField = findViewById<EditText>(R.id.editPassword)
        val phoneField = findViewById<EditText>(R.id.editPhone)
        val registerButton = findViewById<Button>(R.id.btnRegister)

        registerButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val phone = phoneField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear cuenta en Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                    val userData = mapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "isOnline" to false,
                        "lat" to 0.0,
                        "lng" to 0.0
                    )

                    // Guardar datos en la Realtime Database
                    database.child("users").child(uid).setValue(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar datos: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
