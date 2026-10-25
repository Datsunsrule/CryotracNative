package com.cryotrac.paranormal.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
            // Scope the ViewModel to the activity, not this nav entry, so the session
            // (timer, counters, recording, marks) survives a trip to the home screen.
            val activity = LocalContext.current as ComponentActivity
            MainScreen(
                onHome = { nav.navigate("home") { popUpTo("main") { inclusive = true } } },
                vm = viewModel(viewModelStoreOwner = activity)
            )
        }
    }
}
