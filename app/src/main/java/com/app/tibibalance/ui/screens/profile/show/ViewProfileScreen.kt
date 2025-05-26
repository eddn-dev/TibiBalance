/* ui/screens/profile/ViewProfileScreen.kt */
package com.app.tibibalance.ui.screens.profile.show

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.containers.ProfileContainer
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.components.inputs.InputText
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.navigation.Screen
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ViewProfileScreen(
    navController: NavHostController,
    vm: ViewProfileViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF3EA8FE).copy(alpha = .25f), Color.White)
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        when {
            ui.loading -> Centered("Cargando…")

            ui.error != null -> Centered(ui.error!!)

            ui.user != null -> ProfileContent(
                user = ui.user!!,
                onEdit = { navController.navigate(Screen.EditProfile.route) },
                onSignOut = vm::signOut
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ProfileContent(
    user      : com.app.domain.entities.User,
    onEdit    : () -> Unit,
    onSignOut : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))

        FormContainer {

            if (user.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Imagen de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                )
            }
            else {
                ProfileContainer(
                    imageResId = R.drawable.avatar_placeholder,
                    size = 120.dp,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(2.dp))

            /* ---- nombre ---- */
            Subtitle(text = user.displayName ?: "Sin nombre")

            Spacer(Modifier.height(20.dp))

            /* ---- fecha nacimiento ---- */
            Subtitle(text = "Fecha de nacimiento:")
            val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            InputText(
                value         = user.birthDate.toJavaLocalDate().format(dateFmt),
                onValueChange = {},            // sólo lectura
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(Modifier.height(10.dp))

            /* ---- correo ---- */
            Subtitle(text = "Correo electrónico:")
            InputText(
                value         = user.email,
                onValueChange = {},
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(Modifier.height(22.dp))

            /* ---- botones ---- */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryButton(
                    text = "Editar perfil",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
                ImageContainer(
                    resId = R.drawable.ic_viewprofile,
                    contentDescription = "Cerrar sesión",
                    modifier = Modifier
                        .size(120.dp)
                )
            }
        }
    }
}

/* helper para pantalla centrada */
@Composable
private fun Centered(text: String) =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Description(text = text)
    }
