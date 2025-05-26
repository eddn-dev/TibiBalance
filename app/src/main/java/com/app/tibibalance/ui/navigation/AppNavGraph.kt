package com.app.tibibalance.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.tibibalance.R
import com.app.tibibalance.ui.screens.auth.forgot.ForgotPasswordScreen
import com.app.tibibalance.ui.screens.auth.signin.SignInScreen
import com.app.tibibalance.ui.screens.auth.signup.SignUpScreen
import com.app.tibibalance.ui.screens.auth.verify.VerifyEmailScreen
import com.app.tibibalance.ui.screens.launch.LaunchScreen
import com.app.tibibalance.ui.screens.main.MainScreen
import com.app.tibibalance.ui.screens.onboarding.OnboardingPage
import com.app.tibibalance.ui.screens.onboarding.OnboardingRoute
import com.app.tibibalance.ui.screens.profile.EditProfileScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
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
        composable(Screen.Main.route)        { MainScreen(navController) }
        composable(Screen.EditProfile.route) { EditProfileScreen(navController)}
    }
}