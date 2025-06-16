package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.RoundedIconButton
import com.app.tibibalance.ui.components.buttons.TextButtonLink
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.IconContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.inputs.iconByName
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.utils.SettingItem


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitListSection(
    habits: List<HabitNotifUi>,
    onToggle: (HabitNotifUi) -> Unit,
    vm: ConfigureNotificationViewModel = hiltViewModel()
) {
    // Estado local para controlar la visibilidad del modal “SABER MÁS”
    var showHelpModal by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 130.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        /* encabezado */

        FormContainer(backgroundColor = MaterialTheme.colorScheme.surfaceVariant)
        {
            Subtitle(
                text = "Emociones",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        EmotionReminderSettingItem(vm)
        Spacer(Modifier.height(12.dp))
        FormContainer(backgroundColor = MaterialTheme.colorScheme.surfaceVariant)
        {
            Subtitle(
                text = "Hábitos",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        habits.forEach { habit ->
            SettingItem(
                leadingIcon = {
                    IconContainer(
                        icon = iconByName(habit.icon),
                        contentDescription = habit.name,
                        modifier = Modifier.size(24.dp),
                    )
                },
                text = habit.name,
                trailing = {
                    RoundedIconButton(
                        onClick = { onToggle(habit) },
                        icon = if (habit.enabled)
                            Icons.Default.NotificationsActive
                        else
                            Icons.Default.NotificationsOff,
                        modifier = Modifier.size(32.dp),
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.selectHabit(habit) }
            )
        }

        Spacer(Modifier.height(16.dp))

        /* Enlace “SABER MÁS” */
        TextButtonLink(
            text = "SABER MÁS",
            onClick = {
                showHelpModal = true
            }
        )
    }

    // ----------------- Modal personalizado “SABER MÁS” -----------------
    if (showHelpModal) {
        ModalContainer(
            onDismissRequest = { showHelpModal = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Recuerda que los mensajes personalizados son esa" ,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "\uD83E\uDDE9frase motivadora\uD83C\uDFC6" ,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "que das de alta junto con tu hábito. ✨",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                ImageContainer(
                    resId = R.drawable.ic_settings,
                    contentDescription = "Icono de configuración",
                    modifier = Modifier.size(80.dp)

                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Puedes activar o desactivar las \nnotificaciones de tus hábitos," +
                            "\nsin alterar su configuración. 🔔",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
