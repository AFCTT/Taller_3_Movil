package com.example.taller3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si el usuario está autenticado, lo mandamos al Menú
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(Intent(this, MenuActivity::class.java))
        } else {
            startActivity(Intent(this, MapActivity::class.java))
        }

        // Finaliza esta actividad para que no regrese aquí al presionar atrás
        finish()
    }
}
