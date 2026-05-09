package com.cryotrac.paranormal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cryotrac.paranormal.ui.screens.HomeScreen
import com.cryotrac.paranormal.ui.screens.MainScreen

@Composable
fun NavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(onEnter = {
                nav.navigate("main") { popUpTo("home") { inclusive = true } }
            })
        }
        composable("main") {
            MainScreen(onHome = {
                nav.navigate("home") { popUpTo("main") { inclusive = true } }
            })
        }
    }
}
