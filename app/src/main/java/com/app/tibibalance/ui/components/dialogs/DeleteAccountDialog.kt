package com.app.tibibalance.ui.components.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.DangerButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton

@Composable
fun DeleteAccountDialog(
    visible: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            tonalElevation = 6.dp,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .widthIn(min = 280.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp) // más compacto
            ) {
                // Título
                Text(
                    text = "⚠️ Eliminar cuenta ⚠️",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                // Instrucción
                Text(
                    text = "Introduce tu contraseña para confirmar.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                // Imagen de Tibio (más grande y con padding ajustado)
                // Imagen de Tibio (más grande y con padding reducido entre texto e inputs)
                Image(
                    painter = painterResource(id = R.drawable.ic_goodbye),
                    contentDescription = "Tibio despidiéndose",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(vertical = 0.2.dp) // espacio más cerrado arriba y abajo
                )


                // Inputs
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error si existe
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Advertencia final
                Text(
                    text = "Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "Cancelar",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    DangerButton(
                        text = "Eliminar",
                        onClick = {
                            when {
                                password.isBlank() || confirmPassword.isBlank() -> {
                                    errorMessage = "Ambos campos son obligatorios"
                                }
                                password != confirmPassword -> {
                                    errorMessage = "Las contraseñas no coinciden"
                                }
                                else -> {
                                    errorMessage = null
                                    onConfirm(password)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = password.isNotBlank() && confirmPassword.isNotBlank()
                    )
                }
            }
        }
    }
}
