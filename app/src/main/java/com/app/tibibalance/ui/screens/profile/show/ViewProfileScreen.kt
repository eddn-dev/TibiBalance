/* ui/screens/profile/ViewProfileScreen.kt */
package com.app.tibibalance.ui.screens.profile.show

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.containers.ProfileContainer
import com.app.tibibalance.ui.components.buttons.AchievementAccessItem
import androidx.compose.ui.platform.testTag

import com.app.tibibalance.ui.components.inputs.InputText
import com.app.tibibalance.tutorial.tutorialTarget
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.components.utils.gradient
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ViewProfileScreen(
    navController: NavHostController,
    vm: ViewProfileViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val tutorialVm: com.app.tibibalance.tutorial.TutorialViewModel = hiltViewModel()

    Box(
        Modifier
            .fillMaxSize()
            .background(gradient())
    ) {
        when {
            ui.loading -> Centered("Cargando…")

            ui.error != null -> Centered(ui.error!!)

            ui.user != null -> ProfileContent(
                user = ui.user!!,
                onEdit = { navController.navigate(Screen.EditProfile.route) },
                onSignOut = vm::signOut,
                navController = navController,
                tutorialVm = tutorialVm
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
    navController: NavHostController,
    tutorialVm: com.app.tibibalance.tutorial.TutorialViewModel? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .testTag("profile_section")
            .let { mod -> tutorialVm?.let { mod.tutorialTarget(it, "profile_section") } ?: mod },
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


            AchievementAccessItem(
                resId = R.drawable.ic_tibio_champion, // ← tu recurso en drawable
                label = "Ver logros",
                onClick = {navController.navigate(Screen.Achievements.route)}
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
