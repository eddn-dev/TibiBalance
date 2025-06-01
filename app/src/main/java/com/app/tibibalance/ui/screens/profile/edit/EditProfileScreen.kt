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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.InputDate
import com.app.tibibalance.ui.components.inputs.InputEmail
import com.app.tibibalance.ui.components.inputs.InputText
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.components.utils.gradient
import kotlinx.datetime.LocalDate
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileScreen(
    nav: NavHostController,
    vm : EditProfileViewModel = hiltViewModel()
) {
    val ui by vm.state.collectAsState()

    /* ---------------- selector imagen ---------------- */
    var pendingCallback by remember {
        mutableStateOf<(Uri?) -> Unit>({})
    }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        pendingCallback(uri)
        uri?.let(vm::pickPhoto)
    }

    /* ---------------- diálogos ---------------- */
    ModalInfoDialog(visible = ui.loading, loading = true)

    ModalInfoDialog(
        visible = ui.success,
        icon = Icons.Default.Check,
        message = "Cambios guardados",
        primaryButton = DialogButton("Cerrar") {
            vm.consumeSuccess()
            nav.popBackStack()
        }
    )

    ModalInfoDialog(
        visible = ui.error != null,
        icon = Icons.Default.Error,
        iconColor = MaterialTheme.colorScheme.error,
        message = ui.error,
        primaryButton = DialogButton("Cerrar") { vm.consumeError() }
    )

    /* ---------------- pantalla principal ---------------- */
    ui.user?.let { user ->
        var name      by remember { mutableStateOf(user.displayName.orEmpty()) }
        var birthDate by remember { mutableStateOf(user.birthDate) }
        var photoUri  by remember { mutableStateOf<Uri?>(null) }

        /* --- date-picker --- */
        val ctx = LocalContext.current
        val pickerDlg = remember {
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
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .background(gradient())
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Title("Editar información personal")

            /* ---------- foto ---------- */
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
                    contentDescription = "Foto",
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

            /* ---------- formulario ---------- */
            FormContainer(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {

                InputText(
                    value         = name,
                    onValueChange = { name = it },
                    placeholder   = "Nombre de usuario",
                )

                Spacer(Modifier.height(12.dp))
                InputDate(
                    value = "%02d/%02d/%04d".format(
                        birthDate.dayOfMonth, birthDate.monthNumber, birthDate.year
                    ),
                    label   = "Fecha de nacimiento",
                    onClick = { pickerDlg.show() }
                )

                Spacer(Modifier.height(12.dp))
                InputEmail(
                    value         = user.email,
                    onValueChange = {},               // sin cambio
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

            /* ---------- acciones ---------- */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = "Guardar",
                    onClick = { vm.save(name, birthDate, photoUri) },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = "Cancelar",
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/* fila contraseña */
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
