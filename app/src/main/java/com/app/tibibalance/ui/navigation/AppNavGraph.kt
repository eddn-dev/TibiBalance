package com.app.tibibalance.ui.navigation

import android.os.Build
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // Added
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
// Removed: import androidx.navigation.compose.rememberNavController
import com.app.tibibalance.R
import com.app.tibibalance.ui.screens.auth.forgot.ForgotPasswordScreen
import com.app.tibibalance.ui.screens.auth.signin.SignInScreen
import com.app.tibibalance.ui.screens.auth.signup.SignUpScreen
import com.app.tibibalance.ui.screens.auth.verify.VerifyEmailScreen
import com.app.tibibalance.ui.screens.changepassword.ChangePasswordScreen
// import com.app.tibibalance.ui.screens.home.HomeScreen // HomeScreen might not be a direct route from here
import com.app.tibibalance.ui.screens.launch.LaunchScreen
import com.app.tibibalance.ui.screens.main.MainScreen
import com.app.tibibalance.ui.screens.onboarding.OnboardingPage
import com.app.tibibalance.ui.screens.onboarding.OnboardingRoute
import com.app.tibibalance.ui.screens.profile.edit.EditProfileScreen
import com.app.tibibalance.ui.screens.settings.notification.ConfigureNotificationScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController, startHabitId: String?) { // Modified signature
    NavHost(navController = navController, startDestination = Screen.Launch.route) {

        composable(Screen.Onboarding.route) {
            // lista de páginas (puedes externalizar a un `remember`)
            val pages = remember {
                listOf(
                    OnboardingPage(R.string.onb_title_1, R.string.onb_desc_1, R.raw.anim_health),
                    OnboardingPage(R.string.onb_title_2, R.string.onb_desc_2, R.raw.anim_habit),
                    OnboardingPage(R.string.onb_title_3, R.string.onb_desc_3, R.raw.anim_stats)
                )
            }
            OnboardingRoute(navController, pages)
        }

        composable(Screen.Launch.route)      { LaunchScreen(navController) }
        composable(Screen.SignIn.route)      { SignInScreen(navController) }
        composable(Screen.SignUp.route)      { SignUpScreen(navController) }
        composable(Screen.VerifyEmail.route) { VerifyEmailScreen(navController) }
        composable(Screen.Forgot.route)      { ForgotPasswordScreen(navController) }
        composable(Screen.Main.route)        { MainScreen(navController) } // MainScreen contains Habits, Stats, etc.
        composable(Screen.EditProfile.route) { EditProfileScreen(navController) }
        composable(Screen.ChangePassword.route) { ChangePasswordScreen(navController)}
        composable(Screen.ConfigureNotif.route) { ConfigureNotificationScreen(navController) }
        // TODO: Add route for habit detail: e.g., composable(Screen.HabitDetail.route) { ... }
    }

    LaunchedEffect(startHabitId, navController) {
        if (startHabitId != null) {
            Log.d("AppNavGraph", "Attempting to navigate due to startHabitId: $startHabitId")
            // Placeholder navigation: Navigate to the main screen which might contain the habits list.
            // A more specific route like "habit_detail/{habitId}" would be ideal.
            // For now, navigating to MainScreen ensures the app responds to the notification click.
            navController.navigate(Screen.Main.route) { // Or Screen.Habits.route if it exists and is more direct
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
            // The startHabitId is "consumed" by this navigation.
            // If MainActivity's intent is not cleared or changed, subsequent recompositions of AppNavGraph
            // due to other reasons might re-trigger this if startHabitId remains the same.
            // The removeExtra in TibiBalanceRoot is one attempt to mitigate this.
            // A ViewModel approach to signal "navigation handled" would be more robust.
        }
    }
}