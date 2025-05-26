package com.app.tibibalance.ui.screens.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.domain.entities.User
import com.app.tibibalance.ui.components.navigation.BottomNavBar
import com.app.tibibalance.ui.components.navigation.bottomItems
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.screens.emotional.EmotionalCalendarScreen
import com.app.tibibalance.ui.screens.habits.HabitsScreen
import com.app.tibibalance.ui.screens.profile.show.ViewProfileScreen
import com.app.tibibalance.ui.screens.settings.SettingsScreen
import kotlinx.datetime.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
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
            composable(Screen.Emotions.route) { EmotionalCalendarScreen() }
            composable(Screen.Habits.route)   { HabitsScreen() }   // 👈 ya conectada
            composable(Screen.Profile.route)  { ViewProfileScreen(rootNav) }
            composable(Screen.Settings.route) {

                val demoUser = remember {
                    User(
                        uid = "demo",
                        email = "demo@tibi.app",
                        displayName = "Demo User",
                        photoUrl = "https://lh3.googleusercontent.com/a/ACg8ocIfGMQpHarn5OmTkSerBFPzC2--zfNppkug79c6aOxoVKC9U9OvIw=s96-c",
                        birthDate = LocalDate(2000, 1, 1)
                    )
                }

                SettingsScreen(
                    user          = demoUser,
                    navController = rootNav,               // usa nav raíz para flows externos
                    onNavigateUp  = { mainNav.popBackStack() },

                    /* —— Cuenta —— */
                    onDevices        = { /* TODO */ },
                    onAchievements   = { /* TODO */ },

                    /* —— Sesión —— */
                    onSignOut        = { /* TODO signOut() */ },
                    onDeleteAccount  = { /* TODO open DeleteAccount route */ },

                    /* —— Preferencias —— */
                    onChangeTheme       = { /* TODO updateTheme(it) */ },
                    onToggleGlobalNotif = { /* TODO setGlobalNotif(it) */ },
                    onToggleTTS         = { /* TODO setTTS(it) */ },

                    /* —— Legal —— */
                    onOpenTerms    = { /* TODO rootNav.navigate(Screen.Terms.route) */ },
                    onOpenPrivacy  = { /* TODO rootNav.navigate(Screen.Privacy.route) */ }
                )
            }
        }
    }
}
