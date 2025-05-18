package com.app.tibibalance.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.tibibalance.ui.screens.auth.ForgotPasswordScreen
import com.app.tibibalance.ui.screens.auth.signin.SignInScreen
import com.app.tibibalance.ui.screens.auth.signup.SignUpScreen
import com.app.tibibalance.ui.screens.auth.verify.VerifyEmailScreen
import com.app.tibibalance.ui.screens.settings.ConfigureNotificationScreen
import com.app.tibibalance.ui.screens.launch.LaunchScreen
import com.app.tibibalance.ui.screens.profile.EditProfileScreen
import com.app.tibibalance.ui.screens.main.MainScreen
import com.app.tibibalance.ui.screens.settings.ChangePasswordScreenPreviewOnly
import com.app.tibibalance.ui.screens.settings.DeleteAccountScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Launch.route) {
        composable(Screen.Launch.route)      { LaunchScreen(navController) }
        composable(Screen.SignIn.route)      { SignInScreen(navController) }
        composable(Screen.SignUp.route)      { SignUpScreen(navController) }
        composable(Screen.VerifyEmail.route) { VerifyEmailScreen(navController) }
        composable(Screen.Forgot.route)      { ForgotPasswordScreen(navController) }
        composable(Screen.Main.route)        { MainScreen(navController) }

        composable(Screen.NotificationSettings.route) {
            ConfigureNotificationScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(Screen.EditPersonal.route) { EditProfileScreen(navController) }
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreenPreviewOnly(navController)
        }
        composable("delete_account/{isGoogleUser}") { backStackEntry ->
            val isGoogle = backStackEntry.arguments?.getString("isGoogleUser")?.toBooleanStrictOrNull() ?: false
            DeleteAccountScreen(navController, isGoogle)
        }
    }
}