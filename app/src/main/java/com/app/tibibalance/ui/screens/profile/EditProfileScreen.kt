/* ui/screens/profile/EditProfileScreen.kt */
package com.app.tibibalance.ui.screens.profile

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.domain.entities.User
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.inputs.InputDate          // ← nuevo
import com.app.tibibalance.ui.components.inputs.InputText          // ← nuevo
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.texts.Title
import kotlinx.datetime.LocalDate
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileScreen(
    user          : User,
    canChangePass : Boolean,
    onPickPhoto   : (onResult: (Uri?) -> Unit) -> Unit,
    onSave        : (name: String, birthDate: LocalDate?, photo: Uri?) -> Unit,
    onChangePass  : () -> Unit,
    onCancel      : () -> Unit,
) {
    /* ----- estado local ----- */
    var name      by remember { mutableStateOf(user.displayName.orEmpty()) }
    var birthDate by remember { mutableStateOf(user.birthDate) }
    var photoUri  by remember { mutableStateOf<Uri?>(null) }

    /* ----- date-picker ----- */
    val ctx = LocalContext.current
    val picker = DatePickerDialog(
        ctx,
        { _, y, m, d -> birthDate = LocalDate(y, m + 1, d) },
        birthDate.year, birthDate.monthNumber - 1, birthDate.dayOfMonth
    ).apply {
        datePicker.maxDate = Calendar.getInstance()
            .apply { add(Calendar.YEAR, -18) }.timeInMillis
    }

    val gradient = Brush.verticalGradient(listOf(Color(0xFFC3E2FA), Color.White))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Title("Editar información personal")

        /* --------- Foto --------- */
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ImageContainer(
                resId = R.drawable.avatar_placeholder,
                contentDescription = "Foto",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { onPickPhoto { photoUri = it } },
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { onPickPhoto { photoUri = it } }) { Text("Cambiar foto") }
        }

        /* --------- Formulario --------- */
        FormContainer(backgroundColor = Color(0xFFDEEDF4)) {

            InputText(
                value         = name,
                onValueChange = { name = it },
                placeholder   = "Nombre de usuario",
            )

            /* Fecha de nacimiento – usa InputDate */
            Spacer(Modifier.height(12.dp))
            InputDate(
                value   = "%02d/%02d/%04d".format(
                    birthDate.dayOfMonth, birthDate.monthNumber, birthDate.year
                ),
                label   = "Fecha de nacimiento",
                onClick = { picker.show() }
            )

            /* Contraseña */
            Spacer(Modifier.height(12.dp))
            Subtitle("Contraseña")
            if (canChangePass) {
                SettingRow("••••••••••••••", onChangePass)
            } else {
                Text(
                    "Autenticado con Google – no se puede cambiar aquí.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        /* --------- acciones --------- */
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(
                text = "Guardar",
                onClick = { onSave(name, birthDate, photoUri) },
                modifier = Modifier.weight(1f)
            )

            SecondaryButton(
                text = "Cancelar",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/* Fila “Contraseña” abre cambio */
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
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}
