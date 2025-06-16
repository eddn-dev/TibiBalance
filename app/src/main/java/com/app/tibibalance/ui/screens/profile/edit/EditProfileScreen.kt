/* ui/screens/profile/EditProfileScreen.kt */
package com.app.tibibalance.ui.screens.profile.edit

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalAchievementDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.InputDate
import com.app.tibibalance.ui.components.inputs.InputEmail
import com.app.tibibalance.ui.components.inputs.InputText
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.gradient
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import kotlinx.datetime.LocalDate
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileScreen(
    nav: NavHostController,
    vm : EditProfileViewModel = hiltViewModel()
) {
    /* ---------------- State ---------------- */
    val ui by vm.state.collectAsState()

    /* ---------------- Logros ---------------- */
    var pendingAch by remember { mutableStateOf<AchievementUnlocked?>(null) }
    LaunchedEffect(Unit) {
        vm.unlocked.collect { pendingAch = it }
    }

    /* ---------------- Image picker ---------------- */
    var pendingCallback by remember { mutableStateOf<(Uri?) -> Unit>({}) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> pendingCallback(uri); uri?.let(vm::pickPhoto) }

    /* ---------------- Modales genéricos ---------------- */
    ModalInfoDialog(visible = ui.loading, loading = true)

    ModalInfoDialog(
        visible = ui.success,
        icon    = Icons.Default.Check,
        message = "Cambios guardados",
        primaryButton = DialogButton("Cerrar") {
            vm.consumeSuccess()
            /* Si hay logro pendiente lo mostraremos; si no, volvemos atrás */
            if (pendingAch == null) nav.popBackStack()
        }
    )

    ModalInfoDialog(
        visible = ui.error != null,
        icon    = Icons.Default.Error,
        iconColor = MaterialTheme.colorScheme.error,
        message = ui.error,
        primaryButton = DialogButton("Cerrar", vm::consumeError)
    )

    /* ---------------- Pantalla principal ---------------- */
    ui.user?.let { user ->
        var name      by remember { mutableStateOf(user.displayName.orEmpty()) }
        var birthDate by remember { mutableStateOf(user.birthDate) }
        var photoUri  by remember { mutableStateOf<Uri?>(null) }

        val ctx = LocalContext.current
        val dobPicker = remember {
            DatePickerDialog(
                ctx,
                { _, y, m, d -> birthDate = LocalDate(y, m + 1, d) },
                birthDate.year, birthDate.monthNumber - 1, birthDate.dayOfMonth
            ).apply {
                datePicker.maxDate = Calendar.getInstance()
                    .apply { add(Calendar.YEAR, -18) }.timeInMillis
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient())
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Title("Editar información personal")

            /* ---------- Foto ---------- */
            val photo = photoUri?.toString() ?: ui.photoUri ?: user.photoUrl
            if (photo != null) {
                AsyncImage(
                    model = photo,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable {
                            pendingCallback = { uri -> photoUri = uri }
                            picker.launch("image/*")
                        },
                    contentScale = ContentScale.Crop
                )
            } else {
                ImageContainer(
                    resId = R.drawable.avatar_placeholder,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable {
                            pendingCallback = { uri -> photoUri = uri }
                            picker.launch("image/*")
                        },
                    contentScale = ContentScale.Crop
                )
            }

            /* ---------- Formulario ---------- */
            FormContainer(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {

                InputText(
                    value         = name,
                    onValueChange = { name = it },
                    placeholder   = "Nombre de usuario",
                )

                Spacer(Modifier.height(12.dp))

                InputDate(
                    value   = "%02d/%02d/%04d".format(
                        birthDate.dayOfMonth, birthDate.monthNumber, birthDate.year
                    ),
                    label   = "Fecha de nacimiento",
                    onClick = { dobPicker.show() }
                )

                Spacer(Modifier.height(12.dp))

                InputEmail(
                    value         = user.email,
                    onValueChange = {},
                    readOnly      = true,
                    enabled       = false
                )

                Spacer(Modifier.height(12.dp))
                Subtitle("Contraseña")

                if (vm.canChangePassword) {
                    SettingRow("••••••••••••••") {
                        nav.navigate(Screen.ChangePassword.route)
                    }
                } else {
                    Text(
                        "Autenticado con Google – no se puede cambiar aquí.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            /* ---------- Acciones ---------- */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text     = "Guardar",
                    onClick  = { vm.save(name, birthDate, photoUri) },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text     = "Cancelar",
                    onClick  = { nav.popBackStack() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    /* ------------ Modal de logro desbloqueado -------------- */
    pendingAch?.let { ach ->
        ModalAchievementDialog(
            visible = true,
            iconResId = R.drawable.ic_tibio_camera,
            title     = "¡Logro desbloqueado!",
            message   = "${ach.name}\n${ach.description}",
            primaryButton = DialogButton("Aceptar") {
                pendingAch = null
                nav.popBackStack()
            }
        )
    }
}

/* ------------------------------------------------------------------ */
/* Helper fila contraseña                                             */
/* ------------------------------------------------------------------ */
@Composable
private fun SettingRow(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.Default.ChevronRight, null)
    }
}
