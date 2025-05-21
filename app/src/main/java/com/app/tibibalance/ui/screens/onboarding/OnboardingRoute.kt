/**
 * @file    OnboardingRoute.kt
 * @ingroup ui_screens_onboarding
 * @brief   Glue entre ViewModel y OnboardingScreen.
 */
package com.app.tibibalance.ui.screens.onboarding

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.tibibalance.ui.navigation.Screen
import kotlinx.coroutines.flow.filter

@Composable
fun OnboardingRoute(
    nav: NavHostController,
    pages: List<OnboardingPage>,              // re-usa tu modelo
    vm: OnboardingViewModel = hiltViewModel()
) {
    /* 1 ▸ en cuanto el usuario *ya* lo haya completado, salta fuera */
    val completed by vm.status.collectAsState()
    LaunchedEffect(completed.tutorialCompleted) {
        if (completed.tutorialCompleted) {
            nav.navigate(Screen.Main.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
    }

    /* 2 ▸ UI */
    OnboardingScreen(
        pages = pages,
        onComplete = {
            vm.completeTutorial()             // marcar & sync
            // navegación ocurre vía LaunchedEffect ↑
        },
        viewModel = vm                       // para las composiciones Lottie
    )
}
