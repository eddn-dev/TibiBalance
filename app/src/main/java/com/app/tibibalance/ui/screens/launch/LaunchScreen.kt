/**
 * @file      LaunchScreen.kt
 * @ingroup   ui_screens_launch
 * @brief     Pantalla de arranque y ruteo condicional.
 *
 * @details
 *  UI:
 *    • Fondo degradado vertical con color primario.
 *    • Logo (ImageContainer).
 *    • Animación Lottie in-loop.
 *    • Lema/subtítulo centrado.
 *    • Botones “Iniciar sesión” y “Registrarse”.
 *
 *  Navegación:
 *    - Si no hay sesión -> muestra botones.
 *    - Si logueado & verificado -> navega a Main eliminar Launch del back-stack.
 *    - Si logueado & no verificado -> navega a VerifyEmail.
 */
package com.app.tibibalance.ui.screens.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.utils.gradient
import com.app.tibibalance.ui.navigation.Screen

@Composable
fun LaunchScreen(
    nav: NavController,
    vm: LaunchViewModel = hiltViewModel()
) {
    /* ── 1 ▸ estado de sesión ─────────────────────────────────────────── */
    val session by vm.sessionState.collectAsState()

    if (session == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
        return
    }

    /* side-effect de navegación */
    LaunchedEffect(session) {
        session?.let { s ->
            when {
                !s.loggedIn -> Unit                     // sigue en Launch
                !s.verified -> nav.navigate(Screen.VerifyEmail.route) {
                    popUpTo(Screen.Launch.route) { inclusive = true }
                }
                s.onboardingCompleted == false -> nav.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Launch.route) { inclusive = true }
                }
                s.onboardingCompleted == true -> nav.navigate(Screen.Main.route) {
                    popUpTo(Screen.Launch.route) { inclusive = true }
                }
            }
        }
    }

    if (session!!.loggedIn) return

    /* ── 3 ▸ fondo degradado ──────────────────────────────────────────── */
    val bg = gradient()

    /* ── 4 ▸ UI principal ─────────────────────────────────────────────── */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ImageContainer(
            resId = R.drawable.img_launch,  // tu drawable
            modifier = Modifier.height(200.dp),
            contentDescription = null
        )

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.tibianimation)
        )
        LottieAnimation(
            composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(160.dp)
        )

        Subtitle(
            text = stringResource(R.string.launch_subtitle),
            modifier = Modifier.padding(top = 8.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PrimaryButton(
                text = stringResource(R.string.btn_sign_in),
                onClick = { nav.navigate(Screen.SignIn.route) }
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = stringResource(R.string.btn_sign_up),
                onClick = { nav.navigate(Screen.SignUp.route) }
            )
        }
    }
}
