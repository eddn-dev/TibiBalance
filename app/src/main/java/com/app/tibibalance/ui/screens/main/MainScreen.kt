package com.app.tibibalance.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding // Import the padding modifier
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.tibibalance.ui.components.navigation.BottomNavBar
import com.app.tibibalance.ui.components.navigation.bottomItems
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.screens.habits.HabitsScreen

@Composable
fun MainScreen(rootNav: NavHostController) {
    /* ── controlador privado para el bottom nav ── */
    val mainNav = rememberNavController()

    /* ── estado reactivo para la pestaña activa ── */
    val current = mainNav.currentBackStackEntryAsState()
    val currentRoute = current.value?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                items = bottomItems,
                selectedRoute = currentRoute,
                onItemClick = { route ->
                    /* evita duplicados en la pila */
                    if (route != currentRoute) {
                        mainNav.navigate(route) { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = mainNav,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route)     { /* TODO HomeScreen() */ }
            composable(Screen.Emotions.route) { /* TODO EmotionsScreen() */ }
            composable(Screen.Habits.route)   { HabitsScreen() }   // 👈 ya conectada
            composable(Screen.Profile.route)  { /* TODO ProfileScreen() */ }
            composable(Screen.Settings.route) { /* TODO SettingsScreen() */ }
        }
    }
}
