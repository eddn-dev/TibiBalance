/**
 * @file    VerifyEmailScreen.kt
 * @ingroup ui_screens_auth
 * @brief   Composable para la pantalla de verificación de correo electrónico.
 *
 * @details
 * Esta pantalla se muestra después de que un usuario se registra con correo y contraseña,
 * o cuando un usuario que ha iniciado sesión pero no ha verificado su correo intenta
 * acceder a contenido protegido.
 *
 * Responsabilidades:
 * - Informar al usuario que se ha enviado un correo de verificación.
 * - Proveer una ilustración y texto instructivo.
 * - Ofrecer un botón para "Reenviar correo" si el usuario no lo recibió o expiró.
 * - Ofrecer un botón para que el usuario indique "Ya lo verifiqué", lo que desencadena
 * una comprobación del estado de verificación en el backend.
 * - Ofrecer una opción para "Cerrar sesión" si el usuario desea abandonar el proceso.
 * - Interactuar con [VerifyEmailViewModel] para manejar la lógica de reenvío,
 * verificación y cierre de sesión.
 * - Mostrar feedback al usuario mediante [com.app.tibibalance.ui.components.dialogs.ModalInfoDialog] para estados de carga,
 * éxito (correo reenviado, verificación exitosa) o errores.
 * - Navegar a [Screen.Launch] si el usuario cierra sesión.
 * - Navegar a [Screen.Main] si la verificación es exitosa.
 *
 * La UI utiliza un fondo degradado y componentes reutilizables como [Header],
 * [ImageContainer], [PrimaryButton], y [TextButtonLink].
 *
 * @see VerifyEmailViewModel ViewModel que gestiona la lógica y el estado de esta pantalla.
 * @see VerifyEmailUiState Estados de la UI para esta pantalla.
 * @see com.app.tibibalance.ui.components.dialogs.ModalInfoDialog Componente para mostrar diálogos de carga, éxito y error.
 * @see Header Componente para la barra superior de la pantalla.
 * @see ImageContainer Componente para mostrar la ilustración.
 * @see PrimaryButton Componente para los botones de acción principales.
 * @see TextButtonLink Componente para el enlace de "Cerrar sesión".
 * @see Screen Rutas de navegación de la aplicación.
 */
package com.app.tibibalance.ui.screens.auth.verify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.*
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.TextButtonLink
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.navigation.Screen

/**
 * @brief Composable principal para la pantalla de verificación de correo electrónico.
 *
 * @param nav El [NavController] utilizado para la navegación entre pantallas.
 * @param vm La instancia de [VerifyEmailViewModel] (inyectada por Hilt) que gestiona
 * la lógica y el estado de esta pantalla.
 */
@Composable
fun VerifyEmailScreen(
    nav: NavController,
    vm: VerifyEmailViewModel = hiltViewModel()
) {
    /* ---------- State ---------- */
    // Observa el estado de la UI emitido por el ViewModel.
    val uiState by vm.ui.collectAsState()

    /* ---------- React to side-effects (Navegación post-cierre de sesión) ---------- */
    // Efecto lanzado cuando uiState cambia, específicamente para manejar el cierre de sesión.
    LaunchedEffect(uiState) {
        if (uiState is VerifyEmailUiState.SignedOut) {
            // Navega a la pantalla de inicio (LaunchScreen).
            nav.navigate(Screen.Launch.route) {
                // Limpia la pila de navegación hasta LaunchScreen para evitar volver.
                popUpTo(Screen.Launch.route) { inclusive = true }
            }
            vm.clear() // Limpia el estado en el ViewModel a Idle.
        }
    }

    /* ---------- Dialog flags (para ModalInfoDialog) ---------- */
    // Determina si el ModalInfoDialog debe mostrarse y qué contenido tendrá.
    val loading  = uiState is VerifyEmailUiState.Loading // True si la VM está procesando algo.
    val success  = uiState as? VerifyEmailUiState.Success // Contiene mensaje de éxito si no es null.
    val error    = uiState as? VerifyEmailUiState.Error // Contiene mensaje de error si no es null.
    // El diálogo se muestra si está cargando, o si hay un mensaje de éxito o error.
    val showDialog = loading || success != null || error != null

    ModalInfoDialog(
        visible = showDialog, // Visibilidad del diálogo.
        loading = loading,    // Muestra spinner si está cargando.
        icon = when {      // Icono según el estado.
            success != null -> Icons.Default.Check
            error != null -> Icons.Default.Error
            else -> null
        },
        iconColor = when {      // Color del icono.
            success != null -> MaterialTheme.colorScheme.onPrimaryContainer
            error != null -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onPrimaryContainer
        },
        iconBgColor = when {    // Color de fondo para el contenedor del icono.
            success != null -> MaterialTheme.colorScheme.primaryContainer
            error != null -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        title = when {          // Título del diálogo.
            success != null -> "Listo"
            error != null -> "Error"
            else -> null
        },
        message = success?.message ?: error?.message, // Mensaje principal.
        primaryButton = when { // Botón primario del diálogo.
            success != null -> DialogButton("Aceptar") {
                vm.clear()
                if (success.goHome) {
                    nav.navigate(Screen.Launch.route) {          // 👈 cambia Main → Launch
                        popUpTo(Screen.Launch.route) { inclusive = true }
                    }
                }
            }

            error != null -> DialogButton("Aceptar") {
                vm.clear()
            }
            else -> null
        },
        // El diálogo no se puede descartar con el botón "Atrás" o pulsando fuera si está cargando.
        dismissOnBack = !loading,
        dismissOnClickOutside = !loading
    )

    /* ---------- Background ---------- */
    // Define un fondo degradado para la pantalla.
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF3EA8FE).copy(alpha = .25f), Color.White)
    )

    // Contenedor Box principal que ocupa toda la pantalla y aplica el fondo.
    Box(
        Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        /* ---------- Header ---------- */
        // Barra superior fija de la pantalla.
        Header(
            title          = stringResource(R.string.verify_email_title),
            showBackButton = false, // No se muestra botón de retroceso.
            modifier       = Modifier
                .fillMaxWidth()
                .background(Color.White) // Fondo blanco para el Header.
                .height(56.dp) // Altura estándar para la barra de aplicación.
                .align(Alignment.TopCenter) // Alinea el Header en la parte superior.
        )

        /* ---------- Content ---------- */
        // Columna para organizar el contenido principal de la pantalla.
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Padding para evitar solapamiento con el Header y para los bordes.
                .padding(top = 80.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // Centra el contenido horizontalmente.
            verticalArrangement = Arrangement.Center // Centra el contenido verticalmente.
        ) {

            /* Texto principal */
            Text(
                "¡Revisa tu correo!",
                style     = MaterialTheme.typography.headlineSmall, // Estilo de texto prominente.
                textAlign = TextAlign.Center // Texto centrado.
            )

            Spacer(Modifier.height(8.dp)) // Espacio vertical.

            /* Ilustración */
            // Muestra una imagen relacionada con el envío de correos.
            ImageContainer(
                resId = R.drawable.img_verify, // ID del recurso drawable.
                contentDescription = "Email enviado", // Descripción para accesibilidad.
                modifier = Modifier.size(300.dp) // Tamaño de la imagen.
            )

            Spacer(Modifier.height(8.dp))

            /* Subtítulo/Instrucción */
            Text(
                "Se ha enviado a tu correo un enlace\npara verificar tu cuenta",
                style     = MaterialTheme.typography.bodyMedium, // Estilo de texto estándar.
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(30.dp))

            /* Botón: Reenviar correo */
            PrimaryButton(
                text = "Reenviar correo",
                onClick = vm::resend, // Llama al método del ViewModel para reenviar.
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Altura del botón.
            )

            Spacer(Modifier.height(15.dp))

            /* Botón: Ya lo verifiqué */
            PrimaryButton(
                text = "Ya lo verifiqué",
                container = Color(0xFF3EA8FE), // Color de fondo personalizado.
                onClick = vm::verify, // Llama al método del ViewModel para verificar.
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(Modifier.height(20.dp))

            /* Enlace: Cerrar sesión */
            TextButtonLink(
                text = "Cerrar sesión",
                onClick = vm::signOut
            )
        }
    }
}