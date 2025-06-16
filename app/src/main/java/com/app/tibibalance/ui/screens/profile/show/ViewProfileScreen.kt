/* ui/screens/profile/ViewProfileScreen.kt */
package com.app.tibibalance.ui.screens.profile.show

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.AchievementAccessItem
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.containers.ProfileContainer
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.utils.gradient
import com.app.tibibalance.ui.navigation.Screen
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/* ---------- Nuevo composable reutilizable ----------- */
@Composable
private fun InfoItem(
    label : String,
    value : String,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Subtitle(text = label)
        Spacer(Modifier.height(4.dp))
        Description(text = value)
    }
}

/* ---------------- Pantalla principal ---------------- */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ViewProfileScreen(
    navController: NavHostController,
    vm: ViewProfileViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(gradient())
    ) {
        when {
            ui.loading -> Centered("Cargando…")
            ui.error != null -> Centered(ui.error!!)
            ui.user?.displayName.isNullOrBlank() || ui.user?.email.isNullOrBlank() ->
                Centered("Cargando perfil…")
            ui.user != null -> ProfileContent(
                user = ui.user!!,
                onEdit = { navController.navigate(Screen.EditProfile.route) },
                onSignOut = vm::signOut,
                navController = navController
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ProfileContent(
    user         : com.app.domain.entities.User,
    onEdit       : () -> Unit,
    onSignOut    : () -> Unit,
    navController: NavHostController
) {
    val dateFmt   = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val birthDate = user.birthDate.toJavaLocalDate().format(dateFmt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .testTag("profile_section"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))

        FormContainer {

            /* ---- Foto de perfil ---- */
            if (user.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Imagen de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                )
            } else {
                ProfileContainer(
                    imageResId = R.drawable.avatar_placeholder,
                    size = 120.dp,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                )
            }

            Spacer(Modifier.height(12.dp))

            /* ---- Nombre ---- */
            Subtitle(text = user.displayName ?: "Sin nombre")

            Spacer(Modifier.height(24.dp))

            /* ---- Datos básicos (etiqueta + valor) ---- */
            InfoItem(label = "Fecha de nacimiento:", value = birthDate)
            Spacer(Modifier.height(16.dp))
            InfoItem(label = "Correo electrónico:",   value = user.email)

            Spacer(Modifier.height(28.dp))

            AchievementAccessItem(
                resId = R.drawable.ic_tibio_champion,
                label = "Ver logros",
                onClick = { navController.navigate(Screen.Achievements.route) }
            )

            Spacer(Modifier.height(28.dp))

            /* ---- Botones ---- */
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
                    modifier = Modifier.size(120.dp)
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
