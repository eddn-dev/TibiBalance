package com.app.tibibalance.ui.screens.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.tibibalance.ui.components.navigation.BottomNavBar
import com.app.tibibalance.ui.components.navigation.bottomItems
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.screens.emotional.EmotionalCalendarScreen
import com.app.tibibalance.ui.screens.habits.HabitsScreen
import com.app.tibibalance.ui.screens.home.HomeScreen
import com.app.tibibalance.ui.screens.profile.show.ViewProfileScreen
import com.app.tibibalance.ui.screens.settings.SettingsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(rootNav: NavHostController) {
    /* ── controlador privado para el bottom nav ── */
    val mainNav = rememberNavController()
    val tutorialVm: com.app.tibibalance.tutorial.TutorialViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    /* ── estado reactivo para la pestaña activa ── */
    val current = mainNav.currentBackStackEntryAsState()
    val currentRoute = current.value?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                items = bottomItems,
                selectedRoute = currentRoute,
                onItemClick = { route ->
                    if (route != currentRoute) {
                        mainNav.navigate(route) { launchSingleTop = true }
                    }
                },
                tutorialVm = tutorialVm
            )
        }
    ) { padding ->
        NavHost(
            navController = mainNav,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route)     { HomeScreen() }
            composable(Screen.Emotions.route) { EmotionalCalendarScreen() }
            composable(Screen.Habits.route)   { HabitsScreen() }
            composable(Screen.Profile.route)  { ViewProfileScreen(rootNav) }
            composable(Screen.Settings.route) {

                SettingsScreen(
                    navController = rootNav,               // usa nav raíz para flows externos
                )
            }
        }
    }
}
