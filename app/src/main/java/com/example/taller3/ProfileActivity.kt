package com.example.taller3

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailTextView: TextView
    private lateinit var btnUpdate: Button
    private lateinit var btnChangePassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

        nameEditText = findViewById(R.id.editName)
        phoneEditText = findViewById(R.id.editPhone)
        emailTextView = findViewById(R.id.txtEmail)
        btnUpdate = findViewById(R.id.btnUpdateProfile)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        // Mostrar datos actuales
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                nameEditText.setText(snapshot.child("name").getValue(String::class.java))
                phoneEditText.setText(snapshot.child("phone").getValue(String::class.java))
                emailTextView.text = snapshot.child("email").getValue(String::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        })

        // Guardar cambios en nombre y teléfono
        btnUpdate.setOnClickListener {
            val newName = nameEditText.text.toString()
            val newPhone = phoneEditText.text.toString()

            if (newName.isNotEmpty() && newPhone.isNotEmpty()) {
                val updates = mapOf(
                    "name" to newName,
                    "phone" to newPhone
                )
                dbRef.updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Cambiar contraseña
        btnChangePassword.setOnClickListener {
            val input = EditText(this)
            input.hint = "Nueva contraseña"

            AlertDialog.Builder(this)
                .setTitle("Cambiar contraseña")
                .setView(input)
                .setPositiveButton("Cambiar") { _, _ ->
                    val newPassword = input.text.toString()
                    if (newPassword.length >= 6) {
                        auth.currentUser?.updatePassword(newPassword)
                            ?.addOnSuccessListener {
                                Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener {
                                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}
