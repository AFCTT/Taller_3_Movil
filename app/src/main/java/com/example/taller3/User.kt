package com.example.taller3

data class User(val name: String = "",
                val email: String = "",
                val phone: String = "",
                val isOnline: Boolean = false,
                val latitude: Double = 0.0,
                val longitude: Double = 0.0,
                val profilePictureUrl: String? = null // For profile picture
 )
