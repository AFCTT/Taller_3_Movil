package com.example.taller3

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.taller3.screens.LoginScreen
import com.example.taller3.screens.RegisterScreen
import com.example.taller3.screens.MenuScreen
import com.example.taller3.screens.ProfileScreen
import com.example.taller3.screens.MapScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("menu") {
            MenuScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("map") {
            MapScreen(navController)
        }
    }
}
